import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { PackagePlus, Plus } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { adminCommerceApi, adminCommerceKeys } from '../../entities/commerce/api'
import type { Product } from '../../entities/commerce/types'
import type { ApiError } from '../../shared/api/client'
import { Field, Notice, Row, StatusBadge } from '../../shared/ui'
import { useState } from 'react'

const productSchema = z.object({
  name: z.string().min(1),
  price: z.coerce.number().int().positive(),
  stock: z.coerce.number().int().nonnegative(),
})

const stockSchema = z.object({
  productId: z.coerce.number().int().positive(),
  quantity: z.coerce.number().int().positive(),
})

type ProductForm = z.infer<typeof productSchema>
type StockForm = z.infer<typeof stockSchema>

export function ProductsPage() {
  const queryClient = useQueryClient()
  const [notice, setNotice] = useState('')
  const [error, setError] = useState('')
  const products = useQuery({ queryKey: adminCommerceKeys.products, queryFn: adminCommerceApi.listProducts })
  const productForm = useForm<ProductForm>({
    resolver: zodResolver(productSchema) as never,
    defaultValues: { name: '', price: 5000, stock: 10 },
  })
  const stockForm = useForm<StockForm>({
    resolver: zodResolver(stockSchema) as never,
    defaultValues: { quantity: 1 },
  })
  const onError = (apiError: ApiError) => {
    setError(apiError.message)
    setNotice('')
  }
  const invalidateProducts = async () => {
    await queryClient.invalidateQueries({ queryKey: adminCommerceKeys.products })
    await queryClient.invalidateQueries({ queryKey: adminCommerceKeys.summary })
  }
  const createProduct = useMutation({
    mutationFn: async (form: ProductForm) => {
      const product = await adminCommerceApi.createProduct({ name: form.name, price: form.price })
      await adminCommerceApi.createInventory(product.id, { quantity: form.stock })
      return product
    },
    onSuccess: async (product) => {
      setNotice(`상품 #${product.id} 및 재고 생성`)
      setError('')
      productForm.reset({ name: '', price: 5000, stock: 10 })
      await invalidateProducts()
    },
    onError,
  })
  const increaseStock = useMutation({
    mutationFn: (form: StockForm) => adminCommerceApi.increaseInventory(form.productId, { quantity: form.quantity }),
    onSuccess: async (inventory) => {
      setNotice(`상품 #${inventory.productId} 재고 ${inventory.quantity}개`)
      setError('')
      stockForm.reset({ productId: inventory.productId, quantity: 1 })
      await queryClient.invalidateQueries({ queryKey: adminCommerceKeys.inventory(inventory.productId) })
    },
    onError,
  })

  return (
    <div className="page">
      <header className="page-header">
        <div>
          <h1>상품</h1>
          <p>상품을 만들고 재고를 확인하거나 보충합니다.</p>
        </div>
      </header>
      <Notice text={notice} error={error} />
      <div className="content two-column">
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
          <h2>재고 보충</h2>
          <form className="grid" onSubmit={stockForm.handleSubmit((form) => increaseStock.mutate(form))}>
            <Field label="상품" error={stockForm.formState.errors.productId?.message}>
              <select {...stockForm.register('productId')}>
                <option value="">선택</option>
                {products.data?.map((product) => <option key={product.id} value={product.id}>#{product.id} {product.name}</option>)}
              </select>
            </Field>
            <Field label="수량" error={stockForm.formState.errors.quantity?.message}>
              <input type="number" {...stockForm.register('quantity')} />
            </Field>
            <button className="button secondary" disabled={increaseStock.isPending}>
              <Plus size={16} /> 보충
            </button>
          </form>
        </section>
      </div>

      <section className="panel">
        <h2>상품 목록</h2>
        <table className="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>상품명</th>
              <th>가격</th>
              <th>재고</th>
              <th>상태</th>
            </tr>
          </thead>
          <tbody>
            {products.isLoading ? <Row colSpan={5} text="불러오는 중" /> : null}
            {!products.isLoading && !products.data?.length ? <Row colSpan={5} text="상품 없음" /> : null}
            {products.data?.map((product) => <ProductRow key={product.id} product={product} />)}
          </tbody>
        </table>
      </section>
    </div>
  )
}

function ProductRow(props: { product: Product }) {
  const inventory = useQuery({
    queryKey: adminCommerceKeys.inventory(props.product.id),
    queryFn: () => adminCommerceApi.getInventory(props.product.id),
    retry: false,
  })
  return (
    <tr>
      <td>#{props.product.id}</td>
      <td>{props.product.name}</td>
      <td>{props.product.price.toLocaleString()} KRW</td>
      <td>{inventory.data?.quantity ?? '-'}</td>
      <td><StatusBadge value={props.product.saleStatus} /></td>
    </tr>
  )
}
