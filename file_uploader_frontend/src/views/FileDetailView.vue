<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElCard, ElButton, ElTag, ElDescriptions, ElDialog, ElForm, ElFormItem, ElInput } from 'element-plus'
import { ArrowLeft, Download, Lock, Unlock, Delete } from '@element-plus/icons-vue'
import { listFileVoByIdUsingGet, deleteFileUsingPost, downloadFileUsingGet, encryptUsingPost, decryptUsingPost } from '@/api/fileController'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const fileId = ref(route.params.id)
const fileDetail = ref<API.FileInfoVO | null>(null)
const loading = ref(true)
const dialogVisible = ref(false)
const encryptForm = ref({
  filePassword: '',
  isEncryption: 1
})
const isDownloadOperation = ref(false)

// 获取文件详情
const fetchFileDetail = async () => {
  try {
    const params: any = {
      id: fileId.value,
      current: 1,
      pageSize: 1
    }
    
    // 不添加userId参数，直接根据id查询文件
    // if (userStore.loginUser?.id) {
    //   params.userId = userStore.loginUser.id
    // }
    
    const response = await listFileVoByIdUsingGet(params)

    if (response.code === 0 && response.data && response.data.records && response.data.records.length > 0) {
      fileDetail.value = response.data.records[0]
    } else {
      ElMessage.error('未找到文件信息')
      router.push('/file/list')
    }
  } catch (error) {
    console.error('获取文件详情出错:', error)
    ElMessage.error('获取文件详情时发生错误')
  } finally {
    loading.value = false
  }
}

// 页面加载时获取文件详情
onMounted(() => {
  fetchFileDetail()
})

// 返回文件列表
const goBack = () => {
  router.push('/file/list')
}

// 下载文件
const downloadFile = async () => {
  if (!fileDetail.value) return

  try {
    // 如果是加密文件，需要先输入密码
    if (fileDetail.value.isEncryption === 1) {
      encryptForm.value.filePassword = ''
      dialogVisible.value = true
      // 标记当前操作是下载
      isDownloadOperation.value = true
      return
    }
    
    const response = await downloadFileUsingGet({ id: String(fileDetail.value.id) })
    if (response && response.url) {
      // 创建一个隐藏的a标签来触发下载
      const link = document.createElement('a')
      link.href = response.url
      link.download = fileDetail.value.name || 'download'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      ElMessage.success('文件下载成功')
    } else {
      ElMessage.error('获取下载链接失败')
    }
  } catch (error) {
    console.error('下载文件出错:', error)
    ElMessage.error('下载文件时发生错误')
  }
}

// 删除文件
const deleteFile = async () => {
  if (!fileDetail.value) return

  try {
    const response = await deleteFileUsingPost({ id: fileDetail.value.id })
    if (response.code === 0 && response.data) {
      ElMessage.success('文件删除成功')
      router.push('/file/list')
    } else {
      ElMessage.error(response.message || '文件删除失败')
    }
  } catch (error) {
    console.error('删除文件出错:', error)
    ElMessage.error('删除文件时发生错误')
  }
}

// 打开加密对话框
const openEncryptDialog = () => {
  if (!fileDetail.value) return
  dialogVisible.value = true
}

// 确认加密/解密
const confirmEncryption = async () => {
  if (!fileDetail.value) return

  try {
    // 如果是下载操作
    if (isDownloadOperation.value) {
      if (!encryptForm.value.filePassword) {
        ElMessage.warning('请输入解密密码')
        return
      }
      
      const blob = await downloadFileUsingGet({
        id: String(fileDetail.value.id),
        filePassword: encryptForm.value.filePassword
      })

      if (blob) {
        // 创建一个隐藏的a标签来触发下载
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = fileDetail.value.name || 'download'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        ElMessage.success('文件下载成功')
      } else {
        ElMessage.error('下载文件失败')
      }
      
      dialogVisible.value = false
      isDownloadOperation.value = false
      return
    }
    
    let response
    if (fileDetail.value.isEncryption === 1) {
      // 当前已加密，执行解密操作
      if (!encryptForm.value.filePassword) {
        ElMessage.warning('请输入解密密码')
        return
      }
      
      response = await decryptUsingPost({ 
        fileId: fileDetail.value.id,
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
        id: fileDetail.value.id,
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
    fetchFileDetail() // 重新获取文件详情
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
  <div class="file-detail-container">
    <div class="detail-header">
      <el-button @click="goBack" class="back-button">
        <el-icon><arrow-left /></el-icon>返回
      </el-button>
      <h2 class="detail-title">文件详情</h2>
    </div>

    <el-card v-loading="loading" class="file-info-card">
      <template v-if="fileDetail">
        <div class="file-header">
          <div class="file-name-container">
            <h3 class="file-name">{{ fileDetail.name }}</h3>
            <el-tag :type="getFileTypeTag(fileDetail.name).type" size="large">
              {{ getFileTypeTag(fileDetail.name).text }}
            </el-tag>
          </div>

          <div class="file-actions">
            <el-button type="primary" @click="downloadFile">
              <el-icon><download /></el-icon>下载
            </el-button>
            <el-button 
              :type="fileDetail.isEncryption === 1 ? 'success' : 'warning'"
              @click="openEncryptDialog"
            >
              <el-icon v-if="fileDetail.isEncryption === 1"><unlock /></el-icon>
              <el-icon v-else><lock /></el-icon>
              {{ fileDetail.isEncryption === 1 ? '解密' : '加密' }}
            </el-button>
            <el-button type="danger" @click="deleteFile">
              <el-icon><delete /></el-icon>删除
            </el-button>
          </div>
        </div>

        <el-descriptions :column="2" border class="file-descriptions">
          <el-descriptions-item label="文件ID">{{ fileDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="文件名">{{ fileDetail.name }}</el-descriptions-item>
          <el-descriptions-item label="文件大小">{{ formatFileSize(fileDetail.fileSize || 0) }}</el-descriptions-item>
          <el-descriptions-item label="文件类型">{{ getFileTypeTag(fileDetail.name).text }}</el-descriptions-item>
          <el-descriptions-item label="上传时间">{{ fileDetail.createTime }}</el-descriptions-item>
          <el-descriptions-item label="加密状态">
            <el-tag v-if="fileDetail.isEncryption === 1" type="warning">
              <el-icon><lock /></el-icon>已加密
            </el-tag>
            <el-tag v-else type="success">
              <el-icon><unlock /></el-icon>未加密
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="文件URL" span="2">
            <div class="file-url">{{ fileDetail.url }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </template>
    </el-card>

    <!-- 加密/解密对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isDownloadOperation ? '下载加密文件' : (fileDetail?.isEncryption === 1 ? '文件解密' : '文件加密')"
      width="400px"
    >
      <el-form :model="encryptForm" label-width="80px">
        <el-form-item label="密码">
          <el-input
            v-model="encryptForm.filePassword"
            type="password"
            :placeholder="isDownloadOperation ? '请输入解密密码' : (fileDetail?.isEncryption === 1 ? '请输入解密密码' : '请输入加密密码')"
            show-password
          />
        </el-form-item>
        <p v-if="!isDownloadOperation && fileDetail?.isEncryption === 1" class="decrypt-notice">
          确定要解密文件 "{{ fileDetail?.name }}" 吗？
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
.file-detail-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.detail-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.back-button {
  margin-right: 15px;
}

.detail-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.file-info-card {
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border-radius: 12px;
}

.file-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
}

.file-name-container {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.file-name {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.file-actions {
  display: flex;
  gap: 10px;
}

.file-descriptions {
  margin-top: 20px;
}

.file-url {
  word-break: break-all;
  background-color: #f5f7fa;
  padding: 8px 12px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 14px;
}

.decrypt-notice {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  margin: 0;
  padding: 10px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 768px) {
  .file-header {
    flex-direction: column;
    align-items: stretch;
  }

  .file-name-container {
    justify-content: center;
  }

  .file-actions {
    justify-content: center;
  }
}
</style>
