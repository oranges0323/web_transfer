// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** deleteUser POST /fileapi/user/delete */
export async function deleteUserUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteUserUsingPOSTParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/fileapi/user/delete', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** getUserById GET /fileapi/user/get */
export async function getUserByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getUserByIdUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseUser_>('/fileapi/user/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** listUserVOById GET /fileapi/user/get/list/vo */
export async function listUserVoByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listUserVOByIdUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageUserVO_>('/fileapi/user/get/list/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** getLoginUser GET /fileapi/user/get/login */
export async function getLoginUserUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseLoginUserVO_>('/fileapi/user/get/login', {
    method: 'GET',
    ...(options || {}),
  })
}

/** getUserVOById GET /fileapi/user/get/vo */
export async function getUserVoByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getUserVOByIdUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseUserVO_>('/fileapi/user/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** userLogin POST /fileapi/user/login */
export async function userLoginUsingPost(
  body: API.UserLoginRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseLoginUserVO_>('/fileapi/user/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** userLogout POST /fileapi/user/logout */
export async function userLogoutUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean_>('/fileapi/user/logout', {
    method: 'POST',
    ...(options || {}),
  })
}

/** userRegister POST /fileapi/user/register */
export async function userRegisterUsingPost(
  body: API.UserRegisterRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseLong_>('/fileapi/user/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** updateUser POST /fileapi/user/update */
export async function updateUserUsingPost(
  body: API.UserUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean_>('/fileapi/user/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
