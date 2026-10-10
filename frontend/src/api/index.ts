import axios from 'axios'
import type { AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import type {
  ApiResponse, EnvRecord, DeviceInfo, DeviceCreate, DeviceUpdate,
  GreenhouseInfo, GreenhouseCreate, GreenhouseUpdate, AlertInfo, AlertRule,
} from '@/types/env.d'
import { getToken, clearToken } from '@/utils/auth'

// ==================== 拦截器 ====================

/** 请求拦截器 — 自动注入 Token */
function injectToken(config: InternalAxiosRequestConfig) {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}

/** 响应拦截器 — 401 自动跳转登录（不在登录页时才跳转） */
function handleAuthError(error: any) {
  if (error.response?.status === 401) {
    clearToken()
    // 不在登录页才跳转，避免死循环
    if (!window.location.pathname.includes('/login')) {
      window.location.href = '/login'
    }
  }
  return Promise.reject(error)
}

const envHttp = axios.create({ baseURL: 'http://localhost:8000/api/environment', timeout: 10000 })
const apiHttp = axios.create({ baseURL: 'http://localhost:8000/api', timeout: 10000 })

envHttp.interceptors.request.use(injectToken, Promise.reject)
apiHttp.interceptors.request.use(injectToken, Promise.reject)
envHttp.interceptors.response.use(r => r, handleAuthError)
apiHttp.interceptors.response.use(r => r, handleAuthError)

// ==================== 环境数据 ====================

export async function fetchLatest(deviceId: string): Promise<EnvRecord | null> {
  const res = await envHttp.get<ApiResponse<EnvRecord>>('/latest', { params: { deviceId } })
  return res.data.code === 200 ? res.data.data : null
}

export async function fetchHistory(
  deviceId: string, start?: string, end?: string,
): Promise<EnvRecord[]> {
  console.log('[API fetchHistory] 请求参数:', { deviceId, start, end })
  const res = await envHttp.get<ApiResponse<EnvRecord[]>>('/history', {
    params: { deviceId, start, end },
  })
  console.log('[API fetchHistory] 后端返回 code:', res.data.code, '| data长度:', Array.isArray(res.data.data) ? res.data.data.length : '非数组', '| msg:', res.data.msg)
  if (Array.isArray(res.data.data) && res.data.data.length > 0) {
    console.log('[API fetchHistory] 第一条:', res.data.data[0])
  }
  return res.data.code === 200 ? res.data.data : []
}

export interface DataRange {
  first: string | null
  last: string | null
  count: number
}

/** 获取设备数据的起止时间范围 */
export async function fetchDataRange(deviceId: string): Promise<DataRange | null> {
  const res = await envHttp.get<ApiResponse<DataRange>>('/range', { params: { deviceId } })
  return res.data.code === 200 ? res.data.data : null
}

/** 获取设备最近 N 条数据（迷你走势用） */
export async function fetchRecent(deviceId: string, limit = 60): Promise<EnvRecord[]> {
  const res = await envHttp.get<ApiResponse<EnvRecord[]>>('/recent', { params: { deviceId, limit } })
  return res.data.code === 200 ? res.data.data : []
}

// ==================== 大棚 CRUD ====================

export async function fetchGreenhouses(): Promise<GreenhouseInfo[]> {
  const res = await apiHttp.get<ApiResponse<GreenhouseInfo[]>>('/greenhouses')
  return res.data.code === 200 ? res.data.data : []
}

export async function fetchGreenhouseDevices(greenhouseId: number): Promise<DeviceInfo[]> {
  const res = await apiHttp.get<ApiResponse<DeviceInfo[]>>(`/greenhouses/${greenhouseId}/devices`)
  return res.data.code === 200 ? res.data.data : []
}

export async function createGreenhouse(data: GreenhouseCreate): Promise<GreenhouseInfo | null> {
  const res = await apiHttp.post<ApiResponse<GreenhouseInfo>>('/greenhouses', data)
  return res.data.code === 200 ? res.data.data : null
}

export async function updateGreenhouse(id: number, data: GreenhouseUpdate): Promise<GreenhouseInfo | null> {
  const res = await apiHttp.put<ApiResponse<GreenhouseInfo>>(`/greenhouses/${id}`, data)
  return res.data.code === 200 ? res.data.data : null
}

export async function deleteGreenhouse(id: number): Promise<{ ok: boolean; msg: string }> {
  const res = await apiHttp.delete<ApiResponse<null>>(`/greenhouses/${id}`)
  return { ok: res.data.code === 200, msg: res.data.msg }
}

// ==================== 设备管理 CRUD ====================

/** 获取所有设备 */
export async function fetchDevices(): Promise<DeviceInfo[]> {
  const res = await envHttp.get<ApiResponse<DeviceInfo[]>>('/devices')
  return res.data.code === 200 ? res.data.data : []
}

export async function createDevice(data: DeviceCreate): Promise<DeviceInfo | null> {
  const res = await apiHttp.post<ApiResponse<DeviceInfo>>('/devices', data)
  return res.data.code === 200 ? res.data.data : null
}

export async function updateDevice(code: string, data: DeviceUpdate): Promise<DeviceInfo | null> {
  const res = await apiHttp.put<ApiResponse<DeviceInfo>>(`/devices/${code}`, data)
  return res.data.code === 200 ? res.data.data : null
}

export async function deleteDevice(code: string): Promise<boolean> {
  const res = await apiHttp.delete<ApiResponse<null>>(`/devices/${code}`)
  return res.data.code === 200
}

// ==================== 告警 API ====================

export interface AlertPage {
  records: AlertInfo[]
  total: number
}

export async function fetchAlerts(params?: { status?: string; deviceId?: string; page?: number; size?: number }): Promise<AlertPage> {
  const res = await apiHttp.get<ApiResponse<AlertPage>>('/alerts', { params })
  return res.data.code === 200 ? res.data.data : { records: [], total: 0 }
}

export async function fetchActiveAlerts(deviceId?: string): Promise<AlertInfo[]> {
  const res = await apiHttp.get<ApiResponse<AlertInfo[]>>('/alerts/active', { params: { deviceId } })
  return res.data.code === 200 ? res.data.data : []
}

export async function confirmAlert(alertId: number): Promise<boolean> {
  const res = await apiHttp.post<ApiResponse<null>>(`/alerts/${alertId}/confirm`)
  return res.data.code === 200
}

export async function ignoreAlert(alertId: number): Promise<boolean> {
  const res = await apiHttp.post<ApiResponse<null>>(`/alerts/${alertId}/ignore`)
  return res.data.code === 200
}

export async function confirmAllAlerts(): Promise<boolean> {
  const res = await apiHttp.post<ApiResponse<null>>('/alerts/confirm-all')
  return res.data.code === 200
}

// ==================== 告警规则 API ====================

export async function fetchAlertRules(): Promise<AlertRule[]> {
  const res = await apiHttp.get<ApiResponse<AlertRule[]>>('/alert-rules')
  return res.data.code === 200 ? res.data.data : []
}

export async function updateAlertRule(id: number, data: Partial<AlertRule>): Promise<AlertRule | null> {
  const res = await apiHttp.put<ApiResponse<AlertRule>>(`/alert-rules/${id}`, data)
  return res.data.code === 200 ? res.data.data : null
}

// ==================== 授权管理 ====================

export interface OperatorInfo {
  id: number
  username: string
  role: string
}

/** 获取所有操作员列表（仅管理员可用） */
export async function fetchOperators(): Promise<OperatorInfo[]> {
  const res = await apiHttp.get<ApiResponse<OperatorInfo[]>>('/access/operators')
  return res.data.code === 200 ? res.data.data : []
}

/** 获取某个操作员被授权的大棚ID列表 */
export async function fetchUserGreenhouses(userId: number): Promise<number[]> {
  const res = await apiHttp.get<ApiResponse<number[]>>(`/access/user/${userId}/greenhouses`)
  return res.data.code === 200 ? res.data.data : []
}

/** 获取所有大棚（供授权选择器用） */
export async function fetchAllGreenhousesForAccess(): Promise<GreenhouseInfo[]> {
  const res = await apiHttp.get<ApiResponse<GreenhouseInfo[]>>('/access/greenhouses')
  return res.data.code === 200 ? res.data.data : []
}

/** 保存某操作员的大棚授权（全量覆盖） */
export async function saveUserAccess(userId: number, greenhouseIds: number[]): Promise<boolean> {
  const res = await apiHttp.put<ApiResponse<null>>(`/access/user/${userId}`, greenhouseIds)
  return res.data.code === 200
}

/** 查询当前登录用户被授权的大棚ID列表（admin 返回 { all: true }） */
export async function fetchMyGreenhouseAccess(): Promise<{ all: boolean; greenhouseIds: number[] | null }> {
  const res = await apiHttp.get<ApiResponse<{ all: boolean; greenhouseIds: number[] | null }>>('/access/my-greenhouses')
  return res.data.code === 200 ? res.data.data : { all: false, greenhouseIds: [] }
}

// ==================== 用户管理（仅管理员） ====================

export interface UserAccount {
  id: number
  username: string
  role: string
  lastLoginAt: string | null
  createdAt: string | null
}

/** 获取所有用户列表 */
export async function fetchUsers(): Promise<UserAccount[]> {
  const res = await apiHttp.get<ApiResponse<UserAccount[]>>('/users')
  return res.data.code === 200 ? res.data.data : []
}

/** 调整用户角色 */
export async function updateUserRole(userId: number, role: string): Promise<boolean> {
  const res = await apiHttp.put<ApiResponse<null>>(`/users/${userId}/role`, null, { params: { role } })
  return res.data.code === 200
}

/** 重置用户密码 */
export async function resetUserPassword(userId: number, newPassword: string): Promise<boolean> {
  const res = await apiHttp.put<ApiResponse<null>>(`/users/${userId}/password`, { newPassword })
  return res.data.code === 200
}
