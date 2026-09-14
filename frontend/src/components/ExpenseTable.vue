<template>
  <el-table :data="rows"
    ><el-table-column
      prop="title"
      label="标题"
    /><el-table-column
      prop="total"
      label="金额"
    /><el-table-column label="状态"
      ><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column
    ><el-table-column label="操作"
      ><template #default="{ row }"
        ><el-button
          v-if="row.status === 'APPROVED'"
          link
          type="primary"
          @click="$emit('pay', row)"
          >标记已付</el-button
        ></template
      ></el-table-column
    ></el-table
  >
</template>
<script setup>
import StatusTag from './StatusTag.vue'
defineProps({ rows: Array })
defineEmits(['pay'])
</script>
