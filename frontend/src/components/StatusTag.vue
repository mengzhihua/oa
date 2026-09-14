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
  LEAVE: '请假',
  DRAFT: '草稿',
  PUBLISHED: '已发布',
  REVOKED: '已撤回',
}
const type = computed(() => {
  if (['APPROVED', 'PAID', 'CONFIRMED', 'NORMAL', 'PUBLISHED'].includes(props.value)) return 'success'
  if (['REJECTED', 'ABSENT', 'REVOKED'].includes(props.value)) return 'danger'
  if (['PENDING', 'RUNNING', 'OPEN', 'CALCULATED', 'LATE', 'EARLY'].includes(props.value)) return 'warning'
  return 'info'
})
</script>
