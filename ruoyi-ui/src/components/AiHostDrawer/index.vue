<template>
  <el-drawer
    :visible.sync="drawer"
    direction="rtl"
    size="720px"
    :wrapper-closable="true"
    :modal="true"
    :modal-append-to-body="true"
    :append-to-body="false"
    :destroy-on-close="false"
    :modal-class="'ai-host-drawer-modal'"
    :with-header="false"
    @closed="onClosed"
  >
    <div class="ai-drawer">
      <header class="drawer-header">
        <div class="drawer-header-left">
          <div class="drawer-icon">
            <span>▦</span>
          </div>
          <div class="drawer-title-block">
            <h2 class="drawer-title">主机管理</h2>
            <p class="drawer-subtitle">SSH 连接信息 · 仅本账号可见</p>
          </div>
        </div>
        <div class="drawer-header-right">
          <div class="stat-pills">
            <span class="stat-pill stat-on"><i /> {{ stats.on }} 在线</span>
            <span class="stat-pill stat-off"><i /> {{ stats.off }} 离线</span>
            <span class="stat-pill stat-unknown"><i /> {{ stats.unknown }} 未知</span>
          </div>
          <button class="drawer-close" @click="drawer = false">×</button>
        </div>
      </header>

      <div class="drawer-body">
        <div class="filter-bar">
          <el-input v-model="query.hostName" placeholder="主机名" clearable size="small" class="ai-input-dark" />
          <el-input v-model="query.ip" placeholder="IP 地址" clearable size="small" class="ai-input-dark" />
          <button class="ai-btn-ghost" @click="getList">
            <span>⌕</span><span>查询</span>
          </button>
          <button class="ai-btn-ghost" @click="resetQuery">
            <span>↺</span><span>重置</span>
          </button>
          <div class="filter-bar-right">
            <button class="ai-btn-ghost" @click="getList">
              <span>⟳</span><span>刷新</span>
            </button>
            <button class="ai-btn-ghost" :disabled="selected.length===0" @click="handleDelete(null)" v-hasPermi="['ai:host:remove']">
              <span>🗑</span><span>批量删除</span>
            </button>
            <button class="ai-btn-primary-sm" @click="handleAdd" v-hasPermi="['ai:host:add']">
              <span>＋</span><span>新增主机</span>
            </button>
          </div>
        </div>

        <div class="host-grid" v-loading="loading">
          <div
            v-for="h in list"
            :key="h.hostId"
            class="host-card"
            :class="['status-' + hostStatusClass(h.status), { 'is-selected': selected.includes(h.hostId) }]"
          >
            <div class="host-card-head">
              <el-checkbox :value="selected.includes(h.hostId)" @change="toggleSelect(h.hostId)" class="host-card-check" />
              <span class="host-card-dot" />
              <div class="host-card-title">
                <div class="host-card-name">{{ h.hostName }}</div>
                <div class="host-card-id">#{{ h.hostId }}</div>
              </div>
              <span class="status-tag" :class="hostStatusClass(h.status)">
                <i />{{ statusMap[h.status] }}
              </span>
            </div>
            <div class="host-card-meta">
              <div class="meta-row">
                <span class="meta-key">IP</span>
                <span class="meta-val mono">{{ h.ip }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-key">PORT</span>
                <span class="meta-val mono">{{ h.sshPort }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-key">USER</span>
                <span class="meta-val mono">{{ h.username }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-key">AUTH</span>
                <span class="auth-badge" :class="h.authType==='0' ? 'pwd' : 'key'">
                  <span class="auth-icon">{{ h.authType==='0' ? '⚿' : '⚷' }}</span>
                  <span>{{ h.authType==='0' ? '口令' : '私钥' }}</span>
                </span>
              </div>
            </div>
            <div class="host-card-actions">
              <button class="card-action" @click="handleEdit(h)" v-hasPermi="['ai:host:edit']">编辑</button>
              <span class="action-divider" />
              <button class="card-action card-action-test" @click="handleTest(h)" v-hasPermi="['ai:host:test']">⚡ 测试</button>
              <span class="action-divider" />
              <button class="card-action card-action-danger" @click="handleDelete(h)" v-hasPermi="['ai:host:remove']">删除</button>
            </div>
          </div>
          <div v-if="!loading && !list.length" class="host-empty">
            <div class="host-empty-icon">▢</div>
            <div class="host-empty-text">{{ hasFilter ? '没有匹配的主机' : '还没有主机，点右上角「新增主机」开始' }}</div>
          </div>
        </div>
      </div>

      <!-- 编辑/新增 弹窗 -->
      <el-dialog
        :title="formTitle"
        :visible.sync="formOpen"
        width="640px"
        :close-on-click-modal="false"
        :append-to-body="true"
        :modal-append-to-body="true"
        :lock-scroll="false"
        @close="resetForm"
        custom-class="ai-dialog"
      >
        <el-form ref="form" :model="form" :rules="rules" label-width="100px" size="small">
          <el-form-item label="主机名" prop="hostName">
            <el-input v-model="form.hostName" placeholder="例：prod-web-01" />
          </el-form-item>
          <div class="form-row">
            <el-form-item label="IP 地址" prop="ip">
              <el-input v-model="form.ip" placeholder="IPv4 或 IPv6" />
            </el-form-item>
            <el-form-item label="SSH 端口">
              <el-input-number v-model="form.sshPort" :min="1" :max="65535" controls-position="right" />
            </el-form-item>
          </div>
          <div class="form-row">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" />
            </el-form-item>
            <el-form-item label="协议">
              <el-radio-group v-model="form.sshProtocol">
                <el-radio-button label="SSH2">SSH2</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </div>
          <el-form-item label="认证方式" prop="authType">
            <el-radio-group v-model="form.authType" @change="onAuthTypeChange">
              <el-radio-button label="0">口令</el-radio-button>
              <el-radio-button label="1">私钥</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="form.authType==='0'" label="口令" :prop="form.hostId ? '' : 'password'">
            <el-input v-model="form.password" type="password" show-password :placeholder="form.hostId?'留空不修改':'请输入登录口令'" />
          </el-form-item>
          <el-form-item v-if="form.authType==='1'" label="私钥" :prop="form.hostId ? '' : 'privateKey'">
            <el-input v-model="form.privateKey" type="textarea" :rows="5" :placeholder="form.hostId?'留空不修改':'粘贴 BEGIN...END 块'" />
          </el-form-item>
          <el-form-item label="所属部门" prop="deptId">
            <treeselect v-if="deptTree.length" v-model="form.deptId" :options="deptTree" :show-count="true" placeholder="选择部门" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.remark" type="textarea" :rows="2" />
          </el-form-item>
        </el-form>
        <div slot="footer" class="form-footer">
          <button class="ai-btn-ghost" @click="formOpen=false">取消</button>
          <button class="ai-btn-ghost" @click="handleTestInline">
            <span>⚡</span><span>测试连接</span>
          </button>
          <button class="ai-btn-primary-sm" :disabled="submitting" @click="submitForm">
            <span v-if="!submitting">保存</span>
            <span v-else>保存中…</span>
          </button>
        </div>
      </el-dialog>

      <!-- 测试结果弹窗 -->
      <el-dialog title="连通性测试" :visible.sync="testOpen" width="420px" :append-to-body="true" :modal-append-to-body="true" :lock-scroll="false" custom-class="ai-dialog">
        <div class="test-result" :class="testResult.success ? 'success' : 'failed'">
          <div class="test-result-icon">{{ testResult.success ? '✓' : '✕' }}</div>
          <div class="test-result-title">{{ testResult.success ? '连接成功' : '连接失败' }}</div>
          <div class="test-result-msg">{{ testResult.message }}</div>
        </div>
      </el-dialog>
    </div>
  </el-drawer>
</template>

<script>
import { listHost, addHost, updateHost, delHost, testHost, testAndSaveHost } from '@/api/ai/host'
import { deptTreeSelect } from '@/api/system/user'
import Treeselect from '@riophae/vue-treeselect'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'

export default {
  name: 'AiHostDrawer',
  props: {
    value: { type: Boolean, default: false }
  },
  data() {
    return {
      statusMap: { '0': '在线', '1': '离线', '2': '未知' },
      loading: false, submitting: false, drawer: false, formOpen: false, testOpen: false,
      testResult: { success: false, message: '' },
      query: { hostName: '', ip: '' },
      list: [], selected: [],
      formTitle: '',
      form: this.initForm(),
      deptTree: [],
      rules: {
        hostName: [{ required: true, message: '请输入主机名', trigger: 'blur' }],
        ip: [{ required: true, message: '请输入 IP', trigger: 'blur' }],
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        authType: [{ required: true, message: '请选择认证方式', trigger: 'change' }],
        password: [{ required: true, message: '请输入口令', trigger: 'blur' }],
        privateKey: [{ required: true, message: '请输入私钥', trigger: 'blur' }],
        deptId: [{ required: true, message: '请选择部门', trigger: 'change' }]
      }
    }
  },
  computed: {
    stats() {
      const on = this.list.filter(h => h.status === '0').length
      const off = this.list.filter(h => h.status === '1').length
      const unknown = this.list.filter(h => h.status === '2' || h.status == null).length
      return { on, off, unknown }
    },
    hasFilter() {
      return !!(this.query.hostName || this.query.ip)
    }
  },
  components: { Treeselect },
  watch: {
    value(v) { this.drawer = v; if (v) this.getList() }
  },
  methods: {
    initForm() {
      return { hostId: null, hostName: '', ip: '', sshPort: 22, sshProtocol: 'SSH2', username: '', authType: '0', password: '', privateKey: '', deptId: null, remark: '' }
    },
    hostStatusClass(status) {
      return status === '0' ? 'on' : (status === '1' ? 'off' : 'unknown')
    },
    toggleSelect(hostId) {
      const idx = this.selected.indexOf(hostId)
      if (idx >= 0) this.selected.splice(idx, 1)
      else this.selected.push(hostId)
    },
    onAuthTypeChange() {
      this.form.password = ''
      this.form.privateKey = ''
    },
    getList() {
      this.loading = true
      listHost({ ...this.query, pageNum: 1, pageSize: 100 }).then(r => { this.list = r.rows || []; this.loading = false })
    },
    resetQuery() { this.query = { hostName: '', ip: '' }; this.getList() },
    handleAdd() {
      this.formTitle = '新增主机'; this.form = this.initForm()
      this.loadDeptTree(); this.formOpen = true
    },
    handleEdit(row) {
      this.formTitle = '编辑主机'
      this.form = { ...row, password: '', privateKey: '' }
      this.loadDeptTree(); this.formOpen = true
    },
    handleTest(row) {
      testAndSaveHost(row.hostId).then(r => { this.testResult = r.data || { success: false, message: '无响应' }; this.testOpen = true; this.getList() })
    },
    handleTestInline() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.submitting = true
        testHost(this.form).then(r => { this.submitting = false; this.testResult = r.data || { success: false, message: '无响应' }; this.testOpen = true })
          .catch(() => { this.submitting = false })
      })
    },
    handleDelete(row) {
      const ids = row && row.hostId ? [row.hostId] : this.selected
      if (!ids.length) return
      this.$modal.confirm('确认删除选中主机？').then(() => delHost(ids.join(',')))
        .then(() => { this.$modal.msgSuccess('删除成功'); this.getList() }).catch(() => {})
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.submitting = true
        const op = this.form.hostId ? updateHost(this.form) : addHost(this.form)
        op.then(() => { this.submitting = false; this.$modal.msgSuccess(this.form.hostId ? '修改成功' : '新增成功'); this.formOpen = false; this.getList() })
          .catch(() => { this.submitting = false })
      })
    },
    loadDeptTree() {
      if (this.deptTree.length) return
      deptTreeSelect().then(r => {
        // /system/user/deptTree 端点已返回 treeselect 兼容格式 { id, label, children }，直接使用
        this.deptTree = r.data || []
      })
    },
    onClosed() { this.$emit('input', false); this.query = { hostName: '', ip: '' } },
    resetForm() { this.form = this.initForm() }
  }
}
</script>

<style>
/* 全局：覆盖 Element UI 弹窗/抽屉默认样式，融入 light 主题 */
.ai-host-drawer-modal { background: rgba(0, 0, 0, 0.45) !important; backdrop-filter: blur(2px); }
.ai-dialog { background: #ffffff !important; border: 1px solid #ebeef5; border-radius: 12px !important; overflow: hidden; box-shadow: 0 12px 32px rgba(0,0,0,0.12) !important; }
.ai-dialog__title { color: #1f2329 !important; font-weight: 600; padding: 16px 20px 0 !important; }
.ai-dialog__body { padding: 16px 24px !important; color: #4e5969 !important; }
.ai-dialog__footer { padding: 12px 20px 16px !important; }

/* 表单元素 light 适配（弹窗被 teleport 到 body，必须全局覆盖） */
.ai-dialog .el-form-item__label { color: #4e5969 !important; font-size: 12px; letter-spacing: 0.02em; }
.ai-dialog .el-input__inner,
.ai-dialog .el-textarea__inner { background: #fff !important; border-color: #dcdfe6 !important; color: #1f2329 !important; caret-color: #f59e0b; }
.ai-dialog .el-input__inner::placeholder,
.ai-dialog .el-textarea__inner::placeholder { color: #86909c !important; }
.ai-dialog .el-input__inner:focus,
.ai-dialog .el-textarea__inner:focus { border-color: #f59e0b !important; box-shadow: 0 0 0 2px rgba(245,158,11,0.15) !important; }
.ai-dialog .el-input__prefix .el-input__icon,
.ai-dialog .el-input__suffix .el-input__icon { color: #86909c; }
.ai-dialog .el-input-number__decrease,
.ai-dialog .el-input-number__increase { background: #f7f8fa !important; color: #4e5969 !important; border-color: #dcdfe6 !important; }
.ai-dialog .el-input-number__decrease:hover,
.ai-dialog .el-input-number__increase:hover { color: #f59e0b !important; }
.ai-dialog .el-input-number .el-input__inner { padding-left: 8px; padding-right: 8px; }
.ai-dialog .el-radio-button__inner { background: #fff !important; border-color: #dcdfe6 !important; color: #4e5969 !important; }
.ai-dialog .el-radio-button__orig-radio:checked + .el-radio-button__inner { background: rgba(245,158,11,0.10) !important; border-color: #f59e0b !important; color: #f59e0b !important; box-shadow: -1px 0 0 0 #f59e0b !important; }
.ai-dialog .el-radio__label { color: #4e5969 !important; padding-left: 6px; }
.ai-dialog .el-radio__input.is-checked .el-radio__inner { background: #f59e0b !important; border-color: #f59e0b !important; }
.ai-dialog .el-radio__input.is-checked + .el-radio__label { color: #f59e0b !important; }
.ai-dialog .vue-treeselect__control { background: #fff !important; border-color: #dcdfe6 !important; }
.ai-dialog .vue-treeselect__value-container,
.ai-dialog .vue-treeselect__placeholder { color: #86909c !important; }
.ai-dialog .vue-treeselect__single-value { color: #1f2329 !important; }
.ai-dialog .el-form-item.is-required:not(.is-no-asterisk) .el-form-item__label-wrap > .el-form-item__label::before,
.ai-dialog .el-form-item.is-required:not(.is-no-asterisk) > .el-form-item__label::before { color: #ef4444 !important; }
.ai-dialog .el-form-item__error { color: #ef4444 !important; }
.ai-dialog .el-dialog__close { color: #86909c !important; }
.ai-dialog .el-dialog__close:hover { color: #f59e0b !important; }
</style>

<style scoped>
.ai-drawer { display: flex; flex-direction: column; height: 100%; background: #ffffff; color: #1f2329; font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", sans-serif; }

/* Header */
.drawer-header { display: flex; align-items: center; justify-content: space-between; padding: 20px 24px; border-bottom: 1px solid #ebeef5; background: #ffffff; }
.drawer-header-left { display: flex; align-items: center; gap: 14px; }
.drawer-icon { width: 44px; height: 44px; border-radius: 10px; background: linear-gradient(135deg, #f59e0b, #d97706); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 20px; font-weight: 700; box-shadow: 0 4px 12px rgba(245,158,11,0.25); }
.drawer-title-block { }
.drawer-title { margin: 0; font-size: 16px; font-weight: 600; color: #1f2329; letter-spacing: 0.02em; }
.drawer-subtitle { margin: 2px 0 0 0; font-size: 11px; color: #86909c; letter-spacing: 0.05em; }
.drawer-header-right { display: flex; align-items: center; gap: 12px; }
.stat-pills { display: flex; gap: 6px; }
.stat-pill { display: inline-flex; align-items: center; gap: 5px; padding: 4px 10px; border-radius: 999px; font-size: 11px; font-family: "SF Mono", "JetBrains Mono", Consolas, monospace; font-weight: 500; letter-spacing: 0.05em; }
.stat-pill i { width: 6px; height: 6px; border-radius: 50%; }
.stat-pill.stat-on { background: rgba(16,185,129,0.10); color: #047857; }
.stat-pill.stat-on i { background: #10b981; }
.stat-pill.stat-off { background: rgba(239,68,68,0.10); color: #b91c1c; }
.stat-pill.stat-off i { background: #ef4444; }
.stat-pill.stat-unknown { background: #f1f3f5; color: #86909c; }
.stat-pill.stat-unknown i { background: #86909c; }
.drawer-close { width: 32px; height: 32px; background: transparent; border: 1px solid #ebeef5; border-radius: 6px; color: #86909c; font-size: 18px; cursor: pointer; transition: all .12s; }
.drawer-close:hover { color: #1f2329; border-color: #dcdfe6; background: #f7f8fa; }

/* Body */
.drawer-body { flex: 1; padding: 18px 24px 24px; overflow: hidden; display: flex; flex-direction: column; }

.filter-bar { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; margin-bottom: 14px; }
.filter-bar-right { margin-left: auto; display: flex; gap: 6px; }
.ai-input-dark { width: 160px; }
.ai-input-dark >>> .el-input__inner { background: #fff !important; border-color: #dcdfe6 !important; color: #1f2329 !important; font-size: 12px; }
.ai-input-dark >>> .el-input__inner::placeholder { color: #86909c; }
.ai-input-dark >>> .el-input__inner:focus { border-color: #f59e0b !important; }

/* Buttons */
.ai-btn-ghost { display: inline-flex; align-items: center; gap: 5px; padding: 7px 12px; background: #fff; border: 1px solid #dcdfe6; border-radius: 6px; color: #4e5969; font-size: 12px; cursor: pointer; transition: all .12s; font-family: inherit; }
.ai-btn-ghost:hover:not(:disabled) { color: #1f2329; border-color: #b3b6bb; background: #f7f8fa; }
.ai-btn-ghost:disabled { opacity: .4; cursor: not-allowed; }
.ai-btn-ghost span:first-child { font-size: 13px; }
.ai-btn-primary-sm { display: inline-flex; align-items: center; gap: 5px; padding: 7px 14px; background: linear-gradient(135deg, #f59e0b, #d97706); color: #fff; border: none; border-radius: 6px; font-size: 12px; font-weight: 600; cursor: pointer; transition: all .12s; font-family: inherit; }
.ai-btn-primary-sm:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(245,158,11,0.30); }
.ai-btn-primary-sm:disabled { opacity: .5; cursor: not-allowed; }

/* Card Grid */
.host-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 12px; flex: 1; overflow-y: auto; padding: 4px 4px 8px; align-content: start; }
.host-card { background: #fff; border: 1px solid #ebeef5; border-radius: 10px; padding: 12px 14px; display: flex; flex-direction: column; gap: 10px; transition: border-color .15s, box-shadow .15s, transform .15s; position: relative; }
.host-card:hover { border-color: rgba(245,158,11,0.5); box-shadow: 0 4px 12px rgba(0,0,0,0.05); transform: translateY(-1px); }
.host-card.is-selected { border-color: #f59e0b; box-shadow: 0 0 0 2px rgba(245,158,11,0.15); }
.host-card.status-on::before { content: ''; position: absolute; left: 0; top: 14px; bottom: 14px; width: 3px; background: #10b981; border-radius: 0 2px 2px 0; }
.host-card.status-off::before { content: ''; position: absolute; left: 0; top: 14px; bottom: 14px; width: 3px; background: #ef4444; border-radius: 0 2px 2px 0; }
.host-card.status-unknown::before { content: ''; position: absolute; left: 0; top: 14px; bottom: 14px; width: 3px; background: #dcdfe6; border-radius: 0 2px 2px 0; }

.host-card-head { display: flex; align-items: center; gap: 8px; }
.host-card-check >>> .el-checkbox__inner { background: #fff; border-color: #dcdfe6; }
.host-card-check >>> .el-checkbox__input.is-checked .el-checkbox__inner { background: #f59e0b; border-color: #f59e0b; }
.host-card-check >>> .el-checkbox__input.is-checked + .el-checkbox__label { color: #f59e0b; }
.host-card-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.host-card.status-on .host-card-dot { background: #10b981; box-shadow: 0 0 6px rgba(16,185,129,0.5); animation: pulse 2s ease-in-out infinite; }
.host-card.status-off .host-card-dot { background: #ef4444; }
.host-card.status-unknown .host-card-dot { background: #86909c; }
.host-card-title { flex: 1; min-width: 0; }
.host-card-name { font-size: 14px; font-weight: 600; color: #1f2329; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.host-card-id { font-size: 10px; color: #86909c; font-family: "SF Mono", Consolas, monospace; margin-top: 1px; }

.status-tag { display: inline-flex; align-items: center; gap: 4px; padding: 2px 8px; border-radius: 4px; font-size: 10px; font-weight: 600; letter-spacing: 0.05em; font-family: "SF Mono", Consolas, monospace; flex-shrink: 0; }
.status-tag i { width: 5px; height: 5px; border-radius: 50%; }
.status-tag.on { background: rgba(16,185,129,0.10); color: #047857; }
.status-tag.on i { background: #10b981; }
.status-tag.off { background: rgba(239,68,68,0.10); color: #b91c1c; }
.status-tag.off i { background: #ef4444; }
.status-tag.unknown { background: #f1f3f5; color: #86909c; }
.status-tag.unknown i { background: #86909c; }

.host-card-meta { display: flex; flex-direction: column; gap: 4px; padding: 8px 0; border-top: 1px dashed #ebeef5; border-bottom: 1px dashed #ebeef5; }
.meta-row { display: flex; justify-content: space-between; align-items: center; font-size: 12px; }
.meta-key { color: #86909c; font-size: 10px; letter-spacing: 0.1em; font-family: "SF Mono", Consolas, monospace; }
.meta-val { color: #1f2329; }
.meta-val.mono { font-family: "SF Mono", "JetBrains Mono", Consolas, monospace; }

.auth-badge { display: inline-flex; align-items: center; gap: 3px; padding: 1px 8px; border-radius: 4px; font-size: 11px; font-family: "SF Mono", Consolas, monospace; }
.auth-badge.pwd { background: rgba(59,130,246,0.08); color: #1d4ed8; }
.auth-badge.key { background: rgba(245,158,11,0.10); color: #b45309; }
.auth-icon { font-size: 11px; }

.host-card-actions { display: flex; align-items: center; gap: 0; }
.card-action { background: transparent; border: none; color: #4e5969; font-size: 12px; cursor: pointer; padding: 4px 10px; transition: color .12s, background .12s; font-family: inherit; border-radius: 4px; }
.card-action:hover { color: #1f2329; background: #f7f8fa; }
.action-divider { width: 1px; height: 12px; background: #ebeef5; }
.card-action-test { color: #047857; }
.card-action-test:hover { color: #047857; background: rgba(16,185,129,0.08); }
.card-action-danger { color: #b91c1c; }
.card-action-danger:hover { color: #b91c1c; background: rgba(239,68,68,0.08); }

@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: .4; } }

/* Empty state */
.host-empty { grid-column: 1 / -1; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 60px 20px; color: #86909c; }
.host-empty-icon { font-size: 48px; color: #dcdfe6; font-family: "SF Mono", Consolas, monospace; }
.host-empty-text { font-size: 13px; margin-top: 8px; }

/* Form */
.form-row { display: flex; gap: 16px; }
.form-row >>> .el-form-item { flex: 1; }
.form-footer { display: flex; justify-content: flex-end; gap: 8px; }

/* Test result */
.test-result { display: flex; flex-direction: column; align-items: center; padding: 24px 16px; text-align: center; }
.test-result-icon { width: 56px; height: 56px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 28px; font-weight: 700; margin-bottom: 12px; }
.test-result.success .test-result-icon { background: rgba(16,185,129,0.10); color: #047857; }
.test-result.failed .test-result-icon { background: rgba(239,68,68,0.10); color: #b91c1c; }
.test-result-title { font-size: 16px; font-weight: 600; color: #1f2329; margin-bottom: 6px; }
.test-result-msg { color: #4e5969; font-size: 12px; max-width: 320px; word-break: break-all; }
</style>
