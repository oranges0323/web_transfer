
<template>
  <div class="user-center-container">
    <el-card class="user-card">
      <template #header>
        <div class="card-header">
          <h2>个人中心</h2>
        </div>
      </template>

      <div class="user-info">
        <div class="avatar-container">
          <el-avatar :size="100" :src="loginUser?.userAvatar || defaultAvatar" />
          <el-button class="change-avatar-btn" size="small" @click="showAvatarDialog = true">
            更换头像
          </el-button>
        </div>

        <el-form
          ref="userFormRef"
          :model="userForm"
          :rules="userRules"
          label-width="80px"
          class="user-form"
        >
          <el-form-item label="账号">
            <el-input :value="loginUser?.userAccount" disabled />
          </el-form-item>
          <el-form-item label="用户名" prop="userName">
            <el-input v-model="userForm.userName" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="个人简介" prop="userProfile">
            <el-input
              v-model="userForm.userProfile"
              type="textarea"
              :rows="4"
              placeholder="请输入个人简介"
            />
          </el-form-item>
          <el-form-item label="角色">
            <el-tag :type="loginUser?.userRole === 'admin' ? 'danger' : 'primary'">
              {{ loginUser?.userRole === 'admin' ? '管理员' : '普通用户' }}
            </el-tag>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleUpdateUser">
              保存修改
            </el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 修改头像对话框 -->
    <el-dialog v-model="showAvatarDialog" title="更换头像" width="30%">
      <el-form>
        <el-form-item label="头像URL">
          <el-input v-model="avatarUrl" placeholder="请输入头像URL" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAvatarDialog = false">取消</el-button>
          <el-button type="primary" @click="handleUpdateAvatar">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updateUserUsingPost } from '@/api/userController'
import type { UserUpdateRequest } from '@/api/typings'

const userStore = useUserStore()
const userFormRef = ref<FormInstance>()
const loading = ref(false)
const showAvatarDialog = ref(false)
const avatarUrl = ref('')

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 当前登录用户
const loginUser = computed(() => userStore.loginUser)

// 用户表单数据
const userForm = reactive<UserUpdateRequest>({
  id: 0,
  userName: '',
  userProfile: '',
  userAvatar: '',
  userRole: ''
})

// 表单验证规则
const userRules: FormRules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  userProfile: [
    { max: 200, message: '个人简介不能超过200个字符', trigger: 'blur' }
  ]
}

// 重置表单
const resetForm = () => {
  userForm.userName = loginUser.value?.userName || ''
  userForm.userProfile = loginUser.value?.userProfile || ''
  userForm.userAvatar = loginUser.value?.userAvatar || ''
}

// 更新用户信息
const handleUpdateUser = async () => {
  if (!userFormRef.value) return

  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await updateUserUsingPost(userForm)
        ElMessage.success('更新成功')
        // 重新获取用户信息
        await userStore.fetchLoginUser()
      } catch (error) {
        ElMessage.error('更新失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 更新头像
const handleUpdateAvatar = async () => {
  if (!avatarUrl.value) {
    ElMessage.warning('请输入头像URL')
    return
  }

  userForm.userAvatar = avatarUrl.value
  await handleUpdateUser()
  showAvatarDialog.value = false
}

// 初始化表单数据
const initForm = () => {
  if (loginUser.value) {
    userForm.id = loginUser.value.id || 0
    userForm.userName = loginUser.value.userName || ''
    userForm.userProfile = loginUser.value.userProfile || ''
    userForm.userAvatar = loginUser.value.userAvatar || ''
    userForm.userRole = loginUser.value.userRole || ''
  }
}

// 监听用户信息变化，更新表单数据
watch(loginUser, (newUser) => {
  if (newUser) {
    initForm()
  }
}, { deep: true })

// 初始化
onMounted(() => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    // 跳转到登录页
    userStore.$patch(() => {
      // 这里需要使用 router，但当前没有引入，所以暂时注释
      // router.push('/login')
    })
  } else {
    // 初始化表单数据
    initForm()
  }
})
</script>

<style scoped>
.user-center-container {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.user-card {
  width: 100%;
  max-width: 800px;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0;
  color: #409eff;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}

.change-avatar-btn {
  margin-top: 10px;
}

.user-form {
  width: 100%;
  max-width: 500px;
}

@media (min-width: 768px) {
  .user-info {
    flex-direction: row;
  }

  .avatar-container {
    margin-right: 30px;
    margin-bottom: 0;
  }

  .user-form {
    flex: 1;
  }
}
</style>
