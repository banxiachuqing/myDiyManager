const { defineConfig } = require('@playwright/test')

module.exports = defineConfig({
  testDir: './e2e',
  timeout: 60_000,
  fullyParallel: false,
  workers: 1,
  reporter: [
    ['line'],
    ['html', { outputFolder: '../playwright-report', open: 'never' }]
  ],
  use: {
    baseURL: 'http://localhost:1024',
    headless: false,                  // 用户要求看到浏览器操作过程
    slowMo: 200,                      // 全局慢速 200ms 便于观察
    actionTimeout: 10_000,
    navigationTimeout: 30_000,
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    trace: 'retain-on-failure',
    viewport: { width: 1200, height: 760 }    // 控制浏览器窗口大小
  }
})
