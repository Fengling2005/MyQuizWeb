<template>
  <el-container class="admin-wrapper" style="height: 100%;">
    <el-aside width="200px" class="my-aside">
      <h3 class="title">答题训练系统</h3>
      <el-menu
          style="width: 100%; overflow: hidden;"
          active-text-color="#409EFF"
          background-color="#000000"
          text-color="white"
          :default-active="useRoute().path"
          @select="handleMenuSelect"
          router>
        <el-menu-item index="/admin">
          <el-icon>
            <HomeOutlined/>
          </el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/admin/practice">
          <el-icon>
            <EditPen />
          </el-icon>
          <span>答题训练</span>
        </el-menu-item>
        <el-menu-item index="/admin/marked">
          <el-icon>
            <Star />
          </el-icon>
          <span>我的收藏</span>
        </el-menu-item>
        <el-menu-item index="/admin/wrong">
          <el-icon>
            <Warning />
          </el-icon>
          <span>错题管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/question">
          <el-icon>
            <Notebook />
          </el-icon>
          <span>题库管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/admin">
          <el-icon>
            <User/>
          </el-icon>
          <span>管理员管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>

      <el-header height="60px">
        <el-row :gutter="5">
          <el-col :span="6" style="margin-top: 20px;">
            <el-space>
            </el-space>
          </el-col>
          <el-col :span="6">
          </el-col>
          <el-col :span="9">
          </el-col>
          <el-col :span="3">
            <div style="text-align: right;">
              <el-space style="margin-top: 15px;">
                <el-dropdown v-if="isUserLogin">
                  <div>
                    <el-space>
                      <el-avatar style="width: 30px;height: 30px;border-radius: 50%"
                                 shape="square" :size="30" :src="currentUser.avatarUrl"></el-avatar>
                      <span style="font-size: 16px">  {{ currentUser.username }}</span></el-space>
                  </div>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item><span @click="editCurrentUser">个人信息</span></el-dropdown-item>
                      <el-dropdown-item><span @click="editPassword">修改密码</span></el-dropdown-item>
                      <el-dropdown-item divided><span @click="logout">退出登录</span></el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>

              </el-space>
            </div>
          </el-col>
        </el-row>
      </el-header>
      <el-main style="background-color: RGB(245,245,245)" class="my-main">
        <router-view/>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import tools from "@/utils/tools.js";
import {ref} from "vue";
import router from "@/router/index.js";
import {ElMessage} from "element-plus";
import {useRoute} from 'vue-router';
import {HomeOutlined} from "@ant-design/icons-vue";
import {User} from '@element-plus/icons-vue'
import "@/styles/admin.css";

const isUserLogin = ref(tools.isLogin())
const currentUser = ref(tools.getCurrentUser())
const activeIndex = ref(useRoute().path)

if (currentUser.value === null) {
  window.location.href = "/login"
}

function handleMenuSelect(key, keyPath) {
  activeIndex.value = key
}


function logout() {
  ElMessage({
    message: '退出登录成功，正在跳转',
    type: 'success'
  });
  localStorage.clear()
  router.push({path: "/login"})
}

function editCurrentUser() {
  router.push({path: "/admin/editCurrentUser"})
}

function editPassword() {
  router.push({path: "/admin/editPassword"})
}


const isCollapse = ref(true)

</script>

<style scoped>
.admin-wrapper {
  min-height: 100vh;
  overflow: hidden;
}

.title {
  color: #1b5e20; /* 修改为深绿色 */
  width: 100%;
  text-align: center;
  margin: 15px 5px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(27, 94, 32, 0.2); /* 修改边框颜色为深绿色半透明 */
  font-size: 18px;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  letter-spacing: 1px; /* 添加字间距提升可读性 */
}

.my-main::-webkit-scrollbar {
  display: none; /* 隐藏滚动条 */
}

.my-aside {
  background: linear-gradient(180deg, #e8f5e9 0%, #c8e6c9 100%);
  overflow-x: hidden;
  box-shadow: 2px 0 10px rgba(76, 175, 80, 0.1);
  transition: all 0.3s ease;
}

.el-menu {
  border-right: 0px;
  background-color: transparent !important;
  transition: all 0.3s ease;
}

.el-menu :deep(.el-menu-item) {
  color: #2e7d32 !important;
  font-weight: 500;
  height: 50px;
  line-height: 50px;
  margin: 5px 10px;
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.el-menu :deep(.el-menu-item):hover {
  background-color: rgba(129, 199, 132, 0.2) !important;
  color: #1b5e20 !important; /* 悬停时使用深绿色 */
  transform: translateX(5px);
  box-shadow: 0 2px 8px rgba(129, 199, 132, 0.3);
}

.el-menu :deep(.el-menu-item.is-active) {
  background-color: #66bb6a !important;
  color: white !important;
  box-shadow: 0 4px 12px rgba(102, 187, 106, 0.4);
}

.el-menu :deep(.el-menu-item.is-active):hover {
  background-color: #4caf50 !important;
  color: white !important;
}

.el-menu :deep(.el-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 4px;
  background-color: #1b5e20; /* 深绿色激活指示条 */
  border-radius: 0 4px 4px 0;
  animation: slideIn 0.3s ease;
}

.el-menu :deep(.el-icon) {
  color: inherit !important;
  margin-right: 8px;
  font-size: 18px;
  transition: transform 0.3s ease;
}

.el-menu :deep(.el-menu-item:hover .el-icon) {
  transform: scale(1.1);
}

.el-header {
  background: linear-gradient(90deg, #e8f5e9 0%, #ffffff 100%);
  border-bottom: 1px solid #c8e6c9;
  box-shadow: 0 2px 12px rgba(129, 199, 132, 0.1);
  padding: 0 20px;
  transition: all 0.3s ease;
}

.el-header:hover {
  box-shadow: 0 4px 16px rgba(129, 199, 132, 0.15);
}

.el-main {
  background: linear-gradient(135deg, #f8fdf8 0%, #f0f9f0 100%);
  padding: 20px;
  transition: all 0.3s ease;
}

.el-dropdown :deep(.el-dropdown-link) {
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 5px;
  border-radius: 20px;
}

.el-dropdown :deep(.el-dropdown-link):hover {
  background-color: rgba(129, 199, 132, 0.1);
}

.el-avatar {
  transition: all 0.3s ease;
}

.el-avatar:hover {
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(102, 187, 106, 0.3);
}

.el-dropdown-menu {
  border: 1px solid #c8e6c9;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.15);
  overflow: hidden;
  animation: dropdownSlide 0.2s ease;
}

.el-dropdown-menu__item {
  color: #2e7d32;
  transition: all 0.2s ease;
}

.el-dropdown-menu__item:hover {
  background-color: #e8f5e9;
  color: #1b5e20; /* 深绿色文字 */
  transform: translateX(3px);
}

.el-dropdown-menu__item--divided {
  border-top: 1px solid #e8f5e9 !important;
}

@keyframes slideIn {
  from {
    transform: translateY(-10px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@keyframes dropdownSlide {
  from {
    transform: translateY(-10px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .my-aside {
    width: 64px !important;
  }

  .title {
    font-size: 14px;
    margin: 10px 0;
    color: #1b5e20; /* 保持深绿色 */
  }

  .el-menu :deep(span) {
    display: none;
  }

  .el-menu :deep(.el-menu-item) {
    margin: 5px;
    padding: 0 !important;
    justify-content: center;
  }

  .el-menu :deep(.el-icon) {
    margin-right: 0;
  }
}

/* 滚动条美化 */
.my-main {
  scrollbar-width: thin;
  scrollbar-color: #c8e6c9 transparent;
}

.my-main::-webkit-scrollbar {
  width: 6px;
  display: block;
}

.my-main::-webkit-scrollbar-track {
  background: transparent;
}

.my-main::-webkit-scrollbar-thumb {
  background-color: #c8e6c9;
  border-radius: 20px;
}

.my-main::-webkit-scrollbar-thumb:hover {
  background-color: #a5d6a7;
}
</style>