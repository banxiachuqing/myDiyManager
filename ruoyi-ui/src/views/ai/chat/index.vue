<template>
  <div class="app-container chat-page ai-shell">
    <header class="ai-topbar">
      <div class="ai-topbar-left">
        <div class="ai-logo">
          <span class="ai-logo-dot" />
          <span class="ai-logo-text">AURA</span>
          <span class="ai-logo-suffix">·AI 助手</span>
        </div>
        <nav class="ai-tabs">
          <button
            v-for="t in tabs"
            :key="t.key"
            class="ai-tab"
            :class="{ active: activeTab === t.key }"
            :disabled="t.perm && !hasPermi(t.perm)"
            @click="activeTab = t.key"
          >
            <span class="ai-tab-icon">{{ t.icon }}</span>
            <span class="ai-tab-label">{{ t.label }}</span>
            <span v-if="activeTab === t.key" class="ai-tab-bar" />
          </button>
        </nav>
      </div>
      <div class="ai-topbar-right">
        <span class="ai-pulse"><i /> LLM 链路就绪</span>
      </div>
    </header>

    <div class="main-layout">
      <aside class="session-sidebar">
        <div class="sidebar-header">
          <button class="ai-btn-primary" @click="newSession">
            <span>＋</span><span>新建会话</span>
          </button>
        </div>
        <div class="sidebar-list">
          <div
            v-for="s in sessions"
            :key="s.sessionId"
            class="session-item"
            :class="{ active: s.sessionId === sessionId }"
            @click="switchSession(s.sessionId)"
          >
            <div class="session-icon">
              <span style="color: var(--ai-accent);">▍</span>
            </div>
            <div class="session-body">
              <el-tooltip
                :content="s.preview || defaultTitleFor(s)"
                placement="top"
                :show-after="300"
                popper-class="ai-session-tooltip"
              >
                <div class="session-title" :title="s.preview || defaultTitleFor(s)">{{ s.preview || defaultTitleFor(s) }}</div>
              </el-tooltip>
              <div class="session-meta">
                <span class="meta-dot" />
                <span>{{ formatTime(s.lastActiveAt) }}</span>
              </div>
            </div>
            <button class="session-delete" title="删除会话" @click.stop="confirmDelete(s)">×</button>
          </div>
          <div v-if="!sessions.length" class="empty">
            <div class="empty-icon">∅</div>
            <div class="empty-text">暂无会话</div>
          </div>
        </div>
      </aside>
      <div class="tab-content">
        <ChatTab ref="chatTabRef" v-show="activeTab==='chat'" @manage="goManage" :session-id="sessionId" @session-changed="onSessionChanged" @session-created="onSessionCreated" />
        <OpsTab ref="opsTabRef" v-show="activeTab==='ops'" :session-id="sessionId" @session-changed="onSessionChanged" @session-created="onSessionCreated" />
      </div>
    </div>
  </div>
</template>

<script>
import { listSessions, deleteSession } from '@/api/ai/chat'
import ChatTab from './ChatTab'
import OpsTab from './OpsTab'

export default {
  name: 'AiChat',
  components: { ChatTab, OpsTab },
  data() {
    return {
      activeTab: 'chat',
      sessions: [],
      sessionId: null,
      creatingNew: false,  // 标记"用户主动新建"模式，loadSessions 时不自动选中
      tabs: [
        { key: 'chat', label: '智能问答', icon: '◐' },
        { key: 'ops',  label: '主机运维', icon: '◧', perm: 'ai:chat:cmd' }
      ]
    }
  },
  watch: {
    activeTab() {
      // 切 tab 时：标记"新建模式"（sidebar 不自动选第一个）+ 清空两个子组件的聊天区，
      // 让用户进入 tab 看到欢迎页而不是上一个 tab 的内容
      this.creatingNew = true
      this.sessionId = null
      this.$refs.chatTabRef && this.$refs.chatTabRef.hardReset && this.$refs.chatTabRef.hardReset()
      this.$refs.opsTabRef && this.$refs.opsTabRef.hardReset && this.$refs.opsTabRef.hardReset()
      this.loadSessions()
    }
  },
  created() { this.loadSessions() },
  methods: {
    loadSessions() {
      const tabType = this.activeTab === 'chat' ? '0' : '1'
      listSessions({ tabType }).then(r => {
        this.sessions = r.data || []
        // 仅在"非新建模式"且"sessionId 还未确定"时自动选第一个
        if (!this.creatingNew && !this.sessionId && this.sessions.length) {
          this.sessionId = this.sessions[0].sessionId
        }
        this.creatingNew = false
      })
    },
    newSession() {
      // 三重保险清空右侧：
      // 1. 标记 creatingNew 让 loadSessions 不自动选第一个
      // 2. 直接调 ref.hardReset() 同步清掉子组件数据（绕开 watch 异步时序）
      // 3. sessionId=null 触发子组件 watch sessionId 的 null 分支
      this.creatingNew = true
      this.$refs.chatTabRef && this.$refs.chatTabRef.hardReset && this.$refs.chatTabRef.hardReset()
      this.$refs.opsTabRef && this.$refs.opsTabRef.hardReset && this.$refs.opsTabRef.hardReset()
      this.sessionId = null
    },
    switchSession(id) { this.creatingNew = false; this.sessionId = id },
    onSessionCreated(id) {
      // 兜底：保留兼容（实际新代码走 onSessionChanged）
      this.creatingNew = false
      this.sessionId = id
      this.loadSessions()
    },
    onSessionChanged(sessionId) {
      // 真实 sessionId（来自后端 done 帧）
      this.creatingNew = false
      this.sessionId = sessionId
      this.loadSessions()
    },
    async confirmDelete(s) {
      try {
        await this.$confirm(`确认删除会话「${this.titleFor(s)}」？相关消息和命令也会一起删除。`, '删除会话', {
          type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消', confirmButtonClass: 'el-button--danger'
        })
      } catch (e) { return }
      try {
        await deleteSession(s.sessionId)
        this.$message.success('已删除')
        if (this.sessionId === s.sessionId) {
          // 删除的是当前会话：清空右侧
          this.creatingNew = true
          this.sessionId = null
        }
        this.loadSessions()
      } catch (e) {
        this.$message.error('删除失败：' + (e.message || '未知错误'))
      }
    },
    titleFor(s) {
      return s.preview || this.defaultTitleFor(s)
    },
    defaultTitleFor(s) {
      if (!s.activeHostIds) return '智能问答会话'
      const count = s.activeHostIds.split(',').length
      return '运维会话 · ' + count + ' 台主机'
    },
    formatTime(t) {
      if (!t) return ''
      const d = new Date(t)
      const now = new Date()
      if (d.toDateString() === now.toDateString()) return d.toTimeString().slice(0, 5)
      return (d.getMonth() + 1) + '/' + d.getDate()
    },
    hasPermi(perm) {
      // 简化版：交给后端 403 兜底，前端仅在 disabled 态使用
      return true
    },
    goManage() {
      this.$router.push('/system/vendor').catch(() => {
        this.$tab && this.$tab.openPage ? this.$tab.openPage('LLM 厂商') : null
      })
    }
  }
}
</script>

<style scoped>
/* ===== AI 模块 light editorial theme（scoped 深度选择器覆盖 Element UI） ===== */
.ai-shell {
  /* palette */
  --ai-bg-base: #ffffff;
  --ai-bg-surface: #ffffff;
  --ai-bg-elevated: #f7f8fa;
  --ai-bg-deep: #f1f3f5;
  --ai-border: #ebeef5;
  --ai-border-strong: #dcdfe6;
  --ai-text-primary: #1f2329;
  --ai-text-secondary: #4e5969;
  --ai-text-muted: #86909c;
  --ai-accent: #f59e0b;
  --ai-accent-soft: rgba(245, 158, 11, 0.10);
  --ai-accent-glow: rgba(245, 158, 11, 0.20);
  --ai-success: #10b981;
  --ai-danger: #ef4444;
  --ai-info: #3b82f6;
  --ai-warning: #f59e0b;
  /* fonts */
  --ai-font-sans: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", "Helvetica Neue", Helvetica, Arial, sans-serif;
  --ai-font-mono: "SF Mono", "JetBrains Mono", "Cascadia Code", Menlo, Consolas, Monaco, monospace;
  /* radii */
  --ai-radius-sm: 6px;
  --ai-radius-md: 10px;
  --ai-radius-lg: 14px;
}
.chat-page { padding: 0; height: calc(100vh - 60px); background: var(--ai-bg-base); color: var(--ai-text-primary); font-family: var(--ai-font-sans); display: flex; flex-direction: column; }
.chat-page >>> .el-tabs__header { display: none; }

/* ===== Topbar ===== */
.ai-topbar { display: flex; align-items: center; justify-content: space-between; padding: 14px 28px; border-bottom: 1px solid var(--ai-border); background: #ffffff; position: sticky; top: 0; z-index: 5; }
.ai-topbar-left { display: flex; align-items: center; gap: 36px; }
.ai-logo { display: flex; align-items: center; gap: 8px; font-family: var(--ai-font-mono); }
.ai-logo-dot { width: 10px; height: 10px; border-radius: 50%; background: var(--ai-accent); box-shadow: 0 0 12px var(--ai-accent-glow); animation: pulse 2s ease-in-out infinite; }
.ai-logo-text { font-weight: 700; letter-spacing: 0.18em; color: var(--ai-text-primary); font-size: 15px; }
.ai-logo-suffix { color: var(--ai-text-muted); font-size: 12px; letter-spacing: 0.1em; }
.ai-tabs { display: flex; gap: 4px; background: var(--ai-bg-elevated); border: 1px solid var(--ai-border); border-radius: 10px; padding: 4px; }
.ai-tab { position: relative; display: flex; align-items: center; gap: 8px; padding: 8px 16px; background: transparent; border: none; color: var(--ai-text-secondary); cursor: pointer; border-radius: 6px; font-size: 13px; font-family: inherit; transition: color .15s, background .15s; }
.ai-tab:hover:not(:disabled) { color: var(--ai-text-primary); background: #fff; }
.ai-tab.active { color: var(--ai-text-primary); background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.ai-tab:disabled { opacity: .4; cursor: not-allowed; }
.ai-tab-icon { font-size: 14px; color: var(--ai-accent); font-family: var(--ai-font-mono); }
.ai-tab-bar { display: none; }
.ai-topbar-right { display: flex; align-items: center; gap: 12px; }
.ai-pulse { display: inline-flex; align-items: center; gap: 8px; font-size: 12px; color: var(--ai-text-secondary); font-family: var(--ai-font-mono); letter-spacing: 0.05em; }
.ai-pulse i { width: 8px; height: 8px; border-radius: 50%; background: var(--ai-success); box-shadow: 0 0 6px rgba(16,185,129,0.5); animation: pulse 1.5s ease-in-out infinite; }
@keyframes pulse { 0%, 100% { opacity: 1; transform: scale(1); } 50% { opacity: .5; transform: scale(.85); } }

/* ===== Layout ===== */
.main-layout { display: flex; flex: 1; min-height: 0; margin: 0; width: 100%; overflow: hidden; }
.tab-content { flex: 1; display: flex; min-width: 0; min-height: 0; }

/* ===== Sidebar ===== */
.session-sidebar { width: 280px; flex-shrink: 0; background: var(--ai-bg-elevated); border-right: 1px solid var(--ai-border); display: flex; flex-direction: column; }
.sidebar-header { padding: 16px; border-bottom: 1px solid var(--ai-border); background: #fff; }
.ai-btn-primary { display: flex; align-items: center; justify-content: center; gap: 6px; width: 100%; padding: 10px 14px; background: linear-gradient(135deg, var(--ai-accent), #d97706); color: #fff; border: none; border-radius: 8px; font-size: 13px; font-weight: 600; letter-spacing: 0.05em; cursor: pointer; transition: transform .12s, box-shadow .12s; font-family: inherit; }
.ai-btn-primary:hover { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(245,158,11,0.30); }
.ai-btn-primary:active { transform: translateY(0); }
.ai-btn-primary span:first-child { font-size: 16px; }
.sidebar-list { flex: 1; overflow-y: auto; padding: 8px; }
.session-item { display: flex; align-items: center; gap: 12px; padding: 10px 12px; cursor: pointer; border-radius: 8px; margin-bottom: 2px; border: 1px solid transparent; transition: background .12s, border-color .12s; background: #fff; }
.session-item + .session-item { margin-top: 6px; }
.session-item:hover { border-color: var(--ai-border-strong); }
.session-item:hover .session-delete { opacity: 1; }
.session-item.active { background: var(--ai-accent-soft); border-color: rgba(245,158,11,0.35); }
.session-delete { width: 22px; height: 22px; background: transparent; border: none; color: #c0c4cc; font-size: 16px; line-height: 1; cursor: pointer; border-radius: 4px; opacity: 0; transition: opacity .12s, color .12s, background .12s; flex-shrink: 0; padding: 0; }
.session-delete:hover { color: #f56c6c; background: rgba(245, 108, 108, 0.08); }
.session-icon { width: 32px; height: 32px; border-radius: 8px; background: var(--ai-bg-elevated); display: flex; align-items: center; justify-content: center; font-family: var(--ai-font-mono); font-size: 16px; flex-shrink: 0; color: var(--ai-accent); }
.session-item.active .session-icon { background: rgba(245,158,11,0.15); }
.session-body { flex: 1; min-width: 0; }
.session-title { font-size: 13px; color: var(--ai-text-primary); font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.session-meta { display: flex; align-items: center; gap: 6px; font-size: 11px; color: var(--ai-text-muted); margin-top: 2px; font-family: var(--ai-font-mono); }
.session-meta .meta-dot { width: 4px; height: 4px; border-radius: 50%; background: var(--ai-text-muted); }
.empty { padding: 60px 0; text-align: center; color: var(--ai-text-muted); }
.empty-icon { font-size: 32px; color: var(--ai-text-muted); font-family: var(--ai-font-mono); }
.empty-text { font-size: 13px; margin-top: 8px; }
</style>
