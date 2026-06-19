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

export type CouponHistory = {
  id: number
  couponId?: number
  memberId: number
  orderId: number
  paymentId: number
  type: 'ISSUED' | 'VOIDED'
}
