<template>
  <PageShell
    title="部门管理"
    code="ORG-DEPT"
    description="维护组织层级、部门负责人和排序"
  >
    <template #actions
      ><el-button
        v-if="canWrite('ADMIN', 'HR')"
        type="primary"
        @click="open()"
        >新增部门</el-button
      ></template
    >
    <div class="split-panel">
      <el-card class="tree-card"
        ><template #header>组织树</template
        ><el-tree
          :data="tree"
          node-key="id"
          :props="{ label: 'name' }"
          default-expand-all
      /></el-card>
      <el-card
        ><el-table
          :data="rows"
          stripe
          ><el-table-column
            prop="name"
            label="部门名称"
          /><el-table-column
            prop="code"
            label="编码"
            width="140"
          /><el-table-column
            prop="parentId"
            label="上级部门"
            width="100"
          /><el-table-column
            prop="leaderEmployeeId"
            label="主管员工"
            width="110"
          /><el-table-column
            prop="sort"
            label="排序"
            width="80"
          /><el-table-column
            label="操作"
            width="180"
            ><template #default="{ row }"
              ><el-button
                v-if="canWrite('ADMIN', 'HR')"
                link
                type="primary"
                @click="open(row)"
                >编辑</el-button
              ><el-button
                v-if="canWrite('ADMIN', 'HR')"
                link
                type="danger"
                @click="remove(row)"
                >删除</el-button
              ></template
            ></el-table-column
          ></el-table
        ></el-card
      >
    </div>
    <el-dialog
      v-model="visible"
      :title="form.id ? '编辑部门' : '新增部门'"
      width="480px"
    >
      <el-form
        :model="form"
        label-width="90px"
      >
        <el-form-item label="上级部门"
          ><DeptTreeSelect
            v-model="form.parentId"
            :data="rows"
        /></el-form-item>
        <el-form-item
          label="部门名称"
          required
          ><el-input v-model="form.name"
        /></el-form-item>
        <el-form-item
          label="部门编码"
          required
          ><el-input v-model="form.code"
        /></el-form-item>
        <el-form-item label="部门主管"><EmployeeSelect v-model="form.leaderEmployeeId" /></el-form-item>
        <el-form-item label="排序"
          ><el-input-number
            v-model="form.sort"
            :min="0"
        /></el-form-item>
      </el-form>
      <template #footer
        ><el-button @click="visible = false">取消</el-button
        ><el-button
          type="primary"
          @click="save"
          >保存</el-button
        ></template
      >
    </el-dialog>
  </PageShell>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orgApi } from '../../api/org'
import { canWrite } from '../../auth'
import DeptTreeSelect from '../../components/DeptTreeSelect.vue'
import EmployeeSelect from '../../components/EmployeeSelect.vue'
import PageShell from '../../components/PageShell.vue'

const rows = ref([])
const visible = ref(false)
const form = reactive({})
const tree = computed(() => {
  const map = Object.fromEntries(rows.value.map((item) => [item.id, { ...item, children: [] }]))
  const roots = []
  rows.value.forEach((item) =>
    item.parentId && map[item.parentId]
      ? map[item.parentId].children.push(map[item.id])
      : roots.push(map[item.id]),
  )
  return roots
})
async function load() {
  rows.value = await orgApi.depts()
}
function open(row = {}) {
  Object.assign(form, {
    id: row.id,
    parentId: row.parentId,
    name: row.name,
    code: row.code,
    leaderEmployeeId: row.leaderEmployeeId,
    sort: row.sort || 0,
  })
  visible.value = true
}
async function save() {
  if (!form.name || !form.code) return ElMessage.warning('请填写部门名称和编码')
  if (form.id) await orgApi.updateDept(form.id, form)
  else await orgApi.createDept(form)
  visible.value = false
  ElMessage.success('保存成功')
  await load()
}
async function remove(row) {
  await ElMessageBox.confirm(`确认删除部门“${row.name}”？`, '删除确认', { type: 'warning' })
  await orgApi.deleteDept(row.id)
  ElMessage.success('删除成功')
  await load()
}
onMounted(load)
</script>
