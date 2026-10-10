<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { login, register } from '@/api/auth'
import { setToken, setUser, saveRemembered, loadRemembered, clearRemembered } from '@/utils/auth'

const router = useRouter()
const loading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')
const isRegister = ref(false)  // 登录 / 注册切换

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  remember: false,
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: (_r: any, v: string, cb: any) => v === form.password ? cb() : cb(new Error('两次密码不一致')), trigger: 'blur' },
  ],
}

const formRef = ref()

// 页面加载时，尝试读取已保存的密码并自动填充
onMounted(() => {
  const saved = loadRemembered()
  if (saved) {
    form.username = saved.username
    form.password = saved.password
    form.remember = true
  }
})

function toggleMode() {
  isRegister.value = !isRegister.value
  errorMsg.value = ''
  successMsg.value = ''
  form.username = ''
  form.password = ''
  form.confirmPassword = ''
  form.remember = false
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  errorMsg.value = ''
  try {
    const result = await login(form.username, form.password)
    if (result) {
      // 记住密码：勾了就存，没勾就清
      if (form.remember) {
        saveRemembered(form.username, form.password)
      } else {
        clearRemembered()
      }
      setToken(result.token)
      setUser(result.userInfo)
      router.replace('/monitor')
    }
  } catch (e: any) {
    errorMsg.value = e.message || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  errorMsg.value = ''
  successMsg.value = ''
  try {
    await register(form.username, form.password)
    successMsg.value = '注册成功！请切换到登录页进行登录'
    // 2 秒后自动切换
    setTimeout(() => toggleMode(), 2000)
  } catch (e: any) {
    errorMsg.value = e.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <!-- 左侧品牌区 -->
      <div class="login-brand">
        <div class="brand-icon">🌿</div>
        <h1 class="brand-title">葛仙米生长环境监测系统</h1>
        <p class="brand-sub">Gexianmi Environmental Monitoring System</p>
        <div class="brand-features">
          <span>实时监测</span><span>·</span><span>数据分析</span><span>·</span><span>智能告警</span>
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="login-form">
        <h2 class="form-title">{{ isRegister ? '创建账户' : '用户登录' }}</h2>
        <p class="form-sub">{{ isRegister ? '注册一个新账户，加入监测系统' : '欢迎回来，请登录您的账户' }}</p>

        <el-alert v-if="errorMsg" :title="errorMsg" type="error" show-icon :closable="false" class="login-error" />
        <el-alert v-if="successMsg" :title="successMsg" type="success" show-icon :closable="false" class="login-error" />

        <el-form v-if="!isRegister" ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="handleLogin">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" size="large" prefix-icon="User" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="form.remember">记住密码</el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" :loading="loading" class="login-btn" @click="handleLogin">
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <el-form v-else ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="handleRegister">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" size="large" prefix-icon="User" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入密码（至少 6 位）" size="large" prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" size="large" prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" :loading="loading" class="login-btn" @click="handleRegister">
              {{ loading ? '注册中...' : '注 册' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-switch">
          <span v-if="!isRegister">还没有账户？</span>
          <span v-else>已有账户？</span>
          <el-button type="primary" link @click="toggleMode">
            {{ isRegister ? '去登录' : '立即注册' }}
          </el-button>
        </div>

        <p class="login-hint">默认管理员：admin / admin123</p>
      </div>
    </div>

    <p class="login-footer">© 2026 葛仙米数字孪生团队 · GexianMI Monitoring System</p>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  background: #ffffff;
  padding: 20px;
}
.login-card {
  display: flex;
  max-width: 860px; width: 100%;
  background: #fff; border-radius: 16px; overflow: hidden;
  box-shadow: 0 4px 24px rgba(0,0,0,.08);
  border: 1px solid #ebeef5;
}

/* 左侧品牌区 */
.login-brand {
  flex: 1; background: linear-gradient(135deg, #1a5276 0%, #2e86c1 100%);
  color: #fff; padding: 48px 36px; display: flex;
  flex-direction: column; justify-content: center; align-items: center; text-align: center;
}
.brand-icon { font-size: 56px; margin-bottom: 16px; }
.brand-title { font-size: 22px; font-weight: 700; margin: 0 0 8px; }
.brand-sub { font-size: 13px; opacity: 0.8; margin: 0 0 24px; font-style: italic; }
.brand-features { font-size: 13px; opacity: 0.75; display: flex; gap: 8px; }

/* 右侧表单区 */
.login-form { flex: 1; padding: 48px 40px; }
.form-title { font-size: 24px; font-weight: 700; color: #303133; margin: 0 0 4px; }
.form-sub { color: #909399; font-size: 14px; margin: 0 0 24px; }
.login-error { margin-bottom: 16px; }
.login-btn { width: 100%; height: 44px; font-size: 16px; }
.login-switch { text-align: center; color: #909399; font-size: 14px; margin-top: 12px; }
.login-hint { text-align: center; color: #c0c4cc; font-size: 12px; margin-top: 12px; }

.login-footer { color: #c0c4cc; font-size: 12px; margin-top: 24px; }

@media (max-width: 640px) {
  .login-card { flex-direction: column; max-width: 400px; }
  .login-brand { padding: 32px 24px; }
  .brand-title { font-size: 18px; }
  .login-form { padding: 32px 24px; }
}
</style>
