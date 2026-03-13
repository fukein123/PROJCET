import { ElMessage, ElMessageBox } from 'element-plus'

interface ConfirmedActionPromptOptions {
  message: string
  inputPlaceholder?: string
  inputType?: 'text' | 'textarea'
  inputValue?: string
  inputValidator?: (value: string) => boolean | string
}

interface ConfirmedActionOptions {
  message: string
  title: string
  action: (promptValue?: string) => Promise<unknown> | unknown
  successMessage?: string
  afterSuccess?: () => Promise<void> | void
  type?: 'success' | 'warning' | 'info' | 'error'
  confirmButtonText?: string
  cancelButtonText?: string
  prompt?: ConfirmedActionPromptOptions
}

export async function runConfirmedAction({
  message,
  title,
  action,
  successMessage,
  afterSuccess,
  type = 'warning',
  confirmButtonText = '确定',
  cancelButtonText = '取消',
  prompt
}: ConfirmedActionOptions) {
  let promptValue: string | undefined

  if (prompt) {
    const result = await ElMessageBox.prompt(prompt.message, title, {
      type,
      confirmButtonText,
      cancelButtonText,
      inputPlaceholder: prompt.inputPlaceholder,
      inputType: prompt.inputType,
      inputValue: prompt.inputValue,
      inputValidator: prompt.inputValidator
    })
    promptValue = result.value
  } else {
    await ElMessageBox.confirm(message, title, {
      type,
      confirmButtonText,
      cancelButtonText
    })
  }

  await action(promptValue)

  if (successMessage) {
    ElMessage.success(successMessage)
  }

  await afterSuccess?.()
}
