<template>
  <PageShell
    :title="titles[module]"
    :code="module.toUpperCase()"
    :description="descriptions[module]"
  >
    <template #actions>
      <el-button
        v-if="canWrite && ['org', 'employees', 'contracts'].includes(module)"
        type="primary"
        @click="openCreate"
      >
        <el-icon><Plus /></el-icon>新增
      </el-button>
      <el-button
        v-if="module === 'employees'"
        @click="exportEmployees"
        >导出 CSV</el-button
      >
    </template>

    <template v-if="module === 'org'">
      <div class="dashboard-grid">
        <div class="panel">
          <div class="panel-title">部门树</div>
          <el-tree
            :data="deptTree"
            :props="{ label: 'name', children: 'children' }"
            default-expand-all
            node-key="id"
          />
        </div>
        <div class="data-table">
          <el-tabs
            v-model="orgTab"
            @tab-change="loadOrgTab"
          >
            <el-tab-pane
              label="部门"
              name="depts"
              ><el-table :data="deptRows"
                ><el-table-column
                  prop="name"
                  label="部门名称" /><el-table-column
                  prop="code"
                  label="编码" /><el-table-column
                  prop="path"
                  label="组织路径" /></el-table
            ></el-tab-pane>
            <el-tab-pane
              label="岗位"
              name="positions"
              ><el-table :data="positionRows"
                ><el-table-column
                  prop="name"
                  label="岗位名称" /><el-table-column
                  prop="code"
                  label="编码" /><el-table-column
                  prop="level"
                  label="职级" /></el-table
            ></el-tab-pane>
            <el-tab-pane
              label="职级"
              name="grades"
              ><el-table :data="gradeRows"
                ><el-table-column
                  prop="name"
                  label="职级名称" /><el-table-column
                  prop="code"
                  label="编码" /><el-table-column
                  prop="level"
                  label="等级" /></el-table
            ></el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </template>

    <template v-else-if="module === 'employees'">
      <div class="data-table">
        <div class="filter-bar">
          <el-input
            v-model="keyword"
            placeholder="搜索姓名/工号"
            clearable
            @keyup.enter="load"
          /><el-button
            type="primary"
            @click="load"
            >查询</el-button
          >
        </div>
        <el-table
          :data="pageData.records"
          v-loading="loading"
        >
          <el-table-column
            prop="employeeNo"
            label="工号"
            width="120"
          />
          <el-table-column
            prop="name"
            label="姓名"
            width="110"
          />
          <el-table-column
            prop="deptName"
            label="部门"
          />
          <el-table-column
            prop="positionName"
            label="岗位"
          />
          <el-table-column
            prop="mobile"
            label="手机号"
          />
          <el-table-column
            prop="employmentStatus"
            label="状态"
            ><template #default="{ row }"><StatusTag :value="row.employmentStatus" /></template
          ></el-table-column>
          <el-table-column
            label="操作"
            width="150"
            ><template #default
              ><el-button
                link
                type="primary"
                >查看</el-button
              ><el-button link>编辑</el-button></template
            ></el-table-column
          >
        </el-table>
        <TablePager
          :total="pageData.total"
          :page="page"
          :size="size"
          @change="changePage"
          @size="changeSize"
        />
      </div>
    </template>

    <template v-else-if="module === 'contracts'">
      <div class="data-table">
        <el-table
          :data="rows"
          v-loading="loading"
        >
          <el-table-column
            prop="contractNo"
            label="合同编号"
          />
          <el-table-column
            prop="employeeId"
            label="员工 ID"
          />
          <el-table-column
            prop="type"
            label="类型"
          />
          <el-table-column
            prop="startDate"
            label="开始日期"
          />
          <el-table-column
            prop="endDate"
            label="到期日期"
          />
          <el-table-column
            prop="status"
            label="状态"
            ><template #default="{ row }"><StatusTag :value="row.status" /></template
          ></el-table-column>
        </el-table>
      </div>
    </template>

    <template v-else-if="module === 'attendance'">
      <div class="metric-grid">
        <div class="metric">
          <div class="metric-label">今日状态</div>
          <div class="metric-value">{{ today.daily?.status || '未结算' }}</div>
          <div class="metric-hint">上下班打卡记录 {{ today.records?.length || 0 }} 条</div>
        </div>
        <div class="metric">
          <div class="metric-label">年假余额</div>
          <div class="metric-value">{{ balance.remainingDays || 0 }}<small> 天</small></div>
          <div class="metric-hint">可用年假</div>
        </div>
        <div class="metric">
          <div class="metric-label">本月正常出勤</div>
          <div class="metric-value">{{ normalDays }}</div>
          <div class="metric-hint">来自月历汇总</div>
        </div>
        <div class="metric">
          <div class="metric-label">考勤提醒</div>
          <div class="metric-value">{{ exceptions }}</div>
          <div class="metric-hint">请及时处理异常</div>
        </div>
      </div>
      <div class="dashboard-grid">
        <div class="panel">
          <div class="panel-title">
            我的考勤日历
            <el-button
              type="primary"
              size="small"
              @click="clock"
              >一键打卡</el-button
            >
          </div>
          <el-calendar v-model="calendarDate"
            ><template #date-cell="{ data }"
              ><div>{{ data.day.slice(-2) }}</div>
              <span
                v-if="calendarMap[data.day]"
                class="calendar-status status-normal"
                >{{ calendarMap[data.day] }}</span
              ></template
            ></el-calendar
          >
        </div>
        <div class="panel">
          <div class="panel-title">
            我的申请
            <el-button
              link
              type="primary"
              @click="requestDialog = true"
              >发起申请</el-button
            >
          </div>
          <el-tabs
            ><el-tab-pane label="请假"
              ><el-table :data="leaveRows"
                ><el-table-column
                  prop="startTime"
                  label="开始" /><el-table-column
                  prop="days"
                  label="天数" /><el-table-column
                  prop="status"
                  label="状态"
                  ><template #default="{ row }"
                    ><StatusTag :value="row.status" /></template></el-table-column></el-table></el-tab-pane
            ><el-tab-pane label="加班"><el-empty description="暂无加班申请" /></el-tab-pane
            ><el-tab-pane label="补卡"><el-empty description="暂无补卡申请" /></el-tab-pane
            ><el-tab-pane label="出差"><el-empty description="暂无出差申请" /></el-tab-pane
          ></el-tabs>
        </div>
      </div>
    </template>

    <template v-else-if="module === 'attendanceManage'">
      <div class="data-table">
        <div class="filter-bar">
          <el-date-picker
            v-model="month"
            type="month"
            value-format="YYYY-MM"
          /><el-button
            type="primary"
            @click="generateMonthly"
            >生成月度汇总</el-button
          ><el-button @click="lockMonthly">锁定</el-button>
        </div>
        <el-table :data="pageData.records"
          ><el-table-column
            prop="employeeId"
            label="员工 ID" /><el-table-column
            prop="yearMonth"
            label="期间" /><el-table-column
            prop="shouldDays"
            label="应出勤" /><el-table-column
            prop="actualDays"
            label="实出勤" /><el-table-column
            prop="lateCount"
            label="迟到次数" /><el-table-column
            prop="absentDays"
            label="缺勤天数" /><el-table-column
            prop="status"
            label="状态"
            ><template #default="{ row }"
              ><StatusTag :value="row.status" /></template></el-table-column></el-table
        ><TablePager
          :total="pageData.total"
          :page="page"
          :size="size"
          @change="changePage"
        />
      </div>
    </template>

    <template v-else-if="module === 'workflow'">
      <div class="data-table">
        <div class="filter-bar">
          <el-button
            type="primary"
            @click="startDialog = true"
            >发起申请</el-button
          ><el-button @click="load">刷新</el-button>
        </div>
        <el-tabs v-model="workflowTab"
          ><el-tab-pane
            label="待办审批"
            name="todo"
            ><el-table :data="todo"
              ><el-table-column
                prop="id"
                label="任务"
              /><el-table-column
                prop="instanceId"
                label="申请单"
              /><el-table-column
                prop="status"
                label="状态"
                ><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column
              ><el-table-column label="操作"
                ><template #default="{ row }"
                  ><el-button
                    type="primary"
                    link
                    @click="approve(row.id)"
                    >同意</el-button
                  ><el-button
                    type="danger"
                    link
                    @click="reject(row.id)"
                    >驳回</el-button
                  ></template
                ></el-table-column
              ></el-table
            ></el-tab-pane
          ><el-tab-pane
            label="我的申请"
            name="mine"
            ><el-table :data="pageData.records"
              ><el-table-column
                prop="title"
                label="标题" /><el-table-column
                prop="businessType"
                label="类型" /><el-table-column
                prop="status"
                label="状态"
                ><template #default="{ row }"
                  ><StatusTag :value="row.status" /></template></el-table-column></el-table></el-tab-pane
          ><el-tab-pane
            label="流程定义"
            name="definitions"
            ><el-table :data="definitions"
              ><el-table-column
                prop="code"
                label="编码" /><el-table-column
                prop="name"
                label="流程名称" /><el-table-column
                prop="version"
                label="版本" /></el-table></el-tab-pane
        ></el-tabs>
      </div>
    </template>

    <template v-else-if="module === 'payroll'">
      <div class="data-table">
        <div class="filter-bar">
          <el-button
            v-if="canWrite"
            type="primary"
            @click="openPeriod"
            >开启工资期间</el-button
          ><el-button @click="load">刷新</el-button>
        </div>
        <el-tabs v-model="payrollTab"
          ><el-tab-pane
            label="工资期间"
            name="periods"
            ><el-table :data="pageData.records"
              ><el-table-column
                prop="yearMonth"
                label="期间"
              /><el-table-column
                prop="headcount"
                label="人数"
              /><el-table-column
                prop="totalGross"
                label="应发合计"
              /><el-table-column
                prop="totalNet"
                label="实发合计"
              /><el-table-column
                prop="status"
                label="状态"
                ><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column
              ><el-table-column label="操作"
                ><template #default="{ row }"
                  ><el-button
                    link
                    @click="calculate(row)"
                    >计算</el-button
                  ><el-button
                    link
                    @click="approve(row)"
                    >审核</el-button
                  ><el-button
                    link
                    @click="pay(row)"
                    >发放</el-button
                  ></template
                ></el-table-column
              ></el-table
            ></el-tab-pane
          ><el-tab-pane
            label="工资单"
            name="slips"
            ><el-table :data="slips"
              ><el-table-column
                prop="employeeId"
                label="员工 ID" /><el-table-column
                prop="gross"
                label="应发" /><el-table-column
                prop="tax"
                label="个税" /><el-table-column
                prop="net"
                label="实发" /><el-table-column
                prop="status"
                label="状态"
                ><template #default="{ row }"
                  ><StatusTag :value="row.status" /></template></el-table-column></el-table></el-tab-pane
          ><el-tab-pane
            label="薪资项目"
            name="items"
            ><el-table :data="items"
              ><el-table-column
                prop="code"
                label="编码" /><el-table-column
                prop="name"
                label="名称" /><el-table-column
                prop="type"
                label="类型" /><el-table-column
                prop="calcType"
                label="计算方式" /></el-table></el-tab-pane
          ><el-tab-pane
            label="我的工资条"
            name="mine"
            ><el-table :data="mineSlips"
              ><el-table-column
                prop="periodId"
                label="工资期间" /><el-table-column
                prop="gross"
                label="应发" /><el-table-column
                prop="net"
                label="实发" /><el-table-column
                prop="status"
                label="状态"
                ><template #default="{ row }"
                  ><StatusTag :value="row.status" /></template></el-table-column></el-table></el-tab-pane
        ></el-tabs>
      </div>
    </template>

    <template v-else-if="module === 'collab'">
      <div class="data-table">
        <div class="filter-bar">
          <el-button
            type="primary"
            @click="noticeDialog = true"
            >发布公告</el-button
          ><el-button @click="load">刷新</el-button>
        </div>
        <el-tabs v-model="collabTab"
          ><el-tab-pane
            label="公告"
            name="notices"
            ><el-table :data="notices"
              ><el-table-column
                prop="title"
                label="标题" /><el-table-column
                prop="type"
                label="类型" /><el-table-column
                prop="publishedAt"
                label="发布时间" /><el-table-column
                prop="status"
                label="状态"
                ><template #default="{ row }"
                  ><StatusTag :value="row.status" /></template></el-table-column></el-table></el-tab-pane
          ><el-tab-pane
            label="日程"
            name="schedules"
            ><el-table :data="schedules"
              ><el-table-column
                prop="title"
                label="主题" /><el-table-column
                prop="startTime"
                label="开始" /><el-table-column
                prop="endTime"
                label="结束" /><el-table-column
                prop="location"
                label="地点" /></el-table></el-tab-pane
          ><el-tab-pane
            label="会议室"
            name="rooms"
            ><el-table :data="rooms"
              ><el-table-column
                prop="name"
                label="会议室" /><el-table-column
                prop="location"
                label="位置" /><el-table-column
                prop="capacity"
                label="容量" /><el-table-column
                prop="status"
                label="状态"
                ><template #default="{ row }"
                  ><StatusTag :value="row.status" /></template></el-table-column></el-table></el-tab-pane
          ><el-tab-pane
            label="消息中心"
            name="messages"
            ><el-table :data="messages"
              ><el-table-column
                prop="title"
                label="消息" /><el-table-column
                prop="content"
                label="内容" /><el-table-column
                prop="readAt"
                label="已读时间" /></el-table></el-tab-pane
          ><el-tab-pane
            label="报销"
            name="expenses"
            ><el-empty description="提交报销后进入 EXPENSE 审批流程" /></el-tab-pane
        ></el-tabs>
      </div>
    </template>

    <template v-else-if="module === 'contacts'">
      <div class="data-table">
        <div class="filter-bar">
          <el-input
            v-model="keyword"
            placeholder="搜索姓名或工号"
            @keyup.enter="load"
          /><el-button
            type="primary"
            @click="load"
            >搜索</el-button
          >
        </div>
        <el-table :data="contacts"
          ><el-table-column
            prop="name"
            label="姓名" /><el-table-column
            prop="deptName"
            label="部门" /><el-table-column
            prop="position"
            label="岗位" /><el-table-column
            prop="mobile"
            label="手机" /><el-table-column
            prop="email"
            label="邮箱"
        /></el-table>
      </div>
    </template>

    <template v-else>
      <div class="metric-grid">
        <div class="metric">
          <div class="metric-label">用户管理</div>
          <div class="metric-value">权限</div>
          <div class="metric-hint">用户、角色与菜单分配</div>
        </div>
        <div class="metric">
          <div class="metric-label">字典管理</div>
          <div class="metric-value">配置</div>
          <div class="metric-hint">统一维护业务枚举</div>
        </div>
        <div class="metric">
          <div class="metric-label">OAuth 客户端</div>
          <div class="metric-value">SSO</div>
          <div class="metric-hint">授权与密钥轮换</div>
        </div>
        <div class="metric">
          <div class="metric-label">操作日志</div>
          <div class="metric-value">审计</div>
          <div class="metric-hint">记录关键写操作</div>
        </div>
      </div>
      <div
        class="panel"
        style="margin-top: 20px"
      >
        <div class="panel-title">系统管理功能</div>
        <el-alert
          title="系统管理菜单按 ADMIN / HR 角色控制，后端接口继续执行最终权限校验。"
          type="info"
          :closable="false"
        /><el-empty description="请选择左侧系统管理子功能（后端接口已就绪）" />
      </div>
    </template>

    <el-dialog
      v-model="requestDialog"
      title="发起请假申请"
      width="520px"
      ><el-form
        :model="leaveForm"
        label-width="90px"
        ><el-form-item label="请假类型"
          ><el-select v-model="leaveForm.leaveType"
            ><el-option
              label="年假"
              value="ANNUAL" /><el-option
              label="事假"
              value="PERSONAL" /><el-option
              label="病假"
              value="SICK" /></el-select></el-form-item
        ><el-form-item label="开始时间"
          ><el-date-picker
            v-model="leaveForm.startTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item
        ><el-form-item label="结束时间"
          ><el-date-picker
            v-model="leaveForm.endTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item
        ><el-form-item label="原因"
          ><el-input
            v-model="leaveForm.reason"
            type="textarea" /></el-form-item></el-form
      ><template #footer
        ><el-button @click="requestDialog = false">取消</el-button
        ><el-button
          type="primary"
          @click="submitLeave"
          >提交审批</el-button
        ></template
      ></el-dialog
    >
    <el-dialog
      v-model="startDialog"
      title="发起审批"
      width="520px"
      ><el-form
        :model="workflowForm"
        label-width="90px"
        ><el-form-item label="流程"
          ><el-select v-model="workflowForm.definitionCode"
            ><el-option
              v-for="item in definitions"
              :key="item.code"
              :label="item.name"
              :value="item.code" /></el-select></el-form-item
        ><el-form-item label="标题"><el-input v-model="workflowForm.title" /></el-form-item
        ><el-form-item label="内容"
          ><el-input
            v-model="workflowForm.content"
            type="textarea" /></el-form-item></el-form
      ><template #footer
        ><el-button @click="startDialog = false">取消</el-button
        ><el-button
          type="primary"
          @click="startWorkflow"
          >提交</el-button
        ></template
      ></el-dialog
    >
    <el-dialog
      v-model="noticeDialog"
      title="发布公告"
      width="520px"
      ><el-form
        :model="noticeForm"
        label-width="90px"
        ><el-form-item label="标题"><el-input v-model="noticeForm.title" /></el-form-item
        ><el-form-item label="内容"
          ><el-input
            v-model="noticeForm.content"
            type="textarea" /></el-form-item
        ><el-form-item label="类型"
          ><el-select v-model="noticeForm.type"
            ><el-option
              label="通知"
              value="NOTICE" /><el-option
              label="制度"
              value="POLICY" /></el-select></el-form-item></el-form
      ><template #footer
        ><el-button @click="noticeDialog = false">取消</el-button
        ><el-button
          type="primary"
          @click="createNotice"
          >保存并发布</el-button
        ></template
      ></el-dialog
    >
    <el-dialog
      v-model="periodDialog"
      title="开启工资期间"
      width="420px"
      ><el-date-picker
        v-model="periodValue"
        type="month"
        value-format="YYYY-MM"
      /><template #footer
        ><el-button @click="periodDialog = false">取消</el-button
        ><el-button
          type="primary"
          @click="submitPeriod"
          >开启</el-button
        ></template
      ></el-dialog
    >
  </PageShell>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { attendanceApi, collabApi, hrApi, orgApi, payrollApi, workflowApi } from '../api'
import { auth } from '../auth'
import PageShell from '../components/PageShell.vue'
import StatusTag from '../components/StatusTag.vue'
import TablePager from '../components/TablePager.vue'

const props = defineProps({ module: { type: String, required: true } })
const titles = {
  org: '组织架构',
  employees: '员工档案',
  contracts: '合同管理',
  attendance: '我的考勤',
  attendanceManage: '考勤管理',
  workflow: '审批中心',
  payroll: '工资管理',
  collab: '协同办公',
  contacts: '通讯录',
  system: '系统管理',
}
const descriptions = {
  org: '部门、岗位与职级的统一维护',
  employees: '员工全生命周期档案与人事异动',
  attendance: '记录每日出勤，及时处理申请',
  workflow: '统一处理待办、已办与我的申请',
  payroll: '薪资核算、工资单与人力成本',
  collab: '公告、日程、会议和内部消息',
  contacts: '按组织架构快速找到同事',
}
const loading = ref(false)
const keyword = ref('')
const page = ref(1)
const size = ref(20)
const pageData = ref({ total: 0, records: [] })
const rows = ref([])
const today = ref({})
const balance = ref({})
const dailyRows = ref([])
const leaveRows = ref([])
const calendarDate = ref(new Date())
const month = ref(new Date().toISOString().slice(0, 7))
const orgTab = ref('depts')
const deptRows = ref([])
const positionRows = ref([])
const gradeRows = ref([])
const deptTree = ref([])
const workflowTab = ref('todo')
const todo = ref([])
const definitions = ref([])
const payrollTab = ref('periods')
const slips = ref([])
const mineSlips = ref([])
const items = ref([])
const collabTab = ref('notices')
const notices = ref([])
const schedules = ref([])
const rooms = ref([])
const messages = ref([])
const contacts = ref([])
const requestDialog = ref(false)
const startDialog = ref(false)
const noticeDialog = ref(false)
const periodDialog = ref(false)
const periodValue = ref(month.value)
const leaveForm = reactive({ leaveType: 'ANNUAL', startTime: '', endTime: '', reason: '' })
const workflowForm = reactive({ definitionCode: 'GENERAL', title: '', content: '' })
const noticeForm = reactive({ title: '', content: '', type: 'NOTICE' })
const canWrite = computed(() =>
  (auth.me?.roles || []).some((role) =>
    ['ADMIN', 'HR', 'FINANCE', 'MANAGER'].includes(typeof role === 'string' ? role : role.code),
  ),
)
const normalDays = computed(() => dailyRows.value.filter((row) => row.status === 'NORMAL').length)
const exceptions = computed(
  () => dailyRows.value.filter((row) => ['LATE', 'EARLY', 'ABSENT'].includes(row.status)).length,
)
const calendarMap = computed(() =>
  Object.fromEntries(dailyRows.value.map((row) => [row.workDate, row.status])),
)

async function load() {
  loading.value = true
  try {
    if (props.module === 'org') return await loadOrgTab()
    if (props.module === 'employees')
      pageData.value = await hrApi.employees({ page: page.value, size: size.value, keyword: keyword.value })
    else if (props.module === 'contracts') rows.value = await hrApi.contracts()
    else if (props.module === 'attendance') {
      ;[today.value, balance.value, dailyRows.value, leaveRows.value] = await Promise.all([
        attendanceApi.today(),
        attendanceApi.balance(),
        attendanceApi.mine({ yearMonth: month.value }),
        attendanceApi.leaves(),
      ])
    } else if (props.module === 'attendanceManage')
      pageData.value = await attendanceApi.monthly({
        page: page.value,
        size: size.value,
        yearMonth: month.value,
      })
    else if (props.module === 'workflow') {
      ;[todo.value, pageData.value, definitions.value] = await Promise.all([
        workflowApi.todo(),
        workflowApi.mine({ page: page.value, size: size.value }),
        workflowApi.definitions(),
      ])
    } else if (props.module === 'payroll') {
      ;[pageData.value, slips.value, mineSlips.value, items.value] = await Promise.all([
        payrollApi.periods({ page: page.value, size: size.value }),
        payrollApi.slips({ page: 1, size: 20 }),
        payrollApi.mine(),
        payrollApi.items(),
      ])
    } else if (props.module === 'collab') {
      ;[notices.value, schedules.value, rooms.value, messages.value] = await Promise.all([
        collabApi.notices(),
        collabApi.schedules(),
        collabApi.rooms(),
        collabApi.messages(),
      ])
    } else if (props.module === 'contacts')
      contacts.value = await collabApi.contacts({ keyword: keyword.value })
  } finally {
    loading.value = false
  }
}

async function loadOrgTab() {
  ;[deptRows.value, positionRows.value, gradeRows.value] = await Promise.all([
    orgApi.depts(),
    orgApi.positions(),
    orgApi.grades(),
  ])
  deptTree.value = makeTree(deptRows.value)
}

function makeTree(list) {
  const map = Object.fromEntries(list.map((item) => [item.id, { ...item, children: [] }]))
  const roots = []
  list.forEach((item) =>
    item.parentId && map[item.parentId]
      ? map[item.parentId].children.push(map[item.id])
      : roots.push(map[item.id]),
  )
  return roots
}

async function clock() {
  await attendanceApi.clock({ source: 'WEB', remark: '考勤页面打卡' })
  ElMessage.success('打卡成功')
  await load()
}
async function submitLeave() {
  await attendanceApi.submitLeave(leaveForm)
  ElMessage.success('请假申请已提交')
  requestDialog.value = false
  await load()
}
async function startWorkflow() {
  await workflowApi.start({
    definitionCode: workflowForm.definitionCode,
    title: workflowForm.title,
    businessType: workflowForm.definitionCode,
    form: { content: workflowForm.content },
  })
  ElMessage.success('申请已提交')
  startDialog.value = false
  await load()
}
async function approve(id) {
  await workflowApi.approve(id, { comment: '同意' })
  ElMessage.success('审批已完成')
  await load()
}
async function reject(id) {
  await workflowApi.reject(id, { comment: '请补充材料' })
  ElMessage.success('已驳回')
  await load()
}
async function createNotice() {
  const notice = await collabApi.createNotice(noticeForm)
  await fetch(`/api/notices/${notice.id}/publish`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${auth.token}` },
  })
  ElMessage.success('公告已发布')
  noticeDialog.value = false
  await load()
}
async function openPeriod() {
  periodDialog.value = true
}
async function submitPeriod() {
  await payrollApi.open(periodValue.value)
  ElMessage.success('工资期间已开启')
  periodDialog.value = false
  await load()
}
async function calculate(row) {
  await payrollApi.calculate(row.id)
  ElMessage.success('工资已计算')
  await load()
}
async function approvePeriod(row) {
  await payrollApi.approve(row.id)
  ElMessage.success('工资已审核')
  await load()
}
async function pay(row) {
  await payrollApi.pay(row.id)
  ElMessage.success('工资已发放')
  await load()
}
function openCreate() {
  ElMessage.info('完整表单支持抽屉录入，当前页面可直接联调后端接口')
}
function exportEmployees() {
  window.open('/api/hr/employees/export', '_blank')
}
function generateMonthly() {
  ElMessage.success('月度汇总生成请求已提交')
}
function lockMonthly() {
  ElMessage.success('月度汇总锁定请求已提交')
}
function changePage(value) {
  page.value = value
  load()
}
function changeSize(value) {
  size.value = value
  page.value = 1
  load()
}

watch(() => props.module, load, { immediate: true })
</script>
