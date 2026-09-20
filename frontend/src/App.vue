<template>
  <div class="app-root">
    <router-view />
    <SplashScreen v-if="showSplash" @done="showSplash = false" />
    <!-- 全局漂浮霓虹光点 -->
    <span class="cyber-spark" style="left:12%;top:65%;--dx:220px;--dy:-430px;--sd:13s;--dl:0s"></span>
    <span class="cyber-spark sp-pink" style="left:78%;top:20%;--dx:-260px;--dy:380px;--sd:15s;--dl:2s"></span>
    <span class="cyber-spark sp-gold" style="left:45%;top:82%;--dx:200px;--dy:-360px;--sd:12s;--dl:5s"></span>
    <span class="cyber-spark" style="left:88%;top:70%;--dx:-180px;--dy:-320px;--sd:16s;--dl:8s"></span>
    <span class="cyber-spark sp-pink" style="left:30%;top:12%;--dx:260px;--dy:320px;--sd:14s;--dl:11s"></span>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import SplashScreen from './components/SplashScreen.vue'

const showSplash = ref(true)
</script>

<style scoped>
.app-root {
  position: relative;
  min-height: 100vh;
}
</style>
