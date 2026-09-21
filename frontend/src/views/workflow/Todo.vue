<template>
  <PageShell
    title="待办审批"
    code="WF-TODO"
    description="处理分配给你的审批任务"
  >
    <el-card>
      <el-table :data="rows">
        <el-table-column prop="instanceTitle" label="申请标题" min-width="220" />
        <el-table-column prop="instanceNo" label="申请单号" width="170" />
        <el-table-column prop="nodeName" label="节点" width="140">
          <template #default="{ row }">{{ row.nodeName || `第 ${row.nodeSeq || '-'} 节点` }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }"><StatusTag :value="row.status" /></template>
        </el-table-column>
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
            <el-button type="primary" link @click="open(row)">审批</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-drawer v-model="visible" :title="drawerTitle" size="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="标题">{{
          current.instanceTitle || current.title || '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="申请单号">{{
          current.instanceNo || current.instanceId || '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="业务编号">{{ current.businessId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="节点">{{
          current.nodeName || current.nodeSeq || '-'
        }}</el-descriptions-item>
      </el-descriptions>
      <el-input
        v-model="comment"
        type="textarea"
        :rows="4"
        placeholder="审批意见"
        class="drawer-input"
      />
      <ApprovalTimeline :tasks="tasks" />
      <template #footer>
        <el-button @click="action('reject')">驳回</el-button>
        <el-button type="primary" @click="action('approve')">同意</el-button>
        <el-button @click="transferVisible = true">转办</el-button>
      </template>
    </el-drawer>
    <el-dialog v-model="transferVisible" title="转办">
      <el-input v-model="toUserId" placeholder="转办员工用户 ID" />
      <template #footer>
        <el-button @click="transferVisible = false">取消</el-button>
        <el-button type="primary" @click="transfer">确定</el-button>
      </template>
    </el-dialog>
  </PageShell>
</template>
<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { workflowApi } from '../../api/workflow'
import ApprovalTimeline from '../../components/ApprovalTimeline.vue'
import PageShell from '../../components/PageShell.vue'
import StatusTag from '../../components/StatusTag.vue'
const rows = ref([])
const current = ref({})
const tasks = ref([])
const visible = ref(false)
const transferVisible = ref(false)
const comment = ref('')
const toUserId = ref()
const drawerTitle = computed(() => current.value.instanceTitle || current.value.title || '审批详情')
async function load() {
  rows.value = await workflowApi.todo()
}
async function open(row) {
  const detail = await workflowApi.instance(row.instanceId)
  current.value = {
    ...row,
    instanceTitle: detail.title || row.instanceTitle,
    instanceNo: detail.instanceNo || row.instanceNo,
    businessId: detail.businessId || row.businessId,
    title: detail.title || row.instanceTitle,
    nodeName: row.nodeName,
    nodeSeq: row.nodeSeq
  }
  tasks.value = detail.tasks || []
  visible.value = true
}
async function action(type) {
  await workflowApi[type](current.value.id, { comment: comment.value })
  visible.value = false
  ElMessage.success(type === 'approve' ? '审批通过' : '已驳回')
  await load()
}
async function transfer() {
  await workflowApi.transfer(current.value.id, { toUserId: toUserId.value })
  transferVisible.value = false
  visible.value = false
  ElMessage.success('已转办')
  await load()
}
onMounted(load)
</script>
