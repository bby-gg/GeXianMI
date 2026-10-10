import axios from 'axios'
import type { UserInfo } from '@/utils/auth'

const http = axios.create({ baseURL: 'http://localhost:8000/api/auth', timeout: 10000 })

export async function login(username: string, password: string): Promise<{ token: string; userInfo: UserInfo } | null> {
  const res = await http.post<{ code: number; msg: string; data: { token: string; userInfo: UserInfo } }>('/login', { username, password })
  if (res.data.code === 200) return res.data.data
  throw new Error(res.data.msg || '登录失败')
}

export async function changePassword(userId: number, oldPwd: string, newPwd: string): Promise<boolean> {
  const res = await http.put<{ code: number; msg: string }>('/password', null, { params: { userId, oldPwd, newPwd } })
  return res.data.code === 200
}

export async function register(username: string, password: string): Promise<boolean> {
  const res = await http.post<{ code: number; msg: string }>('/register', { username, password })
  if (res.data.code === 200) return true
  throw new Error(res.data.msg || '注册失败')
}
