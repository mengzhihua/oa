<template>
  <el-timeline>
    <el-timeline-item
      v-for="task in tasks || []"
      :key="task.id"
      :timestamp="task.handledAt || '待处理'"
    >
      <strong>第 {{ task.nodeSeq }} 节点</strong>
      <StatusTag :value="task.status" />
      <span class="timeline-comment">{{ task.comment || '审批任务' }}</span>
    </el-timeline-item>
  </el-timeline>
</template>

<script setup>
import StatusTag from './StatusTag.vue'
defineProps({ tasks: Array })
</script>
