import request from '@/utils/request'
import type { PageResult } from './user'

export interface DynamicModel {
  id: number
  title: string
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
  content: string
  categoryId: number
  userId: number
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
  status: number
}

export interface BannerModel {
  id: number
  title: string
  imageUrl: string
  activityId: number
  sort: number
  status: number
}

export interface ForumCategoryModel {
  id: number
  name: string
  sort: number
  status: number
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

export function pageNoticesApi(params: { current: number; size: number; onlyPublished?: boolean }) {
  return request.get<never, PageResult<NoticeModel>>('/api/content/notices/page', { params })
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

export function listForumCategoriesApi() {
  return request.get<never, ForumCategoryModel[]>('/api/content/forum/categories')
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
  return request.post('/api/content/forum/posts/my', payload)
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
