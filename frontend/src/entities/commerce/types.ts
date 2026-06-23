export type Member = {
  id: number
  name: string
  email: string
}

export type Product = {
  id: number
  name: string
  price: number
  saleStatus: 'ON_SALE' | 'STOPPED'
  couponAccrualCount: number
  exchangeEligible: boolean
}

export type Inventory = {
  id: number
  productId: number
  quantity: number
}

export type OrderLine = {
  productId: number
  productName: string
  unitPrice: number
  quantity: number
  lineAmount: number
}

export type Order = {
  id: number
  memberId: number
  status: 'CREATED' | 'CANCELLED' | 'PAID' | 'REFUNDED'
  totalAmount: number
  currency: string
  paymentId?: number
  lines: OrderLine[]
}

export type PayOrderResult = {
  orderId: number
  paymentId: number
  orderStatus: Order['status']
  paymentStatus: string
  paidAmount: number
  issuedCouponCount: number
}

export type RefundOrderResult = {
  orderId: number
  paymentId: number
  orderStatus: Order['status']
  paymentStatus: string
  voidedCouponCount: number
}

export type Coupon = {
  id: number
  memberId: number
  orderId: number
  paymentId: number
  status: 'ISSUED' | 'VOIDED' | 'EXCHANGED'
}

export type PageResponse<T> = {
  items: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  hasNext: boolean
}

export type MemberPageResponse = PageResponse<Member>

export type ProductPageResponse = PageResponse<Product>

export type OrderPageResponse = PageResponse<Order>

export type CouponHistory = {
  id: number
  couponId?: number
  memberId: number
  orderId: number
  paymentId: number
  type: 'ISSUED' | 'VOIDED' | 'EXCHANGED'
}

export type CouponPageResponse = PageResponse<Coupon>

export type CouponHistoryPageResponse = PageResponse<CouponHistory>

export type CouponWallet = {
  memberId: number
  issuedCouponCount: number
  exchangedCouponCount: number
  voidedCouponCount: number
  totalCouponCount: number
  exchangeableSetCount: number
  remainingToNextExchange: number
  recentHistories: CouponHistory[]
}

export type CouponExchangeResult = {
  coupon: Coupon
  history: CouponHistory
}

export type ApproveCouponExchangeResult = {
  memberId: number
  productId: number
  productName: string
  exchangedCouponCount: number
  remainingIssuedCouponCount: number
  exchangedCouponIds: number[]
}

export type CouponConsistencyReport = {
  consistent: boolean
  totalCouponCount: number
  totalIssueHistoryCount: number
  totalVoidHistoryCount: number
  totalExchangeHistoryCount: number
  memberRows: MemberCouponConsistencyRow[]
  orderRows: OrderCouponConsistencyRow[]
}

export type MemberCouponConsistencyRow = {
  memberId: number
  issuedCouponCount: number
  voidedCouponCount: number
  exchangedCouponCount: number
  issueHistoryCount: number
  voidHistoryCount: number
  exchangeHistoryCount: number
  exchangeableSetCount: number
  remainingToNextExchange: number
  consistent: boolean
}

export type OrderCouponConsistencyRow = {
  orderId: number
  memberId: number
  issuedCouponCount: number
  voidedCouponCount: number
  exchangedCouponCount: number
  issueHistoryCount: number
  voidHistoryCount: number
  exchangeHistoryCount: number
  consistent: boolean
}

export type DashboardSummary = {
  memberCount: number
  productCount: number
  orderCount: number
  paidOrderCount: number
  refundedOrderCount: number
  issuedCouponCount: number
}
