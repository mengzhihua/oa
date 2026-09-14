<template>
  <el-timeline>
    <el-timeline-item
      v-for="task in tasks || []"
      :key="task.id"
      :timestamp="task.handledAt ? fmtDateTime(task.handledAt) : '待处理'"
    >
      <strong>{{ task.nodeName || `第 ${task.nodeSeq || '-'} 节点` }}</strong>
      <div>审批人：{{ task.approverName || task.assigneeName || '待指定' }}</div>
      <div><StatusTag :value="task.status" /></div>
      <div class="timeline-comment">意见：{{ task.comment || task.opinion || '暂无意见' }}</div>
    </el-timeline-item>
  </el-timeline>
</template>

<script setup>
import StatusTag from './StatusTag.vue'
import { fmtDateTime } from '../utils/format'
defineProps({ tasks: Array })
</script>
