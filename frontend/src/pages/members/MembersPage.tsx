import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Coffee, UserPlus } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { commerceApi, commerceKeys } from '../../entities/commerce/api'
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
  const [notice, setNotice] = useState('')
  const [error, setError] = useState('')
  const members = useQuery({ queryKey: commerceKeys.members, queryFn: commerceApi.listMembers })
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
  const form = useForm<MemberForm>({ resolver: zodResolver(memberSchema), defaultValues: { name: '', email: '' } })
  const createMember = useMutation({
    mutationFn: commerceApi.createMember,
    onSuccess: async (member) => {
      setSelectedMemberId(member.id)
      setNotice(`회원 #${member.id} 생성`)
      setError('')
      form.reset({ name: '', email: '' })
      await queryClient.invalidateQueries({ queryKey: commerceKeys.members })
      await queryClient.invalidateQueries({ queryKey: commerceKeys.summary })
    },
    onError: (apiError: ApiError) => {
      setError(apiError.message)
      setNotice('')
    },
  })
  const issuedCouponCount = coupons.data?.filter((coupon) => coupon.status === 'ISSUED').length ?? 0

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <h1>회원</h1>
          <p>회원을 생성하고 회원별 도장 쿠폰과 히스토리를 조회합니다.</p>
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
      </div>
    </div>
  )
}
