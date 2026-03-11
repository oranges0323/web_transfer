import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/file/list'
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue'),
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
    },
    {
      path: '/user/center',
      name: 'userCenter',
      component: () => import('../views/UserCenterView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/user/manage',
      name: 'userManage',
      component: () => import('../views/UserManageView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/file/upload',
      name: 'fileUpload',
      component: () => import('../views/FileUploadView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/file/list',
      name: 'fileList',
      component: () => import('../views/FileListView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/file/detail/:id',
      name: 'fileDetail',
      component: () => import('../views/FileDetailView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/my-uploads',
      name: 'myUploads',
      component: () => import('../views/MyUploadsView.vue'),
      meta: { requiresAuth: true }
    },
  ],
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  // 检查路由是否需要认证
  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }
  
  // 检查路由是否需要管理员权限
  if (to.meta.requiresAdmin && token) {
    // 这里应该从store获取用户信息，但为了避免循环依赖，我们暂时使用localStorage
    const userRole = localStorage.getItem('userRole')
    if (userRole !== 'admin') {
      next('/')
      return
    }
  }
  
  next()
})

export default router
