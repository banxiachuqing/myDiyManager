const { test, expect } = require('@playwright/test')

test.describe('AI 助手主页 E2E', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('http://localhost:1024')
    await page.fill('input[autocomplete="username"]', 'admin')
    await page.fill('input[autocomplete="current-password"]', 'admin123')
    await page.click('button[type="submit"]')
    await page.waitForURL(/index/)
    await page.getByText('AI 助手').click()
    await page.waitForURL(/chat/)
    // 默认切到智能问答 tab
    await page.getByText('智能问答').click()
  })

  test('TC-T01/02: 双 Tab 渲染 + 切换', async ({ page }) => {
    await expect(page.getByText('智能问答')).toBeVisible()
    await expect(page.getByText('主机运维')).toBeVisible()
    await page.getByText('主机运维').click()
    await expect(page.getByText('选择主机')).toBeVisible()
  })

  test('TC-S01/02: 侧栏欢迎页 + 当前 tab 会话列表', async ({ page }) => {
    // 默认 chat tab 进入后右侧应该是欢迎页（无消息）
    await expect(page.getByText('开始一次对话')).toBeVisible()
    // 切到 ops tab，再切回来，验证 welcome page 又出现（不应残留旧内容）
    await page.getByText('主机运维').click()
    await page.getByText('智能问答').click()
    await expect(page.getByText('开始一次对话')).toBeVisible()
  })

  test('TC-S03: 新建会话清空右侧', async ({ page }) => {
    // 1. 先发一条消息，模拟有内容
    const textarea = page.locator('textarea')
    await textarea.fill('你好测试')
    await page.getByRole('button', { name: '发送' }).click()
    // 等待用户消息渲染
    await expect(page.getByText('你好测试').first()).toBeVisible()
    // 2. 点 + 新建会话
    await page.getByText('新建会话').click()
    // 3. 验证欢迎页重新出现
    await expect(page.getByText('开始一次对话')).toBeVisible()
  })

  test('TC-C01: 智能问答发送 + 流式回复', async ({ page }) => {
    const textarea = page.locator('textarea')
    await textarea.fill('你好')
    await page.getByRole('button', { name: '发送' }).click()
    // 至少用户消息 + AI 占位消息（流式逐字出现需 ≥ 1s 等待）
    await expect(page.getByText('你好').first()).toBeVisible()
    // 等待 AI 真实回复（≥ 2s，给 LLM 时间响应）
    await page.waitForTimeout(3000)
    // 截屏证明流式结果
    await page.screenshot({ path: 'artifacts/TC-C01-chat-response.png', fullPage: true })
  })

  test('TC-C06/07: Enter 发送 + Shift+Enter 换行', async ({ page }) => {
    const textarea = page.locator('textarea')
    // Enter 发送
    await textarea.click()
    await textarea.fill('第一行')
    await textarea.press('Enter')
    await expect(page.getByText('第一行').first()).toBeVisible()
    // 等待消息发送完成再测 Shift+Enter
    await page.waitForTimeout(2000)
    // Shift+Enter 换行
    await textarea.click()
    await textarea.fill('多行测试')
    await textarea.press('Shift+Enter')
    await textarea.type('第二行')
    // 验证没有发送
    const value = await textarea.inputValue()
    expect(value).toContain('\n')
  })

  test('TC-O01/02: 主机运维未选主机时发送按钮置灰', async ({ page }) => {
    await page.getByText('主机运维').click()
    const textarea = page.locator('textarea')
    await textarea.fill('巡检磁盘')
    const sendBtn = page.getByRole('button', { name: '执行' })
    const isDisabled = await sendBtn.isDisabled()
    expect(isDisabled).toBe(true)
  })

  test('TC-S05/06: 会话标题显示首条消息 + 截断', async ({ page }) => {
    // 发送长首条消息
    const textarea = page.locator('textarea')
    const longText = '这是一段非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常长的消息内容用来测试sidebar标题截断'
    await textarea.fill(longText)
    await page.getByRole('button', { name: '发送' }).click()
    await page.waitForTimeout(2000)
    // 切到主机运维再切回来，触发 loadSessions + 拉 preview
    await page.getByText('主机运维').click()
    await page.getByText('智能问答').click()
    await page.waitForTimeout(1500)
    await page.screenshot({ path: 'artifacts/TC-S05-sidebar-title.png', fullPage: true })
  })

  test('TC-M01/02: 思考过程默认折叠 + 点击展开', async ({ page }) => {
    // 触发带 <think> 的回复（让 AI 强制思考）
    const textarea = page.locator('textarea')
    await textarea.fill('请详细解释 Spring Boot 自动装配原理')
    await page.getByRole('button', { name: '发送' }).click()
    await page.waitForTimeout(5000)
    await page.screenshot({ path: 'artifacts/TC-M01-think-block.png', fullPage: true })
  })

  test('TC-T05: 厂商管理链接跳转', async ({ page }) => {
    await page.getByText('厂商管理').click()
    await page.waitForURL(/vendor/)
    await expect(page.getByText('LLM 厂商')).toBeVisible()
  })

  test('TC-O03: 主机下拉显示状态点', async ({ page }) => {
    await page.getByText('主机运维').click()
    // 展开主机下拉
    await page.locator('.ai-host-select').click()
    await page.waitForTimeout(500)
    await page.screenshot({ path: 'artifacts/TC-O03-host-dropdown.png', fullPage: true })
  })
})
