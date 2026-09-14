<template>
  <PageShell
    title="流程定义"
    code="WF-DEFINITION"
    description="查看流程定义和节点配置"
  >
    <el-card
      ><el-table :data="rows"
        ><el-table-column
          prop="code"
          label="编码"
        /><el-table-column
          prop="name"
          label="名称"
        /><el-table-column
          prop="version"
          label="版本"
        /><el-table-column label="状态"
          ><template #default="{ row }"
            ><StatusTag :value="row.status ? 'ACTIVE' : 'INACTIVE'" /></template></el-table-column
        ><el-table-column label="节点"
          ><template #default="{ row }"
            ><el-button
              link
              @click="show(row)"
              >查看/编辑节点</el-button
            ></template
          ></el-table-column
        ></el-table
      ></el-card
    >
    <el-dialog
      v-model="visible"
      title="流程节点"
      width="760px"
      ><el-table :data="nodes"
        ><el-table-column
          prop="seq"
          label="序号"
          width="80" /><el-table-column
          prop="name"
          label="节点名称" /><el-table-column
          prop="approverType"
          label="审批人类型" /><el-table-column
          prop="approverRef"
          label="审批人引用" /><el-table-column
          prop="multiMode"
          label="多审批模式" /><el-table-column
          prop="conditionJson"
          label="条件 JSON" /></el-table
    ></el-dialog>
  </PageShell>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { workflowApi } from '../../api/workflow'
import PageShell from '../../components/PageShell.vue'
import StatusTag from '../../components/StatusTag.vue'
const rows = ref([])
const nodes = ref([])
const visible = ref(false)
async function load() {
  rows.value = await workflowApi.definitions()
}
async function show(row) {
  const detail = await workflowApi.definition(row.id)
  nodes.value = detail.nodes || []
  visible.value = true
}
onMounted(load)
</script>
