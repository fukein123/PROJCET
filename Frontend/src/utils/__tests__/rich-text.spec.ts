import { describe, expect, it } from 'vitest'
import { sanitizeRichText, richTextToPlainText, hasMeaningfulRichText } from '../rich-text'

describe('rich text helpers', () => {
  it('preserves expanded safe formatting tags', () => {
    const html = '<h4>小标题</h4><p><strike>旧内容</strike><br />新内容</p><hr><script>alert(1)</script>'

    expect(sanitizeRichText(html)).toBe('<h4>小标题</h4><p><s>旧内容</s><br>新内容</p><hr>')
  })

  it('extracts plain text and meaningful content correctly', () => {
    const html = '<p>社区 <strong>志愿</strong> 服务</p>'

    expect(richTextToPlainText(html)).toBe('社区 志愿 服务')
    expect(hasMeaningfulRichText(html)).toBe(true)
    expect(hasMeaningfulRichText('<p><br></p>')).toBe(false)
  })
})
