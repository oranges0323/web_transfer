<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElUpload, ElButton, ElForm, ElFormItem, ElInput, ElSwitch, ElProgress, ElTabs, ElTabPane, ElTable, ElTableColumn, ElTag, ElPagination, ElSelect, ElOption, ElDialog } from 'element-plus'
import { UploadFilled, Lock, Unlock, Search, Download, View, Delete, Refresh } from '@element-plus/icons-vue'
import { uploadUsingPost, encryptUsingPost, listFileVoByIdUsingGet, deleteFileUsingPost, downloadFileUsingGet } from '@/api/fileController'

const router = useRouter()
const userStore = useUserStore()

// 文件上传相关
const fileList = ref<any[]>([])
const uploadProgress = ref(0)
const isUploading = ref(false)
const isEncrypting = ref(false)
const encryptPassword = ref('')
const enableEncryption = ref(false)



// 上传文件前的处理
const beforeUpload = (file: any) => {
  // 检查文件大小（这里限制为100MB）
  const isLt100M = file.size / 1024 / 1024 < 100
  if (!isLt100M) {
    ElMessage.error('文件大小不能超过 100MB!')
    return false
  }
  return true
}

// 文件上传处理
const handleUpload = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请选择要上传的文件')
    return
  }

  if (enableEncryption.value && !encryptPassword.value) {
    ElMessage.warning('启用加密时，请输入加密密码')
    return
  }

  isUploading.value = true
  uploadProgress.value = 0

  try {
    const file = fileList.value[0].raw

    // 构建上传参数
    const uploadParams: any = {}

    // 添加用户ID参数
    if (userStore.loginUser?.id) {
      uploadParams.userId = userStore.loginUser.id
    }

    const uploadResult = await uploadUsingPost(uploadParams, file, {
      onUploadProgress: (progressEvent: any) => {
        uploadProgress.value = Math.round(
          (progressEvent.loaded * 100) / progressEvent.total
        )
      }
    })

    if (uploadResult.code === 0 && uploadResult.data) {
      ElMessage.success('文件上传成功!')

      // 如果需要加密
      if (enableEncryption.value) {
        isEncrypting.value = true
        const encryptResult = await encryptUsingPost({
          id: uploadResult.data.id,
          isEncryption: 1,
          filePassword: encryptPassword.value
        })

        if (encryptResult.code === 0 && encryptResult.data) {
          ElMessage.success('文件加密成功!')
        } else {
          ElMessage.error(encryptResult.message || '文件加密失败')
        }
        isEncrypting.value = false
      }

      router.push('/my-uploads')

      // 清空文件列表和表单
      fileList.value = []
      enableEncryption.value = false
      encryptPassword.value = ''
    } else {
      ElMessage.error(uploadResult.message || '文件上传失败')
    }
  } catch (error) {
    console.error('上传出错:', error)
    ElMessage.error('上传过程中出现错误')
  } finally {
    isUploading.value = false
  }
}



// 跳转到我的上传
const goToMyUploads = () => {
  router.push('/my-uploads')
}
</script>

<template>
  <div class="file-upload-container">
    <div class="upload-card">
      <div class="upload-header">
        <h2 class="upload-title">文件上传</h2>
        <el-button type="info" @click="goToMyUploads">
          <el-icon><view /></el-icon>我的上传
        </el-button>
      </div>

      <el-form :model="{ encryptPassword }" label-width="100px">
        <el-form-item label="选择文件">
          <el-upload
            class="upload-demo"
            drag
            :limit="1"
            :auto-upload="false"
            :on-change="(file: any) => fileList = [file]"
            :before-upload="beforeUpload"
            :file-list="fileList"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击选择文件</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                单次上传文件大小不超过 100MB
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="加密设置">
          <div class="encryption-settings">
            <el-switch
              v-model="enableEncryption"
              active-text="启用加密"
              inactive-text="不加密"
            />
            <el-icon v-if="enableEncryption" class="lock-icon"><lock /></el-icon>
            <el-icon v-else class="lock-icon"><unlock /></el-icon>
          </div>
        </el-form-item>

        <el-form-item v-if="enableEncryption" label="加密密码">
          <el-input
            v-model="encryptPassword"
            type="password"
            placeholder="请输入加密密码"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <div class="action-buttons">
            <el-button
              type="primary"
              :loading="isUploading"
              @click="handleUpload"
            >
              {{ isUploading ? '上传中...' : '上传文件' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>

      <div v-if="isUploading" class="progress-container">
        <el-progress :percentage="uploadProgress" />
        <p v-if="isEncrypting" class="encrypting-text">正在加密文件，请稍候...</p>
      </div>
    </div>


  </div>
</template>

<style scoped>
.file-upload-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: calc(100vh - 64px);
  padding: 20px;
}

.upload-card {
  width: 100%;
  max-width: 900px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 30px;
}

.upload-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.upload-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 30px;
  text-align: center;
}

.upload-demo {
  width: 100%;
}

.encryption-settings {
  display: flex;
  align-items: center;
  gap: 10px;
}

.lock-icon {
  color: #409eff;
  font-size: 18px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.progress-container {
  margin-top: 20px;
}

.encrypting-text {
  text-align: center;
  margin-top: 10px;
  color: #409eff;
  font-size: 14px;
}

:deep(.el-upload-dragger) {
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .file-upload-container {
    padding: 10px;
  }

  .upload-card {
    padding: 15px;
  }

  .upload-title {
    font-size: 20px;
  }

  .upload-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .encryption-settings {
    flex-direction: column;
    align-items: flex-start;
  }
}


</style>
