<template>
  <el-select
    v-model="value"
    filterable
    remote
    clearable
    :remote-method="search"
    :loading="loading"
    placeholder="搜索员工"
    @change="$emit('update:modelValue', $event)"
  >
    <el-option
      v-for="item in options"
      :key="item.id"
      :label="`${item.name} (${item.employeeNo})`"
      :value="item.id"
    />
  </el-select>
</template>

<script setup>
import { computed, ref } from 'vue'
import { hrApi } from '../api/hr'

const props = defineProps({ modelValue: [Number, String] })
const emit = defineEmits(['update:modelValue'])
const options = ref([])
const loading = ref(false)
const value = computed({
  get: () => props.modelValue,
  set: (next) => emit('update:modelValue', next),
})
async function search(keyword) {
  if (!keyword) return
  loading.value = true
  try {
    const data = await hrApi.employees({ page: 1, size: 20, keyword })
    options.value = data.records || []
  } finally {
    loading.value = false
  }
}
</script>
