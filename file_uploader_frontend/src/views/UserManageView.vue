
<template>
  <div class="user-manage-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>用户管理</h2>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-container">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="账号">
            <el-input
              v-model="searchForm.userAccount"
              placeholder="请输入账号"
              clearable
            />
          </el-form-item>
          <el-form-item label="用户名">
            <el-input
              v-model="searchForm.userName"
              placeholder="请输入用户名"
              clearable
            />
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="searchForm.userRole" placeholder="请选择角色" clearable>
              <el-option label="管理员" value="admin" />
              <el-option label="普通用户" value="user" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 表格区域 -->
      <el-table
        v-loading="loading"
        :data="userList"
        style="width: 100%"
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userAccount" label="账号" />
        <el-table-column prop="userName" label="用户名" />
        <el-table-column prop="userProfile" label="个人简介" show-overflow-tooltip />
        <el-table-column prop="userRole" label="角色">
          <template #default="scope">
            <el-tag :type="scope.row.userRole === 'admin' ? 'danger' : 'primary'">
              {{ scope.row.userRole === 'admin' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button
              link
              type="primary"
              size="small"
              @click="handleViewUser(scope.row)"
            >
              查看
            </el-button>
            <el-button
              link
              type="primary"
              size="small"
              @click="handleEditUser(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              @click="handleDeleteUser(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 查看用户详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="用户详情" width="30%">
      <div class="user-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="ID">{{ currentUser?.id }}</el-descriptions-item>
          <el-descriptions-item label="账号">{{ currentUser?.userAccount }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentUser?.userName }}</el-descriptions-item>
          <el-descriptions-item label="个人简介">{{ currentUser?.userProfile }}</el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag :type="currentUser?.userRole === 'admin' ? 'danger' : 'primary'">
              {{ currentUser?.userRole === 'admin' ? '管理员' : '普通用户' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentUser?.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ currentUser?.updateTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 编辑用户对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑用户" width="30%">
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="editForm.userName" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="个人简介" prop="userProfile">
          <el-input
            v-model="editForm.userProfile"
            type="textarea"
            :rows="4"
            placeholder="请输入个人简介"
          />
        </el-form-item>
        <el-form-item label="角色" prop="userRole">
          <el-select v-model="editForm.userRole" placeholder="请选择角色">
            <el-option label="管理员" value="admin" />
            <el-option label="普通用户" value="user" />
          </el-select>
        </el-form-item>
        <el-form-item label="头像" prop="userAvatar">
          <el-input v-model="editForm.userAvatar" placeholder="请输入头像URL" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="editLoading" @click="handleConfirmEdit">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { listUserVoByIdUsingGet, deleteUserUsingPost, updateUserUsingPost } from '@/api/userController'
import type { UserVO, UserUpdateRequest } from '@/api/typings'

// 加载状态
const loading = ref(false)
const editLoading = ref(false)

// 用户列表
const userList = ref<UserVO[]>([])

// 分页参数
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  userAccount: '',
  userName: '',
  userRole: ''
})

// 当前查看的用户
const currentUser = ref<UserVO | null>(null)
const viewDialogVisible = ref(false)

// 编辑表单
const editFormRef = ref<FormInstance>()
const editDialogVisible = ref(false)
const editForm = reactive<UserUpdateRequest>({
  id: 0,
  userName: '',
  userProfile: '',
  userRole: '',
  userAvatar: ''
})

// 编辑表单验证规则
const editRules: FormRules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  userProfile: [
    { max: 200, message: '个人简介不能超过200个字符', trigger: 'blur' }
  ],
  userRole: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 获取用户列表
const getUserList = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    const res = await listUserVoByIdUsingGet(params)
    if (res.data) {
      userList.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  getUserList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.userAccount = ''
  searchForm.userName = ''
  searchForm.userRole = ''
  handleSearch()
}

// 分页大小改变
const handleSizeChange = (val: number) => {
  pagination.pageSize = val
  getUserList()
}

// 当前页改变
const handleCurrentChange = (val: number) => {
  pagination.current = val
  getUserList()
}

// 查看用户详情
const handleViewUser = (user: UserVO) => {
  currentUser.value = user
  viewDialogVisible.value = true
}

// 编辑用户
const handleEditUser = (user: UserVO) => {
  editForm.id = user.id || 0
  editForm.userName = user.userName || ''
  editForm.userProfile = user.userProfile || ''
  editForm.userRole = user.userRole || ''
  editForm.userAvatar = user.userAvatar || ''
  editDialogVisible.value = true
}

// 确认编辑
const handleConfirmEdit = async () => {
  if (!editFormRef.value) return

  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      editLoading.value = true
      try {
        await updateUserUsingPost(editForm)
        ElMessage.success('更新成功')
        editDialogVisible.value = false
        getUserList()
      } catch (error) {
        ElMessage.error('更新失败')
      } finally {
        editLoading.value = false
      }
    }
  })
}

// 删除用户
const handleDeleteUser = (user: UserVO) => {
  ElMessageBox.confirm(
    `确定要删除用户 "${user.userName}" 吗？此操作不可恢复！`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        await deleteUserUsingPost({ id: user.id })
        ElMessage.success('删除成功')
        getUserList()
      } catch (error) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {
      // 用户取消删除
    })
}

// 初始化
onMounted(() => {
  getUserList()
})
</script>

<style scoped>
.user-manage-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  color: #409eff;
}

.search-container {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.user-detail {
  margin: 10px 0;
}
</style>
