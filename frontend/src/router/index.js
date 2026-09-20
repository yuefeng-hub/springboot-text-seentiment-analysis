import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/analyze',
    children: [
      { path: 'analyze', component: () => import('../views/Analyze.vue'), meta: { title: '情感分析' } },
      { path: 'quiz', component: () => import('../views/Quiz.vue'), meta: { title: '情感自测' } },
      { path: 'chat', component: () => import('../views/AiChat.vue'), meta: { title: 'AI问答' } },
      { path: 'history', component: () => import('../views/History.vue'), meta: { title: '历史记录' } },
      { path: 'stats', component: () => import('../views/Stats.vue'), meta: { title: '统计分析' } },
      { path: 'dict', component: () => import('../views/Dict.vue'), meta: { title: '词典管理', admin: true } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 登录守卫：未登录跳转登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
