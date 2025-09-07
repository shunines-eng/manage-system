<template>
  <div class="user-management-container">
    <el-card class="page-header-card">
      <div slot="header" class="card-header">
        <span>用户管理</span>
      </div>
      <div class="page-description">
        <p>管理系统中的所有用户信息，包括查看、编辑和删除用户。</p>
      </div>
    </el-card>

    <!-- 搜索和添加按钮区域 -->
    <el-row :gutter="20" class="search-add-row">
      <el-col :span="16">
        <el-input
          v-model="searchQuery"
          placeholder="搜索用户名或邮箱"
          prefix-icon="el-icon-search"
          clearable
          @clear="handleSearch"
          @keyup.enter.native="handleSearch"
        />
      </el-col>
      <el-col :span="8" class="add-button-col">
        <el-button type="primary" @click="showAddUserModal">
          <i class="el-icon-plus"></i> 添加用户
        </el-button>
      </el-col>
    </el-row>

    <!-- 用户列表 -->
    <el-card class="user-list-card">
      <el-table :data="filteredUsers" style="width: 100%" border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="fullName" label="姓名" min-width="120" />
        <el-table-column prop="age" label="年龄" width="80" align="center" />
        <el-table-column prop="gender" label="性别" width="80" align="center" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="phone" label="电话" min-width="120" />
        <el-table-column prop="role" label="角色" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.role === 'ROLE_ADMIN' ? 'success' : 'info'">
              {{ scope.row.role === 'ROLE_ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="80" align="center">
          <template slot-scope="scope">
            <el-switch
              v-model="scope.row.enabled"
              active-color="#13ce66"
              inactive-color="#ff4949"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="primary" size="small" @click="showEditUserModal(scope.row)">
              <i class="el-icon-edit"></i> 编辑
            </el-button>
            <el-button type="danger" size="small" @click="showDeleteConfirm(scope.row.id)">
              <i class="el-icon-delete"></i> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="users.length"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          :current-page="currentPage"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑用户模态框 -->
    <el-dialog
      :title="isEditMode ? '编辑用户' : '添加用户'"
      :visible.sync="userModalVisible"
      width="50%
    >
      <el-form ref="userForm" :model="formUser" :rules="userRules" label-width="100px">
        <el-form-item label="用户名" prop="username" :disabled="isEditMode">
          <el-input v-model="formUser.username" placeholder="请输入用户名" readonly />
        </el-form-item>
        <el-form-item label="姓名" prop="fullName">
          <el-input v-model="formUser.fullName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input v-model.number="formUser.age" placeholder="请输入年龄" type="number" min="0" max="150" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="formUser.gender" placeholder="请选择性别">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
            <el-option label="保密" value="保密" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formUser.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="formUser.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="角色" prop="role" v-if="$store.getters.userInfo?.role === 'ROLE_ADMIN'">
          <el-select v-model="formUser.role" placeholder="请选择角色">
            <el-option label="普通用户" value="ROLE_USER" />
            <el-option label="管理员" value="ROLE_ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="enabled">
          <el-switch v-model="formUser.enabled" />
        </el-form-item>
        <el-form-item v-if="!isEditMode" label="密码" prop="password">
          <el-input v-model="formUser.password" type="password" placeholder="请输入密码" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="userModalVisible = false">取消</el-button>
        <el-button type="primary" @click="submitUserForm">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'UserManagement',
  data() {
    return {
      users: [],
      searchQuery: '',
      currentPage: 1,
      pageSize: 10,
      userModalVisible: false,
      isEditMode: false,
      formUser: {
        id: null,
        username: '',
        fullName: '',
        age: null,
        gender: '',
        email: '',
        phone: '',
        role: 'ROLE_USER',
        enabled: true,
        password: ''
      },
      userRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
          { validator: (rule, value, callback) => {
              if (this.isEditMode) {
                // 编辑模式下不验证用户名变化
                callback();
              } else {
                callback();
              }
            }, trigger: 'blur' }
        ],
        fullName: [
          { required: true, message: '请输入姓名', trigger: 'blur' },
          { max: 50, message: '姓名不能超过 50 个字符', trigger: 'blur' }
        ],
        age: [
          { type: 'number', min: 0, max: 150, message: '年龄必须在 0 到 150 之间', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    filteredUsers() {
      if (!this.searchQuery) {
        return this.users.slice((this.currentPage - 1) * this.pageSize, this.currentPage * this.pageSize)
      }
      const query = this.searchQuery.toLowerCase()
      const filtered = this.users.filter(user => 
        user.username.toLowerCase().includes(query) || 
        user.email.toLowerCase().includes(query)
      )
      return filtered.slice((this.currentPage - 1) * this.pageSize, this.currentPage * this.pageSize)
    }
  },
  mounted() {
    this.loadUsers()
  },
  methods: {
    async loadUsers() {
      try {
        const response = await axios.get('/api/admin/users')
        this.users = response.data.users
      } catch (error) {
        this.$message.error('获取用户列表失败：' + (error.response?.data || error.message))
      }
    },
    handleSearch() {
      this.currentPage = 1
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
    },
    handleCurrentChange(current) {
      this.currentPage = current
    },
    showAddUserModal() {
      this.isEditMode = false
      this.formUser = {
        id: null,
        username: '',
        fullName: '',
        age: null,
        gender: '',
        email: '',
        phone: '',
        role: 'ROLE_USER',
        enabled: true,
        password: ''
      }
      this.userModalVisible = true
    },
    showEditUserModal(user) {
      this.isEditMode = true
      // 深拷贝用户对象
      this.formUser = {
        ...user,
        age: user.age ? parseInt(user.age) : null
      }
      this.userModalVisible = true
    },
    async submitUserForm() {
      this.$refs.userForm.validate(async (valid) => {
        if (valid) {
          try {
            let response
            if (this.isEditMode) {
              // 编辑用户
              response = await axios.put(`/api/admin/users/${this.formUser.id}`, this.formUser)
            } else {
              // 添加用户
              // 注意：在实际项目中，添加用户可能需要使用专门的注册接口
              // 这里为了演示，我们使用更新接口的方式
              response = await axios.post('/api/admin/users', this.formUser)
            }
            this.$message.success(response.data.message || '操作成功')
            this.userModalVisible = false
            this.loadUsers()
          } catch (error) {
            this.$message.error('操作失败：' + (error.response?.data || error.message))
          }
        }
      })
    },
    async handleStatusChange(user) {
      try {
        await axios.put(`/api/admin/users/${user.id}`, { enabled: user.enabled })
        this.$message.success('状态更新成功')
      } catch (error) {
        // 恢复原来的状态
        user.enabled = !user.enabled
        this.$message.error('状态更新失败：' + (error.response?.data || error.message))
      }
    },
    showDeleteConfirm(userId) {
      this.$confirm('此操作将永久删除该用户，是否继续？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await axios.delete(`/api/admin/users/${userId}`)
          this.$message.success(response.data.message || '删除成功')
          this.loadUsers()
        } catch (error) {
          this.$message.error('删除失败：' + (error.response?.data || error.message))
        }
      }).catch(() => {
        this.$message.info('已取消删除')
      })
    }
  }
}
</script>

<style scoped>
.user-management-container {
  padding: 20px;
}

.page-header-card {
  margin-bottom: 20px;
}

.page-description {
  margin-top: 10px;
  color: #606266;
}

.search-add-row {
  margin-bottom: 20px;
}

.add-button-col {
  text-align: right;
}

.user-list-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  text-align: right;
}
</style>