<template>
  <PageShell
    title="字典管理"
    code="SYS-DICT"
    ><el-card
      ><el-form inline
        ><el-form-item label="字典类型"><el-input v-model="type" /></el-form-item
        ><el-button
          type="primary"
          @click="load"
          >查询</el-button
        ></el-form
      ><el-table :data="rows"
        ><el-table-column
          prop="type"
          label="类型" /><el-table-column
          prop="label"
          label="标签" /><el-table-column
          prop="value"
          label="值" /><el-table-column
          prop="sort"
          label="排序" /><el-table-column
          prop="status"
          label="状态" /></el-table></el-card
  ></PageShell>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { systemApi } from '../../api/system'
import PageShell from '../../components/PageShell.vue'
const type = ref('')
const rows = ref([])
async function load() {
  rows.value = await systemApi.dicts({ type: type.value || undefined })
}
onMounted(load)
</script>
