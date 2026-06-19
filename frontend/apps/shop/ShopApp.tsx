import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Coffee, ShoppingBag, UserPlus } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { shopCommerceApi, shopCommerceKeys } from '../../src/entities/commerce/api'
import type { Member, Product } from '../../src/entities/commerce/types'
import type { ApiError } from '../../src/shared/api/client'
import { Field, Notice, StatusBadge } from '../../src/shared/ui'

const signupSchema = z.object({
  name: z.string().min(1),
  email: z.string().email(),
})

type SignupForm = z.infer<typeof signupSchema>

export function ShopApp() {
  const queryClient = useQueryClient()
  const [member, setMember] = useState<Member | null>(null)
  const [notice, setNotice] = useState('')
  const [error, setError] = useState('')
  const products = useQuery({ queryKey: shopCommerceKeys.products, queryFn: shopCommerceApi.listProducts })
  const coupons = useQuery({
    queryKey: member ? shopCommerceKeys.coupons(member.id) : shopCommerceKeys.couponsIdle,
    queryFn: () => shopCommerceApi.listCoupons(member!.id),
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
      signupForm.reset({ name: '', email: '' })
      await queryClient.invalidateQueries({ queryKey: shopCommerceKeys.coupons(createdMember.id) })
    },
    onError,
  })
  const buyProduct = useMutation({
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
      if (member) {
        await queryClient.invalidateQueries({ queryKey: shopCommerceKeys.coupons(member.id) })
        await queryClient.invalidateQueries({ queryKey: shopCommerceKeys.histories(member.id) })
      }
    },
    onError,
  })
  const issuedCouponCount = coupons.data?.filter((coupon) => coupon.status === 'ISSUED').length ?? 0

  return (
    <main className="shop-shell">
      <section className="shop-hero">
        <div>
          <h1>쇼핑몰</h1>
          <p>회원 가입 후 상품을 구매하면 도장 쿠폰이 적립됩니다.</p>
        </div>
        <div className="shop-coupon">
          <Coffee size={18} />
          <strong>보유 쿠폰 {issuedCouponCount}장</strong>
          <span>{member ? `${member.name} 회원` : '가입 후 확인'}</span>
        </div>
      </section>

      <Notice text={notice} error={error} />

      <section className="content two-column">
        <div className="panel">
          <h2>회원 가입</h2>
          <form className="grid" onSubmit={signupForm.handleSubmit((values) => signup.mutate(values))}>
            <Field label="이름" error={signupForm.formState.errors.name?.message}>
              <input {...signupForm.register('name')} placeholder="Lee" />
            </Field>
            <Field label="이메일" error={signupForm.formState.errors.email?.message}>
              <input {...signupForm.register('email')} placeholder="lee@example.com" />
            </Field>
            <button className="button" disabled={signup.isPending}>
              <UserPlus size={16} /> 가입
            </button>
          </form>
        </div>

        <div className="panel">
          <h2>이용 상태</h2>
          <div className="quick-stats">
            <span><ShoppingBag size={16} /> 상품 {products.data?.length ?? 0}</span>
            <span><Coffee size={16} /> 쿠폰 {issuedCouponCount}/10</span>
          </div>
        </div>
      </section>

      <section className="panel">
        <h2>상품</h2>
        <div className="product-list">
          {products.isLoading ? <p>불러오는 중</p> : null}
          {!products.isLoading && !products.data?.length ? <p>상품 없음</p> : null}
          {products.data?.map((product) => (
            <article className="product-card" key={product.id}>
              <div>
                <strong>{product.name}</strong>
                <span>{product.price.toLocaleString()} KRW</span>
              </div>
              <StatusBadge value={product.saleStatus} />
              <button
                className="button"
                disabled={!member || buyProduct.isPending || product.saleStatus !== 'ON_SALE'}
                onClick={() => buyProduct.mutate(product)}
              >
                <ShoppingBag size={16} /> {product.name} 구매
              </button>
            </article>
          ))}
        </div>
      </section>
    </main>
  )
}
