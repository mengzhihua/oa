<template>
  <PageShell
    title="我的申请"
    code="ATT-REQUEST"
    description="提交考勤申请并跟踪审批进度"
  >
    <el-tabs v-model="active"
      ><el-tab-pane
        v-for="tab in tabs"
        :key="tab.key"
        :label="tab.label"
        :name="tab.key"
        ><div class="toolbar">
          <el-button
            type="primary"
            @click="open(tab.key)"
            >新建{{ tab.label }}</el-button
          >
        </div>
        <el-table
          :data="lists[tab.key]"
          stripe
          ><el-table-column
            prop="id"
            label="编号"
            width="80"
          /><el-table-column
            prop="reason"
            label="事由"
          /><el-table-column
            prop="status"
            label="状态"
            ><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column
          ><el-table-column label="操作"
            ><template #default="{ row }"
              ><el-button
                v-if="row.status === 'PENDING'"
                link
                type="danger"
                @click="cancel(tab.key, row)"
                >撤回</el-button
              ><el-button
                link
                @click="showTimeline(row)"
                >查看轨迹</el-button
              ></template
            ></el-table-column
          ></el-table
        ></el-tab-pane
      ></el-tabs
    >
    <el-dialog
      v-model="visible"
      :title="`新建${activeLabel}`"
      width="520px"
      ><el-form
        :model="form"
        label-width="90px"
        ><template v-if="active === 'leave'"
          ><el-form-item label="请假类型"
            ><el-select v-model="form.leaveType"
              ><el-option
                label="年假"
                value="ANNUAL" /><el-option
                label="病假"
                value="SICK" /><el-option
                label="事假"
                value="PERSONAL" /></el-select></el-form-item
          ><el-form-item label="开始时间"
            ><el-date-picker
              v-model="form.startTime"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item
          ><el-form-item label="结束时间"
            ><el-date-picker
              v-model="form.endTime"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item
          ><el-form-item label="天数"
            ><el-input-number
              v-model="form.days"
              :min="0.5" /></el-form-item></template
        ><template v-if="active === 'overtime'"
          ><el-form-item label="开始时间"
            ><el-date-picker
              v-model="form.startTime"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item
          ><el-form-item label="结束时间"
            ><el-date-picker
              v-model="form.endTime"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item
          ><el-form-item label="小时数"
            ><el-input-number
              v-model="form.hours"
              :min="0.5" /></el-form-item
          ><el-form-item label="类型"
            ><el-select v-model="form.type"
              ><el-option
                label="工作日"
                value="WORKDAY" /><el-option
                label="周末"
                value="WEEKEND" /><el-option
                label="节假日"
                value="HOLIDAY" /></el-select></el-form-item></template
        ><template v-if="active === 'patch'"
          ><el-form-item label="工作日期"
            ><el-date-picker
              v-model="form.workDate"
              value-format="YYYY-MM-DD" /></el-form-item
          ><el-form-item label="打卡类型"
            ><el-select v-model="form.clockType"
              ><el-option
                label="上班"
                value="IN" /><el-option
                label="下班"
                value="OUT" /></el-select></el-form-item
          ><el-form-item label="补卡时间"
            ><el-date-picker
              v-model="form.clockTime"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item></template
        ><template v-if="active === 'trip'"
          ><el-form-item label="开始日期"
            ><el-date-picker
              v-model="form.startDate"
              value-format="YYYY-MM-DD" /></el-form-item
          ><el-form-item label="结束日期"
            ><el-date-picker
              v-model="form.endDate"
              value-format="YYYY-MM-DD" /></el-form-item
          ><el-form-item label="目的地"><el-input v-model="form.destination" /></el-form-item
          ><el-form-item label="天数"
            ><el-input-number
              v-model="form.days"
              :min="1" /></el-form-item></template
        ><el-form-item label="事由"
          ><el-input
            v-model="form.reason"
            type="textarea" /></el-form-item></el-form
      ><template #footer
        ><el-button @click="visible = false">取消</el-button
        ><el-button
          type="primary"
          @click="save"
          >提交</el-button
        ></template
      ></el-dialog
    >
    <el-drawer
      v-model="timelineVisible"
      title="审批轨迹"
      ><ApprovalTimeline :tasks="timeline"
    /></el-drawer>
  </PageShell>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { attendanceApi } from '../../api/attendance'
import { workflowApi } from '../../api/workflow'
import ApprovalTimeline from '../../components/ApprovalTimeline.vue'
import PageShell from '../../components/PageShell.vue'
import StatusTag from '../../components/StatusTag.vue'
const tabs = [
  { key: 'leave', label: '请假' },
  { key: 'overtime', label: '加班' },
  { key: 'patch', label: '补卡' },
  { key: 'trip', label: '出差' },
]
const active = ref('leave')
const visible = ref(false)
const timelineVisible = ref(false)
const timeline = ref([])
const form = reactive({})
const lists = reactive({ leave: [], overtime: [], patch: [], trip: [] })
const activeLabel = computed(() => tabs.find((tab) => tab.key === active.value)?.label)
async function load() {
  lists.leave = await attendanceApi.leaves()
  lists.overtime = await attendanceApi.overtimes()
  lists.patch = await attendanceApi.patches()
  lists.trip = await attendanceApi.trips()
}
function open(type) {
  active.value = type
  Object.assign(form, { leaveType: 'ANNUAL', type: 'WORKDAY', days: 1, hours: 1, reason: '' })
  visible.value = true
}
async function save() {
  const api = {
    leave: attendanceApi.submitLeave,
    overtime: attendanceApi.submitOvertime,
    patch: attendanceApi.submitPatch,
    trip: attendanceApi.submitTrip,
  }[active.value]
  await api(form)
  visible.value = false
  ElMessage.success('申请已提交')
  await load()
}
async function cancel(type, row) {
  await attendanceApi.cancel(
    type === 'leave'
      ? 'LEAVE'
      : type === 'overtime'
        ? 'OVERTIME'
        : type === 'patch'
          ? 'PATCH_CLOCK'
          : 'BUSINESS_TRIP',
    row.id,
  )
  ElMessage.success('已撤回')
  await load()
}
async function showTimeline(row) {
  const instanceId = row.wfInstanceId || row.instanceId
  if (!instanceId) {
    timeline.value = []
    timelineVisible.value = true
    ElMessage.warning('该申请暂无审批流程')
    return
  }
  const detail = await workflowApi.instance(instanceId)
  timeline.value = detail.tasks || []
  timelineVisible.value = true
}
onMounted(load)
</script>
