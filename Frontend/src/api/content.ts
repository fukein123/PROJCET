import request from '@/utils/request'
import type { PageResult } from './user'

export interface DynamicModel {
  id: number
  title: string
  source: string
  content: string
  imageUrl?: string
  type: string
  views: number
  status: number
  publishTime: string
}

export interface NoticeModel {
  id: number
  title: string
  content: string
  status: number
  publishTime: string
}

export interface PostModel {
  id: number
  title: string
  coverImage?: string
  summary?: string
  content: string
  categoryId: number
  categoryName?: string
  userId: number
  userName?: string
  realName?: string
  status: string
  views: number
  auditReason?: string
  createTime?: string
}

export interface CommentModel {
  id: number
  targetType: string
  targetId: number
  userId: number
  content: string
  testDataTag?: string
  status: number
  createTime?: string
}

export interface BannerModel {
  id: number
  title: string
  imageUrl: string
  activityId?: number | null
  sort: number
  status: number
  createTime?: string
}

export interface ForumCategoryModel {
  id: number
  name: string
  sort: number
  status: number
  createTime?: string
}

export interface MallProductModel {
  id: number
  name: string
  imageUrl: string
  summary: string
  pointsCost: number
  stock: number
  status: number
  createTime?: string
}

export interface ExchangeOrderModel {
  id: number
  orderNo: string
  userId: number
  productId?: number
  userName?: string
  realName?: string
  productName: string
  productImage?: string
  productSummary?: string
  quantity: number
  pointsPerItem: number
  totalPoints: number
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  requestKey?: string
  status: string
  statusReason?: string
  createTime?: string
  shippedTime?: string
  receivedTime?: string
  cancelledTime?: string
}

export interface HomePayload {
  banners: BannerModel[]
  hotDynamics: DynamicModel[]
  notices: NoticeModel[]
  hotPosts: PostModel[]
}

export interface FavoriteModel {
  id: number
  activityId: number
  activityTitle?: string
  activityAddress?: string
  activityStartTime?: string
  activityEndTime?: string
  note?: string
  tag?: string
  priority?: number
  createTime: string
}

export function homeApi() {
  return request.get<never, HomePayload>('/api/content/home')
}

export function pageDynamicsApi(params: {
  current: number
  size: number
  type?: string
  keyword?: string
  onlyPublished?: boolean
}) {
  return request.get<never, PageResult<DynamicModel>>('/api/content/dynamics/page', { params })
}

export function dynamicDetailApi(id: number) {
  return request.get<never, DynamicModel>(`/api/content/dynamics/${id}`)
}

export function saveDynamicApi(payload: Partial<DynamicModel>) {
  return request.post('/api/content/dynamics', payload)
}

export function updateDynamicApi(id: number, payload: Partial<DynamicModel>) {
  return request.put(`/api/content/dynamics/${id}`, payload)
}

export function deleteDynamicApi(id: number) {
  return request.delete(`/api/content/dynamics/${id}`)
}

export function batchDeleteDynamicsApi(ids: number[]) {
  return request.post('/api/content/dynamics/batch-delete', { ids })
}

export function batchArchiveDynamicsApi(ids: number[]) {
  return request.post('/api/content/dynamics/batch-archive', { ids })
}

export function batchRestoreDynamicsApi(ids: number[]) {
  return request.post('/api/content/dynamics/batch-restore', { ids })
}

export function pageNoticesApi(params: { current: number; size: number; onlyPublished?: boolean }) {
  return request.get<never, PageResult<NoticeModel>>('/api/content/notices/page', { params })
}

export function noticeDetailApi(id: number) {
  return request.get<never, NoticeModel>(`/api/content/notices/${id}`)
}

export function saveNoticeApi(payload: Partial<NoticeModel>) {
  return request.post('/api/content/notices', payload)
}

export function updateNoticeApi(id: number, payload: Partial<NoticeModel>) {
  return request.put(`/api/content/notices/${id}`, payload)
}

export function deleteNoticeApi(id: number) {
  return request.delete(`/api/content/notices/${id}`)
}

export function batchDeleteNoticesApi(ids: number[]) {
  return request.post('/api/content/notices/batch-delete', { ids })
}

export function batchArchiveNoticesApi(ids: number[]) {
  return request.post('/api/content/notices/batch-archive', { ids })
}

export function batchRestoreNoticesApi(ids: number[]) {
  return request.post('/api/content/notices/batch-restore', { ids })
}

export function listManageBannersApi() {
  return request.get<never, BannerModel[]>('/api/content/admin/banners')
}

export function saveBannerApi(payload: Partial<BannerModel>) {
  return request.post('/api/content/banners', payload)
}

export function updateBannerApi(id: number, payload: Partial<BannerModel>) {
  return request.put(`/api/content/banners/${id}`, payload)
}

export function deleteBannerApi(id: number) {
  return request.delete(`/api/content/banners/${id}`)
}

export function batchDeleteBannersApi(ids: number[]) {
  return request.post('/api/content/banners/batch-delete', { ids })
}

export function pageForumPostsApi(params: {
  current: number
  size: number
  keyword?: string
  status?: string
  onlyMine?: boolean
  onlyApproved?: boolean
}) {
  return request.get<never, PageResult<PostModel>>('/api/content/forum/posts/page', { params })
}

export function forumPostDetailApi(id: number) {
  return request.get<never, PostModel>(`/api/content/forum/posts/${id}`)
}

export function listForumCategoriesApi() {
  return request.get<never, ForumCategoryModel[]>('/api/content/forum/categories')
}

export function listManageForumCategoriesApi() {
  return request.get<never, ForumCategoryModel[]>('/api/content/admin/forum/categories')
}

export function saveForumCategoryApi(payload: Partial<ForumCategoryModel>) {
  return request.post('/api/content/forum/categories', payload)
}

export function updateForumCategoryApi(id: number, payload: Partial<ForumCategoryModel>) {
  return request.put(`/api/content/forum/categories/${id}`, payload)
}

export function deleteForumCategoryApi(id: number) {
  return request.delete(`/api/content/forum/categories/${id}`)
}

export function batchDeleteForumCategoriesApi(ids: number[]) {
  return request.post('/api/content/forum/categories/batch-delete', { ids })
}

export function saveMyPostApi(payload: Partial<PostModel>) {
  return request.post<never, PostModel>('/api/content/forum/posts/my', payload)
}

export function undoMyPostApi(id: number) {
  return request.delete(`/api/content/forum/posts/my/${id}/undo`)
}

export function updateForumPostApi(id: number, payload: Partial<PostModel>) {
  return request.put(`/api/content/forum/posts/${id}`, payload)
}

export function auditPostApi(id: number, params: { status: string; reason?: string }) {
  return request.put(`/api/content/forum/posts/${id}/audit`, null, { params })
}

export function deleteForumPostApi(id: number) {
  return request.delete(`/api/content/forum/posts/${id}`)
}

export function batchDeleteForumPostsApi(ids: number[]) {
  return request.post('/api/content/forum/posts/batch-delete', { ids })
}

export function pageCommentsApi(params: {
  current: number
  size: number
  onlyMine?: boolean
  targetType?: string
  targetId?: number
  includeTestData?: boolean
}) {
  return request.get<never, PageResult<CommentModel>>('/api/content/comments/page', { params })
}

export function addCommentApi(payload: Partial<CommentModel>) {
  return request.post('/api/content/comments', payload)
}

export function deleteCommentApi(id: number) {
  return request.delete(`/api/content/comments/${id}`)
}

export function batchDeleteCommentsApi(ids: number[]) {
  return request.post('/api/content/comments/batch-delete', { ids })
}

export function myFavoritesApi() {
  return request.get<never, FavoriteModel[]>('/api/content/favorites')
}

export function pageFavoritesApi(params: { current: number; size: number }) {
  return request.get<never, PageResult<FavoriteModel>>('/api/content/favorites/page', { params })
}

export function createFavoriteApi(payload: {
  activityId: number
  note?: string
  tag?: string
  priority?: number
}) {
  return request.post('/api/content/favorites', payload)
}

export function updateFavoriteApi(
  id: number,
  payload: {
    note?: string
    tag?: string
    priority?: number
  }
) {
  return request.put(`/api/content/favorites/${id}`, payload)
}

export function removeFavoriteByIdApi(id: number) {
  return request.delete(`/api/content/favorites/id/${id}`)
}

export function batchDeleteFavoritesApi(ids: number[]) {
  return request.post('/api/content/favorites/batch-delete', { ids })
}

export function addFavoriteApi(activityId: number) {
  return request.post(`/api/content/favorites/${activityId}`)
}

export function removeFavoriteApi(activityId: number) {
  return request.delete(`/api/content/favorites/${activityId}`)
}

export function pageMallProductsApi(params: {
  current: number
  size: number
  keyword?: string
  status?: number
  onlyEnabled?: boolean
}) {
  return request.get<never, PageResult<MallProductModel>>('/api/content/mall-products/page', { params })
}

export function adminPageMallProductsApi(params: {
  current: number
  size: number
  keyword?: string
  status?: number
}) {
  return request.get<never, PageResult<MallProductModel>>('/api/content/admin/mall-products/page', { params })
}

export function saveMallProductApi(payload: Partial<MallProductModel>) {
  return request.post('/api/content/mall-products', payload)
}

export function updateMallProductApi(id: number, payload: Partial<MallProductModel>) {
  return request.put(`/api/content/mall-products/${id}`, payload)
}

export function disableMallProductApi(id: number) {
  return request.delete(`/api/content/mall-products/${id}`)
}

export function enableMallProductApi(id: number) {
  return request.put(`/api/content/mall-products/${id}/enable`)
}

export function batchDisableMallProductsApi(ids: number[]) {
  return request.post('/api/content/mall-products/batch-delete', { ids })
}

export function batchEnableMallProductsApi(ids: number[]) {
  return request.post('/api/content/mall-products/batch-enable', { ids })
}

export function pageMyExchangeOrdersApi(params: { current: number; size: number; status?: string }) {
  return request.get<never, PageResult<ExchangeOrderModel>>('/api/content/orders/page', { params })
}

export function createExchangeOrderApi(payload: {
  productId: number
  quantity: number
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  requestKey: string
}) {
  return request.post<never, ExchangeOrderModel>('/api/content/orders', payload)
}

export function adminPageExchangeOrdersApi(params: {
  current: number
  size: number
  orderNo?: string
  productKeyword?: string
  userKeyword?: string
  status?: string
}) {
  return request.get<never, PageResult<ExchangeOrderModel>>('/api/content/admin/orders/page', { params })
}

export function adminExchangeOrderDetailApi(id: number) {
  return request.get<never, ExchangeOrderModel>(`/api/content/admin/orders/${id}`)
}

export function updateExchangeOrderStatusApi(
  id: number,
  payload: {
    status: string
    reason?: string
  }
) {
  return request.put(`/api/content/orders/${id}/status`, payload)
}
