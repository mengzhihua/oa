<template>
  <PageShell
    title="班次管理"
    code="ATT-SHIFT"
    ><template #actions
      ><el-button
        v-if="canWrite('ADMIN', 'HR')"
        type="primary"
        @click="open()"
        >新增班次</el-button
      ></template
    ><el-card
      ><el-table :data="rows"
        ><el-table-column
          prop="code"
          label="编码"
        /><el-table-column
          prop="name"
          label="名称"
        /><el-table-column
          prop="workStart"
          label="上班"
        /><el-table-column
          prop="workEnd"
          label="下班"
        /><el-table-column
          prop="restStart"
          label="午休开始"
        /><el-table-column
          prop="restEnd"
          label="午休结束"
        /><el-table-column
          prop="lateGraceMinutes"
          label="迟到宽限"
        /><el-table-column label="操作"
          ><template #default="{ row }"
            ><el-button
              link
              v-if="canWrite('ADMIN', 'HR')"
              @click="open(row)"
              >编辑</el-button
            ><el-button
              link
              type="danger"
              v-if="canWrite('ADMIN', 'HR')"
              @click="remove(row)"
              >删除</el-button
            ></template
          ></el-table-column
        ></el-table
      ></el-card
    ><el-dialog
      v-model="visible"
      :title="form.id ? '编辑班次' : '新增班次'"
      width="520px"
      ><el-form
        :model="form"
        label-width="100px"
        ><el-form-item
          v-for="item in fields"
          :key="item.key"
          :label="item.label"
          ><el-input v-model="form[item.key]" /></el-form-item
        ><el-form-item label="默认班次"><el-switch v-model="form.isDefault" /></el-form-item></el-form
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
import { attendanceApi } from '../../api/attendance'
import { canWrite } from '../../auth'
import PageShell from '../../components/PageShell.vue'
const fields = [
  { key: 'code', label: '编码' },
  { key: 'name', label: '名称' },
  { key: 'workStart', label: '上班时间' },
  { key: 'workEnd', label: '下班时间' },
  { key: 'restStart', label: '午休开始' },
  { key: 'restEnd', label: '午休结束' },
  { key: 'lateGraceMinutes', label: '迟到宽限' },
  { key: 'earlyGraceMinutes', label: '早退宽限' },
]
const rows = ref([])
const visible = ref(false)
const form = reactive({})
async function load() {
  rows.value = await attendanceApi.shifts()
}
function open(row = {}) {
  Object.assign(form, row)
  visible.value = true
}
async function save() {
  await attendanceApi.saveShift(form)
  visible.value = false
  ElMessage.success('保存成功')
  await load()
}
async function remove(row) {
  await ElMessageBox.confirm('确认删除班次？', '删除确认')
  await attendanceApi.deleteShift(row.id)
  ElMessage.success('删除成功')
  await load()
}
onMounted(load)
</script>
