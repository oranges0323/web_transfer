// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** decrypt POST /fileapi/file/decrypt */
export async function decryptUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.decryptUsingPOSTParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/fileapi/file/decrypt', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** deleteFile POST /fileapi/file/delete */
export async function deleteFileUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteFileUsingPOSTParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/fileapi/file/delete', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** downloadFile GET /fileapi/file/download */
export async function downloadFileUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.downloadFileUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.UrlResource>('/fileapi/file/download', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** encrypt POST /fileapi/file/encrypt */
export async function encryptUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.encryptUsingPOSTParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/fileapi/file/encrypt', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** listFileVOById GET /fileapi/file/get/list/vo */
export async function listFileVoByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listFileVOByIdUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageFileInfoVO_>('/fileapi/file/get/list/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** upload POST /fileapi/file/upload */
export async function uploadUsingPost(body: {}, file?: File, options?: { [key: string]: any }) {
  const formData = new FormData()

  if (file) {
    formData.append('file', file)
  }

  Object.keys(body).forEach((ele) => {
    const item = (body as any)[ele]

    if (item !== undefined && item !== null) {
      if (typeof item === 'object' && !(item instanceof File)) {
        if (item instanceof Array) {
          item.forEach((f) => formData.append(ele, f || ''))
        } else {
          formData.append(ele, new Blob([JSON.stringify(item)], { type: 'application/json' }))
        }
      } else {
        formData.append(ele, item)
      }
    }
  })

  return request<API.BaseResponseFileInfoVO_>('/fileapi/file/upload', {
    method: 'POST',
    data: formData,
    requestType: 'form',
    ...(options || {}),
  })
}
