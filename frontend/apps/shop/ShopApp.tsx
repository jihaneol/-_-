import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Accessibility, Check, Coffee, CreditCard, Flame, Gift, Languages, PackageCheck, RefreshCcw, Search, ShieldCheck, ShoppingBag, ShoppingCart, Snowflake, Star, Timer, Truck, User, UserPlus } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { shopCommerceApi, shopCommerceKeys } from '../../src/entities/commerce/api'
import type { CouponHistory, CouponWallet, Member, Product, ProductPageResponse } from '../../src/entities/commerce/types'
import type { ApiError } from '../../src/shared/api/client'
import { Field, Notice, PaginationControls, StatusBadge } from '../../src/shared/ui'

const signupSchema = z.object({
  name: z.string().min(1),
  email: z.string().email(),
})

type SignupForm = z.infer<typeof signupSchema>
type ShopView = 'program05' | 'guide06' | 'catalog07' | 'order08' | 'home' | 'mypage' | 'detail' | 'checkout' | 'login' | 'signup'

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
  const addToCart = (product: Product) => {
    setSelectedProduct(product)
    setNotice(`${product.name} 장바구니 담기`)
    setError('')
    openView('order08')
  }
  const openCheckout = (product: Product) => {
    if (!member) {
      setError('먼저 가입해주세요.')
      setNotice('')
      openView('home')
      return
    }
    setSelectedProduct(product)
    openView('checkout')
  }
  const productItems = products.data?.items ?? []
  const fallbackProduct = selectedProduct ?? productItems[0] ?? null
  const cartProduct = selectedProduct

  return (
    <main className="shop-shell">
      <ShopHeader currentView={view} member={member} wallet={wallet.data} hasCart={cartProduct !== null} onOpen={openView} />
      <Notice text={notice} error={error} />
      <KioskUtilityBar member={member} wallet={wallet.data} />
      <KioskStepRail currentView={view} hasCart={cartProduct !== null} member={member} onOpen={openView} />

      {view === 'program05' ? (
        <ShopProgramPage
          member={member}
          wallet={wallet.data}
          onGuide={() => openView('guide06')}
          onCatalog={() => openView('catalog07')}
        />
      ) : null}

      {view === 'guide06' ? (
        <ShopCouponGuidePage
          member={member}
          wallet={wallet.data}
          onCatalog={() => openView('catalog07')}
          onLogin={() => openView('login')}
          onSignup={() => openView('signup')}
        />
      ) : null}

      {view === 'catalog07' ? (
        <ShopCatalogPage
          products={productItems}
          productPage={products.data}
          isLoading={products.isLoading}
          member={member}
          onOpenDetail={openDetail}
          onCart={addToCart}
          onCheckout={openCheckout}
          onPreviousPage={() => setProductPage((page) => Math.max(0, page - 1))}
          onNextPage={() => setProductPage((page) => page + 1)}
        />
      ) : null}

      {view === 'order08' && cartProduct ? (
        <ShopOrderPreviewPage
          product={cartProduct}
          member={member}
          onDetail={() => openDetail(cartProduct)}
          onCheckout={() => openCheckout(cartProduct)}
        />
      ) : null}

      {view === 'order08' && !cartProduct ? (
        <ShopEmptyCartPage onCatalog={() => openView('catalog07')} />
      ) : null}

      {view === 'home' ? (
        <ShopHomePage
          products={productItems}
          isLoading={products.isLoading}
          member={member}
          wallet={wallet.data}
          onLogin={() => openView('login')}
          onOpenDetail={openDetail}
          onCart={addToCart}
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
          onCart={addToCart}
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
          onCatalog={() => openView('catalog07')}
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

function KioskUtilityBar(props: { member: Member | null; wallet?: CouponWallet }) {
  return (
    <section className="shop-kiosk-utilities" aria-label="키오스크 빠른 설정">
      <div className="shop-kiosk-mode">
        <strong>매장 주문 모드</strong>
        <span>픽업 · 쿠폰 적립 · 결제</span>
      </div>
      <div className="shop-kiosk-tools">
        <span><Languages size={16} /> KR</span>
        <span>EN</span>
        <span>JP</span>
        <span>CN</span>
        <button type="button"><Accessibility size={16} /> 큰글씨</button>
        <button type="button">도움</button>
      </div>
      <div className="shop-kiosk-member">
        <span>{props.member ? `${props.member.name} 회원` : '비회원 주문 가능'}</span>
        <strong>쿠폰 {props.wallet?.issuedCouponCount ?? 0}장</strong>
      </div>
    </section>
  )
}

function KioskStepRail(props: {
  currentView: ShopView
  hasCart: boolean
  member: Member | null
  onOpen: (view: ShopView) => void
}) {
  const steps: Array<{ label: string; view: ShopView; active: boolean; done: boolean }> = [
    { label: '메뉴 선택', view: 'catalog07', active: props.currentView === 'home' || props.currentView === 'catalog07' || props.currentView === 'detail', done: props.hasCart },
    { label: '옵션·장바구니', view: 'order08', active: props.currentView === 'order08', done: props.hasCart },
    { label: '픽업 결제', view: 'checkout', active: props.currentView === 'checkout', done: false },
    { label: '쿠폰 확인', view: 'mypage', active: props.currentView === 'mypage', done: props.member !== null },
  ]
  return (
    <section className="shop-kiosk-step-rail" aria-label="주문 단계">
      {steps.map((step, index) => (
        <button
          key={step.label}
          className={`${step.active ? 'active' : ''} ${step.done ? 'done' : ''}`}
          type="button"
          onClick={() => props.onOpen(step.view)}
        >
          <span>{index + 1}</span>
          <strong>{step.label}</strong>
        </button>
      ))}
    </section>
  )
}

function ShopHeader(props: {
  currentView: ShopView
  member: Member | null
  wallet?: CouponWallet
  hasCart: boolean
  onOpen: (view: ShopView) => void
}) {
  return (
    <header className="shop-header">
      <button className="shop-logo-button" type="button" onClick={() => props.onOpen('home')}>Stamp Mall</button>
      <nav className="shop-nav" aria-label="쇼핑몰 메뉴">
        <button className={props.currentView === 'home' ? 'active' : ''} type="button" onClick={() => props.onOpen('home')}>홈</button>
        <button className={props.currentView === 'catalog07' || props.currentView === 'detail' ? 'active' : ''} type="button" onClick={() => props.onOpen('catalog07')}>상품</button>
        <button className={props.currentView === 'guide06' || props.currentView === 'program05' ? 'active' : ''} type="button" onClick={() => props.onOpen('guide06')}>혜택</button>
        <button className={props.currentView === 'mypage' ? 'active' : ''} type="button" onClick={() => props.onOpen('mypage')}>마이페이지</button>
      </nav>
      <label className="shop-search">
        <Search size={16} />
        <span className="sr-only">상품 검색</span>
        <input placeholder="상품명, 쿠폰 적립 상품 검색" />
      </label>
      <div className="shop-header-actions">
        <button className="shop-icon-button" type="button" onClick={() => props.onOpen('guide06')}>
          <Gift size={17} />
          <span>{props.wallet?.issuedCouponCount ?? 0}</span>
        </button>
        <button className="shop-icon-button" type="button" onClick={() => props.onOpen('order08')}>
          <ShoppingCart size={17} />
          <span>{props.hasCart ? 1 : 0}</span>
        </button>
        <button className={`button secondary ${props.currentView === 'login' ? 'active' : ''}`} type="button" onClick={() => props.onOpen('login')}>
          <User size={16} /> {props.member ? props.member.name : '로그인'}
        </button>
        <button className="button" type="button" onClick={() => props.onOpen('signup')}>가입하기</button>
      </div>
    </header>
  )
}

function ShopPageTabs(props: { currentView: ShopView; onOpen: (view: ShopView) => void }) {
  const tabs: Array<{ view: ShopView; label: string }> = [
    { view: 'program05', label: '05 프로그램' },
    { view: 'guide06', label: '06 쿠폰 안내' },
    { view: 'catalog07', label: '07 상품 목록' },
    { view: 'order08', label: '08 주문 확인' },
    { view: 'home', label: '09 메인' },
    { view: 'mypage', label: '10 마이페이지' },
    { view: 'detail', label: '11 상세' },
    { view: 'checkout', label: '12 결제' },
  ]
  return (
    <div className="shop-page-tabs" aria-label="Figma 화면">
      {tabs.map((tab) => (
        <button
          key={tab.view}
          className={props.currentView === tab.view ? 'active' : ''}
          type="button"
          onClick={() => props.onOpen(tab.view)}
        >
          {tab.label}
        </button>
      ))}
    </div>
  )
}

function ShopProgramPage(props: {
  member: Member | null
  wallet?: CouponWallet
  onGuide: () => void
  onCatalog: () => void
}) {
  return (
    <section className="shop-view shop-flow-view">
      <div className="shop-flow-hero">
        <div>
          <span className="shop-page-mark">05</span>
          <h1>쿠폰 교환 프로그램</h1>
          <p>구매 금액이 쿠폰으로 쌓이고, 쿠폰 10장이 모이면 교환 상품으로 이어지는 쇼핑몰 흐름입니다.</p>
          <div className="shop-hero-actions">
            <button className="button" type="button" onClick={props.onCatalog}><ShoppingBag size={16} /> 상품 둘러보기</button>
            <button className="button secondary" type="button" onClick={props.onGuide}><Gift size={16} /> 쿠폰 안내</button>
          </div>
        </div>
        <div className="shop-program-card">
          <strong>{props.member ? `${props.member.name}님의 쿠폰` : '게스트 쿠폰'}</strong>
          <span>{props.wallet?.issuedCouponCount ?? 0}장 적립 중</span>
          <StampBoard count={props.wallet?.issuedCouponCount ?? 0} size="small" />
        </div>
      </div>
      <div className="shop-program-steps">
        {[
          ['구매', '5,000원 단위 결제마다 쿠폰이 자동 적립됩니다.'],
          ['확인', '마이페이지에서 적립 현황과 최근 내역을 확인합니다.'],
          ['교환', '10장을 모으면 5,000원 상품 교환 대상이 됩니다.'],
        ].map(([title, body]) => (
          <article key={title}>
            <strong>{title}</strong>
            <p>{body}</p>
          </article>
        ))}
      </div>
    </section>
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
        <span className="shop-page-mark">06</span>
        <h1>쿠폰 적립 안내</h1>
        <p>적립 기준, 교환 조건, 현재 상태를 한 화면에서 확인합니다.</p>
      </div>
      <div className="shop-guide-layout">
        <section className="shop-guide-board">
          <h2>현재 적립판</h2>
          <strong>{props.wallet?.issuedCouponCount ?? 0} / 10장</strong>
          <p>다음 교환까지 {props.wallet?.remainingToNextExchange ?? 10}장 남았습니다.</p>
          <StampBoard count={props.wallet?.issuedCouponCount ?? 0} />
          <button className="button" type="button" onClick={props.onCatalog}>상품 보러가기</button>
        </section>
        <section className="shop-guide-rules">
          <h2>적립 규칙</h2>
          <div className="shop-rule-list">
            <div><strong>5,000원</strong><span>결제 금액 기준 쿠폰 1장</span></div>
            <div><strong>10장</strong><span>교환 상품 1개 신청 가능</span></div>
            <div><strong>즉시 반영</strong><span>결제 완료 후 지갑에 자동 반영</span></div>
          </div>
        </section>
        <section className="shop-guide-signup">
          <h2>{props.member ? '회원 연결 완료' : '회원 혜택 시작'}</h2>
          {props.member ? (
            <>
              <p>{props.member.email}</p>
              <button className="button secondary" type="button" onClick={props.onCatalog}>상품 보러가기</button>
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
  onCart: (product: Product) => void
  onCheckout: (product: Product) => void
  onPreviousPage: () => void
  onNextPage: () => void
}) {
  return (
    <section className="shop-view">
      <div className="shop-page-title">
        <h1>상품 목록</h1>
        <p>커피 키오스크처럼 카테고리에서 메뉴를 고르고 장바구니에 담아 결제합니다.</p>
      </div>
      <div className="shop-kiosk-menu-layout">
        <aside className="shop-kiosk-categories" aria-label="커피 카테고리">
          {['전체 메뉴', '커피', '라떼', '티·에이드', '디저트', '교환 상품'].map((category, index) => (
            <button className={index === 0 ? 'active' : ''} key={category} type="button">
              {category}
            </button>
          ))}
        </aside>
        <div>
          <div className="shop-catalog-toolbar">
            <button className="active" type="button">추천순</button>
            <button type="button">쿠폰 적립</button>
            <button type="button">빠른 주문</button>
            <button type="button">낮은 가격순</button>
            <span>상품 {props.productPage?.totalElements ?? 0}개</span>
          </div>
          <PaginationControls
            label="상품"
            page={props.productPage}
            isLoading={props.isLoading}
            onPrevious={props.onPreviousPage}
            onNext={props.onNextPage}
          />
          <div className="shop-catalog-list">
            {props.isLoading ? <p>상품을 불러오는 중</p> : null}
            {!props.isLoading && !props.products.length ? <p>상품 없음</p> : null}
            {props.products.map((product) => (
              <article className="shop-catalog-row" key={product.id}>
                <button className="shop-catalog-photo" type="button" onClick={() => props.onOpenDetail(product)}>
                  <Coffee size={24} />
                  <span>{product.exchangeEligible ? '교환' : '커피'}</span>
                </button>
                <div>
                  <strong>{product.name}</strong>
                  <span>구매 시 쿠폰 {productCouponCount(product)}장 적립</span>
                </div>
                <StatusBadge value={product.saleStatus} />
                <strong>{product.price.toLocaleString()} KRW</strong>
                <div className="shop-catalog-actions">
                  <button className="button secondary" disabled={product.saleStatus !== 'ON_SALE'} type="button" onClick={() => props.onCart(product)}>담기</button>
                  <button className="button" disabled={!props.member || product.saleStatus !== 'ON_SALE'} type="button" onClick={() => props.onCheckout(product)}>구매</button>
                </div>
              </article>
            ))}
          </div>
        </div>
        <aside className="shop-kiosk-ticket">
          <span>주문 요약</span>
          <strong>{props.products[0]?.name ?? '메뉴 선택 전'}</strong>
          <p>메뉴를 담으면 옵션 확인 후 바로 픽업 결제로 이동합니다.</p>
          <button className="button secondary" type="button" disabled={!props.products.length} onClick={() => props.products[0] ? props.onCart(props.products[0]) : undefined}>
            빠른 담기
          </button>
        </aside>
      </div>
    </section>
  )
}

function ShopOrderPreviewPage(props: {
  product: Product
  member: Member | null
  onDetail: () => void
  onCheckout: () => void
}) {
  const couponCount = productCouponCount(props.product)
  return (
    <section className="shop-view">
      <div className="shop-page-title">
        <h1>장바구니</h1>
        <p>결제 전 상품, 회원, 적립 예정 쿠폰을 확인합니다.</p>
      </div>
      <div className="shop-order-preview">
        <section className="shop-order-card">
          <h2>주문 상품</h2>
          <div className="shop-checkout-item">
            <div className="shop-thumb">상품</div>
            <div>
              <strong>{props.product.name}</strong>
              <span>수량 1</span>
            </div>
            <strong>{props.product.price.toLocaleString()} KRW</strong>
          </div>
          <CoffeeOptionPanel />
        </section>
        <section className="shop-order-card">
          <h2>구매자</h2>
          <div className="shop-buyer-box">
            <strong>{props.member?.name ?? '게스트'}</strong>
            <span>{props.member?.email ?? '가입 후 결제할 수 있습니다.'}</span>
          </div>
        </section>
        <aside className="shop-order-card shop-order-total">
          <h2>적립 예정</h2>
          <strong>쿠폰 {couponCount}장</strong>
          <p>결제 완료 시 쿠폰 지갑에 반영됩니다.</p>
          <button className="button" type="button" disabled={!props.member} onClick={props.onCheckout}>
            <CreditCard size={16} /> 결제로 이동
          </button>
          <button className="button secondary" type="button" onClick={props.onDetail}>상품 상세</button>
        </aside>
      </div>
    </section>
  )
}

function ShopEmptyCartPage(props: { onCatalog: () => void }) {
  return (
    <section className="shop-view">
      <div className="shop-empty-cart">
        <ShoppingCart size={32} />
        <h1>장바구니</h1>
        <p>아직 담긴 상품이 없습니다. 상품을 먼저 고른 뒤 결제로 이동할 수 있습니다.</p>
        <button className="button" type="button" onClick={props.onCatalog}>상품 보러가기</button>
      </div>
    </section>
  )
}

function ShopHomePage(props: {
  products: Product[]
  isLoading: boolean
  member: Member | null
  wallet?: CouponWallet
  onLogin: () => void
  onOpenDetail: (product: Product) => void
  onCart: (product: Product) => void
  onCheckout: (product: Product) => void
}) {
  const issuedCouponCount = props.wallet?.issuedCouponCount ?? 0
  const primaryProduct = props.products[0]
  return (
    <>
      <section className="shop-hero-band">
        <div>
          <span className="shop-hero-kicker">Coffee Kiosk Order</span>
          <h1>커피 주문을 키오스크처럼 빠르게 끝내세요</h1>
          <p>메뉴 선택, 옵션 확인, 픽업 결제, 쿠폰 적립까지 한 흐름으로 이어집니다. 결제 후 마이페이지에서 적립 현황과 교환 가능 상태를 바로 확인합니다.</p>
          <div className="shop-hero-actions">
            <button className="button" type="button" onClick={() => primaryProduct ? props.onCart(primaryProduct) : undefined}><ShoppingBag size={16} /> 빠른 주문</button>
            <a className="button secondary" href="#products"><Search size={16} /> 메뉴 보기</a>
          </div>
        </div>
        <div className="shop-hero-visual" aria-hidden="true">
          <div className="shop-hero-product large">{primaryProduct?.name ?? '추천 상품'}<br />{(primaryProduct?.price ?? 0).toLocaleString()} KRW</div>
          <div className="shop-hero-product wide">ICE/HOT 선택<br />픽업 결제</div>
          <div className="shop-coupon-float">
            <strong>쿠폰 현황</strong>
            <StampBoard count={issuedCouponCount} size="small" />
          </div>
        </div>
      </section>

      <section className="shop-benefit-strip" aria-label="쇼핑 혜택">
        <span><Coffee size={17} /> 메뉴 선택</span>
        <span><PackageCheck size={17} /> 옵션 확인</span>
        <span><ShieldCheck size={17} /> 픽업 결제</span>
        <span><Star size={17} /> 쿠폰 자동 적립</span>
      </section>

      <section className="shop-kiosk-shortcuts" aria-label="커피 주문 바로가기">
        <button type="button" onClick={() => primaryProduct ? props.onCart(primaryProduct) : undefined}><Timer size={17} /> 최근 주문처럼 담기</button>
        <button type="button" onClick={() => primaryProduct ? props.onOpenDetail(primaryProduct) : undefined}><Coffee size={17} /> 메뉴 상세 보기</button>
        <button type="button" onClick={() => props.onCheckout(primaryProduct!)} disabled={!primaryProduct || !props.member}><CreditCard size={17} /> 바로 결제</button>
      </section>

      <section className="shop-task-grid">
        <div className="shop-panel shop-login-cta">
          <h2>로그인 후 쿠폰 지갑 확인</h2>
          <p>회원 시작과 쿠폰 지갑 확인은 로그인 화면에서 이어집니다. 새 회원은 상단 가입하기에서 별도 가입 화면으로 이동합니다.</p>
          <button className="button secondary" type="button" onClick={props.onLogin}>
            <User size={16} /> 로그인으로 이동
          </button>
        </div>

        <CouponSummaryPanel member={props.member} wallet={props.wallet} />
      </section>

      <section className="shop-section" id="products">
        <div className="shop-section-head">
          <div>
            <h2>추천 상품</h2>
            <p>키오스크 메뉴판처럼 빠르게 고르고, 쿠폰은 결제 혜택으로 확인합니다.</p>
          </div>
          <span className="status info">상품 {props.products.length}개</span>
        </div>
        <div className="shop-category-row" aria-label="상품 카테고리">
          <button className="active" type="button">전체</button>
          <button type="button">커피</button>
          <button type="button">빠른 주문</button>
          <button type="button">교환 추천</button>
        </div>
        <div className="product-list shop-product-list">
          {props.isLoading ? <p>불러오는 중</p> : null}
          {!props.isLoading && !props.products.length ? <p>상품 없음</p> : null}
          {props.products.map((product, index) => (
            <ShopProductCard
              key={product.id}
              product={product}
              featured={index === 0}
              disabled={!props.member || product.saleStatus !== 'ON_SALE'}
              onDetail={() => props.onOpenDetail(product)}
              onCart={() => props.onCart(product)}
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
          <p>상품 목록은 로그인 없이 확인할 수 있고, 결제는 회원 가입 후 진행됩니다.</p>
          <button className="button secondary" type="button" onClick={props.onCatalog}>상품 먼저 보기</button>
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
        <p>데모 회원을 만들고 상품 구매, 결제, 쿠폰 적립 흐름을 이어갑니다.</p>
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
        <button className="button secondary" type="button" onClick={props.onHome}>상품 보러가기</button>
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
          <h2>다음 구매 추천</h2>
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
  onCart: (product: Product) => void
  onCheckout: (product: Product) => void
  onNotice: (message: string) => void
}) {
  const couponCount = productCouponCount(props.product)
  return (
    <section className="shop-view shop-detail-view">
      <div className="shop-photo-area">
        <span>{props.product.exchangeEligible ? '교환 상품' : '상품 이미지'}</span>
        <span className="status ok">쿠폰 적립 대상</span>
      </div>
      <div className="shop-detail-info">
        <span className="shop-crumb">홈 / 커피 메뉴 / {props.product.name}</span>
        <h1>{props.product.name}</h1>
        <p>결제 후 쿠폰이 자동 적립되는 커피 메뉴입니다. 옵션을 확인한 뒤 픽업 결제로 이동할 수 있습니다.</p>
        <strong className="shop-detail-price">{props.product.price.toLocaleString()} KRW</strong>
        <div className="shop-coupon-notice">
          <strong>쿠폰 {couponCount}장 적립</strong>
          <span>5,000원 단위로 계산되어 결제 완료 즉시 지갑에 반영됩니다.</span>
        </div>
        <CoffeeOptionPanel />
        <div className="shop-detail-actions">
          <button className="button secondary" type="button" onClick={() => props.onCart(props.product)}>장바구니</button>
          <button className="button" type="button" disabled={!props.member || props.product.saleStatus !== 'ON_SALE'} onClick={() => props.onCheckout(props.product)}>
            바로 구매
          </button>
        </div>
        <div className="shop-exchange-box">
          <div>
            <strong>교환 상품 안내</strong>
            <span>쿠폰 10장을 모으면 5,000원 상품으로 교환할 수 있습니다.</span>
          </div>
          <button className="button secondary" type="button" onClick={props.onHome}>상품 더보기</button>
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
        <h1>픽업 결제</h1>
        <p>주문 상품, 픽업 정보, 적립 쿠폰을 확인한 뒤 결제합니다.</p>
      </div>
      <div className="shop-checkout-layout">
        <div className="shop-checkout-main">
          <section className="shop-order-items">
            <h2>주문 상품</h2>
            <div className="shop-checkout-item">
              <div className="shop-thumb">상품</div>
              <div>
                <strong>{props.product.name}</strong>
                <span>수량 1</span>
              </div>
              <strong>{props.product.price.toLocaleString()} KRW</strong>
            </div>
          </section>
          <section className="shop-delivery">
            <h2>픽업 정보</h2>
            <div className="shop-field-grid">
              <label>
                <span>주문자</span>
                <input value={props.member?.name ?? ''} readOnly placeholder="회원 가입 필요" />
              </label>
              <label>
                <span>픽업 방식</span>
                <input value="매장 픽업" readOnly />
              </label>
              <label className="wide">
                <span>제조 요청</span>
                <input value="결제 후 즉시 제조" readOnly />
              </label>
            </div>
          </section>
        </div>
        <aside className="shop-summary">
          <h2>결제 요약</h2>
          <div className="shop-sum-row"><span>상품 금액</span><strong>{props.product.price.toLocaleString()} KRW</strong></div>
          <div className="shop-sum-row"><span>픽업비</span><strong>0 KRW</strong></div>
          <div className="shop-sum-total"><span>총 결제</span><strong>{props.product.price.toLocaleString()} KRW</strong></div>
          <div className="shop-earn-coupon">
            <strong>쿠폰 {couponCount}장 적립 예정</strong>
            <span>결제 성공 후 쿠폰 지갑에 즉시 반영됩니다.</span>
          </div>
          <button className="button" type="button" disabled={!props.member || props.pending} onClick={props.onPay}>
            <CreditCard size={16} /> 결제하기
          </button>
          <button className="button secondary" type="button" onClick={props.onBack}>상품으로 돌아가기</button>
          <p>결제하면 주문 생성, 결제 승인, 쿠폰 발급이 한 번에 검증됩니다.</p>
        </aside>
      </div>
    </section>
  )
}

function CoffeeOptionPanel() {
  return (
    <div className="shop-coffee-options" aria-label="옵션 선택">
      <div>
        <span>온도</span>
        <div className="shop-option-buttons">
          <button className="active" type="button"><Snowflake size={15} /> ICE</button>
          <button type="button"><Flame size={15} /> HOT</button>
        </div>
      </div>
      <div>
        <span>컵</span>
        <div className="shop-option-buttons">
          <button className="active" type="button">매장컵</button>
          <button type="button">개인컵</button>
        </div>
      </div>
      <div>
        <span>픽업</span>
        <div className="shop-option-buttons">
          <button className="active" type="button"><Timer size={15} /> 즉시 제조</button>
        </div>
      </div>
    </div>
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
  disabled: boolean
  onDetail: () => void
  onCart: () => void
  onCheckout: () => void
}) {
  const couponCount = productCouponCount(props.product)
  return (
    <article className={`product-card shop-product-card ${props.featured ? 'featured' : ''}`}>
      <button className="shop-product-photo" type="button" onClick={props.onDetail}>
        <span>{props.product.exchangeEligible ? '교환 상품' : '추천 상품'}</span>
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
        <button className="button secondary" disabled={props.product.saleStatus !== 'ON_SALE'} onClick={props.onCart}>
          <ShoppingCart size={16} /> {props.product.name} 담기
        </button>
        <button className="button" disabled={props.disabled} onClick={props.onCheckout}>
          <ShoppingBag size={16} /> {props.product.name} 구매
        </button>
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
