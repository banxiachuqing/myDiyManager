<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="厂商名称" prop="vendorName">
        <el-input v-model="queryParams.vendorName" placeholder="请输入厂商名称" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 140px">
          <el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['ai:vendor:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="selected.length===0" @click="handleDelete" v-hasPermi="['ai:vendor:remove']">批量删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-refresh" size="mini" @click="getList">刷新</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column label="编号" prop="vendorId" width="80" />
      <el-table-column label="厂商" prop="vendorName" />
      <el-table-column label="Base URL" prop="baseUrl" show-overflow-tooltip />
      <el-table-column label="模型" prop="modelName" />
      <el-table-column label="API Key" prop="apiKeyMask" width="160" />
      <el-table-column label="状态" prop="status" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status==='0'?'success':'info'">{{ statusOptions.find(o=>o.value===scope.row.status).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="默认" prop="isDefault" width="80">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.isDefault==='1'" type="warning">是</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" @click="handleEdit(scope.row)" v-hasPermi="['ai:vendor:edit']">编辑</el-button>
          <el-button size="mini" type="text" @click="handleTest(scope.row)" v-hasPermi="['ai:vendor:test']">测试</el-button>
          <el-button v-if="scope.row.isDefault!=='1'" size="mini" type="text" @click="handleSetDefault(scope.row)" v-hasPermi="['ai:vendor:default']">设为默认</el-button>
          <el-button size="mini" type="text" style="color:#f56c6c" @click="handleDelete(scope.row)" v-hasPermi="['ai:vendor:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialogTitle" :visible.sync="dialogOpen" width="600px" append-to-body :close-on-click-modal="false" @close="cancel">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="厂商名称" prop="vendorName"><el-input v-model="form.vendorName" /></el-form-item>
        <el-form-item label="Base URL" prop="baseUrl"><el-input v-model="form.baseUrl" /></el-form-item>
        <el-form-item label="API Key" prop="apiKey">
          <el-input v-model="form.apiKey" :type="showKey?'text':'password'" :placeholder="form.vendorId?'留空不修改':'请输入 API Key'" />
          <el-checkbox v-model="showKey" style="margin-top:4px">显示明文</el-checkbox>
        </el-form-item>
        <el-form-item label="模型名" prop="modelName"><el-input v-model="form.modelName" /></el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="o in statusOptions" :key="o.value" :label="o.value">{{ o.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="设为默认" prop="isDefault">
          <el-radio-group v-model="form.isDefault">
            <el-radio v-for="o in isDefaultOptions" :key="o.value" :label="o.value">{{ o.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="超时(秒)"><el-input-number v-model="form.timeoutSec" :min="5" :max="300" /></el-form-item>
        <el-form-item label="最大 Token"><el-input-number v-model="form.maxTokens" :min="1" :max="32768" /></el-form-item>
        <el-form-item label="温度"><el-input-number v-model="form.temperature" :min="0" :max="2" :step="0.1" :precision="1" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button @click="handleTestInline">测 试</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保 存</el-button>
      </div>
    </el-dialog>

    <el-dialog title="连通性测试" :visible.sync="testOpen" width="500px" append-to-body>
      <el-result :icon="testResult.success?'success':'error'" :title="testResult.success?'连接成功':'连接失败'" :subTitle="testResult.message"></el-result>
    </el-dialog>
  </div>
</template>

<script>
import { listVendor, addVendor, updateVendor, delVendor, setDefaultVendor, testVendor, testAndSaveVendor } from '@/api/ai/vendor'
import { statusOptions, isDefaultOptions } from './data'

export default {
  name: 'AiVendor',
  data() {
    return {
      statusOptions, isDefaultOptions,
      showSearch: true, loading: false, submitting: false,
      dataList: [], selected: [], total: 0,
      queryParams: { pageNum: 1, pageSize: 10, vendorName: '', status: '' },
      dialogTitle: '', dialogOpen: false, testOpen: false,
      testResult: { success: false, message: '' },
      form: this.initForm(),
      showKey: false,
      rules: {
        vendorName: [{ required: true, message: '请输入厂商名称', trigger: 'blur' }],
        baseUrl:    [{ required: true, message: '请输入 Base URL', trigger: 'blur' }],
        modelName:  [{ required: true, message: '请输入模型名', trigger: 'blur' }],
        status:     [{ required: true, message: '请选择状态', trigger: 'change' }],
        isDefault:  [{ required: true, message: '请选择是否默认', trigger: 'change' }]
      }
    }
  },
  watch: {
    dialogOpen(v) { if (!v) this.form = this.initForm() }
  },
  created() { this.getList() },
  methods: {
    initForm() { return { vendorId: null, vendorName: '', baseUrl: '', apiKey: '', modelName: '', status: '0', isDefault: '0', timeoutSec: 60, maxTokens: 2048, temperature: 0.7, remark: '' } },
    getList() {
      this.loading = true
      listVendor(this.queryParams).then(r => { this.dataList = r.rows; this.total = r.total; this.loading = false })
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.queryParams = { pageNum: 1, pageSize: 10, vendorName: '', status: '' }; this.getList() },
    handleSelectionChange(rows) { this.selected = rows.map(r => r.vendorId) },
    handleAdd() { this.dialogTitle = '新增厂商'; this.form = this.initForm(); this.dialogOpen = true },
    handleEdit(row) { this.dialogTitle = '编辑厂商'; this.form = Object.assign({}, row, { apiKey: '' }); this.dialogOpen = true },
    handleSetDefault(row) {
      this.$modal.confirm('确认将 "' + row.vendorName + '" 设为默认厂商？').then(() => setDefaultVendor(row.vendorId))
        .then(() => { this.$modal.msgSuccess('设置成功'); this.getList() }).catch(() => {})
    },
    handleDelete(row) {
      const ids = row.vendorId ? [row.vendorId] : this.selected
      this.$modal.confirm('确认删除选中厂商？').then(() => delVendor(ids.join(',')))
        .then(() => { this.$modal.msgSuccess('删除成功'); this.getList() }).catch(() => {})
    },
    handleTest(row) {
      testAndSaveVendor(row.vendorId).then(r => { this.testResult = r.data || { success: false, message: '无响应' }; this.testOpen = true; this.getList() })
    },
    handleTestInline() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.submitting = true
        testVendor(this.form).then(r => { this.submitting = false; this.testResult = r.data || { success: false, message: '无响应' }; this.testOpen = true })
          .catch(() => { this.submitting = false })
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.submitting = true
        const op = this.form.vendorId ? updateVendor(this.form) : addVendor(this.form)
        op.then(() => { this.submitting = false; this.$modal.msgSuccess(this.form.vendorId ? '修改成功' : '新增成功'); this.dialogOpen = false; this.getList() })
          .catch(() => { this.submitting = false })
      })
    },
    cancel() { this.dialogOpen = false; this.form = this.initForm() }
  }
}
</script>
