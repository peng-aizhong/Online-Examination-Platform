<template>
  <el-container>
    <router-view></router-view>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { onMounted } from 'vue'

const router = useRouter()

onMounted(() => {
  const token = localStorage.getItem('token')
  if (!token && router.currentRoute.value.path !== '/login' && router.currentRoute.value.path !== '/register') {
    router.push('/login')
  }
})
</script>

<style scoped>
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #555;
}
</style>
