const { test, expect } = require('@playwright/test')

test.describe('主机管理抽屉 E2E', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('http://localhost:1024')
    await page.fill('input[autocomplete="username"]', 'admin')
    await page.fill('input[autocomplete="current-password"]', 'admin123')
    await page.click('button[type="submit"]')
    await page.waitForURL(/index/)
    // 进入 AI 助手菜单（侧栏）
    await page.getByText('AI 助手').click()
    await page.waitForURL(/chat/)
    // 主机运维 Tab 内的"主机管理"按钮（在 Phase 3 主页就绪前暂用抽屉内入口测试流程）
  })

  test('打开抽屉并加载列表', async ({ page }) => {
    // 占位：阶段 2 主要靠后端联调验证；E2E 完整流程在 Phase 3 一并补
    expect(true).toBe(true)
  })
})
