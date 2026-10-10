/** 环境数据记录 */
export interface EnvRecord {
  id: number
  deviceId: string
  timestamp: string
  waterTemp: number | null
  ph: number | null
  dissolvedOxygen: number | null
  lightIntensity: number | null
  conductivity: number | null
}

/** 后端统一响应格式 */
export interface ApiResponse<T> {
  code: number
  msg: string
  data: T
}

/** 大棚信息 */
export interface GreenhouseInfo {
  id: number
  code: string
  name: string
  location: string
  status: 'normal' | 'maintenance' | 'closed'
  area: number | null
  notes: string
  deviceCount: number
  createdAt: string | null
}

/** 大棚表单 */
export interface GreenhouseCreate {
  code: string
  name: string
  location?: string
  area?: number
  notes?: string
}
export interface GreenhouseUpdate {
  name?: string
  location?: string
  status?: string
  area?: number
  notes?: string
}

/** 设备信息（对应 devices 表） */
export interface DeviceInfo {
  code: string
  greenhouseId: number | null
  greenhouseName: string
  name: string
  location: string
  status: 'online' | 'offline' | 'maintenance'
  installedAt: string | null
  notes: string
  createdAt: string | null
}

/** 新增设备表单 */
export interface DeviceCreate {
  code: string
  name: string
  greenhouseId?: number
  location?: string
  installedAt?: string
  notes?: string
}

/** 编辑设备表单 */
export interface DeviceUpdate {
  name?: string
  greenhouseId?: number
  location?: string
  status?: string
  installedAt?: string
  notes?: string
}

/** 告警记录 */
export interface AlertInfo {
  id: number
  deviceId: string
  greenhouseId: number | null
  param: string
  paramName: string
  value: number
  thresholdLow: number | null
  thresholdHigh: number | null
  level: 'warning' | 'error'
  message: string
  status: 'active' | 'confirmed' | 'ignored' | 'resolved'
  triggeredAt: string | null
  confirmedAt: string | null
}

/** 告警规则 */
export interface AlertRule {
  id: number
  param: string
  paramName: string
  unit: string
  minVal: number | null
  maxVal: number | null
  thresholdLow: number | null
  thresholdHigh: number | null
  level: 'warning' | 'error'
  enabled: number
}
