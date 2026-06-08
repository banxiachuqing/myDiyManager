<template>
  <div class="chat-main">
    <div class="toolbar">
      <div class="toolbar-group">
        <span class="toolbar-label">目标</span>
        <el-select v-model="hostIds" multiple filterable collapse-tags placeholder="选择主机" class="ai-host-select">
          <el-option v-for="h in hosts" :key="h.hostId" :label="`${h.hostName} (${h.ip})`" :value="h.hostId">
            <div class="host-option">
              <span class="host-status" :class="h.status === '0' ? 'on' : 'off'" />
              <span class="host-name">{{ h.hostName }}</span>
              <span class="host-ip">{{ h.ip }}</span>
            </div>
          </el-option>
        </el-select>
        <button class="ai-btn-secondary" @click="drawer = true">
          <span>▤</span><span>主机管理</span>
        </button>
      </div>
    </div>
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
        <span v-if="hostIds.length" class="host-count-badge">
          <span class="count-num">{{ hostIds.length }}</span>
          <span class="count-text">台主机已选</span>
        </span>
      </div>
    </div>
    <div class="chat-area" ref="scrollRef">
      <template v-for="m in messages">
        <MessageBubble :key="m.messageId" :role="m.role" :content="m.content" :status="m.status" />
        <!-- 命令框跟对应的 ai 消息一起渲染（按 messageId 关联），而不是堆在所有消息之后 -->
        <div v-for="(cmd, idx) in (m.commands || [])" :key="m.messageId + ':' + cmd.cmdId" class="cmd-slot">
          <CommandCard :cmd="cmd" :idx="idx + 1" :hosts="hosts" @decide="onDecide" />
        </div>
      </template>
      <div v-if="!messages.length" class="empty-state">
        <div class="empty-icon">▣</div>
        <h3 class="empty-title">选择主机后开始运维</h3>
        <p class="empty-desc">支持多机并发执行 · 高危命令需人工复核 · 命令可一键放行</p>
      </div>
    </div>
    <div class="input-area">
      <div class="input-shell">
        <textarea
          v-model="input"
          class="ai-textarea"
          rows="2"
          placeholder="描述你的运维诉求... · Enter 发送 · Shift+Enter 换行"
          @keydown="onInputKeydown"
        />
        <div class="input-meta">
          <span class="input-tip">⌘ AI 将生成可执行的命令 · {{ hostIds.length || 0 }} 台目标</span>
          <button class="ai-send-btn" :disabled="sending || !input.trim()" @click="send">
            <span v-if="!sending">执行</span>
            <span v-else class="ai-send-loading"><i /><i /><i /></span>
          </button>
        </div>
      </div>
    </div>
    <AiHostDrawer v-model="drawer" />
  </div>
</template>

<script>
import { listAllHost } from '@/api/ai/host'
import { useAiSse } from '@/composables/useAiSse'
import { aiCmdDecision } from '@/api/ai/ops'
import { listCommands, aiListMessages } from '@/api/ai/chat'
import { listVendor, getDefaultVendor } from '@/api/ai/vendor'
import MessageBubble from './MessageBubble'
import CommandCard from './CommandCard'
import AiHostDrawer from '@/components/AiHostDrawer'

export default {
  name: 'OpsTab',
  components: { MessageBubble, CommandCard, AiHostDrawer },
  props: { sessionId: { type: String, default: null } },
  emits: ['session-changed', 'session-created'],
  data() {
    return {
      input: '', sending: false, messages: [], commands: [], hosts: [],
      hostIds: [], vendorId: null, vendorList: [], vendorLoading: false,
      drawer: false, sse: useAiSse()
    }
  },
  watch: {
    sessionId(id) {
      if (!id) { this.hardReset(); return }
      this.$emit('session-created', id)
      this.loadHistory()
    }
  },
  created() { this.loadHosts(); this.loadVendors(); if (this.sessionId) this.loadHistory() },
  methods: {
    hardReset() {
      this.messages = []
      this.commands = []
      this.input = ''
      this.sending = false
    },
    loadHistory() {
      this.messages = []
      this.commands = []
      if (!this.sessionId) return
      aiListMessages(this.sessionId).then(r => {
        const msgs = (r.data || []).map(m => ({
          messageId: m.messageId, role: m.role, content: m.content || '', status: m.status || '1', commands: []
        }))
        this.messages = msgs
        // 并行拉每条 AI 消息的命令，挂在对应 messageId 的 commands 数组上
        const aiIds = msgs.filter(m => m.role === '1').map(m => m.messageId)
        return Promise.all(aiIds.map(id => listCommands({ messageId: id }).then(r => (r.data || [])).catch(() => [])))
          .then(arr => {
            let i = 0
            msgs.forEach(m => {
              if (m.role === '1') {
                // 用 $set 给每条 ai 消息加 commands 字段，触发响应式
                this.$set(m, 'commands', arr[i] || [])
                i++
              }
            })
          })
      }).catch(() => { this.messages = []; this.commands = [] })
    },
    loadHosts() { listAllHost().then(r => { this.hosts = r.data || [] }) },
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
      if (!this.hostIds.length) { this.$message.warning('请至少选择 1 台主机'); return }
      if (!this.vendorId) { this.$message.warning('请先在「厂商管理」配置默认厂商'); return }
      const userMsg = { messageId: 'tmp-' + Date.now(), role: '0', content: this.input, status: '1' }
      this.messages.push(userMsg)
      const aiMsg = { messageId: 'ai-' + Date.now(), role: '1', content: '', status: '0', commands: [] }
      this.messages.push(aiMsg)
      const content = this.input
      this.input = ''
      this.sending = true
      this.sse.open({
        body: { tabType: '1', content, vendorId: this.vendorId, sessionId: this.sessionId, hostIds: this.hostIds },
        onChunk: delta => { aiMsg.content = (aiMsg.content || '') + (delta || '') },
        onDone: data => {
          this.sending = false
          aiMsg.status = '1'
          if (data) {
            if (data.messageId) aiMsg.messageId = data.messageId
            if (data.sessionId && data.sessionId !== this.sessionId) {
              this.$emit('session-changed', data.sessionId)
            }
            if (data.commands) {
              // 新生成的命令直接挂到对应 ai 消息的 commands 数组（不再存到 this.commands）
              this.$set(aiMsg, 'commands', data.commands)
              // 白名单命中 → 后端"自动通过"，但要前端主动调 decide 触发 SSH 执行
              data.commands.forEach(c => {
                if (c.inWhitelist && c.decision === '0') {
                  this.$nextTick(() => this.onDecide({ cmdId: c.cmdId, decision: '1' }))
                }
              })
            }
          }
        },
        onError: e => { this.sending = false; aiMsg.status = '2'; this.$message.error(e.message || '失败') }
      })
    },
    onDecide(payload) {
      aiCmdDecision(payload)
        .then(r => {
          this.$message.success(r.msg || '已处理')
          // 找到 cmd 所属的 ai 消息
          const aiMsg = this.messages.find(m => m.role === '1' && (m.commands || []).some(c => c.cmdId === payload.cmdId))
          const cmd = aiMsg && (aiMsg.commands || []).find(c => c.cmdId === payload.cmdId)
          if (!cmd) return
          if (payload.decision === '3') {
            this.$set(cmd, 'execStatus', '6')
            return
          }
          // 允许/会话内：后端异步执行 SSH，前端每 1s 轮询
          this.$set(cmd, 'execStatus', '1')
          if (!aiMsg) return
          this.pollCmdStatus(payload.cmdId, aiMsg.messageId, 0)
        })
        .catch(e => this.$message.error(e.message))
    },
    pollCmdStatus(cmdId, messageId, attempt) {
      const MAX = 30  // 最多 30 秒
      if (attempt >= MAX) return
      setTimeout(() => {
        listCommands({ messageId }).then(r => {
          const fresh = (r.data || []).find(c => c.cmdId === cmdId)
          const aiMsg = this.messages.find(m => m.role === '1' && (m.commands || []).some(c => c.cmdId === cmdId))
          const cmd = aiMsg && (aiMsg.commands || []).find(c => c.cmdId === cmdId)
          if (fresh && cmd) {
            // 用 $set 逐字段更新，触发 Vue 2 响应式
            this.$set(cmd, 'execStatus', fresh.execStatus)
            if (fresh.execResult != null) this.$set(cmd, 'execResult', fresh.execResult)
            if (fresh.execMs != null) this.$set(cmd, 'execMs', fresh.execMs)
            if (fresh.decision != null) this.$set(cmd, 'decision', fresh.decision)
          }
          if (fresh && fresh.execStatus === '1') {
            this.pollCmdStatus(cmdId, messageId, attempt + 1)
          } else if (fresh && fresh.execStatus && fresh.execStatus !== '0') {
            // 命令执行完成（非"待裁决/执行中"）→ 自动触发 LLM 总结第二轮
            this.triggerSummary(cmdId, fresh.execStatus === '2' || fresh.execStatus === '3')
          }
        }).catch(() => {})
      }, 1000)
    },
    /**
     * 命令执行完成后自动调 LLM 总结：
     *  1. 在对应 aiMsg 下方追加一条新 ai 消息 placeholder
     *  2. send 端点带 cmdId 触发后端 buildUserContent 注入执行结果
     *  3. 流式 chunk 追加到新消息 content
     */
    triggerSummary(cmdId, success) {
      const summaryMsg = {
        messageId: 'sum-' + Date.now(),
        role: '1',
        content: '',
        status: '0',
        commands: [],
        isSummary: true
      }
      this.messages.push(summaryMsg)
      this.$nextTick(this.scrollToBottom)
      const hint = success ? '请根据命令执行结果给出一句话结论和关键指标。' : '请根据命令执行失败的错误信息，给出原因分析。'
      this.summarize = true
      this.sse.open({
        body: { tabType: '1', content: hint, vendorId: this.vendorId, sessionId: this.sessionId, hostIds: this.hostIds, cmdId: cmdId },
        onChunk: delta => { summaryMsg.content = (summaryMsg.content || '') + (delta || '') },
        onDone: data => {
          summaryMsg.status = '1'
          if (data && data.sessionId && data.sessionId !== this.sessionId) {
            this.$emit('session-changed', data.sessionId)
          }
          this.summarize = false
        },
        onError: e => { summaryMsg.status = '2'; this.summarize = false; this.$message.error(e.message || '失败') }
      })
    }
  }
}
</script>

<style scoped>
.chat-main { display: flex; flex-direction: column; height: 100%; background: var(--ai-bg-base); width: 100%; min-width: 0; min-height: 0; }

.toolbar { display: flex; align-items: center; padding: 12px 24px; border-bottom: 1px solid var(--ai-border); background: #fff; }
.toolbar-group { display: flex; align-items: center; gap: 10px; width: 100%; }
.toolbar-label { color: var(--ai-text-muted); font-size: 11px; letter-spacing: 0.18em; font-family: var(--ai-font-mono); text-transform: uppercase; min-width: 36px; }

.ai-host-select { flex: 1; max-width: 480px; }
.ai-host-select >>> .el-input__inner,
.ai-vendor-select >>> .el-input__inner { background: var(--ai-bg-elevated) !important; border-color: var(--ai-border) !important; color: var(--ai-text-primary) !important; font-family: var(--ai-font-mono); font-size: 13px; height: 34px; line-height: 34px; }
.ai-vendor-select { width: 240px; }
.ai-vendor-select >>> .el-input__inner::placeholder,
.ai-host-select >>> .el-input__inner::placeholder { color: var(--ai-text-muted); }
.ai-host-select >>> .el-input__suffix .el-input__icon,
.ai-vendor-select >>> .el-input__suffix .el-input__icon { color: var(--ai-text-secondary); }

.host-option { display: flex; align-items: center; gap: 8px; }
.host-status { width: 6px; height: 6px; border-radius: 50%; }
.host-status.on { background: var(--ai-success); box-shadow: 0 0 6px rgba(16,185,129,0.5); }
.host-status.off { background: var(--ai-text-muted); }
.host-name { color: var(--ai-text-primary); font-size: 13px; }
.host-ip { color: var(--ai-text-muted); font-size: 11px; font-family: var(--ai-font-mono); }

.vendor-option { display: flex; justify-content: space-between; align-items: center; }
.vendor-option-name { color: var(--ai-text-primary); font-size: 13px; }
.vendor-option-tags { display: flex; gap: 4px; }
.badge { display: inline-block; padding: 1px 6px; border-radius: 3px; font-size: 10px; font-weight: 600; letter-spacing: 0.05em; }
.badge-default { background: var(--ai-accent-soft); color: var(--ai-accent); }
.badge-offline { background: rgba(239, 68, 68, 0.10); color: var(--ai-danger); }

.ai-btn-secondary { display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; background: #fff; color: var(--ai-text-secondary); border: 1px solid var(--ai-border-strong); border-radius: 6px; font-size: 12px; font-weight: 500; cursor: pointer; transition: all .12s; font-family: inherit; }
.ai-btn-secondary:hover { color: var(--ai-accent); border-color: var(--ai-accent); background: var(--ai-accent-soft); }
.ai-btn-secondary span:first-child { color: var(--ai-accent); }

.host-count-badge { display: inline-flex; align-items: baseline; gap: 4px; padding: 5px 12px; background: var(--ai-accent-soft); border: 1px solid rgba(245,158,11,0.3); border-radius: 999px; font-size: 11px; color: var(--ai-text-secondary); margin-left: auto; font-family: var(--ai-font-mono); }
.count-num { color: var(--ai-accent); font-weight: 700; font-size: 14px; }
.count-text { letter-spacing: 0.05em; }

/* Chat Area */
.chat-area { flex: 1; overflow-y: auto; padding: 24px; background: linear-gradient(180deg, #fafbfc 0%, #ffffff 60%); }
.cmd-slot { margin-bottom: 12px; }
.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; gap: 12px; padding: 40px 20px; text-align: center; }
.empty-icon { font-size: 56px; color: var(--ai-text-muted); font-family: var(--ai-font-mono); opacity: .4; }
.empty-title { font-size: 16px; color: var(--ai-text-secondary); font-weight: 500; margin: 0; }
.empty-desc { color: var(--ai-text-muted); font-size: 12px; margin: 0; }

/* Input */
.input-area { padding: 16px 24px 24px; background: #fff; border-top: 1px solid var(--ai-border); }
.input-shell { background: #fff; border: 1px solid var(--ai-border); border-radius: var(--ai-radius-md); transition: border-color .15s, box-shadow .15s; }
.input-shell:focus-within { border-color: var(--ai-accent); box-shadow: 0 0 0 3px var(--ai-accent-soft); }
.ai-textarea { display: block; width: 100%; padding: 14px 16px; background: transparent; border: none; outline: none; resize: none; color: var(--ai-text-primary); font-size: 14px; line-height: 1.6; font-family: var(--ai-font-sans); }
.ai-textarea::placeholder { color: var(--ai-text-muted); }
.input-meta { display: flex; justify-content: space-between; align-items: center; padding: 8px 12px; border-top: 1px solid var(--ai-border); }
.input-tip { color: var(--ai-text-muted); font-size: 11px; font-family: var(--ai-font-mono); letter-spacing: 0.05em; }
.ai-send-btn { display: inline-flex; align-items: center; gap: 6px; padding: 6px 18px; background: linear-gradient(135deg, var(--ai-info), #2563eb); color: #fff; border: none; border-radius: 6px; font-size: 12px; font-weight: 600; letter-spacing: 0.08em; cursor: pointer; transition: transform .12s, box-shadow .12s, opacity .12s; font-family: inherit; }
.ai-send-btn:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(59,130,246,0.30); }
.ai-send-btn:disabled { opacity: .4; cursor: not-allowed; }
.ai-send-loading { display: inline-flex; gap: 2px; }
.ai-send-loading i { display: inline-block; width: 4px; height: 4px; border-radius: 50%; background: #fff; animation: bounce 1.2s ease-in-out infinite; }
.ai-send-loading i:nth-child(2) { animation-delay: .2s; }
.ai-send-loading i:nth-child(3) { animation-delay: .4s; }
@keyframes bounce { 0%, 60%, 100% { transform: translateY(0); opacity: .6; } 30% { transform: translateY(-4px); opacity: 1; } }

/* Element UI overrides */
.chat-main >>> .el-select-dropdown { background: #fff !important; border: 1px solid var(--ai-border) !important; box-shadow: 0 6px 20px rgba(0,0,0,0.08) !important; }
.chat-main >>> .el-select-dropdown__item { color: var(--ai-text-primary) !important; }
.chat-main >>> .el-select-dropdown__item.hover, .chat-main >>> .el-select-dropdown__item:hover { background: var(--ai-accent-soft) !important; }
.chat-main >>> .el-select-dropdown__item.selected { color: var(--ai-accent) !important; font-weight: 600; }
</style>
