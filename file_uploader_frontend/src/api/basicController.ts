// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** hello GET /fileapi/hello */
export async function helloUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.helloUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<string>('/fileapi/hello', {
    method: 'GET',
    params: {
      // name has a default value: unknown user
      name: 'unknown user',
      ...params,
    },
    ...(options || {}),
  })
}

/** hello PUT /fileapi/hello */
export async function helloUsingPut(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.helloUsingPUTParams,
  options?: { [key: string]: any }
) {
  return request<string>('/fileapi/hello', {
    method: 'PUT',
    params: {
      // name has a default value: unknown user
      name: 'unknown user',
      ...params,
    },
    ...(options || {}),
  })
}

/** hello POST /fileapi/hello */
export async function helloUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.helloUsingPOSTParams,
  options?: { [key: string]: any }
) {
  return request<string>('/fileapi/hello', {
    method: 'POST',
    params: {
      // name has a default value: unknown user
      name: 'unknown user',
      ...params,
    },
    ...(options || {}),
  })
}

/** hello DELETE /fileapi/hello */
export async function helloUsingDelete(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.helloUsingDELETEParams,
  options?: { [key: string]: any }
) {
  return request<string>('/fileapi/hello', {
    method: 'DELETE',
    params: {
      // name has a default value: unknown user
      name: 'unknown user',
      ...params,
    },
    ...(options || {}),
  })
}

/** hello PATCH /fileapi/hello */
export async function helloUsingPatch(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.helloUsingPATCHParams,
  options?: { [key: string]: any }
) {
  return request<string>('/fileapi/hello', {
    method: 'PATCH',
    params: {
      // name has a default value: unknown user
      name: 'unknown user',
      ...params,
    },
    ...(options || {}),
  })
}
