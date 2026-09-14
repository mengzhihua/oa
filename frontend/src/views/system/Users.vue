<template>
  <PageShell
    title="用户管理"
    code="SYS-USER"
    ><template #actions
      ><el-button
        type="primary"
        @click="open()"
        >新增用户</el-button
      ></template
    ><el-card
      ><el-form inline
        ><el-input
          v-model="keyword"
          placeholder="用户名/姓名"
        /><el-button
          type="primary"
          @click="load"
          >查询</el-button
        ></el-form
      ><el-table :data="rows"
        ><el-table-column
          prop="username"
          label="用户名"
        /><el-table-column
          prop="realName"
          label="姓名"
        /><el-table-column
          prop="employeeId"
          label="员工 ID"
        /><el-table-column label="状态"
          ><template #default="{ row }"
            ><StatusTag :value="row.status ? 'ACTIVE' : 'INACTIVE'" /></template></el-table-column
        ><el-table-column label="操作"
          ><template #default="{ row }"
            ><el-button
              link
              @click="open(row)"
              >编辑</el-button
            ><el-button
              link
              @click="reset(row)"
              >重置密码</el-button
            ><el-button
              link
              @click="remove(row)"
              >停用</el-button
            ></template
          ></el-table-column
        ></el-table
      ><TablePager
        :total="total"
        :page="page"
        :size="size"
        @change="change" /></el-card
    ><el-dialog
      v-model="visible"
      :title="form.id ? '编辑用户' : '新增用户'"
      ><el-form
        :model="form"
        label-width="80px"
        ><el-form-item label="用户名"
          ><el-input
            v-model="form.username"
            :disabled="Boolean(form.id)" /></el-form-item
        ><el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item
        ><el-form-item label="密码"
          ><el-input
            v-model="form.password"
            type="password" /></el-form-item
        ><el-form-item label="员工 ID"><el-input v-model="form.employeeId" /></el-form-item
        ><el-form-item label="状态"><el-switch v-model="form.enabled" /></el-form-item></el-form
      ><template #footer
        ><el-button @click="visible = false">取消</el-button
        ><el-button
          type="primary"
          @click="save"
          >保存</el-button
        ></template
      ></el-dialog
    ></PageShell
  >
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { systemApi } from '../../api/system'
import PageShell from '../../components/PageShell.vue'
import StatusTag from '../../components/StatusTag.vue'
import TablePager from '../../components/TablePager.vue'
const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const keyword = ref('')
const visible = ref(false)
const form = reactive({})
async function load() {
  const data = await systemApi.users({ page: page.value, size: size.value, keyword: keyword.value })
  rows.value = data.records || []
  total.value = data.total || 0
}
function open(row = {}) {
  Object.assign(form, {
    id: row.id,
    username: row.username,
    realName: row.realName,
    password: '',
    employeeId: row.employeeId,
    status: row.status,
    enabled: row.status !== 0,
  })
  visible.value = true
}
async function save() {
  const data = { ...form, status: form.enabled ? 1 : 0 }
  form.id ? await systemApi.updateUser(form.id, data) : await systemApi.createUser(data)
  visible.value = false
  ElMessage.success('保存成功')
  await load()
}
async function reset(row) {
  const { value } = await ElMessageBox.prompt('新密码', '重置密码', { inputType: 'password' })
  await systemApi.resetPassword(row.id, { password: value })
  ElMessage.success('密码已重置')
}
async function remove(row) {
  await ElMessageBox.confirm(`确认停用 ${row.username}？`, '停用确认')
  await systemApi.deleteUser(row.id)
  ElMessage.success('已停用')
  await load()
}
function change(value) {
  page.value = value
  load()
}
onMounted(load)
</script>
