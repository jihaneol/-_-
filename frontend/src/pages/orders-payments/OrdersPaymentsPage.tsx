import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { BadgeDollarSign, Ban, RotateCcw, ShoppingCart } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { adminCommerceApi, adminCommerceKeys } from '../../entities/commerce/api'
import type { ApiError } from '../../shared/api/client'
import { Field, Notice, Row, StatusBadge } from '../../shared/ui'

const orderSchema = z.object({
  memberId: z.coerce.number().int().positive(),
  productId: z.coerce.number().int().positive(),
  quantity: z.coerce.number().int().positive(),
})

const paymentSchema = z.object({
  orderId: z.coerce.number().int().positive(),
  idempotencyKey: z.string().min(1),
})

type OrderForm = z.infer<typeof orderSchema>
type PaymentForm = z.infer<typeof paymentSchema>

export function OrdersPaymentsPage() {
  const queryClient = useQueryClient()
  const [notice, setNotice] = useState('')
  const [error, setError] = useState('')
  const members = useQuery({ queryKey: adminCommerceKeys.members, queryFn: adminCommerceApi.listMembers })
  const products = useQuery({ queryKey: adminCommerceKeys.products, queryFn: adminCommerceApi.listProducts })
  const orders = useQuery({ queryKey: adminCommerceKeys.orders, queryFn: adminCommerceApi.listOrders })
  const orderForm = useForm<OrderForm>({
    resolver: zodResolver(orderSchema) as never,
    defaultValues: { quantity: 1 },
  })
  const paymentForm = useForm<PaymentForm>({
    resolver: zodResolver(paymentSchema) as never,
    defaultValues: { idempotencyKey: `pay-${Date.now()}` },
  })
  const invalidateOrders = async () => {
    await queryClient.invalidateQueries({ queryKey: adminCommerceKeys.orders })
    await queryClient.invalidateQueries({ queryKey: adminCommerceKeys.summary })
    await queryClient.invalidateQueries({ queryKey: adminCommerceKeys.couponConsistency })
  }
  const onError = (apiError: ApiError) => {
    setError(apiError.message)
    setNotice('')
  }
  const createOrder = useMutation({
    mutationFn: adminCommerceApi.createOrder,
    onSuccess: async (order) => {
      setNotice(`주문 #${order.id} 생성`)
      setError('')
      paymentForm.setValue('orderId', order.id)
      await invalidateOrders()
    },
    onError,
  })
  const payOrder = useMutation({
    mutationFn: (form: PaymentForm) => adminCommerceApi.payOrder(form.orderId, { idempotencyKey: form.idempotencyKey }),
    onSuccess: async (result) => {
      setNotice(`결제 완료: 쿠폰 ${result.issuedCouponCount}장 발급`)
      setError('')
      paymentForm.setValue('idempotencyKey', `pay-${Date.now()}`)
      await invalidateOrders()
    },
    onError,
  })
  const cancelOrder = useMutation({
    mutationFn: adminCommerceApi.cancelOrder,
    onSuccess: async (order) => {
      setNotice(`주문 #${order.id} 취소`)
      setError('')
      await invalidateOrders()
    },
    onError,
  })
  const refundOrder = useMutation({
    mutationFn: adminCommerceApi.refundOrder,
    onSuccess: async (result) => {
      setNotice(`환불 완료: 쿠폰 ${result.voidedCouponCount}장 무효화`)
      setError('')
      await invalidateOrders()
    },
    onError,
  })
  const showInvalidForm = () => {
    setError('입력값을 확인해주세요.')
    setNotice('')
  }

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <h1>주문/결제</h1>
          <p>주문 생성, 결제, 결제 전 취소, 전체 환불을 처리합니다.</p>
        </div>
      </header>
      <Notice text={notice} error={error} />
      <div className="content two-column">
        <section className="panel">
          <h2>주문 생성</h2>
          <form
            className="grid"
            onSubmit={orderForm.handleSubmit((form) =>
              createOrder.mutate({
                memberId: form.memberId,
                lines: [{ productId: form.productId, quantity: form.quantity }],
              }),
              showInvalidForm,
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
          <form className="grid" onSubmit={paymentForm.handleSubmit((form) => payOrder.mutate(form), showInvalidForm)}>
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
      </div>

      <section className="panel">
        <h2>주문 목록</h2>
        <table className="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>회원</th>
              <th>상품</th>
              <th>금액</th>
              <th>상태</th>
              <th>작업</th>
            </tr>
          </thead>
          <tbody>
            {orders.isLoading ? <Row colSpan={6} text="불러오는 중" /> : null}
            {!orders.isLoading && !orders.data?.length ? <Row colSpan={6} text="주문 없음" /> : null}
            {orders.data?.map((order) => (
              <tr key={order.id}>
                <td>#{order.id}</td>
                <td>#{order.memberId}</td>
                <td>{order.lines.map((line) => `${line.productName} x${line.quantity}`).join(', ')}</td>
                <td>{order.totalAmount.toLocaleString()} {order.currency}</td>
                <td><StatusBadge value={order.status} /></td>
                <td>
                  <div className="actions">
                    <button
                      className="button secondary"
                      disabled={order.status !== 'CREATED' || cancelOrder.isPending}
                      onClick={() => cancelOrder.mutate(order.id)}
                    >
                      <Ban size={15} /> 취소
                    </button>
                    <button
                      className="button danger"
                      disabled={order.status !== 'PAID' || refundOrder.isPending}
                      onClick={() => refundOrder.mutate(order.id)}
                    >
                      <RotateCcw size={15} /> 환불
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  )
}
