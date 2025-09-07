<template>
  <div class="home-container">
    <el-container>
      <el-header class="home-header">
        <div class="header-left">
          <el-button type="text" @click="toggleSidebar" class="sidebar-toggle">
            <i class="el-icon-menu"></i>
          </el-button>
          <h1>个人信息管理系统</h1>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-info">
              <img src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" class="avatar">
              <span>{{ $store.getters.fullName || $store.getters.username }}</span>
              <i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item command="settings">设置</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>
      <el-container>
        <el-aside :width="sidebarWidth + 'px'" class="home-sidebar" :class="{ 'sidebar-collapsed': isSidebarCollapsed }">
          <el-menu :default-active="activeMenu" class="el-menu-vertical-demo" @select="handleMenuSelect">
            <el-menu-item index="dashboard">
              <i class="el-icon-s-home"></i>
              <span slot="title">首页</span>
            </el-menu-item>
            <el-menu-item index="user-management">
              <i class="el-icon-user"></i>
              <span slot="title">用户管理</span>
            </el-menu-item>
            <el-menu-item index="personal-info">
              <i class="el-icon-document"></i>
              <span slot="title">个人信息</span>
            </el-menu-item>
            <el-menu-item index="settings">
              <i class="el-icon-setting"></i>
              <span slot="title">系统设置</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main class="home-main">
          <div class="welcome-section">
            <el-card class="welcome-card">
              <div slot="header" class="card-header">
                <span>欢迎回来！</span>
              </div>
              <div class="card-content">
                <p>您好，{{ $store.getters.fullName || $store.getters.username }}，欢迎使用个人信息管理系统。</p>
                <p>今天是 {{ currentDate }}，祝您工作愉快！</p>
              </div>
            </el-card>
          </div>
          <div class="dashboard-stats">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-card class="stat-card">
                  <div class="stat-content">
                    <div class="stat-number">0</div>
                    <div class="stat-label">总用户数</div>
                  </div>
                  <div class="stat-icon">
                    <i class="el-icon-user"></i>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="stat-card">
                  <div class="stat-content">
                    <div class="stat-number">0</div>
                    <div class="stat-label">今日访问</div>
                  </div>
                  <div class="stat-icon">
                    <i class="el-icon-view"></i>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="stat-card">
                  <div class="stat-content">
                    <div class="stat-number">0</div>
                    <div class="stat-label">待处理事项</div>
                  </div>
                  <div class="stat-icon">
                    <i class="el-icon-bell"></i>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
          <div class="recent-activities">
            <el-card class="activities-card">
              <div slot="header" class="card-header">
                <span>最近活动</span>
              </div>
              <div class="activities-list">
                <el-empty description="暂无活动记录"></el-empty>
              </div>
            </el-card>
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'Home',
  data() {
    return {
      isSidebarCollapsed: false,
      sidebarWidth: 200,
      collapsedSidebarWidth: 64,
      activeMenu: 'dashboard',
      currentDate: ''
    }
  },
  created() {
    this.getCurrentDate()
  },
  methods: {
    getCurrentDate() {
      const now = new Date()
      const options = {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'long'
      }
      this.currentDate = now.toLocaleDateString('zh-CN', options)
    },
    toggleSidebar() {
      this.isSidebarCollapsed = !this.isSidebarCollapsed
      this.sidebarWidth = this.isSidebarCollapsed ? this.collapsedSidebarWidth : 200
    },
    handleMenuSelect(key) {
      this.activeMenu = key
      // 这里可以根据选中的菜单项执行相应的操作
      console.log('Selected menu:', key)
    },
    handleCommand(command) {
      switch (command) {
        case 'profile':
          this.$router.push('/profile')
          break
        case 'settings':
          this.$router.push('/settings')
          break
        case 'logout':
          this.handleLogout()
          break
        default:
          break
      }
    },
    handleLogout() {
      this.$confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$store.dispatch('logout')
        this.$router.push('/login')
        this.$message.success('已成功退出登录')
      }).catch(() => {
        // 用户取消退出
      })
    }
  }
}
</script>

<style scoped>
.home-container {
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

.home-header {
  background: white;
  height: 60px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.sidebar-toggle {
  margin-right: 20px;
}

.header-left h1 {
  color: #333;
  font-size: 20px;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #333;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  margin-right: 10px;
}

.home-sidebar {
  background: #2c3e50;
  transition: width 0.3s ease;
  overflow: hidden;
}

.sidebar-collapsed .el-menu-item span {
  display: none;
}

.sidebar-collapsed .el-menu-item i {
  margin-right: 0;
  font-size: 18px;
}

.el-menu-vertical-demo {
  background: transparent;
  border-right: none;
}

.el-menu-item {
  color: #ecf0f1;
  height: 60px;
  line-height: 60px;
  font-size: 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.el-menu-item:hover {
  background: #34495e;
}

.el-menu-item.is-active {
  background: #1890ff;
  color: white;
}

.home-main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

.welcome-section {
  margin-bottom: 20px;
}

.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.card-header {
  font-size: 16px;
  font-weight: bold;
}

.welcome-card .card-header {
  color: white;
}

.card-content {
  padding: 20px 0;
}

.card-content p {
  margin: 10px 0;
  font-size: 14px;
}

.dashboard-stats {
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  overflow: hidden;
  background: white;
  transition: transform 0.3s ease;
  height: 120px;
  display: flex;
  align-items: center;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.stat-content {
  flex: 1;
  padding: 20px;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.stat-icon {
  width: 80px;
  height: 80px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
}

.stat-icon i {
  font-size: 32px;
  color: #667eea;
}

.recent-activities {
  margin-bottom: 20px;
}

.activities-card {
  background: white;
  min-height: 300px;
}

.activities-list {
  padding: 20px 0;
}
</style>