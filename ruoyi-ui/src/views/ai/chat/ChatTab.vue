<template>
  <div class="chat-main">
    <div class="toolbar">
      <div class="toolbar-group">
        <span class="toolbar-label">LLM</span>
        <el-select v-model="vendorId" placeholder="选择厂商" filterable class="ai-vendor-select" :loading="vendorLoading" @change="onVendorChange">
          <el-option v-for="v in vendorList" :key="v.vendorId" :label="vendorLabel(v)" :value="v.vendorId" :disabled="v.status !== '0'">
            <div class="vendor-option">
              <span class="vendor-option-name">{{ v.vendorName }}</span>
              <span class="vendor-option-tags">
                <span v-if="v.isDefault === '1'" class="badge badge-default">默认</span>
                <span v-if="v.status !== '0'" class="badge badge-offline">已停用</span>
              </span>
            </div>
          </el-option>
        </el-select>
        <span v-if="vendorId" class="vendor-status">
          <span class="status-dot" :class="currentVendorStatus" />
          <span class="status-text">{{ currentVendorStatus === 'on' ? '在线' : '离线' }}</span>
        </span>
      </div>
      <div class="toolbar-group toolbar-group-right">
        <span class="manage-link" @click="$emit('manage')">
          <span class="manage-icon">⚙</span>
          <span>厂商管理</span>
        </span>
      </div>
    </div>
    <div class="chat-area" ref="scrollRef">
      <div v-if="!messages.length" class="empty-state">
        <div class="empty-illust">
          <div class="empty-ring" />
          <div class="empty-ring empty-ring-2" />
          <div class="empty-core">A</div>
        </div>
        <h3 class="empty-title">开始一次对话</h3>
        <p class="empty-desc">提出任何业务问题 · 支持多轮上下文 · 流式响应</p>
        <div class="empty-suggestions">
          <span class="suggestion">「解释一下 sys_menu 表结构」</span>
          <span class="suggestion">「写一个 Spring Boot 全局异常处理」</span>
          <span class="suggestion">「给我 5 个 MySQL 索引优化建议」</span>
        </div>
      </div>
      <MessageBubble v-for="m in messages" :key="m.messageId" :role="m.role" :content="m.content" :status="m.status" />
    </div>
    <div class="input-area">
      <div class="input-shell">
        <textarea
          v-model="input"
          class="ai-textarea"
          rows="2"
          placeholder="向 AI 提问... · Enter 发送 · Shift+Enter 换行"
          @keydown="onInputKeydown"
        />
        <div class="input-meta">
          <span class="input-tip">⏎ 发送 · {{ input.length }} 字</span>
          <button class="ai-send-btn" :disabled="sending || !input.trim()" @click="send">
            <span v-if="!sending">发送</span>
            <span v-else class="ai-send-loading"><i /><i /><i /></span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { useAiSse } from '@/composables/useAiSse'
import { listVendor, getDefaultVendor } from '@/api/ai/vendor'
import { aiListMessages } from '@/api/ai/chat'
import MessageBubble from './MessageBubble'

export default {
  name: 'ChatTab',
  components: { MessageBubble },
  props: { sessionId: { type: String, default: null } },
  emits: ['manage', 'session-changed', 'session-created'],
  data() {
    return {
      input: '', sending: false, messages: [], vendorId: null, vendorList: [], vendorLoading: false,
      sse: useAiSse()
    }
  },
  computed: {
    currentVendorStatus() {
      const v = this.vendorList.find(x => x.vendorId === this.vendorId)
      if (!v) return 'off'
      return v.status === '0' ? 'on' : 'off'
    }
  },
  watch: {
    sessionId(id) {
      // sessionId 变 null（新建会话）→ 立即彻底清空并终止；不要回发事件，
      // 否则父级 onSessionCreated 会再 loadSessions 把 sessionId 重新设回自动选中第一个
      if (!id) { this.hardReset(); return }
      this.$emit('session-created', id)
      this.loadHistory()
    },
    // 流式输出时自动滚到最新消息
    messages: {
      handler() { this.$nextTick(this.scrollToBottom) },
      deep: false
    },
    // 监听最后一条消息内容变化（流式 chunk by chunk）
    'messages': {
      handler(list) {
        if (list && list.length) this.$nextTick(this.scrollToBottom)
      },
      deep: true
    }
  },
  created() { this.loadVendors(); if (this.sessionId) this.loadHistory() },
  methods: {
    hardReset() {
      // 彻底清空：messages、commands、sse 句柄重置
      this.messages = []
      this.commands = []
      this.input = ''
      this.sending = false
    },
    loadHistory() {
      this.messages = []
      if (!this.sessionId) return
      aiListMessages(this.sessionId).then(r => {
        this.messages = (r.data || []).map(m => ({
          messageId: m.messageId, role: m.role, content: m.content || '', status: m.status || '1'
        }))
        this.$nextTick(this.scrollToBottom)
      }).catch(() => { this.messages = [] })
    },
    scrollToBottom() {
      const el = this.$refs.scrollRef
      if (!el) return
      el.scrollTop = el.scrollHeight
    },
    vendorLabel(v) {
      return v.vendorName + (v.status === '0' ? '' : '（已停用）')
    },
    loadVendors() {
      this.vendorLoading = true
      Promise.all([listVendor({ pageNum: 1, pageSize: 100 }), getDefaultVendor()])
        .then(([listResp, defResp]) => {
          this.vendorList = (listResp && listResp.rows) || []
          const def = defResp && defResp.data
          const fromList = this.vendorList.find(v => v.isDefault === '1' && v.status === '0')
          if (fromList) {
            this.vendorId = fromList.vendorId
          } else if (def && def.vendorId && this.vendorList.some(v => v.vendorId === def.vendorId)) {
            this.vendorId = def.vendorId
          } else {
            const firstAvail = this.vendorList.find(v => v.status === '0')
            this.vendorId = firstAvail ? firstAvail.vendorId : null
          }
        })
        .catch(() => { this.vendorList = [] })
        .finally(() => { this.vendorLoading = false })
    },
    onVendorChange(id) { this.vendorId = id },
    onInputKeydown(e) {
      // Enter 发送；Shift+Enter 换行（textarea 默认行为）
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault()
        this.send()
      }
    },
    send() {
      if (!this.input.trim() || this.sending) return
      if (!this.vendorId) { this.$message.warning('请先在「厂商管理」配置默认厂商'); return }
      const userMsg = { messageId: 'tmp-' + Date.now(), role: '0', content: this.input, status: '1' }
      this.messages.push(userMsg)
      const aiMsg = { messageId: 'ai-' + Date.now(), role: '1', content: '', status: '0' }
      this.messages.push(aiMsg)
      const content = this.input
      this.input = ''
      this.sending = true
      this.sse.open({
        body: { tabType: '0', content, vendorId: this.vendorId, sessionId: this.sessionId },
        onChunk: delta => { aiMsg.content = (aiMsg.content || '') + (delta || '') },
        onDone: data => {
          this.sending = false
          aiMsg.status = '1'
          if (data) {
            if (data.messageId) aiMsg.messageId = data.messageId
            if (data.sessionId && data.sessionId !== this.sessionId) {
              // 新会话首次 send：把后端真实 sessionId 同步给父组件，触发 sidebar 刷新
              this.$emit('session-changed', data.sessionId)
            }
          }
        },
        onError: e => { this.sending = false; aiMsg.status = '2'; this.$message.error(e.message || '失败') }
      })
    }
  }
}
</script>

<style scoped>
.chat-main { display: flex; flex-direction: column; height: 100%; background: var(--ai-bg-base); width: 100%; min-width: 0; min-height: 0; }

/* ===== Toolbar ===== */
.toolbar { display: flex; justify-content: space-between; align-items: center; padding: 12px 24px; border-bottom: 1px solid var(--ai-border); background: #fff; }
.toolbar-group { display: flex; align-items: center; gap: 10px; }
.toolbar-group-right { gap: 16px; }
.toolbar-label { color: var(--ai-text-muted); font-size: 11px; letter-spacing: 0.18em; font-family: var(--ai-font-mono); text-transform: uppercase; }
.ai-vendor-select { width: 240px; }
.ai-vendor-select >>> .el-input__inner { background: var(--ai-bg-elevated) !important; border-color: var(--ai-border) !important; color: var(--ai-text-primary) !important; font-family: var(--ai-font-mono); font-size: 13px; height: 34px; line-height: 34px; }
.ai-vendor-select >>> .el-input__inner::placeholder { color: var(--ai-text-muted); }
.ai-vendor-select >>> .el-input__suffix .el-input__icon { color: var(--ai-text-secondary); }
.vendor-option { display: flex; justify-content: space-between; align-items: center; }
.vendor-option-name { color: var(--ai-text-primary); font-size: 13px; }
.vendor-option-tags { display: flex; gap: 4px; }
.badge { display: inline-block; padding: 1px 6px; border-radius: 3px; font-size: 10px; font-weight: 600; letter-spacing: 0.05em; }
.badge-default { background: var(--ai-accent-soft); color: var(--ai-accent); }
.badge-offline { background: rgba(239, 68, 68, 0.10); color: var(--ai-danger); }
.vendor-status { display: inline-flex; align-items: center; gap: 6px; font-size: 12px; color: var(--ai-text-secondary); font-family: var(--ai-font-mono); }
.status-dot { width: 8px; height: 8px; border-radius: 50%; }
.status-dot.on { background: var(--ai-success); box-shadow: 0 0 8px rgba(16,185,129,0.5); animation: pulse 2s ease-in-out infinite; }
.status-dot.off { background: var(--ai-text-muted); }
.manage-link { display: inline-flex; align-items: center; gap: 6px; color: var(--ai-text-secondary); font-size: 12px; cursor: pointer; padding: 6px 10px; border-radius: 6px; transition: color .12s, background .12s; }
.manage-link:hover { color: var(--ai-accent); background: var(--ai-accent-soft); }
.manage-icon { font-size: 14px; }

/* ===== Chat Area ===== */
.chat-area { flex: 1; overflow-y: auto; padding: 32px 24px; background: linear-gradient(180deg, #fafbfc 0%, #ffffff 60%); }
.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; gap: 16px; padding: 40px 20px; text-align: center; }
.empty-illust { position: relative; width: 120px; height: 120px; display: flex; align-items: center; justify-content: center; }
.empty-ring { position: absolute; inset: 0; border: 1px solid var(--ai-border-strong); border-radius: 50%; border-top-color: var(--ai-accent); animation: spin 8s linear infinite; }
.empty-ring-2 { inset: 14px; border-top-color: var(--ai-info); animation: spin 6s linear infinite reverse; opacity: .6; }
.empty-core { width: 56px; height: 56px; border-radius: 50%; background: linear-gradient(135deg, var(--ai-accent), #d97706); color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 24px; font-family: var(--ai-font-mono); box-shadow: 0 4px 16px var(--ai-accent-glow); }
.empty-title { font-size: 18px; color: var(--ai-text-primary); font-weight: 600; margin: 0; }
.empty-desc { color: var(--ai-text-secondary); font-size: 13px; margin: 0; }
.empty-suggestions { display: flex; flex-direction: column; gap: 8px; margin-top: 12px; max-width: 480px; width: 100%; }
.suggestion { padding: 10px 16px; background: #fff; border: 1px solid var(--ai-border); border-radius: 8px; color: var(--ai-text-secondary); font-size: 13px; cursor: pointer; transition: all .15s; text-align: left; }
.suggestion:hover { color: var(--ai-text-primary); border-color: var(--ai-accent); background: var(--ai-accent-soft); transform: translateX(2px); }
@keyframes spin { to { transform: rotate(360deg); } }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: .4; } }

/* ===== Input Area ===== */
.input-area { padding: 16px 24px 24px; background: #fff; border-top: 1px solid var(--ai-border); }
.input-shell { background: #fff; border: 1px solid var(--ai-border); border-radius: var(--ai-radius-md); transition: border-color .15s, box-shadow .15s; }
.input-shell:focus-within { border-color: var(--ai-accent); box-shadow: 0 0 0 3px var(--ai-accent-soft); }
.ai-textarea { display: block; width: 100%; padding: 14px 16px; background: transparent; border: none; outline: none; resize: none; color: var(--ai-text-primary); font-size: 14px; line-height: 1.6; font-family: var(--ai-font-sans); }
.ai-textarea::placeholder { color: var(--ai-text-muted); }
.input-meta { display: flex; justify-content: space-between; align-items: center; padding: 8px 12px; border-top: 1px solid var(--ai-border); }
.input-tip { color: var(--ai-text-muted); font-size: 11px; font-family: var(--ai-font-mono); letter-spacing: 0.05em; }
.ai-send-btn { display: inline-flex; align-items: center; gap: 6px; padding: 6px 18px; background: linear-gradient(135deg, var(--ai-accent), #d97706); color: #fff; border: none; border-radius: 6px; font-size: 12px; font-weight: 600; letter-spacing: 0.08em; cursor: pointer; transition: transform .12s, box-shadow .12s, opacity .12s; font-family: inherit; }
.ai-send-btn:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 4px 12px var(--ai-accent-glow); }
.ai-send-btn:disabled { opacity: .4; cursor: not-allowed; }
.ai-send-loading { display: inline-flex; gap: 2px; }
.ai-send-loading i { display: inline-block; width: 4px; height: 4px; border-radius: 50%; background: #fff; animation: bounce 1.2s ease-in-out infinite; }
.ai-send-loading i:nth-child(2) { animation-delay: .2s; }
.ai-send-loading i:nth-child(3) { animation-delay: .4s; }
@keyframes bounce { 0%, 60%, 100% { transform: translateY(0); opacity: .6; } 30% { transform: translateY(-4px); opacity: 1; } }

/* Element UI 内部下拉面板 */
.chat-main >>> .el-select-dropdown { background: #fff !important; border: 1px solid var(--ai-border) !important; box-shadow: 0 6px 20px rgba(0,0,0,0.08) !important; }
.chat-main >>> .el-select-dropdown__item { color: var(--ai-text-primary) !important; }
.chat-main >>> .el-select-dropdown__item.hover, .chat-main >>> .el-select-dropdown__item:hover { background: var(--ai-accent-soft) !important; }
.chat-main >>> .el-select-dropdown__item.selected { color: var(--ai-accent) !important; font-weight: 600; }
</style>
