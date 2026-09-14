<template>
  <PageShell
    title="菜单管理"
    code="SYS-MENU"
    description="查看系统菜单树和菜单排序"
    ><el-card
      ><el-table
        :data="treeRows"
        row-key="id"
        default-expand-all
        ><el-table-column
          prop="name"
          label="菜单名称" /><el-table-column
          prop="code"
          label="编码" /><el-table-column
          prop="path"
          label="路由" /><el-table-column
          prop="type"
          label="类型" /><el-table-column
          prop="sort"
          label="排序" /><el-table-column label="状态"
          ><template #default="{ row }"
            ><StatusTag
              :value="row.status ? 'ACTIVE' : 'INACTIVE'" /></template></el-table-column></el-table></el-card
  ></PageShell>
</template>
<script setup>
import { computed, onMounted, ref } from 'vue'
import { systemApi } from '../../api/system'
import PageShell from '../../components/PageShell.vue'
import StatusTag from '../../components/StatusTag.vue'
const rows = ref([])
const treeRows = computed(() => {
  const map = Object.fromEntries(rows.value.map((item) => [item.id, { ...item, children: [] }]))
  const roots = []
  rows.value.forEach((item) =>
    item.parentId && map[item.parentId]
      ? map[item.parentId].children.push(map[item.id])
      : roots.push(map[item.id]),
  )
  return roots
})
onMounted(async () => {
  rows.value = await systemApi.menus()
})
</script>
