<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElTable, ElTableColumn, ElButton, ElPagination, ElInput, ElSelect, ElOption, ElTag, ElDialog, ElForm, ElFormItem, ElInputNumber } from 'element-plus'
import { Search, Download, View, Delete, Lock, Unlock, Upload, Refresh } from '@element-plus/icons-vue'
import { listFileVoByIdUsingGet, deleteFileUsingPost, downloadFileUsingGet, encryptUsingPost, decryptUsingPost } from '@/api/fileController'

const router = useRouter()
const userStore = useUserStore()
const fileList = ref<API.FileInfoVO[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchName = ref('')
const fileType = ref('')
const isEncryption = ref<number | null>(null)
const dialogVisible = ref(false)
const currentFile = ref<API.FileInfoVO | null>(null)
const encryptForm = ref({
  filePassword: '',
  isEncryption: 1
})
const isDownloadOperation = ref(false)

// 获取文件列表
const fetchFileList = async () => {
  loading.value = true
  try {
    const params: any = {
      current: currentPage.value,
      pageSize: pageSize.value,
      sortField: 'createTime',
      sortOrder: 'descend'
    }

    // 不限制用户ID，显示所有用户上传的文件
    // params.userId = userStore.loginUser.id

    if (searchName.value) {
      params.name = searchName.value
    }

    if (fileType.value) {
      params.fileType = fileType.value
    }

    if (isEncryption.value !== null) {
      params.isEncryption = isEncryption.value
    }

    const response = await listFileVoByIdUsingGet(params)
    if (response.code === 0 && response.data) {
      fileList.value = response.data.records || []
      total.value = Number(response.data.total) || 0
    } else {
      ElMessage.error(response.message || '获取文件列表失败')
    }
  } catch (error) {
    console.error('获取文件列表出错:', error)
    ElMessage.error('获取文件列表时发生错误')
  } finally {
    loading.value = false
  }
}

// 页面加载时获取文件列表
onMounted(() => {
  fetchFileList()
})

// 处理页码变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchFileList()
}

// 处理每页显示数量变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchFileList()
}

// 搜索文件
const handleSearch = () => {
  currentPage.value = 1
  fetchFileList()
}

// 重置搜索条件
const handleReset = () => {
  searchName.value = ''
  fileType.value = ''
  isEncryption.value = null
  currentPage.value = 1
  fetchFileList()
}

// 跳转到文件上传页面
const goToUpload = () => {
  router.push('/file/upload')
}

// 查看文件详情
const viewFileDetail = (file: API.FileInfoVO) => {
  router.push(`/file/detail/${file.id}`)
}

// 下载文件
const downloadFile = async (file: API.FileInfoVO) => {
  try {
    // 如果是加密文件，需要先输入密码
    if (file.isEncryption === 1) {
      currentFile.value = file
      encryptForm.value.filePassword = ''
      dialogVisible.value = true
      // 标记当前操作是下载
      isDownloadOperation.value = true
      return
    }

    const blob = await downloadFileUsingGet({ id: String(file.id) })
    if (blob) {
      // 创建一个隐藏的a标签来触发下载
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = file.name || 'download'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      ElMessage.success('文件下载成功')
    } else {
      ElMessage.error('下载失败')
    }
  } catch (error) {
    console.error('下载文件出错:', error)
    ElMessage.error('下载文件时发生错误')
  }
}

// 删除文件
const deleteFile = async (file: API.FileInfoVO) => {
  try {
    const response = await deleteFileUsingPost({ id: file.id })
    if (response.code === 0 && response.data) {
      ElMessage.success('文件删除成功')
      fetchFileList() // 刷新列表
    } else {
      ElMessage.error(response.message || '文件删除失败')
    }
  } catch (error) {
    console.error('删除文件出错:', error)
    ElMessage.error('删除文件时发生错误')
  }
}

// 打开加密对话框
const openEncryptDialog = (file: API.FileInfoVO) => {
  currentFile.value = file
  encryptForm.value.filePassword = ''
  dialogVisible.value = true
}

// 确认加密/解密
const confirmEncryption = async () => {
  if (!currentFile.value) return

  try {
    // 如果是下载操作
    if (isDownloadOperation.value) {
      if (!encryptForm.value.filePassword) {
        ElMessage.warning('请输入解密密码')
        return
      }

      const response = await downloadFileUsingGet({
        id: String(currentFile.value.id),
        filePassword: encryptForm.value.filePassword
      })

      if (response && response.url) {
        // 创建一个隐藏的a标签来触发下载
        const link = document.createElement('a')
        link.href = response.url
        link.download = currentFile.value.name || 'download'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        ElMessage.success('文件下载成功')
      } else {
        ElMessage.error('获取下载链接失败')
      }

      dialogVisible.value = false
      isDownloadOperation.value = false
      return
    }

    let response
    if (currentFile.value.isEncryption === 1) {
      // 当前已加密，执行解密操作
      if (!encryptForm.value.filePassword) {
        ElMessage.warning('请输入解密密码')
        return
      }

      response = await decryptUsingPost({
        fileId: currentFile.value.id,
        filePassword: encryptForm.value.filePassword
      })
      if (response.code === 0 && response.data) {
        ElMessage.success('文件解密成功')
      } else {
        ElMessage.error(response.message || '文件解密失败')
      }
    } else {
      // 当前未加密，执行加密操作
      if (!encryptForm.value.filePassword) {
        ElMessage.warning('请输入加密密码')
        return
      }

      response = await encryptUsingPost({
        id: currentFile.value.id,
        isEncryption: 1,
        filePassword: encryptForm.value.filePassword
      })

      if (response.code === 0 && response.data) {
        ElMessage.success('文件加密成功')
      } else {
        ElMessage.error(response.message || '文件加密失败')
      }
    }

    dialogVisible.value = false
    fetchFileList() // 刷新列表
  } catch (error) {
    console.error('加密/解密出错:', error)
    ElMessage.error('加密/解密过程中发生错误')
  }
}

// 格式化文件大小
const formatFileSize = (size: number) => {
  if (!size) return '0 B'

  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let index = 0
  let fileSize = Number(size) // 确保是数字类型

  while (fileSize >= 1024 && index < units.length - 1) {
    fileSize /= 1024
    index++
  }

  return `${fileSize.toFixed(2)} ${units[index]}`
}

// 获取文件类型
const getFileType = (filename: string) => {
  if (!filename) return 'unknown'

  const ext = filename.split('.').pop()?.toLowerCase()

  if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'svg', 'webp'].includes(ext || '')) {
    return 'image'
  } else if (['mp4', 'avi', 'mkv', 'mov', 'wmv', 'flv', 'webm'].includes(ext || '')) {
    return 'video'
  } else if (['mp3', 'wav', 'flac', 'aac', 'ogg'].includes(ext || '')) {
    return 'audio'
  } else if (['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt'].includes(ext || '')) {
    return 'document'
  } else if (['zip', 'rar', '7z', 'tar', 'gz'].includes(ext || '')) {
    return 'archive'
  } else {
    return 'other'
  }
}

// 获取文件类型标签
const getFileTypeTag = (filename: string) => {
  const type = getFileType(filename)

  const typeMap = {
    image: { text: '图片', type: 'success' },
    video: { text: '视频', type: 'warning' },
    audio: { text: '音频', type: 'info' },
    document: { text: '文档', type: 'primary' },
    archive: { text: '压缩包', type: 'danger' },
    other: { text: '其他', type: '' }
  }

  return typeMap[type] || typeMap.other
}
</script>

<template>
  <div class="file-list-container">
    <div class="list-header">
      <h2 class="list-title">文件列表</h2>
      <el-button type="primary" @click="goToUpload">
        <el-icon><upload /></el-icon>上传文件
      </el-button>
    </div>

    <div class="filter-container">
      <div class="filter-item">
        <el-input
          v-model="searchName"
          placeholder="搜索文件名"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><search /></el-icon>
          </template>
        </el-input>
      </div>

      <div class="filter-item">
        <el-select v-model="fileType" placeholder="文件类型" clearable>
          <el-option label="图片" value="image" />
          <el-option label="视频" value="video" />
          <el-option label="音频" value="audio" />
          <el-option label="文档" value="document" />
          <el-option label="压缩包" value="archive" />
          <el-option label="其他" value="other" />
        </el-select>
      </div>

      <div class="filter-item">
        <el-select v-model="isEncryption" placeholder="加密状态" clearable>
          <el-option label="已加密" :value="1" />
          <el-option label="未加密" :value="0" />
        </el-select>
      </div>

      <div class="filter-item">
        <el-button type="primary" @click="handleSearch">
          <el-icon><search /></el-icon>搜索
        </el-button>
        <el-button @click="handleReset">
          <el-icon><refresh /></el-icon>重置
        </el-button>
      </div>
    </div>

    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="fileList"
        style="width: 100%"
        stripe
        border
      >
        <el-table-column prop="name" label="文件名" min-width="200">
          <template #default="scope">
            <div class="file-name-cell">
              <span>{{ scope.row.name }}</span>
              <el-tag :type="getFileTypeTag(scope.row.name).type" size="small">
                {{ getFileTypeTag(scope.row.name).text }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="fileSize" label="文件大小" width="120">
          <template #default="scope">
            {{ formatFileSize(scope.row.fileSize || 0) }}
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="上传时间" width="180" />

        <el-table-column prop="isEncryption" label="加密状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.isEncryption === 1" type="warning">
              <el-icon><lock /></el-icon>已加密
            </el-tag>
            <el-tag v-else type="success">
              <el-icon><unlock /></el-icon>未加密
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="300">
          <template #default="scope">
            <el-button size="small" @click="viewFileDetail(scope.row)">
              <el-icon><view /></el-icon>详情
            </el-button>
            <el-button size="small" type="primary" @click="downloadFile(scope.row)">
              <el-icon><download /></el-icon>下载
            </el-button>
            <el-button
              size="small"
              :type="scope.row.isEncryption === 1 ? 'success' : 'warning'"
              @click="openEncryptDialog(scope.row)"
            >
              <el-icon v-if="scope.row.isEncryption === 1"><unlock /></el-icon>
              <el-icon v-else><lock /></el-icon>
              {{ scope.row.isEncryption === 1 ? '解密' : '加密' }}
            </el-button>
            <el-button size="small" type="danger" @click="deleteFile(scope.row)">
              <el-icon><delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 加密/解密对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isDownloadOperation ? '下载加密文件' : (currentFile?.isEncryption === 1 ? '文件解密' : '文件加密')"
      width="400px"
    >
      <el-form :model="encryptForm" label-width="80px">
        <el-form-item label="密码">
          <el-input
            v-model="encryptForm.filePassword"
            type="password"
            :placeholder="currentFile?.isEncryption === 1 ? '请输入解密密码' : '请输入加密密码'"
            show-password
          />
        </el-form-item>
        <p v-if="currentFile?.isEncryption === 1" class="decrypt-notice">
          确定要解密文件 "{{ currentFile?.name }}" 吗？
        </p>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmEncryption">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.file-list-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.list-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.filter-container {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.filter-item {
  min-width: 200px;
}

.table-container {
  background-color: #fff;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.file-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.decrypt-notice {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .file-list-container {
    padding: 10px;
  }

  .list-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .list-title {
    font-size: 20px;
  }

  .filter-container {
    padding: 10px;
  }

  .filter-item {
    min-width: 100%;
  }

  .table-container {
    padding: 10px;
    overflow-x: auto;
  }

  :deep(.el-table) {
    min-width: 700px;
  }

  .pagination-container {
    :deep(.el-pagination) {
      flex-wrap: wrap;
      justify-content: center;
    }
  }
}
</style>
