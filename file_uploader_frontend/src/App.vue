<script setup lang="ts">
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
                  import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElAvatar, ElDropdown, ElDropdownMenu, ElDropdownItem } from 'element-plus'
import { ArrowDown, User, Setting, SwitchButton } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)
const loginUser = computed(() => userStore.loginUser)
const isAdmin = computed(() => userStore.isAdmin)

// 初始化用户状态
onMounted(() => {
  userStore.initUserState()
})

// 退出登录
const handleLogout = async () => {
  await userStore.logout()
  // 退出登录后刷新页面
  window.location.reload()
}

// 跳转到用户中心
const goToUserCenter = () => {
  router.push('/user/center')
}

// 跳转到用户管理
const goToUserManage = () => {
  router.push('/user/manage')
}
</script>

<template>
  <header class="app-header">
    <div class="header-container">
      <div class="left-section">
        <div class="logo-container">
          <img alt="File Uploader" class="logo" src="@/assets/logo.jpg" />
          <span class="app-name">云传输</span>
        </div>

        <nav class="nav-menu">
          <template v-if="isLoggedIn">
            <RouterLink to="/file/upload" class="nav-link">文件上传</RouterLink>
            <RouterLink to="/file/list" class="nav-link">文件列表</RouterLink>
          </template>
          <template v-else>
            <RouterLink to="/login" class="nav-link">登录</RouterLink>
          </template>
        </nav>
      </div>

      <div class="right-section">
        <div class="user-menu">
          <template v-if="isLoggedIn">
            <el-dropdown trigger="click">
              <span class="user-info">
                <el-avatar :size="36" :src="loginUser?.userAvatar" />
                <span class="username">{{ loginUser?.userName || '用户' }}</span>
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="goToUserCenter">
                    <el-icon><user /></el-icon>个人中心
                  </el-dropdown-item>
                  <el-dropdown-item v-if="isAdmin" @click="goToUserManage">
                    <el-icon><setting /></el-icon>用户管理
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    <el-icon><switch-button /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <RouterLink to="/login" class="login-btn">登录</RouterLink>
            <RouterLink to="/register" class="register-btn">注册</RouterLink>
          </template>
        </div>
      </div>
    </div>
  </header>

  <main class="app-main">
    <RouterView />
  </main>
</template>

<style scoped>
.app-header {
  background-color: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 1000;
  border-bottom: 1px solid #ebeef5;
}

.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
  max-width: 1400px;
  margin: 0 auto;
}

.left-section {
  display: flex;
  align-items: center;
}

.right-section {
  display: flex;
  align-items: center;
}

.logo-container {
  display: flex;
  align-items: center;
  margin-right: 40px;
}

.logo {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  margin-right: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.app-name {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  letter-spacing: 0.5px;
}

.nav-menu {
  display: flex;
  align-items: center;
}

.nav-link {
  margin: 0 16px;
  text-decoration: none;
  color: #606266;
  font-weight: 500;
  font-size: 15px;
  transition: all 0.3s;
  position: relative;
  padding: 5px 0;
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background-color: #409eff;
  transition: all 0.3s;
  transform: translateX(-50%);
}

.nav-link:hover,
.nav-link.router-link-active {
  color: #409eff;
}

.nav-link:hover::after,
.nav-link.router-link-active::after {
  width: 100%;
}

.user-menu {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: all 0.3s;
}

.user-info:hover {
  background-color: rgba(0, 0, 0, 0.04);
}

.username {
  margin-left: 10px;
  font-size: 15px;
  color: #303133;
  font-weight: 500;
}

.login-btn,
.register-btn {
  padding: 8px 16px;
  margin-left: 10px;
  border-radius: 20px;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.login-btn {
  color: #409eff;
  border: 1px solid #d9ecff;
  background-color: #ecf5ff;
}

.login-btn:hover {
  background-color: #409eff;
  color: #fff;
  border-color: #409eff;
}

.register-btn {
  color: #fff;
  background-color: #409eff;
  border: 1px solid #409eff;
}

.register-btn:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

.app-main {
  min-height: calc(100vh - 64px);
  background-color: #f5f7fa;
}

@media (max-width: 768px) {
  .header-container {
    padding: 0 16px;
  }

  .left-section {
    flex: 1;
  }

  .logo-container {
    margin-right: 20px;
  }

  .nav-menu {
    display: none;
  }

  .app-name {
    font-size: 18px;
  }

  .username {
    display: none;
  }

  .user-info {
    padding: 5px;
  }

  .right-section {
    flex-shrink: 0;
  }
}

/* Element Plus 下拉菜单样式优化 */
:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
}

:deep(.el-dropdown-menu__item .el-icon) {
  margin-right: 8px;
}
</style>
