<template>
  <PageShell
    title="我的申请"
    code="WF-MINE"
    ><el-card
      ><el-table :data="rows"
        ><el-table-column
          prop="instanceNo"
          label="申请单号"
        /><el-table-column
          prop="title"
          label="标题"
        /><el-table-column
          prop="businessType"
          label="类型"
        /><el-table-column label="状态"
          ><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column
        ><el-table-column
          prop="submittedAt"
          label="提交时间"
        /><el-table-column label="操作"
          ><template #default="{ row }"
            ><el-button
              v-if="['PENDING', 'RUNNING'].includes(row.status)"
              link
              type="danger"
              @click="cancel(row)"
              >撤回</el-button
            ><el-button
              link
              @click="detail(row)"
              >轨迹</el-button
            ></template
          ></el-table-column
        ></el-table
      ><TablePager
        :total="total"
        :page="page"
        :size="size"
        @change="change" /></el-card
    ><el-drawer
      v-model="visible"
      title="审批轨迹"
      ><ApprovalTimeline :tasks="tasks" /></el-drawer
  ></PageShell>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { workflowApi } from '../../api/workflow'
import ApprovalTimeline from '../../components/ApprovalTimeline.vue'
import PageShell from '../../components/PageShell.vue'
import StatusTag from '../../components/StatusTag.vue'
import TablePager from '../../components/TablePager.vue'
const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const visible = ref(false)
const tasks = ref([])
async function load() {
  const data = await workflowApi.instances({ page: page.value, size: size.value })
  rows.value = data.records || []
  total.value = data.total || 0
}
async function cancel(row) {
  await workflowApi.cancel(row.id)
  ElMessage.success('已撤回')
  await load()
}
async function detail(row) {
  tasks.value = (await workflowApi.instance(row.id)).tasks || []
  visible.value = true
}
function change(value) {
  page.value = value
  load()
}
onMounted(load)
</script>
