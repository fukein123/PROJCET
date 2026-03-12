import request from '@/utils/request'

export interface UploadResult {
  url: string
  name: string
  size: number
  originalName: string
}

export function uploadImageApi(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<never, UploadResult>('/api/common/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
