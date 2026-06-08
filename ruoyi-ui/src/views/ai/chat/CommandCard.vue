<template>
  <div class="command-card" :class="{ 'high-risk': cmd.isHighRisk === '1', 'resolved': cmd.execStatus !== '0' }">
    <div class="cmd-header">
      <div class="cmd-header-left">
        <span class="cmd-index">#{{ idx }}</span>
        <span v-if="cmd.isHighRisk === '1'" class="cmd-warning">
          <span class="warn-icon">⚠</span>
          <span>高危操作 · 需人工复核</span>
        </span>
        <span v-else class="cmd-type">{{ verbLabel }}</span>
      </div>
      <div class="cmd-header-right">
        <span class="cmd-id">{{ cmd.cmdId }}</span>
      </div>
    </div>
    <div class="cmd-target">
      <span class="target-label">TARGET</span>
      <span v-for="id in targetHostIds" :key="id" class="target-host">
        <span class="target-dot" />
        {{ hostNameOf(id) }}
      </span>
    </div>
    <div class="terminal">
      <div class="terminal-bar">
        <span class="terminal-dot" /><span class="terminal-dot" /><span class="terminal-dot" />
        <span class="terminal-prompt">~ {{ hostNameOf((targetHostIds||[''])[0]) || 'host' }}</span>
      </div>
      <pre class="terminal-body"><span class="prompt">$ </span><span class="cmd-text">{{ cmd.cmdText }}</span></pre>
    </div>
    <div v-if="cmd.execStatus === '0' && cmd.inWhitelist" class="cmd-decision whitelisted">
      <span class="decision-icon">⏰</span>
      <span>同类命令已放行 · 自动通过</span>
    </div>
    <div v-else-if="cmd.execStatus === '0'" class="cmd-decision">
      <button class="cmd-btn cmd-btn-allow" @click="$emit('decide', { cmdId: cmd.cmdId, decision: '1' })">
        <span>✓</span><span>允许</span>
      </button>
      <button class="cmd-btn cmd-btn-session" @click="$emit('decide', { cmdId: cmd.cmdId, decision: '2' })">
        <span>⏰</span><span>本会话内允许</span>
      </button>
      <button class="cmd-btn cmd-btn-reject" @click="$emit('decide', { cmdId: cmd.cmdId, decision: '3' })">
        <span>✕</span><span>拒绝</span>
      </button>
    </div>
    <div v-else class="cmd-result">
      <details class="result-block">
        <summary>
          <span class="result-status" :class="statusType">{{ statusLabel }}</span>
          <span v-if="cmd.execResult" class="result-summary-text">{{ execResultPreview }}</span>
          <svg class="result-chevron" viewBox="0 0 16 16" width="10" height="10">
            <path d="M3 6 L8 11 L13 6" stroke="currentColor" stroke-width="1.5" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </summary>
        <pre v-if="cmd.execResult" class="result-body">{{ cmd.execResult }}</pre>
      </details>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CommandCard',
  props: { cmd: Object, idx: Number, hosts: { type: Array, default: () => [] } },
  computed: {
    targetHostIds() {
      const ids = (this.cmd.targetHostIds || '').split(',').filter(Boolean)
      return ids
    },
    verbLabel() {
      const text = (this.cmd.cmdText || '').trim()
      const verb = text.split(/\s+/)[0] || ''
      return verb.toUpperCase() || 'COMMAND'
    },
    statusType() {
      return { '1': 'running', '2': 'success', '3': 'partial', '4': 'failed', '5': 'timeout', '6': 'rejected' }[this.cmd.execStatus] || ''
    },
    statusLabel() {
      return { '1': '执行中', '2': '成功', '3': '部分成功', '4': '失败', '5': '超时', '6': '已拒绝' }[this.cmd.execStatus] || '已完成'
    },
    execResultPreview() {
      // 把 execResult 第一行作为 summary 默认文字（折叠时也能看到关键信息）
      const t = this.cmd.execResult || ''
      if (!t) return ''
      const firstLine = t.split('\n').find(l => l.trim()) || t
      return firstLine.length > 80 ? firstLine.slice(0, 80) + '…' : firstLine
    }
  },
  methods: {
    hostNameOf(id) {
      const h = this.hosts.find(x => x.hostId === id)
      return h ? `${h.hostName} (${h.ip})` : id
    }
  }
}
</script>

<style scoped>
.command-card { background: #fff; border: 1px solid var(--ai-border); border-radius: var(--ai-radius-md); margin: 12px 0; overflow: hidden; transition: border-color .15s, box-shadow .15s; box-shadow: 0 1px 3px rgba(0,0,0,0.04); max-width: 78%; }
.command-card:hover { border-color: var(--ai-border-strong); box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.command-card.high-risk { border-color: rgba(239, 68, 68, 0.4); }
.command-card.resolved { opacity: .9; }
.cmd-slot { display: flex; }

.cmd-header { display: flex; justify-content: space-between; align-items: center; padding: 10px 14px; background: var(--ai-bg-elevated); border-bottom: 1px solid var(--ai-border); }
.cmd-header-left { display: flex; align-items: center; gap: 10px; }
.cmd-header-right { display: flex; align-items: center; gap: 8px; }
.cmd-index { font-family: var(--ai-font-mono); color: var(--ai-accent); font-size: 12px; font-weight: 700; letter-spacing: 0.05em; }
.cmd-type { font-family: var(--ai-font-mono); color: var(--ai-text-secondary); font-size: 10px; letter-spacing: 0.12em; padding: 2px 8px; background: #fff; border: 1px solid var(--ai-border); border-radius: 3px; }
.cmd-warning { display: inline-flex; align-items: center; gap: 4px; color: var(--ai-danger); font-size: 11px; font-weight: 600; padding: 2px 8px; background: rgba(239, 68, 68, 0.08); border: 1px solid rgba(239, 68, 68, 0.3); border-radius: 3px; }
.warn-icon { font-size: 12px; }
.cmd-id { font-family: var(--ai-font-mono); color: var(--ai-text-muted); font-size: 10px; letter-spacing: 0.05em; }

.cmd-target { display: flex; flex-wrap: wrap; align-items: center; gap: 6px; padding: 8px 14px; border-bottom: 1px solid var(--ai-border); background: #fafbfc; }
.target-label { font-family: var(--ai-font-mono); color: var(--ai-text-muted); font-size: 10px; letter-spacing: 0.12em; }
.target-host { display: inline-flex; align-items: center; gap: 4px; padding: 2px 8px; background: #fff; border: 1px solid var(--ai-border); border-radius: 4px; font-size: 11px; color: var(--ai-text-secondary); font-family: var(--ai-font-mono); }
.target-dot { width: 5px; height: 5px; border-radius: 50%; background: var(--ai-success); box-shadow: 0 0 4px rgba(16,185,129,0.5); }

.terminal { background: #1e293b; }
.terminal-bar { display: flex; align-items: center; gap: 6px; padding: 6px 14px; border-bottom: 1px solid rgba(255,255,255,0.05); }
.terminal-dot { width: 8px; height: 8px; border-radius: 50%; }
.terminal-dot:nth-child(1) { background: #ff5f57; }
.terminal-dot:nth-child(2) { background: #febc2e; }
.terminal-dot:nth-child(3) { background: #28c840; }
.terminal-prompt { margin-left: 8px; color: rgba(255,255,255,0.5); font-size: 10px; font-family: var(--ai-font-mono); letter-spacing: 0.05em; }
.terminal-body { padding: 12px 14px; margin: 0; font-family: var(--ai-font-mono); font-size: 12.5px; line-height: 1.5; color: #e2e8f0; white-space: pre-wrap; word-break: break-all; }
.prompt { color: #fbbf24; user-select: none; }
.cmd-text { color: #e2e8f0; }

.cmd-decision { display: flex; gap: 8px; padding: 10px 14px; background: #fafbfc; border-top: 1px solid var(--ai-border); }
.cmd-decision.whitelisted { color: var(--ai-text-secondary); font-size: 12px; align-items: center; }
.decision-icon { font-size: 14px; color: var(--ai-warning); margin-right: 4px; }
.cmd-btn { display: inline-flex; align-items: center; gap: 5px; padding: 6px 14px; background: #fff; border: 1px solid var(--ai-border-strong); border-radius: 6px; color: var(--ai-text-secondary); font-size: 12px; font-weight: 500; cursor: pointer; transition: all .12s; font-family: inherit; }
.cmd-btn:hover { color: var(--ai-text-primary); border-color: var(--ai-text-secondary); }
.cmd-btn span:first-child { font-size: 13px; }
.cmd-btn-allow { color: var(--ai-success); border-color: rgba(16, 185, 129, 0.4); background: rgba(16, 185, 129, 0.04); }
.cmd-btn-allow:hover { background: rgba(16, 185, 129, 0.10); border-color: var(--ai-success); }
.cmd-btn-session { color: #b45309; border-color: rgba(180, 83, 9, 0.4); background: rgba(245, 158, 11, 0.04); }
.cmd-btn-session:hover { background: rgba(245, 158, 11, 0.10); border-color: #b45309; }
.cmd-btn-reject { color: var(--ai-danger); border-color: rgba(239, 68, 68, 0.4); background: rgba(239, 68, 68, 0.04); }
.cmd-btn-reject:hover { background: rgba(239, 68, 68, 0.10); border-color: var(--ai-danger); }

.cmd-result { padding: 10px 14px; background: #fafbfc; border-top: 1px solid var(--ai-border); }
.result-block { border-radius: 6px; overflow: hidden; }
.result-block > summary { display: flex; align-items: center; gap: 10px; padding: 4px 8px; cursor: pointer; list-style: none; user-select: none; }
.result-block > summary::-webkit-details-marker { display: none; }
.result-block > summary::marker { content: ''; }
.result-block > summary:hover { background: rgba(0,0,0,0.02); border-radius: 4px; }
.result-summary-text { color: var(--ai-text-muted); font-size: 11px; font-family: var(--ai-font-mono); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; flex: 1; }
.result-chevron { color: var(--ai-text-muted); transition: transform .15s; flex-shrink: 0; }
.result-block[open] .result-chevron { transform: rotate(180deg); }
.result-block[open] > summary { margin-bottom: 8px; }
.result-status { display: inline-block; padding: 2px 10px; border-radius: 3px; font-size: 11px; font-weight: 600; letter-spacing: 0.05em; font-family: var(--ai-font-mono); flex-shrink: 0; }
.result-status.running { background: rgba(59, 130, 246, 0.10); color: var(--ai-info); }
.result-status.success { background: rgba(16, 185, 129, 0.10); color: #047857; }
.result-status.partial { background: rgba(245, 158, 11, 0.10); color: #b45309; }
.result-status.failed, .result-status.timeout { background: rgba(239, 68, 68, 0.10); color: var(--ai-danger); }
.result-status.rejected { background: rgba(91, 100, 120, 0.15); color: var(--ai-text-muted); }
.result-body { background: #1e293b; color: #e2e8f0; padding: 10px 12px; border-radius: 6px; font-size: 12px; font-family: var(--ai-font-mono); white-space: pre-wrap; word-break: break-all; max-height: 240px; overflow-y: auto; margin: 0; }
</style>
