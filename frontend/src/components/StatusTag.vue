<template>
  <el-tag
    size="small"
    :type="type"
    >{{ labels[value] || value || '—' }}</el-tag
  >
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ value: [String, Number] })
const labels = {
  OPEN: '开放',
  RUNNING: '审批中',
  PENDING: '待处理',
  APPROVED: '已通过',
  CONFIRMED: '已确认',
  PAID: '已发放',
  CLOSED: '已关闭',
  REJECTED: '已驳回',
  CANCELED: '已撤回',
  CALCULATED: '已计算',
  LOCKED: '已锁定',
  NORMAL: '正常',
  LATE: '迟到',
  EARLY: '早退',
  ABSENT: '缺勤',
  MISSING: '缺卡',
  LEAVE: '请假',
  DRAFT: '草稿',
  PUBLISHED: '已发布',
  REVOKED: '已撤回',
  LEAVING: '离职中',
  PROBATION: '试用期',
  REGULAR: '正式',
  IN: '上班',
  OUT: '下班',
  TRIP: '出差',
  REST: '休息',
  HOLIDAY: '节假日',
  PATCHED: '补卡',
  WORKDAY: '工作日',
  WEEKEND: '周末',
  ANNUAL: '年假',
  SICK: '病假',
  PERSONAL: '事假',
  MARRIAGE: '婚假',
  MATERNITY: '产假',
  BEREAVEMENT: '丧假',
  COMP: '调休',
  WORKFLOW: '审批',
  NOTICE: '公告',
  SYSTEM: '系统',
  EARNING: '收入',
  DEDUCTION: '扣款',
  COMPANY_COST: '公司成本',
  ACTIVE: '有效',
  INACTIVE: '无效',
  PARTIAL: '部分通过',
}
const type = computed(() => {
  if (
    ['APPROVED', 'PAID', 'CONFIRMED', 'NORMAL', 'PUBLISHED', 'REGULAR', 'ACTIVE', 'PATCHED'].includes(
      props.value,
    )
  )
    return 'success'
  if (['REJECTED', 'ABSENT', 'MISSING', 'REVOKED', 'LEAVING', 'INACTIVE'].includes(props.value)) return 'danger'
  if (['PENDING', 'RUNNING', 'OPEN', 'CALCULATED', 'LATE', 'EARLY', 'PROBATION'].includes(props.value))
    return 'warning'
  return 'info'
})
</script>
