<template>
  <PageShell
    title="公告管理"
    code="CO-NOTICE"
    ><template #actions
      ><el-button
        v-if="canWrite('ADMIN', 'HR')"
        type="primary"
        @click="open()"
        >发布公告</el-button
      ></template
    ><el-card
      ><el-table :data="rows"
        ><el-table-column
          prop="title"
          label="标题"
        /><el-table-column
          prop="type"
          label="类型"
        /><el-table-column
          prop="pinned"
          label="置顶"
        /><el-table-column label="发布时间"
          ><template #default="{ row }">{{ fmtDateTime(row.publishedAt) }}</template></el-table-column
        ><el-table-column label="状态"
          ><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column
        ><el-table-column label="操作"
          ><template #default="{ row }"
            ><el-button
              link
              @click="detail(row)"
              >查看</el-button
            ><el-button
              v-if="canWrite('ADMIN', 'HR')"
              link
              @click="open(row)"
              >编辑</el-button
            ><el-button
              v-if="canWrite('ADMIN', 'HR') && row.status === 'DRAFT'"
              link
              type="success"
              @click="publish(row)"
              >发布</el-button
            ><el-button
              v-if="canWrite('ADMIN', 'HR') && row.status === 'PUBLISHED'"
              link
              type="danger"
              @click="revoke(row)"
              >撤回</el-button
            ></template
          ></el-table-column
        ></el-table
      ><TablePager
        :total="total"
        :page="page"
        :size="size"
        @change="changePage"
        @size="changeSize" /></el-card
    ><el-dialog
      v-model="visible"
      :title="form.id ? '编辑公告' : '发布公告'"
      width="600px"
      ><el-form
        :model="form"
        label-width="80px"
        ><el-form-item label="标题"><el-input v-model="form.title" /></el-form-item
        ><el-form-item label="类型"><el-input v-model="form.type" /></el-form-item
        ><el-form-item label="内容"
          ><el-input
            v-model="form.content"
            type="textarea"
            :rows="8" /></el-form-item
        ><el-form-item label="置顶"
          ><el-switch
            v-model="form.pinned"
            :active-value="1"
            :inactive-value="0" /></el-form-item></el-form
      ><template #footer
        ><el-button @click="visible = false">取消</el-button
        ><el-button
          type="primary"
          @click="save"
          >保存</el-button
        ></template
      ></el-dialog
    ><el-drawer
      v-model="detailVisible"
      title="公告详情"
      ><h2>{{ current.title }}</h2>
      <StatusTag :value="current.status" />
      <p class="notice-content">{{ current.content }}</p></el-drawer
    ></PageShell
  >
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { collabApi } from '../../api/collab'
import { canWrite, hasRole } from '../../auth'
import PageShell from '../../components/PageShell.vue'
import StatusTag from '../../components/StatusTag.vue'
import TablePager from '../../components/TablePager.vue'
import { fmtDateTime } from '../../utils/format'
const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const visible = ref(false)
const detailVisible = ref(false)
const current = ref({})
const form = reactive({})
async function load() {
  if (hasRole('ADMIN', 'HR')) {
    const data = await collabApi.adminNotices({ page: page.value, size: size.value })
    rows.value = data.records || []
    total.value = data.total || 0
  } else {
    rows.value = await collabApi.notices()
    total.value = 0
  }
}
function open(row = {}) {
  Object.assign(form, {
    id: row.id,
    title: row.title,
    type: row.type || '通知',
    content: row.content || '',
    pinned: Number(row.pinned || 0),
  })
  visible.value = true
}
async function save() {
  form.id ? await collabApi.updateNotice(form.id, form) : await collabApi.createNotice(form)
  visible.value = false
  ElMessage.success('公告已保存')
  await load()
}
async function publish(row) {
  await collabApi.publishNotice(row.id)
  ElMessage.success('公告已发布')
  await load()
}
async function revoke(row) {
  await collabApi.revokeNotice(row.id)
  ElMessage.success('公告已撤回')
  await load()
}
async function detail(row) {
  current.value = row
  detailVisible.value = true
  await collabApi.readNotice(row.id)
}
function changePage(value) {
  page.value = value
  load()
}
function changeSize(value) {
  size.value = value
  page.value = 1
  load()
}
onMounted(load)
</script>
