/** Token 管理 */
const TOKEN_KEY = 'gexianmi_token'
const USER_KEY = 'gexianmi_user'
const REMEMBER_KEY = 'gexianmi_remember'

export interface UserInfo {
  id: number
  username: string
  role: string
}

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function getUser(): UserInfo | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try { return JSON.parse(raw) } catch { return null }
}

export function setUser(user: UserInfo) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function isLoggedIn(): boolean {
  return !!getToken()
}

export function isAdmin(): boolean {
  const user = getUser()
  return user?.role === 'admin'
}

export function logout() {
  clearToken()
}

// ==================== 记住密码 ====================

export interface RememberedCred {
  username: string
  password: string // base64 编码，仅防肉眼查看
}

/** 保存记住的密码（base64 编码） */
export function saveRemembered(username: string, password: string) {
  const cred: RememberedCred = {
    username,
    password: btoa(unescape(encodeURIComponent(password))),
  }
  localStorage.setItem(REMEMBER_KEY, JSON.stringify(cred))
}

/** 取出记住的密码 */
export function loadRemembered(): RememberedCred | null {
  const raw = localStorage.getItem(REMEMBER_KEY)
  if (!raw) return null
  try {
    const cred: RememberedCred = JSON.parse(raw)
    return {
      username: cred.username,
      password: decodeURIComponent(escape(atob(cred.password))),
    }
  } catch {
    return null
  }
}

/** 清除记住的密码 */
export function clearRemembered() {
  localStorage.removeItem(REMEMBER_KEY)
}
