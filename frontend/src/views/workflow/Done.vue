<template>
  <PageShell title="已办审批" code="WF-DONE">
    <el-card>
      <el-table :data="rows">
        <el-table-column prop="instanceTitle" label="申请标题" min-width="220" />
        <el-table-column prop="instanceNo" label="申请单号" width="170" />
        <el-table-column prop="nodeName" label="节点" width="140">
          <template #default="{ row }">{{ row.nodeName || `第 ${row.nodeSeq || '-'} 节点` }}</template>
        </el-table-column>
        <el-table-column label="结果" width="110">
          <template #default="{ row }"><StatusTag :value="row.status" /></template>
        </el-table-column>
        <el-table-column prop="comment" label="意见" min-width="160" />
        <el-table-column label="处理时间" width="180">
          <template #default="{ row }">{{ fmtDateTime(row.handledAt) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </PageShell>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { workflowApi } from '../../api/workflow'
import PageShell from '../../components/PageShell.vue'
import StatusTag from '../../components/StatusTag.vue'
import { fmtDateTime } from '../../utils/format'
const rows = ref([])
onMounted(async () => {
  rows.value = await workflowApi.done()
})
</script>
