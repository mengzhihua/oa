<template>
  <PageShell
    title="已办审批"
    code="WF-DONE"
    ><el-card
      ><el-table :data="rows"
        ><el-table-column
          prop="id"
          label="任务"
        /><el-table-column
          prop="instanceId"
          label="申请单"
        /><el-table-column
          prop="nodeSeq"
          label="节点"
        /><el-table-column label="结果"
          ><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column
        ><el-table-column
          prop="comment"
          label="意见"
        /><el-table-column label="处理时间"
          ><template #default="{ row }">{{ fmtDateTime(row.handledAt) }}</template></el-table-column
        ></el-table
      ></el-card
    ></PageShell
  >
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
