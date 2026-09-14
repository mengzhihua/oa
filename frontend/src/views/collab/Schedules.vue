<template>
  <PageShell
    title="我的日程"
    code="CO-SCHEDULE"
    ><template #actions
      ><el-button
        type="primary"
        @click="visible = true"
        >新建日程</el-button
      ></template
    ><el-card
      ><div class="week-grid">
        <div
          v-for="day in days"
          :key="day.key"
          class="week-column"
        >
          <strong>{{ day.label }}</strong
          ><el-card
            v-for="item in grouped[day.key]"
            :key="item.id"
            class="schedule-card"
            ><b>{{ item.title }}</b>
            <p>{{ fmtDateTime(item.startTime) }}</p>
            <span>{{ item.location }}</span></el-card
          >
        </div>
      </div></el-card
    ><el-dialog
      v-model="visible"
      title="新建日程"
      ><el-form
        :model="form"
        label-width="80px"
        ><el-form-item label="标题"><el-input v-model="form.title" /></el-form-item
        ><el-form-item label="开始"
          ><el-date-picker
            v-model="form.startTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item
        ><el-form-item label="结束"
          ><el-date-picker
            v-model="form.endTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item
        ><el-form-item label="地点"><el-input v-model="form.location" /></el-form-item
        ><el-form-item label="提醒分钟"
          ><el-input-number
            v-model="form.remindMinutes"
            :min="0" /></el-form-item></el-form
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { collabApi } from '../../api/collab'
import PageShell from '../../components/PageShell.vue'
import { fmtDateTime } from '../../utils/format'
const visible = ref(false)
const rows = ref([])
const form = reactive({ title: '', startTime: '', endTime: '', location: '', remindMinutes: 10 })
const days = computed(() =>
  Array.from({ length: 7 }, (_, index) => {
    const date = new Date()
    date.setDate(date.getDate() - date.getDay() + index)
    return { key: date.toISOString().slice(0, 10), label: `${date.getMonth() + 1}/${date.getDate()}` }
  }),
)
const grouped = computed(() =>
  Object.fromEntries(
    days.value.map((day) => [
      day.key,
      rows.value.filter((row) => String(row.startTime || '').startsWith(day.key)),
    ]),
  ),
)
async function load() {
  rows.value = await collabApi.schedules({})
}
async function save() {
  await collabApi.createSchedule(form)
  visible.value = false
  ElMessage.success('日程已创建')
  await load()
}
onMounted(load)
</script>
