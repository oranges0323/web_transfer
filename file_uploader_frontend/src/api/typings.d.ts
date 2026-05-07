declare namespace API {
  type BaseResponseBoolean_ = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseFileInfoVO_ = {
    code?: number
    data?: FileInfoVO
    message?: string
  }

  type BaseResponseLoginUserVO_ = {
    code?: number
    data?: LoginUserVO
    message?: string
  }

  type BaseResponseLong_ = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponsePageFileInfoVO_ = {
    code?: number
    data?: PageFileInfoVO_
    message?: string
  }

  type BaseResponsePageUserVO_ = {
    code?: number
    data?: PageUserVO_
    message?: string
  }

  type BaseResponseUser_ = {
    code?: number
    data?: User
    message?: string
  }

  type BaseResponseUserVO_ = {
    code?: number
    data?: UserVO
    message?: string
  }

  type decryptUsingPOSTParams = {
    /** fileId */
    fileId?: number
  }

  type deleteFileUsingPOSTParams = {
    /** id */
    id?: number
  }

  type deleteUserUsingPOSTParams = {
    id?: number
  }

  type downloadFileUsingGETParams = {
    /** id */
    id: string
    /** password */
    password?: string
  }

  type encryptUsingPOSTParams = {
    filePassword?: string
    id?: number
    isEncryption?: number
  }

  type File = {
    absolute?: boolean
    absoluteFile?: File
    absolutePath?: string
    canonicalFile?: File
    canonicalPath?: string
    directory?: boolean
    file?: boolean
    freeSpace?: number
    hidden?: boolean
    name?: string
    parent?: string
    parentFile?: File
    path?: string
    totalSpace?: number
    usableSpace?: number
  }

  type FileInfoVO = {
    createTime?: string
    fileSize?: number
    id?: number
    isEncryption?: number
    name?: string
    url?: string
    userId?: number
  }

  type getUserByIdUsingGETParams = {
    /** id */
    id?: number
  }

  type getUserVOByIdUsingGETParams = {
    /** id */
    id?: number
  }

  type helloUsingDELETEParams = {
    /** name */
    name?: string
  }

  type helloUsingGETParams = {
    /** name */
    name?: string
  }

  type helloUsingPATCHParams = {
    /** name */
    name?: string
  }

  type helloUsingPOSTParams = {
    /** name */
    name?: string
  }

  type helloUsingPUTParams = {
    /** name */
    name?: string
  }

  type InputStream = true

  type listFileVOByIdUsingGETParams = {
    current?: number
    fileFormat?: string
    fileType?: string
    id?: string
    isEncryption?: number
    name?: string
    pageSize?: number
    sortField?: string
    sortOrder?: string
    userId?: number
  }

  type listUserVOByIdUsingGETParams = {
    current?: number
    id?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    userAccount?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }

  type LoginUserVO = {
    createTime?: string
    id?: number
    token?: string
    updateTime?: string
    userAccount?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }

  type PageFileInfoVO_ = {
    current?: number
    pages?: number
    records?: FileInfoVO[]
    size?: number
    total?: number
  }

  type PageUserVO_ = {
    current?: number
    pages?: number
    records?: UserVO[]
    size?: number
    total?: number
  }

  type URI = {
    absolute?: boolean
    authority?: string
    fragment?: string
    host?: string
    opaque?: boolean
    path?: string
    port?: number
    query?: string
    rawAuthority?: string
    rawFragment?: string
    rawPath?: string
    rawQuery?: string
    rawSchemeSpecificPart?: string
    rawUserInfo?: string
    scheme?: string
    schemeSpecificPart?: string
    userInfo?: string
  }

  type URL = {
    authority?: string
    content?: Record<string, any>
    defaultPort?: number
    file?: string
    host?: string
    path?: string
    port?: number
    protocol?: string
    query?: string
    ref?: string
    userInfo?: string
  }

  type UrlResource = {
    description?: string
    file?: File
    filename?: string
    inputStream?: InputStream
    open?: boolean
    readable?: boolean
    uri?: URI
    url?: URL
  }

  type User = {
    createTime?: string
    editTime?: string
    id?: number
    isDelete?: number
    updateTime?: string
    userAccount?: string
    userAvatar?: string
    userName?: string
    userPassword?: string
    userProfile?: string
    userRole?: string
  }

  type UserLoginRequest = {
    userAccount?: string
    userPassword?: string
  }

  type UserRegisterRequest = {
    checkPassword?: string
    userAccount?: string
    userPassword?: string
  }

  type UserUpdateRequest = {
    id?: number
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }

  type UserVO = {
    createTime?: string
    id?: number
    userAccount?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }
}
