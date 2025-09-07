<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h2>个人信息管理系统</h2>
        <p>管理员注册</p>
      </div>
      <el-form :model="registerForm" :rules="rules" ref="registerForm" class="register-form">
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            prefix-icon="el-icon-user"
            clearable
            @blur="checkUsername"
          ></el-input>
        </el-form-item>
        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱"
            prefix-icon="el-icon-message"
            clearable
            @blur="checkEmail"
          ></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="el-icon-lock"
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            prefix-icon="el-icon-lock"
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item prop="fullName">
          <el-input
            v-model="registerForm.fullName"
            placeholder="请输入姓名"
            prefix-icon="el-icon-user-solid"
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号码"
            prefix-icon="el-icon-phone"
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="medium"
            class="register-button"
            :loading="loading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
        <div class="login-link">
          <span>已有账号？</span>
          <router-link to="/login">立即登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Register',
  data() {
    // 密码一致性验证
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.registerForm.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }

    return {
      registerForm: {
        username: '',
        email: '',
        password: '',
        confirmPassword: '',
        fullName: '',
        phone: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
          { pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,20}$/, message: '密码必须包含字母和数字', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ],
        fullName: [
          { required: true, message: '请输入姓名', trigger: 'blur' },
          { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入手机号码', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码', trigger: 'blur' }
        ]
      },
      loading: false,
      usernameChecking: false,
      emailChecking: false
    }
  },
  methods: {
    // 检查用户名是否已存在
    async checkUsername() {
      if (!this.registerForm.username) return

      this.usernameChecking = true
      try {
        const response = await this.$axios.get(`/api/users/check-username?username=${this.registerForm.username}`)
        if (!response.data.available) {
          this.$message.warning('用户名已存在')
        }
      } catch (error) {
        console.error('检查用户名失败:', error)
      } finally {
        this.usernameChecking = false
      }
    },

    // 检查邮箱是否已存在
    async checkEmail() {
      if (!this.registerForm.email) return

      this.emailChecking = true
      try {
        const response = await this.$axios.get(`/api/users/check-email?email=${this.registerForm.email}`)
        if (!response.data.available) {
          this.$message.warning('邮箱已被注册')
        }
      } catch (error) {
        console.error('检查邮箱失败:', error)
      } finally {
        this.emailChecking = false
      }
    },

    async handleRegister() {
      this.$refs.registerForm.validate(async (valid) => {
        if (valid && !this.usernameChecking && !this.emailChecking) {
          this.loading = true
          try {
            await this.$store.dispatch('register', {
              username: this.registerForm.username,
              email: this.registerForm.email,
              password: this.registerForm.password,
              fullName: this.registerForm.fullName,
              phone: this.registerForm.phone
            })
            
            this.$message.success('注册成功，即将跳转到首页')
            this.$router.push('/')
          } catch (error) {
            console.error('注册失败:', error)
            const errorMessage = error.response?.data?.message || '注册失败，请稍后重试'
            this.$message.error(errorMessage)
          } finally {
            this.loading = false
          }
        } else {
          this.$message.warning('请填写完整的注册信息')
          return false
        }
      })
    }
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-box {
  width: 450px;
  padding: 40px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  color: #333;
  margin-bottom: 10px;
  font-size: 24px;
}

.register-header p {
  color: #666;
  font-size: 14px;
}

.register-form {
  width: 100%;
}

.register-button {
  width: 100%;
  height: 40px;
  font-size: 16px;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
}

.login-link a {
  color: #1890ff;
  text-decoration: none;
  margin-left: 5px;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>