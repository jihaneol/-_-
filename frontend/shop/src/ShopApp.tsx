import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Check, Coffee, CreditCard, Gift, RefreshCcw, Search, ShoppingBag, User, UserPlus } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { shopCommerceApi, shopCommerceKeys } from '../../shared/src/entities/commerce/api'
import type { CouponHistory, CouponWallet, Member, Product, ProductPageResponse } from '../../shared/src/entities/commerce/types'
import type { ApiError } from '../../shared/src/shared/api/client'
import { Field, Notice, PaginationControls, StatusBadge } from '../../shared/src/shared/ui'

const signupSchema = z.object({
  name: z.string().min(1),
  email: z.string().email(),
})

type SignupForm = z.infer<typeof signupSchema>
type ShopView = 'home' | 'products' | 'benefits' | 'mypage' | 'detail' | 'checkout' | 'login' | 'signup'

function productCouponCount(product: Product) {
  return product.couponAccrualCount
}

export function ShopApp() {
  const queryClient = useQueryClient()
  const [view, setView] = useState<ShopView>('home')
  const [member, setMember] = useState<Member | null>(null)
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null)
  const [productPage, setProductPage] = useState(0)
  const [notice, setNotice] = useState('')
  const [error, setError] = useState('')
  const products = useQuery({
    queryKey: shopCommerceKeys.products(productPage),
    queryFn: () => shopCommerceApi.listProducts({ page: productPage }),
  })
  const wallet = useQuery({
    queryKey: member ? shopCommerceKeys.wallet(member.id) : shopCommerceKeys.walletIdle,
    queryFn: () => shopCommerceApi.getCouponWallet(member!.id),
    enabled: member !== null,
  })
  const signupForm = useForm<SignupForm>({
    resolver: zodResolver(signupSchema),
    defaultValues: { name: '', email: '' },
  })
  const onError = (apiError: ApiError) => {
    setError(apiError.message)
    setNotice('')
  }
  const signup = useMutation({
    mutationFn: shopCommerceApi.createMember,
    onSuccess: async (createdMember) => {
      setMember(createdMember)
      setNotice(`회원 #${createdMember.id} 가입`)
      setError('')
      setView('home')
      signupForm.reset({ name: '', email: '' })
      await queryClient.invalidateQueries({ queryKey: shopCommerceKeys.wallet(createdMember.id) })
    },
    onError,
  })
  const payProduct = useMutation({
    mutationFn: async (product: Product) => {
      if (!member) {
        throw { code: 'MEMBER_REQUIRED', message: '먼저 가입해주세요.' } satisfies ApiError
      }
      const order = await shopCommerceApi.createOrder({
        memberId: member.id,
        lines: [{ productId: product.id, quantity: 1 }],
      })
      return shopCommerceApi.payOrder(order.id, { idempotencyKey: `shop-pay-${member.id}-${Date.now()}` })
    },
    onSuccess: async (result) => {
      setNotice(`결제 완료: 쿠폰 ${result.issuedCouponCount}장 적립`)
      setError('')
      setView('mypage')
      if (member) {
        await queryClient.invalidateQueries({ queryKey: shopCommerceKeys.wallet(member.id) })
        await queryClient.invalidateQueries({ queryKey: shopCommerceKeys.coupons(member.id) })
        await queryClient.invalidateQueries({ queryKey: shopCommerceKeys.histories(member.id) })
      }
    },
    onError,
  })
  const openView = (nextView: ShopView) => {
    setView(nextView)
    if (typeof navigator !== 'undefined' && navigator.userAgent.includes('jsdom')) {
      return
    }
    try {
      window.scrollTo({ top: 0, behavior: 'smooth' })
    } catch {
    }
  }
  const openDetail = (product: Product) => {
    setSelectedProduct(product)
    openView('detail')
  }
  const openCheckout = (product: Product) => {
    if (!member) {
      setError('회원 가입 후 구매할 수 있습니다.')
      setNotice('')
      openView('signup')
      return
    }
    setSelectedProduct(product)
    openView('checkout')
  }
  const productItems = products.data?.items ?? []
  const fallbackProduct = selectedProduct ?? productItems[0] ?? null

  return (
    <main className="shop-shell">
      <ShopHeader currentView={view} member={member} wallet={wallet.data} onOpen={openView} />
      <Notice text={notice} error={error} />

      {view === 'benefits' ? (
        <ShopCouponGuidePage
          member={member}
          wallet={wallet.data}
          onCatalog={() => openView('products')}
          onLogin={() => openView('login')}
          onSignup={() => openView('signup')}
        />
      ) : null}

      {view === 'products' ? (
        <ShopCatalogPage
          products={productItems}
          productPage={products.data}
          isLoading={products.isLoading}
          member={member}
          onOpenDetail={openDetail}
          onCheckout={openCheckout}
          onPreviousPage={() => setProductPage((page) => Math.max(0, page - 1))}
          onNextPage={() => setProductPage((page) => page + 1)}
        />
      ) : null}

      {view === 'home' ? (
        <ShopHomePage
          products={productItems}
          isLoading={products.isLoading}
          member={member}
          wallet={wallet.data}
          onSignup={() => openView('signup')}
          onBenefits={() => openView('benefits')}
          onOpenDetail={openDetail}
          onCheckout={openCheckout}
        />
      ) : null}

      {view === 'mypage' ? (
        <ShopMyPage
          member={member}
          wallet={wallet.data}
          walletLoading={wallet.isLoading}
          nextProduct={productItems[0] ?? null}
          onHome={() => openView('home')}
          onCheckout={openCheckout}
        />
      ) : null}

      {view === 'detail' && fallbackProduct ? (
        <ShopProductDetailPage
          product={fallbackProduct}
          member={member}
          onHome={() => openView('home')}
          onSignup={() => openView('signup')}
          onCheckout={openCheckout}
          onNotice={(message) => {
            setNotice(message)
            setError('')
          }}
        />
      ) : null}

      {view === 'checkout' && fallbackProduct ? (
        <ShopCheckoutPage
          product={fallbackProduct}
          member={member}
          pending={payProduct.isPending}
          onBack={() => openDetail(fallbackProduct)}
          onPay={() => payProduct.mutate(fallbackProduct)}
        />
      ) : null}

      {view === 'login' ? (
        <ShopLoginPage
          member={member}
          wallet={wallet.data}
          onSignup={() => openView('signup')}
          onMyPage={() => openView('mypage')}
          onCatalog={() => openView('products')}
        />
      ) : null}

      {view === 'signup' ? (
        <ShopSignupPage
          signupForm={signupForm}
          signupPending={signup.isPending}
          onSignup={(values) => signup.mutate(values)}
          onLogin={() => openView('login')}
        />
      ) : null}
    </main>
  )
}

function ShopHeader(props: {
  currentView: ShopView
  member: Member | null
  wallet?: CouponWallet
  onOpen: (view: ShopView) => void
}) {
  return (
    <header className="shop-header">
      <button className="shop-logo-button" type="button" onClick={() => props.onOpen('home')}>
        <Coffee size={22} /> Bean Stamp
      </button>
      <nav className="shop-nav" aria-label="쇼핑몰 메뉴">
        <button className={props.currentView === 'home' ? 'active' : ''} type="button" onClick={() => props.onOpen('home')}>홈</button>
        <button className={props.currentView === 'products' || props.currentView === 'detail' ? 'active' : ''} type="button" onClick={() => props.onOpen('products')}>상품</button>
        <button className={props.currentView === 'benefits' ? 'active' : ''} type="button" onClick={() => props.onOpen('benefits')}>쿠폰 안내</button>
        {props.member ? (
          <button className={props.currentView === 'mypage' ? 'active' : ''} type="button" onClick={() => props.onOpen('mypage')}>내 쿠폰</button>
        ) : null}
      </nav>
      <label className="shop-search">
        <Search size={16} />
        <span className="sr-only">커피 메뉴 검색</span>
        <input placeholder="커피 메뉴 검색" />
      </label>
      <div className="shop-header-actions">
        <span className="shop-session-badge">{props.member ? `쿠폰 ${props.wallet?.issuedCouponCount ?? 0}장` : '게스트 탐색 중'}</span>
        <button className={`button secondary ${props.currentView === 'login' ? 'active' : ''}`} type="button" onClick={() => props.onOpen('login')}>
          <User size={16} /> {props.member ? props.member.name : '로그인'}
        </button>
        {!props.member ? <button className="button" type="button" onClick={() => props.onOpen('signup')}>가입하기</button> : null}
      </div>
    </header>
  )
}

function ShopCouponGuidePage(props: {
  member: Member | null
  wallet?: CouponWallet
  onCatalog: () => void
  onLogin: () => void
  onSignup: () => void
}) {
  return (
    <section className="shop-view">
      <div className="shop-page-title">
        <h1>쿠폰 적립 안내</h1>
        <p>적립 기준, 교환 조건, 현재 상태를 한 화면에서 확인합니다.</p>
      </div>
      <div className="shop-guide-layout">
        <section className="shop-guide-board">
          <h2>현재 적립판</h2>
          <strong>{props.wallet?.issuedCouponCount ?? 0} / 10장</strong>
          <p>다음 교환까지 {props.wallet?.remainingToNextExchange ?? 10}장 남았습니다.</p>
          <StampBoard count={props.wallet?.issuedCouponCount ?? 0} />
          <button className="button" type="button" onClick={props.onCatalog}>메뉴 보러가기</button>
        </section>
        <section className="shop-guide-rules">
          <h2>적립 규칙</h2>
          <div className="shop-rule-list">
            <div><strong>5,000원</strong><span>결제 금액 기준 쿠폰 1장</span></div>
            <div><strong>10장</strong><span>교환 메뉴 1개 신청 가능</span></div>
            <div><strong>즉시 반영</strong><span>결제 완료 후 지갑에 자동 반영</span></div>
          </div>
        </section>
        <section className="shop-guide-signup">
          <h2>{props.member ? '회원 연결 완료' : '회원 혜택 시작'}</h2>
          {props.member ? (
            <>
              <p>{props.member.email}</p>
              <button className="button secondary" type="button" onClick={props.onCatalog}>메뉴 보러가기</button>
            </>
          ) : (
            <div className="shop-guide-actions">
              <p>쿠폰 지갑은 로그인에서 확인하고, 새 회원은 전용 가입 화면에서 생성합니다.</p>
              <button className="button secondary" type="button" onClick={props.onLogin}><User size={16} /> 로그인으로 이동</button>
              <button className="button" type="button" onClick={props.onSignup}><UserPlus size={16} /> 회원 가입</button>
            </div>
          )}
        </section>
      </div>
    </section>
  )
}

function ShopCatalogPage(props: {
  products: Product[]
  productPage?: ProductPageResponse
  isLoading: boolean
  member: Member | null
  onOpenDetail: (product: Product) => void
  onCheckout: (product: Product) => void
  onPreviousPage: () => void
  onNextPage: () => void
}) {
  return (
    <section className="shop-view">
      <div className="shop-page-title">
        <h1>커피 메뉴</h1>
        <p>게스트는 메뉴와 적립 기준을 둘러보고, 회원은 바로 구매해 쿠폰을 적립합니다.</p>
      </div>
      <div className="shop-catalog-surface">
        <div className="shop-catalog-toolbar">
          <button className="active" type="button">전체</button>
          <button type="button">오늘의 추천</button>
          <button type="button">쿠폰 적립</button>
          <button type="button">교환 메뉴</button>
          <span>메뉴 {props.productPage?.totalElements ?? 0}개</span>
        </div>
        <PaginationControls
          label="메뉴"
          page={props.productPage}
          isLoading={props.isLoading}
          onPrevious={props.onPreviousPage}
          onNext={props.onNextPage}
        />
        <div className="shop-catalog-list">
          {props.isLoading ? <p>메뉴를 불러오는 중</p> : null}
          {!props.isLoading && !props.products.length ? <p>메뉴 없음</p> : null}
          {props.products.map((product) => (
            <article className="shop-catalog-row" key={product.id}>
              <button className="shop-catalog-photo" type="button" onClick={() => props.onOpenDetail(product)}>
                <Coffee size={24} />
                <span>{product.exchangeEligible ? '교환 메뉴' : '커피'}</span>
              </button>
              <div>
                <strong>{product.name}</strong>
                <span>구매 시 쿠폰 {productCouponCount(product)}장 적립</span>
              </div>
              <StatusBadge value={product.saleStatus} />
              <strong>{product.price.toLocaleString()} KRW</strong>
              <div className="shop-catalog-actions">
                <button className="button secondary" type="button" onClick={() => props.onOpenDetail(product)}>상세보기</button>
                {props.member ? (
                  <button className="button" disabled={product.saleStatus !== 'ON_SALE'} type="button" onClick={() => props.onCheckout(product)}>구매</button>
                ) : null}
              </div>
            </article>
          ))}
        </div>
      </div>
    </section>
  )
}

function ShopHomePage(props: {
  products: Product[]
  isLoading: boolean
  member: Member | null
  wallet?: CouponWallet
  onSignup: () => void
  onBenefits: () => void
  onOpenDetail: (product: Product) => void
  onCheckout: (product: Product) => void
}) {
  const primaryProduct = props.products[0]
  return (
    <>
      <section className="shop-hero-band">
        <div>
          <span className="shop-hero-kicker">{props.member ? `${props.member.name} 회원` : '오늘의 커피'}</span>
          <h1>따뜻한 커피 한 잔마다 쿠폰이 쌓입니다</h1>
          <p>메뉴를 둘러보고 마음에 드는 커피를 선택하세요. 회원은 구매 후 쿠폰 적립 현황을 내 쿠폰에서 바로 확인할 수 있습니다.</p>
          <div className="shop-hero-actions">
            <a className="button" href="#products"><ShoppingBag size={16} /> 메뉴 보기</a>
            {props.member ? (
              <button className="button secondary" type="button" onClick={props.onBenefits}><Gift size={16} /> 쿠폰 안내</button>
            ) : (
              <button className="button secondary" type="button" onClick={props.onSignup}><UserPlus size={16} /> 회원 가입</button>
            )}
          </div>
        </div>
        <div className="shop-hero-visual">
          <div className="shop-featured-product">
            <Coffee size={32} />
            <span>오늘의 추천</span>
            <strong>{primaryProduct?.name ?? '메뉴 준비 중'}</strong>
            <em>{(primaryProduct?.price ?? 0).toLocaleString()} KRW</em>
            {primaryProduct ? (
              <button className="button secondary" type="button" onClick={() => props.onOpenDetail(primaryProduct)}>상세보기</button>
            ) : null}
          </div>
        </div>
      </section>

      {props.member ? (
        <section className="shop-task-grid">
          <CouponSummaryPanel member={props.member} wallet={props.wallet} />
          <div className="shop-panel shop-login-cta">
            <h2>회원 구매 흐름</h2>
            <p>메뉴 상세에서 구매하면 결제 완료 후 쿠폰 지갑에 적립 수량이 반영됩니다.</p>
            {primaryProduct ? (
              <button className="button secondary" type="button" onClick={() => props.onCheckout(primaryProduct)}>
                <CreditCard size={16} /> 추천 메뉴 구매
              </button>
            ) : null}
          </div>
        </section>
      ) : (
        <section className="shop-guest-strip">
          <div>
            <strong>게스트로 둘러보는 중</strong>
            <span>메뉴 상세와 쿠폰 적립 기준만 표시합니다. 결제와 쿠폰 지갑은 회원 가입 후 열립니다.</span>
          </div>
          <button className="button secondary" type="button" onClick={props.onSignup}>
            <UserPlus size={16} /> 회원 가입
          </button>
        </section>
      )}

      <section className="shop-section" id="products">
        <div className="shop-section-head">
          <div>
            <h2>추천 커피</h2>
            <p>메뉴 정보와 쿠폰 적립 기준을 먼저 확인하세요.</p>
          </div>
          <span className="status info">메뉴 {props.products.length}개</span>
        </div>
        <div className="product-list shop-product-list">
          {props.isLoading ? <p>불러오는 중</p> : null}
          {!props.isLoading && !props.products.length ? <p>메뉴 없음</p> : null}
          {props.products.map((product, index) => (
            <ShopProductCard
              key={product.id}
              product={product}
              featured={index === 0}
              member={props.member}
              disabled={!props.member || product.saleStatus !== 'ON_SALE'}
              onDetail={() => props.onOpenDetail(product)}
              onCheckout={() => props.onCheckout(product)}
            />
          ))}
        </div>
      </section>
    </>
  )
}

function ShopLoginPage(props: {
  member: Member | null
  wallet?: CouponWallet
  onSignup: () => void
  onMyPage: () => void
  onCatalog: () => void
}) {
  return (
    <section className="shop-view">
      <div className="shop-page-title">
        <h1>로그인</h1>
        <p>회원 시작, 쿠폰 지갑 확인, 주문 이어가기를 한 화면에서 처리합니다.</p>
      </div>
      <div className="shop-auth-layout">
        <section className="shop-auth-panel">
          <h2>회원 시작</h2>
          {props.member ? (
            <>
              <p>{props.member.name} 회원으로 연결되어 있습니다.</p>
              <div className="shop-auth-account">
                <strong>{props.member.email}</strong>
                <span>쿠폰 {props.wallet?.issuedCouponCount ?? 0}장 적립 중</span>
              </div>
              <button className="button" type="button" onClick={props.onMyPage}>마이페이지 보기</button>
            </>
          ) : (
            <>
              <p>현재 데모 쇼핑몰은 회원 가입 후 자동 로그인됩니다. 새 회원을 만들면 결제와 쿠폰 적립을 바로 확인할 수 있습니다.</p>
              <button className="button" type="button" onClick={props.onSignup}><UserPlus size={16} /> 회원 가입하기</button>
            </>
          )}
        </section>
        <section className="shop-auth-panel shop-auth-side">
          <h2>비회원 둘러보기</h2>
          <p>커피 메뉴는 로그인 없이 확인할 수 있고, 결제는 회원 가입 후 진행됩니다.</p>
          <button className="button secondary" type="button" onClick={props.onCatalog}>메뉴 먼저 보기</button>
        </section>
      </div>
    </section>
  )
}

function ShopSignupPage(props: {
  signupForm: ReturnType<typeof useForm<SignupForm>>
  signupPending: boolean
  onSignup: (values: SignupForm) => void
  onLogin: () => void
}) {
  return (
    <section className="shop-view">
      <div className="shop-page-title">
        <h1>회원 가입</h1>
        <p>데모 회원을 만들고 커피 구매, 결제, 쿠폰 적립 흐름을 이어갑니다.</p>
      </div>
      <div className="shop-auth-layout">
        <section className="shop-auth-panel">
          <h2>회원 정보</h2>
          <form className="grid" onSubmit={props.signupForm.handleSubmit(props.onSignup)}>
            <Field label="이름" error={props.signupForm.formState.errors.name?.message}>
              <input {...props.signupForm.register('name')} placeholder="Lee" />
            </Field>
            <Field label="이메일" error={props.signupForm.formState.errors.email?.message}>
              <input {...props.signupForm.register('email')} placeholder="lee@example.com" />
            </Field>
            <button className="button" disabled={props.signupPending}>
              <UserPlus size={16} /> 가입
            </button>
          </form>
        </section>
        <section className="shop-auth-panel shop-auth-side">
          <h2>이미 회원인가요?</h2>
          <p>현재 세션에 연결된 회원 상태와 쿠폰 지갑은 로그인 화면에서 확인합니다.</p>
          <button className="button secondary" type="button" onClick={props.onLogin}>로그인으로 이동</button>
        </section>
      </div>
    </section>
  )
}

function ShopMyPage(props: {
  member: Member | null
  wallet?: CouponWallet
  walletLoading: boolean
  nextProduct: Product | null
  onHome: () => void
  onCheckout: (product: Product) => void
}) {
  const issuedCouponCount = props.wallet?.issuedCouponCount ?? 0
  const exchangeableSetCount = props.wallet?.exchangeableSetCount ?? 0
  const exchangedCouponCount = props.wallet?.exchangedCouponCount ?? 0
  const voidedCouponCount = props.wallet?.voidedCouponCount ?? 0
  const exchangeable = exchangeableSetCount > 0

  return (
    <section className="shop-view">
      <div className="shop-page-title">
        <h1>마이페이지</h1>
        <p>쿠폰 적립 현황과 최근 이용 내역을 확인합니다.</p>
      </div>
      <div className="shop-profile-strip">
        <div className="shop-profile-avatar">{props.member?.name.slice(0, 1) ?? 'G'}</div>
        <div>
          <strong>{props.member ? `${props.member.name} 회원` : '게스트'}</strong>
          <span>{props.member?.email ?? '가입 후 쿠폰 지갑을 확인하세요.'}</span>
        </div>
        <button className="button secondary" type="button" onClick={props.onHome}>메뉴 보러가기</button>
      </div>
      <div className="shop-coupon-page-grid">
        <section className="shop-coupon-main">
          <div className="shop-coupon-main-head">
            <h2>쿠폰 지갑</h2>
            <span className={`status ${exchangeable ? 'ok' : 'info'}`}>{exchangeable ? '교환 가능' : '적립 중'}</span>
          </div>
          <strong>{issuedCouponCount} / 10장</strong>
          <p>다음 교환까지 {props.wallet?.remainingToNextExchange ?? 10}장 남았습니다.</p>
          <StampBoard count={issuedCouponCount} />
          <div className="shop-wallet-summary">
            <span><Coffee size={15} /> 적립 중 {issuedCouponCount}</span>
            <span><Gift size={15} /> 교환 가능 {exchangeableSetCount}세트</span>
            <span><Check size={15} /> 교환 완료 {exchangedCouponCount}</span>
            <span><RefreshCcw size={15} /> 회수 {voidedCouponCount}</span>
          </div>
        </section>
        <section className="shop-next-purchase">
          <h2>다음 커피 추천</h2>
          <p>5,000원 단위 결제마다 쿠폰이 자동 적립됩니다.</p>
          {props.nextProduct ? (
            <div className="shop-mini-item">
              <span>{props.nextProduct.name}</span>
              <button className="button" type="button" onClick={() => props.onCheckout(props.nextProduct!)}>구매</button>
            </div>
          ) : null}
        </section>
      </div>
      <section className="shop-mypage-history">
        <h2>최근 쿠폰 내역</h2>
        <p>최근 적립과 교환 내역을 고객 기준으로 확인합니다.</p>
        <div className="shop-history-list">
          {!props.member ? <p>가입 후 쿠폰 내역을 확인할 수 있습니다.</p> : null}
          {props.member && props.walletLoading ? <p>쿠폰 지갑을 불러오는 중</p> : null}
          {props.member && !props.walletLoading && !props.wallet?.recentHistories.length ? <p>아직 쿠폰 내역이 없습니다.</p> : null}
          {props.wallet?.recentHistories.map((history) => <HistoryRow key={history.id} history={history} />)}
        </div>
      </section>
    </section>
  )
}

function ShopProductDetailPage(props: {
  product: Product
  member: Member | null
  onHome: () => void
  onSignup: () => void
  onCheckout: (product: Product) => void
  onNotice: (message: string) => void
}) {
  const couponCount = productCouponCount(props.product)
  return (
    <section className="shop-view shop-detail-view">
      <div className="shop-photo-area">
        <Coffee size={52} />
        <span>{props.product.exchangeEligible ? '교환 커피' : '커피 메뉴'}</span>
        <span className="status ok">쿠폰 적립 대상</span>
      </div>
      <div className="shop-detail-info">
        <span className="shop-crumb">홈 / 커피 메뉴 / {props.product.name}</span>
        <h1>{props.product.name}</h1>
        <p>결제 후 쿠폰이 자동 적립되는 커피 메뉴입니다. 게스트는 메뉴 정보만 확인하고, 회원은 바로 구매할 수 있습니다.</p>
        <strong className="shop-detail-price">{props.product.price.toLocaleString()} KRW</strong>
        <div className="shop-coupon-notice">
          <strong>쿠폰 {couponCount}장 적립</strong>
          <span>5,000원 단위로 계산되어 결제 완료 즉시 지갑에 반영됩니다.</span>
        </div>
        <div className="shop-detail-actions">
          {props.member ? (
            <button className="button" type="button" disabled={props.product.saleStatus !== 'ON_SALE'} onClick={() => props.onCheckout(props.product)}>
              바로 구매
            </button>
          ) : (
            <button className="button" type="button" onClick={props.onSignup}>
              회원 가입 후 구매
            </button>
          )}
          <button className="button secondary" type="button" onClick={props.onHome}>메뉴 더보기</button>
        </div>
        <div className="shop-exchange-box">
          <div>
            <strong>교환 메뉴 안내</strong>
            <span>쿠폰 10장을 모으면 5,000원 커피 메뉴로 교환할 수 있습니다.</span>
          </div>
        </div>
      </div>
    </section>
  )
}

function ShopCheckoutPage(props: {
  product: Product
  member: Member | null
  pending: boolean
  onBack: () => void
  onPay: () => void
}) {
  const couponCount = productCouponCount(props.product)
  return (
    <section className="shop-view">
      <div className="shop-page-title">
        <h1>결제</h1>
        <p>구매 메뉴, 회원 정보, 적립 쿠폰을 확인한 뒤 결제합니다.</p>
      </div>
      <div className="shop-checkout-layout">
        <div className="shop-checkout-main">
          <section className="shop-order-items">
            <h2>주문 메뉴</h2>
            <div className="shop-checkout-item">
              <div className="shop-thumb"><Coffee size={17} /> 커피</div>
              <div>
                <strong>{props.product.name}</strong>
                <span>수량 1</span>
              </div>
              <strong>{props.product.price.toLocaleString()} KRW</strong>
            </div>
          </section>
          <section className="shop-delivery">
            <h2>회원 정보</h2>
            <div className="shop-field-grid">
              <label>
                <span>구매자</span>
                <input value={props.member?.name ?? ''} readOnly placeholder="회원 가입 필요" />
              </label>
              <label>
                <span>이메일</span>
                <input value={props.member?.email ?? ''} readOnly placeholder="회원 가입 필요" />
              </label>
            </div>
          </section>
        </div>
        <aside className="shop-summary">
          <h2>결제 요약</h2>
          <div className="shop-sum-row"><span>메뉴 금액</span><strong>{props.product.price.toLocaleString()} KRW</strong></div>
          <div className="shop-sum-row"><span>추가 비용</span><strong>0 KRW</strong></div>
          <div className="shop-sum-total"><span>총 결제</span><strong>{props.product.price.toLocaleString()} KRW</strong></div>
          <div className="shop-earn-coupon">
            <strong>쿠폰 {couponCount}장 적립 예정</strong>
            <span>결제 성공 후 쿠폰 지갑에 즉시 반영됩니다.</span>
          </div>
          <button className="button" type="button" disabled={!props.member || props.pending} onClick={props.onPay}>
            <CreditCard size={16} /> 결제하기
          </button>
          <button className="button secondary" type="button" onClick={props.onBack}>메뉴로 돌아가기</button>
          <p>결제하면 주문 생성, 결제 승인, 쿠폰 발급이 한 번에 처리됩니다.</p>
        </aside>
      </div>
    </section>
  )
}

function CouponSummaryPanel(props: { member: Member | null; wallet?: CouponWallet }) {
  const issuedCouponCount = props.wallet?.issuedCouponCount ?? 0
  const exchangeableSetCount = props.wallet?.exchangeableSetCount ?? 0
  const exchangeable = exchangeableSetCount > 0
  return (
    <div className="shop-panel shop-wallet" id="coupon">
      <div className="shop-panel-heading">
        <div>
          <h2>내 쿠폰</h2>
          <p>{props.member ? `${props.member.name} 회원의 쿠폰 지갑` : '가입하면 쿠폰 지갑이 열립니다.'}</p>
        </div>
        <span className={`status ${exchangeable ? 'ok' : 'info'}`}>{exchangeable ? '교환 가능' : '적립 중'}</span>
      </div>
      <strong className="shop-wallet-count">{issuedCouponCount} / 10장</strong>
      <StampBoard count={issuedCouponCount} />
      <div className="shop-wallet-summary">
        <span><Coffee size={15} /> 적립 중 {props.wallet?.issuedCouponCount ?? 0}</span>
        <span><Gift size={15} /> 교환 가능 {exchangeableSetCount}세트</span>
        <span><RefreshCcw size={15} /> 회수 {props.wallet?.voidedCouponCount ?? 0}</span>
        <span><Check size={15} /> 교환 완료 {props.wallet?.exchangedCouponCount ?? 0}</span>
        <span><Check size={15} /> 다음 교환까지 {props.wallet?.remainingToNextExchange ?? 10}장</span>
      </div>
    </div>
  )
}

function StampBoard(props: { count: number; size?: 'small' | 'default' }) {
  return (
    <div className={`shop-stamp-board ${props.size === 'small' ? 'small' : ''}`}>
      {Array.from({ length: 10 }).map((_, index) => {
        const filledCount = props.count >= 10 ? 10 : props.count % 10
        return (
          <span key={index} className={index < filledCount ? 'filled' : ''}>
            {index < filledCount ? '✓' : ''}
          </span>
        )
      })}
    </div>
  )
}

function ShopProductCard(props: {
  product: Product
  featured: boolean
  member: Member | null
  disabled: boolean
  onDetail: () => void
  onCheckout: () => void
}) {
  const couponCount = productCouponCount(props.product)
  return (
    <article className={`product-card shop-product-card ${props.featured ? 'featured' : ''}`}>
      <button className="shop-product-photo" type="button" onClick={props.onDetail}>
        <Coffee size={30} />
        <span>{props.product.exchangeEligible ? '교환 메뉴' : props.featured ? '오늘의 추천' : '커피 메뉴'}</span>
      </button>
      <div className="shop-product-body">
        <div>
          <button className="shop-product-name" type="button" onClick={props.onDetail}>{props.product.name}</button>
          <span>{couponCount > 0 ? `구매 시 쿠폰 ${couponCount}장 적립` : '쿠폰 적립 제외'}</span>
        </div>
        <StatusBadge value={props.product.saleStatus} />
      </div>
      <div className="shop-product-action">
        <strong>{props.product.price.toLocaleString()} KRW</strong>
        <button className="button secondary" onClick={props.onDetail}>
          상세보기
        </button>
        {props.member ? (
          <button className="button" disabled={props.disabled} onClick={props.onCheckout}>
            <ShoppingBag size={16} /> 구매
          </button>
        ) : null}
      </div>
    </article>
  )
}

function HistoryRow(props: { history: CouponHistory }) {
  const label = props.history.type === 'ISSUED' ? '쿠폰 적립' : props.history.type === 'EXCHANGED' ? '교환 완료' : '쿠폰 회수'
  return (
    <div className="shop-history-row">
      <span>#{props.history.orderId}</span>
      <strong>{label}</strong>
      <span>쿠폰 #{props.history.couponId ?? '-'}</span>
    </div>
  )
}
