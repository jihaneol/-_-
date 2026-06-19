import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { BadgeDollarSign, Coffee, PackagePlus, RotateCcw, ShoppingCart, UserPlus } from 'lucide-react'
import { useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { commerceApi, commerceKeys } from '../../entities/commerce/api'
import type { ApiError } from '../../shared/api/client'

const memberSchema = z.object({
  name: z.string().min(1),
  email: z.string().email(),
})

const productSchema = z.object({
  name: z.string().min(1),
  price: z.coerce.number().int().positive(),
  stock: z.coerce.number().int().nonnegative(),
})

const orderSchema = z.object({
  memberId: z.coerce.number().int().positive(),
  productId: z.coerce.number().int().positive(),
  quantity: z.coerce.number().int().positive(),
})

const paymentSchema = z.object({
  orderId: z.coerce.number().int().positive(),
  idempotencyKey: z.string().min(1),
})

type MemberForm = z.infer<typeof memberSchema>
type ProductForm = z.infer<typeof productSchema>
type OrderForm = z.infer<typeof orderSchema>
type PaymentForm = z.infer<typeof paymentSchema>

export function CommerceDashboardPage() {
  const queryClient = useQueryClient()
  const [selectedMemberId, setSelectedMemberId] = useState<number | null>(null)
  const [notice, setNotice] = useState<string>('')
  const [error, setError] = useState<string>('')

  const members = useQuery({ queryKey: commerceKeys.members, queryFn: commerceApi.listMembers })
  const products = useQuery({ queryKey: commerceKeys.products, queryFn: commerceApi.listProducts })
  const orders = useQuery({ queryKey: commerceKeys.orders, queryFn: commerceApi.listOrders })
  const coupons = useQuery({
    queryKey: selectedMemberId ? commerceKeys.coupons(selectedMemberId) : ['commerce', 'coupons', 'idle'],
    queryFn: () => commerceApi.listCoupons(selectedMemberId!),
    enabled: selectedMemberId !== null,
  })
  const histories = useQuery({
    queryKey: selectedMemberId ? commerceKeys.histories(selectedMemberId) : ['commerce', 'coupon-histories', 'idle'],
    queryFn: () => commerceApi.listCouponHistories(selectedMemberId!),
    enabled: selectedMemberId !== null,
  })

  const memberForm = useForm<MemberForm>({ resolver: zodResolver(memberSchema), defaultValues: { name: '', email: '' } })
  const productForm = useForm<ProductForm>({
    resolver: zodResolver(productSchema) as never,
    defaultValues: { name: '', price: 5000, stock: 10 },
  })
  const orderForm = useForm<OrderForm>({ resolver: zodResolver(orderSchema) as never })
  const paymentForm = useForm<PaymentForm>({
    resolver: zodResolver(paymentSchema) as never,
    defaultValues: { idempotencyKey: `pay-${Date.now()}` },
  })

  const invalidateCommerce = async () => {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: commerceKeys.members }),
      queryClient.invalidateQueries({ queryKey: commerceKeys.products }),
      queryClient.invalidateQueries({ queryKey: commerceKeys.orders }),
    ])
    if (selectedMemberId) {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: commerceKeys.coupons(selectedMemberId) }),
        queryClient.invalidateQueries({ queryKey: commerceKeys.histories(selectedMemberId) }),
      ])
    }
  }

  const onError = (apiError: ApiError) => {
    setError(apiError.message)
    setNotice('')
  }

  const createMember = useMutation({
    mutationFn: commerceApi.createMember,
    onSuccess: async (member) => {
      setSelectedMemberId(member.id)
      setNotice(`회원 #${member.id} 생성`)
      setError('')
      memberForm.reset({ name: '', email: '' })
      await invalidateCommerce()
    },
    onError,
  })

  const createProduct = useMutation({
    mutationFn: async (form: ProductForm) => {
      const product = await commerceApi.createProduct({ name: form.name, price: form.price })
      await commerceApi.createInventory(product.id, { quantity: form.stock })
      return product
    },
    onSuccess: async (product) => {
      setNotice(`상품 #${product.id} 및 재고 생성`)
      setError('')
      productForm.reset({ name: '', price: 5000, stock: 10 })
      await invalidateCommerce()
    },
    onError,
  })

  const createOrder = useMutation({
    mutationFn: commerceApi.createOrder,
    onSuccess: async (order) => {
      setSelectedMemberId(order.memberId)
      setNotice(`주문 #${order.id} 생성`)
      setError('')
      paymentForm.setValue('orderId', order.id)
      await invalidateCommerce()
    },
    onError,
  })

  const payOrder = useMutation({
    mutationFn: (form: PaymentForm) => commerceApi.payOrder(form.orderId, { idempotencyKey: form.idempotencyKey }),
    onSuccess: async (result) => {
      setNotice(`결제 완료: 쿠폰 ${result.issuedCouponCount}장 발급`)
      setError('')
      await invalidateCommerce()
    },
    onError,
  })

  const refundOrder = useMutation({
    mutationFn: (orderId: number) => commerceApi.refundOrder(orderId),
    onSuccess: async (result) => {
      setNotice(`환불 완료: 쿠폰 ${result.voidedCouponCount}장 무효화`)
      setError('')
      await invalidateCommerce()
    },
    onError,
  })

  const paidOrders = useMemo(() => orders.data?.filter((order) => order.status === 'PAID') ?? [], [orders.data])
  const issuedCouponCount = coupons.data?.filter((coupon) => coupon.status === 'ISSUED').length ?? 0

  return (
    <div className="page">
      <header className="topbar">
        <h1>Commerce Coupon Admin</h1>
        <span>주문 결제, 도장 쿠폰, 전체 환불 운영</span>
      </header>

      <main className="content">
        <section className="grid">
          {notice ? <div className="notice" role="status">{notice}</div> : null}
          {error ? <div className="notice error" role="alert">{error}</div> : null}

          <section className="panel">
            <h2>회원 생성</h2>
            <form className="grid" onSubmit={memberForm.handleSubmit((form) => createMember.mutate(form))}>
              <Field label="이름" error={memberForm.formState.errors.name?.message}>
                <input {...memberForm.register('name')} placeholder="Kim" />
              </Field>
              <Field label="이메일" error={memberForm.formState.errors.email?.message}>
                <input {...memberForm.register('email')} placeholder="kim@example.com" />
              </Field>
              <button className="button" disabled={createMember.isPending}>
                <UserPlus size={16} /> 생성
              </button>
            </form>
          </section>

          <section className="panel">
            <h2>상품 및 재고 생성</h2>
            <form className="grid" onSubmit={productForm.handleSubmit((form) => createProduct.mutate(form))}>
              <Field label="상품명" error={productForm.formState.errors.name?.message}>
                <input {...productForm.register('name')} placeholder="Americano" />
              </Field>
              <div className="split">
                <Field label="가격" error={productForm.formState.errors.price?.message}>
                  <input type="number" {...productForm.register('price')} />
                </Field>
                <Field label="재고" error={productForm.formState.errors.stock?.message}>
                  <input type="number" {...productForm.register('stock')} />
                </Field>
              </div>
              <button className="button" disabled={createProduct.isPending}>
                <PackagePlus size={16} /> 생성
              </button>
            </form>
          </section>

          <section className="panel">
            <h2>주문 생성</h2>
            <form
              className="grid"
              onSubmit={orderForm.handleSubmit((form) =>
                createOrder.mutate({
                  memberId: form.memberId,
                  lines: [{ productId: form.productId, quantity: form.quantity }],
                }),
              )}
            >
              <div className="split">
                <Field label="회원" error={orderForm.formState.errors.memberId?.message}>
                  <select {...orderForm.register('memberId')}>
                    <option value="">선택</option>
                    {members.data?.map((member) => <option key={member.id} value={member.id}>#{member.id} {member.name}</option>)}
                  </select>
                </Field>
                <Field label="상품" error={orderForm.formState.errors.productId?.message}>
                  <select {...orderForm.register('productId')}>
                    <option value="">선택</option>
                    {products.data?.map((product) => <option key={product.id} value={product.id}>#{product.id} {product.name}</option>)}
                  </select>
                </Field>
              </div>
              <Field label="수량" error={orderForm.formState.errors.quantity?.message}>
                <input type="number" {...orderForm.register('quantity')} />
              </Field>
              <button className="button" disabled={createOrder.isPending}>
                <ShoppingCart size={16} /> 주문 생성
              </button>
            </form>
          </section>

          <section className="panel">
            <h2>결제 실행</h2>
            <form className="grid" onSubmit={paymentForm.handleSubmit((form) => payOrder.mutate(form))}>
              <Field label="주문" error={paymentForm.formState.errors.orderId?.message}>
                <select {...paymentForm.register('orderId')}>
                  <option value="">선택</option>
                  {orders.data?.map((order) => <option key={order.id} value={order.id}>#{order.id} {order.status}</option>)}
                </select>
              </Field>
              <Field label="중복 요청 방지 키" error={paymentForm.formState.errors.idempotencyKey?.message}>
                <input {...paymentForm.register('idempotencyKey')} />
              </Field>
              <button className="button" disabled={payOrder.isPending}>
                <BadgeDollarSign size={16} /> 결제
              </button>
            </form>
          </section>
        </section>

        <section className="grid">
          <section className="panel split">
            <Metric label="회원" value={members.data?.length ?? 0} />
            <Metric label="상품" value={products.data?.length ?? 0} />
            <Metric label="주문" value={orders.data?.length ?? 0} />
            <Metric label="사용 가능 도장" value={issuedCouponCount} />
          </section>

          <section className="panel">
            <h2>주문</h2>
            <table className="table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>회원</th>
                  <th>금액</th>
                  <th>상태</th>
                  <th>작업</th>
                </tr>
              </thead>
              <tbody>
                {orders.isLoading ? <Row colSpan={5} text="불러오는 중" /> : null}
                {!orders.isLoading && !orders.data?.length ? <Row colSpan={5} text="주문 없음" /> : null}
                {orders.data?.map((order) => (
                  <tr key={order.id}>
                    <td>#{order.id}</td>
                    <td>#{order.memberId}</td>
                    <td>{order.totalAmount.toLocaleString()} {order.currency}</td>
                    <td><StatusBadge value={order.status} /></td>
                    <td>
                      <button
                        className="button danger"
                        disabled={order.status !== 'PAID' || refundOrder.isPending}
                        onClick={() => refundOrder.mutate(order.id)}
                      >
                        <RotateCcw size={15} /> 환불
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </section>

          <section className="panel">
            <h2>쿠폰 도장</h2>
            <div className="actions">
              <select
                aria-label="쿠폰 조회 회원"
                value={selectedMemberId ?? ''}
                onChange={(event) => setSelectedMemberId(Number(event.target.value) || null)}
              >
                <option value="">회원 선택</option>
                {members.data?.map((member) => <option key={member.id} value={member.id}>#{member.id} {member.name}</option>)}
              </select>
              <span className="status ok"><Coffee size={14} /> {issuedCouponCount}/10</span>
            </div>
            <table className="table">
              <thead>
                <tr>
                  <th>쿠폰</th>
                  <th>주문</th>
                  <th>결제</th>
                  <th>상태</th>
                </tr>
              </thead>
              <tbody>
                {!selectedMemberId ? <Row colSpan={4} text="회원 선택 필요" /> : null}
                {selectedMemberId && coupons.isLoading ? <Row colSpan={4} text="불러오는 중" /> : null}
                {coupons.data?.map((coupon) => (
                  <tr key={coupon.id}>
                    <td>#{coupon.id}</td>
                    <td>#{coupon.orderId}</td>
                    <td>#{coupon.paymentId}</td>
                    <td><StatusBadge value={coupon.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </section>

          <section className="panel">
            <h2>쿠폰 히스토리</h2>
            <table className="table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>쿠폰</th>
                  <th>주문</th>
                  <th>유형</th>
                </tr>
              </thead>
              <tbody>
                {!selectedMemberId ? <Row colSpan={4} text="회원 선택 필요" /> : null}
                {histories.data?.map((history) => (
                  <tr key={history.id}>
                    <td>#{history.id}</td>
                    <td>{history.couponId ? `#${history.couponId}` : '-'}</td>
                    <td>#{history.orderId}</td>
                    <td><StatusBadge value={history.type} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </section>

          {paidOrders.length ? <div className="notice">환불 가능 주문 {paidOrders.length}건</div> : null}
        </section>
      </main>
    </div>
  )
}

function Field(props: { label: string; error?: string; children: React.ReactNode }) {
  return (
    <label className="field">
      <span>{props.label}</span>
      {props.children}
      {props.error ? <span className="status bad">{props.error}</span> : null}
    </label>
  )
}

function Metric(props: { label: string; value: number }) {
  return (
    <div>
      <div className="field"><label>{props.label}</label></div>
      <strong>{props.value.toLocaleString()}</strong>
    </div>
  )
}

function Row(props: { colSpan: number; text: string }) {
  return (
    <tr>
      <td colSpan={props.colSpan}>{props.text}</td>
    </tr>
  )
}

function StatusBadge(props: { value: string }) {
  const kind = props.value === 'PAID' || props.value === 'ISSUED' || props.value === 'AUTHORIZED'
    ? 'ok'
    : props.value === 'CREATED'
      ? 'warn'
      : 'bad'
  return <span className={`status ${kind}`}>{props.value}</span>
}
