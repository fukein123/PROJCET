import type { FormInstance } from 'element-plus'

export async function validateElementForm(form?: FormInstance | null) {
  if (!form) {
    return false
  }

  try {
    await form.validate()
    return true
  } catch {
    return false
  }
}
