import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { afterAll, afterEach, beforeAll, describe, expect, it } from 'vitest'
import { App } from './App'

let members = [{ id: 1, name: 'Kim', email: 'kim@example.com' }]
let products = [{ id: 1, name: 'Americano', price: 12000, saleStatus: 'ON_SALE' }]
let orders: any[] = []
let coupons: any[] = []
let histories: any[] = []

const server = setupServer(
  http.get('/api/dashboard/summary', () => ok({
    memberCount: members.length,
    productCount: products.length,
    orderCount: orders.length,
    paidOrderCount: orders.filter((order) => order.status === 'PAID').length,
    refundedOrderCount: orders.filter((order) => order.status === 'REFUNDED').length,
    issuedCouponCount: coupons.filter((coupon) => coupon.status === 'ISSUED').length,
  })),
  http.get('/api/members', () => ok(members)),
  http.post('/api/members', async ({ request }) => {
    const body = await request.json() as any
    const member = { id: 2, name: body.name, email: body.email }
    members = [...members, member]
    return ok(member)
  }),
  http.get('/api/products', () => ok(products)),
  http.post('/api/products', async ({ request }) => {
    const body = await request.json() as any
    const product = { id: 2, name: body.name, price: body.price, saleStatus: 'ON_SALE' }
    products = [...products, product]
    return ok(product)
  }),
  http.post('/api/products/:productId/inventory', ({ params }) =>
    ok({ id: 1, productId: Number(params.productId), quantity: 10 }),
  ),
  http.get('/api/products/:productId/inventory', ({ params }) =>
    ok({ id: 1, productId: Number(params.productId), quantity: 10 }),
  ),
  http.get('/api/orders', () => ok(orders)),
  http.post('/api/orders', async ({ request }) => {
    const body = await request.json() as any
    const order = {
      id: 1,
      memberId: body.memberId,
      status: 'CREATED',
      totalAmount: 12000,
      currency: 'KRW',
      lines: [{ productId: body.lines[0].productId, productName: 'Americano', unitPrice: 12000, quantity: 1, lineAmount: 12000 }],
    }
    orders = [order]
    return ok(order)
  }),
  http.post('/api/orders/:orderId/pay', ({ params }) => {
    orders = orders.map((order) => order.id === Number(params.orderId) ? { ...order, status: 'PAID', paymentId: 1 } : order)
    coupons = [
      { id: 1, memberId: 1, orderId: 1, paymentId: 1, status: 'ISSUED' },
      { id: 2, memberId: 1, orderId: 1, paymentId: 1, status: 'ISSUED' },
    ]
    histories = [
      { id: 1, couponId: 1, memberId: 1, orderId: 1, paymentId: 1, type: 'ISSUED' },
      { id: 2, couponId: 2, memberId: 1, orderId: 1, paymentId: 1, type: 'ISSUED' },
    ]
    return ok({ orderId: Number(params.orderId), paymentId: 1, orderStatus: 'PAID', paymentStatus: 'AUTHORIZED', paidAmount: 12000, issuedCouponCount: 2 })
  }),
  http.get('/api/members/:memberId/coupons', () => ok(coupons)),
  http.get('/api/members/:memberId/coupon-histories', () => ok(histories)),
)

describe('App', () => {
  beforeAll(() => server.listen())
  afterEach(() => {
    server.resetHandlers()
    window.history.pushState(null, '', '/')
    orders = []
    coupons = []
    histories = []
  })
  afterAll(() => server.close())

  it('navigates split pages and pays an order with stamp coupon result', async () => {
    renderPage()

    await screen.findByRole('heading', { name: '메인' })
    await userEvent.click(screen.getByRole('button', { name: /주문\/결제/ }))
    await screen.findByRole('heading', { name: '주문/결제' })
    await waitFor(() => expect(screen.getAllByText('#1 Kim').length).toBeGreaterThan(0))
    await userEvent.selectOptions(screen.getByLabelText('회원'), '1')
    await userEvent.selectOptions(screen.getByLabelText('상품'), '1')
    await userEvent.clear(screen.getByLabelText('수량'))
    await userEvent.type(screen.getByLabelText('수량'), '1')
    await userEvent.click(screen.getByRole('button', { name: /주문 생성/ }))

    await screen.findByText('주문 #1 생성')
    await userEvent.selectOptions(screen.getByLabelText('주문'), '1')
    await userEvent.clear(screen.getByLabelText('중복 요청 방지 키'))
    await userEvent.type(screen.getByLabelText('중복 요청 방지 키'), 'pay-ui-1')
    await userEvent.click(screen.getByRole('button', { name: /^결제$/ }))

    await screen.findByText('결제 완료: 쿠폰 2장 발급')
    await userEvent.click(screen.getByRole('button', { name: /^회원$/ }))
    await userEvent.selectOptions(screen.getByLabelText('쿠폰 조회 회원'), '1')
    await waitFor(() => expect(screen.getByText('2/10')).toBeInTheDocument())
  })
})

function renderPage() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
  render(
    <QueryClientProvider client={queryClient}>
      <App />
    </QueryClientProvider>,
  )
}

function ok(data: unknown) {
  return HttpResponse.json({ code: 'SUCCESS', message: '요청이 성공했습니다.', data })
}
