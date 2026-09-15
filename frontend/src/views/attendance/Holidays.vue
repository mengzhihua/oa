<template>
  <PageShell
    title="节假日管理"
    code="ATT-HOLIDAY"
    ><template #actions
      ><el-button
        v-if="canWrite('ADMIN', 'HR')"
        type="primary"
        @click="open()"
        >新增节假日</el-button
      ></template
    ><el-card
      ><el-table :data="rows"
        ><el-table-column
          prop="date"
          label="日期"
        /><el-table-column
          prop="name"
          label="名称"
        /><el-table-column label="类型"
          ><template #default="{ row }"><StatusTag :value="row.type" /></template></el-table-column
        ><el-table-column label="操作"
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
      :title="form.id ? '编辑节假日' : '新增节假日'"
      width="420px"
      ><el-form
        :model="form"
        label-width="80px"
        ><el-form-item label="日期"
          ><el-date-picker
            v-model="form.date"
            value-format="YYYY-MM-DD" /></el-form-item
        ><el-form-item label="名称"><el-input v-model="form.name" /></el-form-item
        ><el-form-item label="类型"
          ><el-select v-model="form.type"
            ><el-option
              label="法定节假日"
              value="HOLIDAY" /><el-option
              label="调休上班"
              value="WORKDAY" /></el-select></el-form-item></el-form
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
import StatusTag from '../../components/StatusTag.vue'
const rows = ref([])
const visible = ref(false)
const form = reactive({ type: 'HOLIDAY' })
async function load() {
  rows.value = await attendanceApi.holidays({})
}
function open(row = {}) {
  Object.assign(form, { id: row.id, date: row.date, name: row.name, type: row.type || 'HOLIDAY' })
  visible.value = true
}
async function save() {
  await attendanceApi.saveHoliday(form)
  visible.value = false
  ElMessage.success('保存成功')
  await load()
}
async function remove(row) {
  await ElMessageBox.confirm('确认删除节假日？', '删除确认')
  await attendanceApi.deleteHoliday(row.id)
  ElMessage.success('删除成功')
  await load()
}
onMounted(load)
</script>
