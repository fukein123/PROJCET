<template>
  <div>
    <PortalNavBar />
    <main class="portal-self-shell">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import PortalNavBar from '@/views/portal/PortalNavBar.vue'
</script>

<style scoped>
.portal-self-shell {
  width: min(1180px, calc(100% - 24px));
  margin: 14px auto 40px;
}
</style>
