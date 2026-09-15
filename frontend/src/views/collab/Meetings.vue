<template>
  <PageShell
    title="会议室预约"
    code="CO-MEETING"
    ><template #actions
      ><el-button
        v-if="canWrite('ADMIN', 'HR')"
        @click="roomVisible = true"
        >新增会议室</el-button
      ><el-button
        type="primary"
        @click="visible = true"
        >新建预约</el-button
      ></template
    ><el-row :gutter="16"
      ><el-col
        v-for="room in rooms"
        :key="room.id"
        :span="8"
        ><el-card
          ><h3>{{ room.name }}</h3>
          <p>{{ room.location }} · {{ room.capacity }} 人</p>
          <p>{{ room.equipment }}</p></el-card
        ></el-col
      ></el-row
    ><el-card class="section-card"
      ><el-table :data="bookings"
        ><el-table-column
          prop="roomId"
          label="会议室"
        /><el-table-column
          prop="title"
          label="主题"
        /><el-table-column label="开始"
          ><template #default="{ row }">{{ fmtDateTime(row.startTime) }}</template></el-table-column
        ><el-table-column label="结束"
          ><template #default="{ row }">{{ fmtDateTime(row.endTime) }}</template></el-table-column
        ><el-table-column
          prop="status"
          label="状态"
          ><template #default="{ row }"><StatusTag :value="row.status" /></template></el-table-column
        ><el-table-column label="操作"
          ><template #default="{ row }"
            ><el-button
              link
              type="danger"
              @click="cancel(row)"
              >取消</el-button
            ></template
          ></el-table-column
        ></el-table
      ></el-card
    ><el-dialog
      v-model="roomVisible"
      title="新增会议室"
      ><el-form
        :model="roomForm"
        label-width="80px"
        ><el-form-item label="名称"><el-input v-model="roomForm.name" /></el-form-item
        ><el-form-item label="地点"><el-input v-model="roomForm.location" /></el-form-item
        ><el-form-item label="容量"
          ><el-input-number
            v-model="roomForm.capacity"
            :min="1" /></el-form-item
        ><el-form-item label="设备"><el-input v-model="roomForm.equipment" /></el-form-item></el-form
      ><template #footer
        ><el-button @click="roomVisible = false">取消</el-button
        ><el-button
          type="primary"
          @click="saveRoom"
          >保存</el-button
        ></template
      ></el-dialog
    ><el-dialog
      v-model="visible"
      title="新建预约"
      ><el-form
        :model="form"
        label-width="80px"
        ><el-form-item label="会议室"
          ><el-select v-model="form.roomId"
            ><el-option
              v-for="room in rooms"
              :key="room.id"
              :label="room.name"
              :value="room.id" /></el-select></el-form-item
        ><el-form-item label="主题"><el-input v-model="form.title" /></el-form-item
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
        ><el-form-item label="参会人"
          ><el-input
            v-model="form.attendees"
            placeholder="用户 ID，逗号分隔" /></el-form-item></el-form
      ><template #footer
        ><el-button @click="visible = false">取消</el-button
        ><el-button
          type="primary"
          @click="save"
          >预约</el-button
        ></template
      ></el-dialog
    ></PageShell
  >
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { collabApi } from '../../api/collab'
import { canWrite } from '../../auth'
import PageShell from '../../components/PageShell.vue'
import StatusTag from '../../components/StatusTag.vue'
import { fmtDateTime } from '../../utils/format'
const rooms = ref([])
const bookings = ref([])
const visible = ref(false)
const roomVisible = ref(false)
const form = reactive({})
const roomForm = reactive({ name: '', location: '', capacity: 10, equipment: '', status: 'ACTIVE' })
async function load() {
  rooms.value = await collabApi.rooms()
  bookings.value = await collabApi.bookings({})
}
async function save() {
  await collabApi.createBooking({ ...form, attendees: form.attendees?.split(',').map(Number) })
  visible.value = false
  ElMessage.success('预约成功')
  await load()
}
async function saveRoom() {
  await collabApi.createRoom(roomForm)
  roomVisible.value = false
  ElMessage.success('会议室已新增')
  await load()
}
async function cancel(row) {
  await collabApi.cancelBooking(row.id)
  ElMessage.success('已取消')
  await load()
}
onMounted(load)
</script>
