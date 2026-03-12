import { ElMessage } from 'element-plus'

export function validateImageFile(rawFile: { type: string; size: number }, maxSizeMb = 5) {
  if (!rawFile.type.startsWith('image/')) {
    ElMessage.warning('仅支持上传图片文件')
    return false
  }

  if (rawFile.size / 1024 / 1024 > maxSizeMb) {
    ElMessage.warning(`图片大小不能超过 ${maxSizeMb}MB`)
    return false
  }

  return true
}
