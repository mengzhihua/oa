<template>
  <el-result
    icon="info"
    title="页面不存在"
    sub-title="请检查访问地址是否正确"
  >
    <template #extra
      ><el-button
        type="primary"
        @click="$router.push('/dashboard')"
        >返回工作台</el-button
      ></template
    >
  </el-result>
</template>
