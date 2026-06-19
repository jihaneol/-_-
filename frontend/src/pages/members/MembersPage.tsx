import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Coffee, Gift, UserPlus } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { adminCommerceApi, adminCommerceKeys } from '../../entities/commerce/api'
import type { ApiError } from '../../shared/api/client'
import { Field, Notice, Row, StatusBadge } from '../../shared/ui'

const memberSchema = z.object({
  name: z.string().min(1),
  email: z.string().email(),
})

type MemberForm = z.infer<typeof memberSchema>

export function MembersPage() {
  const queryClient = useQueryClient()
  const [selectedMemberId, setSelectedMemberId] = useState<number | null>(null)
  const [selectedProductId, setSelectedProductId] = useState<number | null>(null)
  const [notice, setNotice] = useState('')
  const [error, setError] = useState('')
  const members = useQuery({ queryKey: adminCommerceKeys.members, queryFn: adminCommerceApi.listMembers })
  const products = useQuery({ queryKey: adminCommerceKeys.products, queryFn: adminCommerceApi.listProducts })
  const consistency = useQuery({ queryKey: adminCommerceKeys.couponConsistency, queryFn: adminCommerceApi.getCouponConsistencyReport })
  const coupons = useQuery({
    queryKey: selectedMemberId ? adminCommerceKeys.coupons(selectedMemberId) : adminCommerceKeys.couponsIdle,
    queryFn: () => adminCommerceApi.listCoupons(selectedMemberId!),
    enabled: selectedMemberId !== null,
  })
  const histories = useQuery({
    queryKey: selectedMemberId ? adminCommerceKeys.histories(selectedMemberId) : adminCommerceKeys.historiesIdle,
    queryFn: () => adminCommerceApi.listCouponHistories(selectedMemberId!),
    enabled: selectedMemberId !== null,
  })
  const form = useForm<MemberForm>({ resolver: zodResolver(memberSchema), defaultValues: { name: '', email: '' } })
  const handleApiError = (apiError: ApiError) => {
    setError(apiError.message)
    setNotice('')
  }
  const createMember = useMutation({
    mutationFn: adminCommerceApi.createMember,
    onSuccess: async (member) => {
      setSelectedMemberId(member.id)
      setNotice(`회원 #${member.id} 생성`)
      setError('')
      form.reset({ name: '', email: '' })
      await queryClient.invalidateQueries({ queryKey: adminCommerceKeys.members })
      await queryClient.invalidateQueries({ queryKey: adminCommerceKeys.summary })
    },
    onError: handleApiError,
  })
  const approveCouponExchange = useMutation({
    mutationFn: (values: { memberId: number; productId: number }) =>
      adminCommerceApi.approveCouponExchange(values.memberId, { productId: values.productId }),
    onSuccess: async (result) => {
      setNotice(`${result.productName} 교환 승인: 쿠폰 ${result.exchangedCouponCount}장 사용`)
      setError('')
      await queryClient.invalidateQueries({ queryKey: adminCommerceKeys.summary })
      await queryClient.invalidateQueries({ queryKey: adminCommerceKeys.coupons(result.memberId) })
      await queryClient.invalidateQueries({ queryKey: adminCommerceKeys.histories(result.memberId) })
      await queryClient.invalidateQueries({ queryKey: adminCommerceKeys.couponConsistency })
    },
    onError: handleApiError,
  })
  const issuedCouponCount = coupons.data?.filter((coupon) => coupon.status === 'ISSUED').length ?? 0
  const exchangedCouponCount = coupons.data?.filter((coupon) => coupon.status === 'EXCHANGED').length ?? 0
  const selectedMember = members.data?.find((member) => member.id === selectedMemberId)
  const exchangeProducts = products.data?.filter((product) => product.price === 5_000 && product.saleStatus === 'ON_SALE') ?? []
  const canApproveExchange = selectedMemberId !== null && selectedProductId !== null && issuedCouponCount >= 10 && !approveCouponExchange.isPending

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <h1>쿠폰 교환 관리자</h1>
          <p>회원을 선택해 발급 쿠폰을 확인하고, 운영자가 교환 처리한 이력을 추적합니다.</p>
        </div>
      </header>
      <Notice text={notice} error={error} />
      <div className="content two-column">
        <section className="panel">
          <h2>회원 생성</h2>
          <form className="grid" onSubmit={form.handleSubmit((values) => createMember.mutate(values))}>
            <Field label="이름" error={form.formState.errors.name?.message}>
              <input {...form.register('name')} placeholder="Kim" />
            </Field>
            <Field label="이메일" error={form.formState.errors.email?.message}>
              <input {...form.register('email')} placeholder="kim@example.com" />
            </Field>
            <button className="button" disabled={createMember.isPending}>
              <UserPlus size={16} /> 생성
            </button>
          </form>
        </section>

        <section className="panel">
          <h2>회원 목록</h2>
          <table className="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>이름</th>
                <th>이메일</th>
                <th>선택</th>
              </tr>
            </thead>
            <tbody>
              {members.isLoading ? <Row colSpan={4} text="불러오는 중" /> : null}
              {!members.isLoading && !members.data?.length ? <Row colSpan={4} text="회원 없음" /> : null}
              {members.data?.map((member) => (
                <tr key={member.id}>
                  <td>#{member.id}</td>
                  <td>{member.name}</td>
                  <td>{member.email}</td>
                  <td>
                    <button className="button secondary" onClick={() => setSelectedMemberId(member.id)}>조회</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      </div>

      <div className="content two-column">
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
            <span className="status warn"><Gift size={14} /> 교환 {exchangedCouponCount}</span>
          </div>
          <div className="exchange-summary">
            <strong>{selectedMember ? `${selectedMember.name} 회원` : '회원 선택 필요'}</strong>
            <span>쿠폰 10장을 5,000원 상품 1개로 교환 승인합니다. 승인 시 쿠폰 10장이 교환 처리되고 상품 재고가 1개 차감됩니다.</span>
            <div className="exchange-controls">
              <select
                aria-label="교환 상품"
                value={selectedProductId ?? ''}
                onChange={(event) => setSelectedProductId(Number(event.target.value) || null)}
              >
                <option value="">교환 상품 선택</option>
                {exchangeProducts.map((product) => <option key={product.id} value={product.id}>#{product.id} {product.name}</option>)}
              </select>
              <button
                className="button"
                disabled={!canApproveExchange}
                onClick={() => {
                  if (selectedMemberId && selectedProductId) {
                    approveCouponExchange.mutate({ memberId: selectedMemberId, productId: selectedProductId })
                  }
                }}
              >
                <Gift size={16} /> 교환 승인 처리
              </button>
            </div>
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
              {selectedMemberId && !coupons.isLoading && !coupons.data?.length ? <Row colSpan={4} text="쿠폰 없음" /> : null}
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
      </div>

      <section className="panel">
        <div className="panel-heading">
          <h2>쿠폰 정합성 리포트</h2>
          <span className={`status ${consistency.data?.consistent === false ? 'bad' : 'ok'}`}>
            {consistency.data?.consistent === false ? '점검필요' : '정상'}
          </span>
        </div>
        <div className="report-summary">
          <span>쿠폰 {consistency.data?.totalCouponCount ?? 0}</span>
          <span>발급 이력 {consistency.data?.totalIssueHistoryCount ?? 0}</span>
          <span>교환 이력 {consistency.data?.totalExchangeHistoryCount ?? 0}</span>
          <span>무효 이력 {consistency.data?.totalVoidHistoryCount ?? 0}</span>
        </div>
        <table className="table">
          <thead>
            <tr>
              <th>회원</th>
              <th>사용가능</th>
              <th>교환</th>
              <th>무효</th>
              <th>교환 가능 세트</th>
              <th>다음 교환까지</th>
              <th>정합성</th>
            </tr>
          </thead>
          <tbody>
            {consistency.isLoading ? <Row colSpan={7} text="불러오는 중" /> : null}
            {!consistency.isLoading && !consistency.data?.memberRows.length ? <Row colSpan={7} text="리포트 없음" /> : null}
            {consistency.data?.memberRows.map((row) => (
              <tr key={row.memberId}>
                <td>#{row.memberId}</td>
                <td>{row.issuedCouponCount}</td>
                <td>{row.exchangedCouponCount}</td>
                <td>{row.voidedCouponCount}</td>
                <td>{row.exchangeableSetCount}</td>
                <td>{row.remainingToNextExchange}</td>
                <td><span className={`status ${row.consistent ? 'ok' : 'bad'}`}>{row.consistent ? '정상' : '점검'}</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  )
}
