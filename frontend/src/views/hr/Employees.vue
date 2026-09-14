<template>
  <PageShell
    title="员工档案"
    code="HR-EMPLOYEE"
    description="查询员工档案，办理入职、转正、调岗和离职"
  >
    <template #actions>
      <el-button
        v-if="canWrite('ADMIN', 'HR')"
        type="primary"
        @click="openHire"
        >入职登记</el-button
      >
      <el-upload
        v-if="canWrite('ADMIN', 'HR')"
        :show-file-list="false"
        :before-upload="importCsv"
        ><el-button>导入 CSV</el-button></el-upload
      >
      <el-button @click="exportCsv">导出 CSV</el-button>
    </template>
    <el-card>
      <el-form
        inline
        @submit.prevent="load"
      >
        <el-form-item label="关键词"
          ><el-input
            v-model="query.keyword"
            clearable
            placeholder="姓名/工号/手机号"
            @keyup.enter="load"
        /></el-form-item>
        <el-form-item label="部门"><DeptTreeSelect v-model="query.deptId" /></el-form-item>
        <el-form-item label="状态"
          ><el-select
            v-model="query.status"
            clearable
            placeholder="全部"
            ><el-option
              label="试用期"
              value="PROBATION" /><el-option
              label="正式"
              value="REGULAR" /><el-option
              label="离职"
              value="LEFT" /></el-select
        ></el-form-item>
        <el-button
          type="primary"
          @click="load"
          >查询</el-button
        >
      </el-form>
      <el-table
        :data="rows"
        stripe
        ><el-table-column
          prop="employeeNo"
          label="工号"
          width="120"
        /><el-table-column
          prop="name"
          label="姓名"
          width="100"
        /><el-table-column
          prop="deptName"
          label="部门"
        /><el-table-column
          prop="mobile"
          label="手机号"
        /><el-table-column
          label="入职日期"
          width="120"
          ><template #default="{ row }">{{ fmtDate(row.hireDate) }}</template></el-table-column
        ><el-table-column
          label="状态"
          width="100"
          ><template #default="{ row }"
            ><StatusTag :value="row.employmentStatus" /></template></el-table-column
        ><el-table-column
          label="操作"
          width="300"
          ><template #default="{ row }"
            ><el-button
              link
              type="primary"
              @click="$router.push(`/hr/employees/${row.id}`)"
              >详情</el-button
            ><el-button
              v-if="canWrite('ADMIN', 'HR')"
              link
              @click="openEdit(row)"
              >编辑</el-button
            ><el-button
              v-if="canWrite('ADMIN', 'HR') && row.employmentStatus === 'PROBATION'"
              link
              @click="regular(row)"
              >转正</el-button
            ><el-button
              v-if="canWrite('ADMIN', 'HR')"
              link
              @click="transfer(row)"
              >调岗</el-button
            ><el-button
              v-if="canWrite('ADMIN', 'HR') && row.employmentStatus !== 'LEFT'"
              link
              type="danger"
              @click="leave(row)"
              >离职</el-button
            ></template
          ></el-table-column
        ></el-table
      >
      <TablePager
        :total="total"
        :page="query.page"
        :size="query.size"
        @change="pageChange"
        @size="sizeChange"
      />
    </el-card>
    <el-drawer
      v-model="formVisible"
      :title="form.id ? '编辑员工' : '入职登记'"
      size="620px"
    >
      <el-form
        :model="form"
        label-width="100px"
      >
        <el-divider content-position="left">基本信息</el-divider>
        <el-form-item
          label="姓名"
          required
          ><el-input v-model="form.name" /></el-form-item
        ><el-form-item label="性别"
          ><el-select
            v-model="form.gender"
            clearable
            ><el-option
              label="男"
              value="MALE" /><el-option
              label="女"
              value="FEMALE" /></el-select></el-form-item
        ><el-form-item label="身份证号"><el-input v-model="form.idCard" /></el-form-item
        ><el-form-item label="手机号"><el-input v-model="form.mobile" /></el-form-item
        ><el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-divider content-position="left">任职信息</el-divider>
        <el-form-item label="部门"><DeptTreeSelect v-model="form.deptId" /></el-form-item
        ><el-form-item label="岗位"
          ><el-input
            v-model="form.positionId"
            placeholder="岗位 ID" /></el-form-item
        ><el-form-item label="职级"
          ><el-input
            v-model="form.gradeId"
            placeholder="职级 ID" /></el-form-item
        ><el-form-item label="员工类型"
          ><el-select v-model="form.employeeType"
            ><el-option
              label="全职"
              value="FULLTIME" /><el-option
              label="兼职"
              value="PARTTIME" /></el-select></el-form-item
        ><el-form-item label="学历"><el-input v-model="form.education" /></el-form-item>
        <el-divider content-position="left">联系信息</el-divider
        ><el-form-item label="地址"><el-input v-model="form.address" /></el-form-item
        ><el-form-item label="紧急联系人"><el-input v-model="form.emergencyContact" /></el-form-item
        ><el-form-item label="备注"
          ><el-input
            v-model="form.remark"
            type="textarea"
        /></el-form-item>
      </el-form>
      <template #footer
        ><el-button @click="formVisible = false">取消</el-button
        ><el-button
          type="primary"
          @click="save"
          >保存</el-button
        ></template
      >
    </el-drawer>
    <el-dialog
      v-model="transferVisible"
      title="调岗"
      width="420px"
      ><el-form
        :model="transferForm"
        label-width="80px"
        ><el-form-item label="部门"><DeptTreeSelect v-model="transferForm.deptId" /></el-form-item
        ><el-form-item label="岗位"><el-input v-model="transferForm.positionId" /></el-form-item
        ><el-form-item label="原因"
          ><el-input
            v-model="transferForm.reason"
            type="textarea" /></el-form-item></el-form
      ><template #footer
        ><el-button @click="transferVisible = false">取消</el-button
        ><el-button
          type="primary"
          @click="saveTransfer"
          >确定</el-button
        ></template
      ></el-dialog
    >
  </PageShell>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { hrApi } from '../../api/hr'
import { canWrite } from '../../auth'
import DeptTreeSelect from '../../components/DeptTreeSelect.vue'
import PageShell from '../../components/PageShell.vue'
import StatusTag from '../../components/StatusTag.vue'
import TablePager from '../../components/TablePager.vue'
import { fmtDate } from '../../utils/format'
const router = useRouter()
const rows = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '', deptId: undefined, status: undefined })
const form = reactive({})
const transferForm = reactive({})
const formVisible = ref(false)
const transferVisible = ref(false)
let selectedId
async function load() {
  const data = await hrApi.employees(query)
  rows.value = data.records || []
  total.value = data.total || 0
}
function openHire() {
  Object.assign(form, {
    id: undefined,
    name: '',
    gender: '',
    idCard: '',
    mobile: '',
    email: '',
    deptId: undefined,
    positionId: undefined,
    gradeId: undefined,
    employeeType: 'FULLTIME',
    education: '',
    address: '',
    emergencyContact: '',
    remark: '',
  })
  formVisible.value = true
}
function openEdit(row) {
  Object.assign(form, row)
  formVisible.value = true
}
async function save() {
  if (!form.name) return ElMessage.warning('请填写员工姓名')
  form.id ? await hrApi.update(form.id, form) : await hrApi.hire(form)
  formVisible.value = false
  ElMessage.success('保存成功')
  await load()
}
async function regular(row) {
  await ElMessageBox.confirm(`确认将 ${row.name} 转为正式员工？`, '转正确认')
  await hrApi.regular(row.id)
  ElMessage.success('操作成功')
  await load()
}
function transfer(row) {
  selectedId = row.id
  Object.assign(transferForm, { deptId: row.deptId, positionId: row.positionId, reason: '' })
  transferVisible.value = true
}
async function saveTransfer() {
  await hrApi.transfer(selectedId, transferForm)
  transferVisible.value = false
  ElMessage.success('调岗成功')
  await load()
}
async function leave(row) {
  const { value } = await ElMessageBox.prompt('请输入离职日期（YYYY-MM-DD）', `确认 ${row.name} 离职`, {
    inputValue: new Date().toISOString().slice(0, 10),
  })
  await hrApi.leave(row.id, { leaveDate: value })
  ElMessage.success('离职已登记')
  await load()
}
async function importCsv(file) {
  const result = await hrApi.importEmployees(file)
  ElMessage.success(`成功 ${result.successCount || 0} 行，失败 ${result.failureCount || 0} 行`)
  await load()
  return false
}
async function exportCsv() {
  const blob = await hrApi.exportEmployees()
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'employees.csv'
  link.click()
  URL.revokeObjectURL(url)
}
function pageChange(page) {
  query.page = page
  load()
}
function sizeChange(size) {
  query.size = size
  query.page = 1
  load()
}
onMounted(load)
</script>
