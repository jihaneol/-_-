import { useQuery } from '@tanstack/react-query'
import { Coffee, CreditCard, Package, RotateCcw, ShoppingCart, Users } from 'lucide-react'
import { commerceApi, commerceKeys } from '../../entities/commerce/api'
import { Metric, Row, StatusBadge } from '../../shared/ui'

export function MainPage() {
  const summary = useQuery({ queryKey: commerceKeys.summary, queryFn: commerceApi.getDashboardSummary })
  const orders = useQuery({ queryKey: commerceKeys.orders, queryFn: commerceApi.listOrders })

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <h1>메인</h1>
          <p>회원, 상품, 주문 결제, 쿠폰 적립 상태를 한 번에 확인합니다.</p>
        </div>
      </header>

      <section className="metric-grid">
        <Metric label="회원" value={summary.data?.memberCount ?? 0} helper="활성 회원" />
        <Metric label="상품" value={summary.data?.productCount ?? 0} helper="판매 관리 상품" />
        <Metric label="주문" value={summary.data?.orderCount ?? 0} helper="삭제 제외" />
        <Metric label="사용 가능 도장" value={summary.data?.issuedCouponCount ?? 0} helper="ISSUED 쿠폰" />
      </section>

      <section className="panel">
        <h2>운영 상태</h2>
        <div className="quick-stats">
          <span><Users size={16} /> 회원 {summary.data?.memberCount ?? 0}</span>
          <span><Package size={16} /> 상품 {summary.data?.productCount ?? 0}</span>
          <span><ShoppingCart size={16} /> 주문 {summary.data?.orderCount ?? 0}</span>
          <span><CreditCard size={16} /> 결제 {summary.data?.paidOrderCount ?? 0}</span>
          <span><RotateCcw size={16} /> 환불 {summary.data?.refundedOrderCount ?? 0}</span>
          <span><Coffee size={16} /> 도장 {summary.data?.issuedCouponCount ?? 0}</span>
        </div>
      </section>

      <section className="panel">
        <h2>최근 주문</h2>
        <table className="table">
          <thead>
            <tr>
              <th>주문</th>
              <th>회원</th>
              <th>금액</th>
              <th>상태</th>
            </tr>
          </thead>
          <tbody>
            {orders.isLoading ? <Row colSpan={4} text="불러오는 중" /> : null}
            {!orders.isLoading && !orders.data?.length ? <Row colSpan={4} text="주문 없음" /> : null}
            {orders.data?.slice(0, 8).map((order) => (
              <tr key={order.id}>
                <td>#{order.id}</td>
                <td>#{order.memberId}</td>
                <td>{order.totalAmount.toLocaleString()} {order.currency}</td>
                <td><StatusBadge value={order.status} /></td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  )
}
