<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="登陆用户名" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="请输入登陆用户名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="登录密码" prop="password">
        <el-input
          v-model="queryParams.password"
          placeholder="请输入登录密码"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="是否通知" prop="notice">
        <el-select v-model="queryParams.notice" placeholder="请选择是否通知" clearable>
          <el-option
            v-for="dict in dict.type.sys_normal_disable"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="模式" prop="model">
        <el-select v-model="queryParams.model" placeholder="请选择模式" clearable>
          <el-option
            v-for="dict in dict.type.xm_step_model"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="定时表达式" prop="cron">
        <el-input
          v-model="queryParams.cron"
          placeholder="请输入定时表达式"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="是否启用" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择是否启用" clearable>
          <el-option
            v-for="dict in dict.type.sys_normal_disable"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="步数" prop="stepCount">
        <el-input
          v-model="queryParams.stepCount"
          placeholder="请输入步数"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="步进" prop="step">
        <el-input
          v-model="queryParams.step"
          placeholder="请输入步进"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['step:config:add']"
        >新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['step:config:edit']"
        >修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['step:config:remove']"
        >删除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['step:config:export']"
        >导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="configList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="登陆用户名" align="center" prop="userName"/>
      <el-table-column label="登录密码" align="center" prop="password"/>
      <el-table-column label="是否通知" align="center" prop="notice">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.notice"/>
        </template>
      </el-table-column>
      <el-table-column label="通知管道" align="center" prop="noticeId">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.xm_step_notice" :value="scope.row.noticeId"/>
        </template>
      </el-table-column>
      <el-table-column label="模式" align="center" prop="model">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.xm_step_model" :value="scope.row.model"/>
        </template>
      </el-table-column>
      <el-table-column label="定时表达式" align="center" prop="cron"/>
      <el-table-column label="是否启用" align="center" prop="status">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="步数" align="center" prop="stepCount"/>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['step:config:edit']"
          >修改
          </el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['step:config:remove']"
          >删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改小米步数配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="登陆用户名" prop="userName">
          <el-input v-model="form.userName" placeholder="请输入登陆用户名(仅支持邮箱登录)"/>
        </el-form-item>
        <el-form-item label="登录密码" prop="password">
          <el-input v-model="form.password" placeholder="请输入登录密码"/>
        </el-form-item>
        <el-form-item label="是否通知" prop="notice">
          <el-radio-group v-model="form.notice">
            <el-radio
              v-for="dict in dict.type.sys_normal_disable"
              :key="dict.value"
              :label="parseInt(dict.value)"
            >{{ dict.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="通知管道" prop="noticeId" v-if="form.notice === 0" >
          <el-select v-model="form.noticeId" multiple placeholder="请选择通知管道">
            <el-option
              v-for="dict in dict.type.xm_step_notice"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="模式" prop="model">
          <el-radio-group v-model="form.model">
            <el-radio
              v-for="dict in dict.type.xm_step_model"
              :key="dict.value"
              :label="dict.value"
            >{{ dict.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="定时表达式" prop="cron">
          <el-input v-model="form.cron" placeholder="请输入定时表达式">
            <template slot="append">
              <el-button type="primary" @click="handleShowCron">
                生成表达式
                <i class="el-icon-time el-icon--right"></i>
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="是否启用" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in dict.type.sys_normal_disable"
              :key="dict.value"
              :label="dict.value"
            >{{ dict.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="步数" prop="stepCount">
          <el-input v-model="form.stepCount" placeholder="请输入步数"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm" :loading="subLoading">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="Cron表达式生成器" :visible.sync="openCron" append-to-body destroy-on-close class="scrollbar">
      <crontab @hide="openCron=false" @fill="crontabFill" :expression="expression"></crontab>
    </el-dialog>

  </div>
</template>

<script>
import {addConfig, delConfig, getConfig, listConfig, updateConfig,changeConfigStatus} from "@/api/step/config";
import Crontab from "@/components/Crontab/index.vue";

export default {
  name: "Config",
  components: {Crontab},
  dicts: ['xm_step_model', 'sys_normal_disable', 'xm_step_notice'],
  data() {
    return {
      // 遮罩层
      loading: true,
      subLoading: false,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: false,
      // 总条数
      total: 0,
      // 小米步数配置表格数据
      configList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      openCron: false,
      expression: "",
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userName: null,
        password: null,
        notice: null,
        noticeId: null,
        model: null,
        cron: null,
        status: null,
        stepCount: null,
        step: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        userName: [
          {required: true, message: "登录用户名不能为空", trigger: "blur"}
        ],
        password: [
          {required: true, message: "登陆密码不能为空", trigger: "blur"}
        ],
        notice: [
          {required: true, message: "必选通知", trigger: "blur"}
        ],
        model: [
          {required: true, message: "必选模式", trigger: "blur"}
        ],
        cron: [
          {required: true, message: "定时任务表达式不能为空", trigger: "blur"}
        ],
        stepCount: [
          {required: true, message: "步数不能为空", trigger: "blur"}
        ],
        status: [
          {required: true, message: "必选状态", trigger: "blur"}
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询小米步数配置列表 */
    getList() {
      this.loading = true;
      listConfig(this.queryParams).then(response => {
        this.configList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        userName: null,
        password: null,
        notice: null,
        noticeId: null,
        model: null,
        cron: null,
        status: null,
        stepCount: null,
        step: null,
        createTime: null,
        updateTime: null
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** cron表达式按钮操作 */
    handleShowCron() {
      this.expression = this.form.cron;
      this.openCron = true;
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加小米步数配置";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getConfig(id).then(response => {
        this.form = response.data;
        this.form.noticeId=response.data.noticeId?response.data.noticeId.split(","):[];
        this.open = true;
        this.title = "修改小米步数配置";
      });
    },
    // 任务状态修改
    handleStatusChange(row) {

       changeConfigStatus(row.id, row.status).then(() => {
        this.$modal.msgSuccess( "成功");
      }).catch(function() {
        row.status = row.status === "0" ? "1" : "0";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {

          var arr = this.form.noticeId
          if (Array.isArray(arr) && arr.length > 0) {
            this.form.noticeId = arr.join(',');
          }else{
            this.form.noticeId=''
          }

          this.subLoading=true;
          if (this.form.id != null) {
            updateConfig(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.subLoading=false;
              this.open = false;
              this.getList();
            });
          } else {
            addConfig(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.subLoading=false;
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除小米步数配置编号为"' + ids + '"的数据项？').then(function () {
        return delConfig(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 确定后回传值 */
    crontabFill(value) {
      this.form.cron = value;
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('step/config/export', {
        ...this.queryParams
      }, `config_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
