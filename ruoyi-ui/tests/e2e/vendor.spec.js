const { test, expect } = require('@playwright/test')

test.describe('LLM 厂商管理 E2E', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('http://localhost:1024')
    await page.fill('input[autocomplete="username"]', 'admin')
    await page.fill('input[autocomplete="current-password"]', 'admin123')
    await page.click('button[type="submit"]')
    await page.waitForURL(/index/)
    await page.getByText('LLM 厂商').click()
    await page.waitForURL(/vendor/)
  })

  test('CRUD 闭环', async ({ page }) => {
    await page.getByRole('button', { name: /新增/ }).click()
    await page.getByLabel('厂商名称').fill('E2E 测试厂商')
    await page.getByLabel('Base URL').fill('https://api.example.com')
    await page.getByLabel('API Key').fill('sk-e2e-test')
    await page.getByLabel('模型名').fill('e2e-model')
    await page.getByRole('button', { name: '保 存' }).click()
    await expect(page.getByText('新增成功')).toBeVisible()
    await expect(page.getByText('E2E 测试厂商')).toBeVisible()

    const row = page.locator('tr', { hasText: 'E2E 测试厂商' })
    await row.getByRole('button', { name: '删除' }).click()
    await page.getByRole('button', { name: '确 定' }).click()
    await expect(page.getByText('删除成功')).toBeVisible()
  })
})
