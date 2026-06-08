<template>
  <div class="message" :class="roleClass">
    <div class="avatar" :class="roleClass">
      <span v-if="role === '0'">U</span>
      <span v-else-if="role === '1'" class="avatar-pulse"><i /><i /><i /></span>
      <span v-else>·</span>
    </div>
    <div class="bubble-wrap">
      <div class="meta">
        <span class="meta-name">{{ roleLabel }}</span>
        <span class="meta-time">{{ timeText }}</span>
        <span v-if="role === '1' && thinkMs" class="meta-think-time">已思考 {{ thinkMs }}s</span>
        <span v-if="status === '0' && role === '1'" class="meta-typing">
          <i /><i /><i />
          <span>正在生成</span>
        </span>
      </div>

      <!-- 思考过程：折叠区，主流 AI 助手风格 -->
      <details v-if="thinkContent" class="think-block">
        <summary>
          <span class="think-icon">💭</span>
          <span class="think-label">已思考</span>
          <span v-if="thinkMs" class="think-duration">{{ thinkMs }}s</span>
          <svg class="think-chevron" viewBox="0 0 16 16" width="10" height="10">
            <path d="M3 6 L8 11 L13 6" stroke="currentColor" stroke-width="1.5" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </summary>
        <div class="think-body" v-html="renderedThink" />
      </details>

      <!-- 主体内容（去除 think 块后） -->
      <div class="bubble" v-html="renderedMain" />

      <!-- 流式输出中：底部闪烁光标 -->
      <span v-if="status === '0' && role === '1' && content && !content.endsWith('<think>') && !isThinkingTail" class="stream-cursor" />
    </div>
  </div>
</template>

<script>
import MarkdownIt from 'markdown-it'
const md = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true,
  highlight: function (str, lang) {
    return '<pre class="code-block"><div class="code-header"><span class="code-lang">' + (lang || 'text') + '</span><button class="code-copy" type="button" onclick="window.__aiCopyCode(this)">复制</button></div><code class="lang-' + (lang || 'text') + '">' + md.utils.escapeHtml(str) + '</code></pre>'
  }
})
// 全局复制函数（避免每个按钮 bind 复杂 handler）
if (typeof window !== 'undefined' && !window.__aiCopyCode) {
  window.__aiCopyCode = function (btn) {
    const code = btn.parentElement.nextElementSibling
    if (!code) return
    const text = code.textContent
    if (navigator.clipboard) {
      navigator.clipboard.writeText(text).then(() => {
        const old = btn.textContent
        btn.textContent = '已复制'
        setTimeout(() => { btn.textContent = old }, 1200)
      })
    } else {
      const ta = document.createElement('textarea')
      ta.value = text
      document.body.appendChild(ta)
      ta.select()
      document.execCommand('copy')
      document.body.removeChild(ta)
      btn.textContent = '已复制'
      setTimeout(() => { btn.textContent = '复制' }, 1200)
    }
  }
}

export default {
  name: 'MessageBubble',
  props: { role: String, content: String, status: String },
  computed: {
    roleClass() { return this.role === '0' ? 'user' : (this.role === '1' ? 'ai' : 'system') },
    roleLabel() { return this.role === '0' ? '你' : (this.role === '1' ? 'AURA' : '系统') },
    timeText() {
      const d = new Date()
      return d.getHours().toString().padStart(2, '0') + ':' + d.getMinutes().toString().padStart(2, '0')
    },
    // 提取 <think>...</think> 块
    parsed() {
      const raw = this.content || ''
      // 匹配最后一个完整 <think> 块 + 主体
      const thinkMatch = raw.match(/<think>([\s\S]*?)<\/think>/)
      if (thinkMatch) {
        return {
          think: thinkMatch[1].trim(),
          main: raw.replace(/<think>[\s\S]*?<\/think>/, '').trim()
        }
      }
      // 流式中可能只有 <think> 还没闭合
      const openMatch = raw.match(/<think>([\s\S]*)$/)
      if (openMatch) {
        return {
          think: openMatch[1].trim(),
          main: '',
          isThinkingTail: true
        }
      }
      return { think: '', main: raw, isThinkingTail: false }
    },
    thinkContent() { return this.parsed.think },
    isThinkingTail() { return !!this.parsed.isThinkingTail },
    thinkMs() {
      // 中文 LLM 思考阶段：约 1 char ≈ 1 token，主流模型 30~50 token/s → 1 char ≈ 20~33ms。
      // 加上思考本身的调度延迟，按 0.08 s/字 估算（更接近人类对"思考用时"的直觉）。
      const t = this.parsed.think || ''
      if (!t) return 0
      return Math.max(1, Math.round(t.length * 0.08))
    },
    renderedThink() { return md.render(this.parsed.think || '') },
    renderedMain() { return md.render(this.parsed.main || '') }
  }
}
</script>

<style scoped>
.message { display: flex; gap: 14px; margin-bottom: 28px; align-items: flex-start; }
.message.user { flex-direction: row-reverse; }

/* Avatar */
.avatar { width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 12px; font-weight: 700; flex-shrink: 0; font-family: var(--ai-font-mono); box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.avatar.user { background: linear-gradient(135deg, #475569, #334155); }
.avatar.ai { background: linear-gradient(135deg, var(--ai-accent), #d97706); position: relative; overflow: hidden; }
.avatar.ai::before { content: ''; position: absolute; inset: -50%; background: conic-gradient(from 0deg, transparent 0%, rgba(255,255,255,0.5) 25%, transparent 50%); animation: spin 4s linear infinite; }
.avatar.ai span { position: relative; z-index: 1; color: #fff; font-size: 10px; font-weight: 800; letter-spacing: 0.05em; }
.avatar.system { background: var(--ai-bg-elevated); color: var(--ai-text-muted); }
.avatar-pulse { display: inline-flex; gap: 2px; position: relative; z-index: 1; }
.avatar-pulse i { display: inline-block; width: 4px; height: 4px; border-radius: 50%; background: #fff; animation: pulse-dot 1.2s ease-in-out infinite; }
.avatar-pulse i:nth-child(2) { animation-delay: .2s; }
.avatar-pulse i:nth-child(3) { animation-delay: .4s; }
@keyframes pulse-dot { 0%, 60%, 100% { opacity: .4; transform: scale(.8); } 30% { opacity: 1; transform: scale(1); } }
@keyframes spin { to { transform: rotate(360deg); } }

/* Layout */
.bubble-wrap { max-width: 78%; min-width: 0; }
.message.user .bubble-wrap { display: flex; flex-direction: column; align-items: flex-end; }

/* Meta */
.meta { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; font-size: 11px; color: var(--ai-text-muted); font-family: var(--ai-font-mono); letter-spacing: 0.04em; }
.message.user .meta { flex-direction: row-reverse; }
.meta-name { color: var(--ai-text-secondary); font-weight: 600; letter-spacing: 0.06em; }
.meta-time { color: var(--ai-text-muted); }
.meta-think-time { color: var(--ai-text-muted); font-size: 10px; }
.meta-typing { display: inline-flex; align-items: center; gap: 4px; color: var(--ai-accent); margin-left: 2px; }
.meta-typing i { width: 3px; height: 3px; border-radius: 50%; background: var(--ai-accent); animation: pulse-dot 1s ease-in-out infinite; }
.meta-typing i:nth-child(2) { animation-delay: .15s; }
.meta-typing i:nth-child(3) { animation-delay: .3s; }

/* Bubble */
.bubble { padding: 12px 16px; border-radius: 12px; font-size: 14px; line-height: 1.75; background: #fff; color: var(--ai-text-primary); border: 1px solid var(--ai-border); word-break: break-word; box-shadow: 0 1px 2px rgba(0,0,0,0.04); }
.message.user .bubble { background: var(--ai-bg-elevated); border-color: var(--ai-border); border-top-right-radius: 4px; }
.message.ai .bubble { background: #fff; border-color: var(--ai-border); border-top-left-radius: 4px; }
.message.system .bubble { background: var(--ai-bg-elevated); color: var(--ai-text-muted); font-size: 12px; border-style: dashed; }

/* 流式输出光标 */
.stream-cursor { display: inline-block; width: 8px; height: 16px; background: var(--ai-accent); margin-left: 2px; vertical-align: text-bottom; animation: cursor-blink 1s steps(2) infinite; }
@keyframes cursor-blink { 0%, 50% { opacity: 1; } 50.01%, 100% { opacity: 0; } }

/* Think block (折叠区) */
.think-block { margin-bottom: 8px; border: 1px solid var(--ai-border); border-radius: 10px; background: var(--ai-bg-elevated); overflow: hidden; }
.think-block > summary { display: flex; align-items: center; gap: 8px; padding: 8px 12px; cursor: pointer; list-style: none; user-select: none; font-size: 12px; color: var(--ai-text-secondary); transition: background .12s; }
.think-block > summary::-webkit-details-marker { display: none; }
.think-block > summary::marker { content: ''; }
.think-block > summary:hover { background: rgba(245,158,11,0.06); }
.think-icon { font-size: 13px; }
.think-label { font-weight: 500; }
.think-duration { color: var(--ai-text-muted); font-family: var(--ai-font-mono); font-size: 11px; margin-left: 2px; }
.think-chevron { margin-left: auto; color: var(--ai-text-muted); transition: transform .15s; flex-shrink: 0; }
.think-block[open] .think-chevron { transform: rotate(180deg); }
.think-block[open] > summary { border-bottom: 1px solid var(--ai-border); }
.think-body { padding: 10px 14px; color: var(--ai-text-muted); font-size: 13px; line-height: 1.65; max-height: 280px; overflow-y: auto; }
.think-body >>> p { margin: 0 0 6px 0; }
.think-body >>> p:last-child { margin-bottom: 0; }
.think-body >>> p:only-child { margin: 0; }

/* Markdown styles in main bubble */
.bubble >>> p { margin: 0 0 10px 0; }
.bubble >>> p:last-child { margin-bottom: 0; }
.bubble >>> h1, .bubble >>> h2, .bubble >>> h3 { color: var(--ai-text-primary); margin: 14px 0 8px 0; font-weight: 600; line-height: 1.3; }
.bubble >>> h1 { font-size: 18px; }
.bubble >>> h2 { font-size: 16px; }
.bubble >>> h3 { font-size: 15px; }
.bubble >>> code { background: var(--ai-bg-elevated); padding: 2px 6px; border-radius: 4px; font-size: 12.5px; color: #d97706; font-family: var(--ai-font-mono); border: 1px solid var(--ai-border); }
.bubble >>> pre { margin: 10px 0; }
.bubble >>> pre.code-block { background: #1e293b; color: #e2e8f0; border-radius: 10px; overflow: hidden; border: 1px solid #334155; }
.bubble >>> .code-header { display: flex; justify-content: space-between; align-items: center; padding: 6px 12px; background: #0f172a; border-bottom: 1px solid #334155; font-family: var(--ai-font-mono); font-size: 11px; color: #94a3b8; letter-spacing: 0.05em; text-transform: lowercase; }
.bubble >>> .code-lang { text-transform: uppercase; }
.bubble >>> .code-copy { background: transparent; border: 1px solid #334155; color: #94a3b8; padding: 2px 8px; border-radius: 4px; font-size: 10px; cursor: pointer; transition: color .12s, border-color .12s; font-family: inherit; }
.bubble >>> .code-copy:hover { color: #e2e8f0; border-color: #94a3b8; }
.bubble >>> pre.code-block code { display: block; padding: 12px 14px; background: transparent; color: #e2e8f0; border: none; font-size: 12.5px; line-height: 1.6; overflow-x: auto; font-family: var(--ai-font-mono); }
.bubble >>> ul, .bubble >>> ol { margin: 8px 0; padding-left: 20px; }
.bubble >>> li { margin: 3px 0; }
.bubble >>> a { color: var(--ai-accent); text-decoration: none; border-bottom: 1px solid transparent; transition: border-color .12s; }
.bubble >>> a:hover { border-bottom-color: var(--ai-accent); }
.bubble >>> blockquote { margin: 10px 0; padding: 8px 14px; border-left: 3px solid var(--ai-accent); background: var(--ai-accent-soft); color: var(--ai-text-secondary); border-radius: 0 6px 6px 0; }
.bubble >>> table { border-collapse: collapse; margin: 10px 0; font-size: 13px; width: 100%; }
.bubble >>> th, .bubble >>> td { border: 1px solid var(--ai-border); padding: 6px 10px; text-align: left; }
.bubble >>> th { background: var(--ai-bg-elevated); color: var(--ai-text-primary); font-weight: 600; }
.bubble >>> strong { font-weight: 600; color: var(--ai-text-primary); }
.bubble >>> em { font-style: italic; color: var(--ai-text-secondary); }
</style>
