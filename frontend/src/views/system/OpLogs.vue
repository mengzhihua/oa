<template>
  <PageShell
    title="操作日志"
    code="SYS-OPLOG"
    ><el-card
      ><el-form inline
        ><el-form-item label="页码"
          ><el-input-number
            v-model="page"
            :min="1" /></el-form-item
        ><el-button
          type="primary"
          @click="load"
          >查询</el-button
        ></el-form
      ><el-table :data="rows"
        ><el-table-column
          prop="username"
          label="用户"
        /><el-table-column
          prop="method"
          label="方法"
        /><el-table-column
          prop="uri"
          label="接口"
        /><el-table-column
          prop="success"
          label="结果"
        /><el-table-column label="时间"
          ><template #default="{ row }">{{ fmtDateTime(row.createdAt) }}</template></el-table-column
        ></el-table
      ><TablePager
        :total="total"
        :page="page"
        :size="size"
        @change="change" /></el-card
  ></PageShell>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { systemApi } from '../../api/system'
import PageShell from '../../components/PageShell.vue'
import TablePager from '../../components/TablePager.vue'
import { fmtDateTime } from '../../utils/format'
const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
async function load() {
  const data = await systemApi.logs({ page: page.value, size: size.value })
  rows.value = data.records || []
  total.value = data.total || 0
}
function change(value) {
  page.value = value
  load()
}
onMounted(load)
</script>
