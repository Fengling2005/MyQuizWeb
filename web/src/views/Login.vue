<template>
  <div class="main-context">
    <el-card class="box-card">
      <el-space direction="vertical" style="width: 100%" size="large">
        <el-space>
          <img src="../assets/xigua.png" width="100%" style="width: 55px">
          <el-space direction="vertical" style="width: 100%" size="small">
            <h2 style="font-style: oblique">答题训练系统</h2>
            <span style="font-style: oblique;font-size: 15px">MyQuizWeb.com</span>
          </el-space>
        </el-space>
        <el-form :model="formData" label-width="0px" ref="formRef">
          <el-form-item label="" prop="username"
                        :rules="[{required:true,message:'请输入用户名',trigger:[ 'blur','change']}]">
            <el-input
                :prefix-icon="User"
                placeholder="请输入账号"
                v-model.trim="formData.username"
                clearable
            ></el-input>
          </el-form-item>
          <el-form-item label="" prop="password"
                        :rules="[{required:true,message:'请输入密码',trigger:[ 'blur','change']}]">
            <el-input
                :prefix-icon="Lock"
                placeholder="请输入密码"
                show-password
                style="width: 240px"
                v-model.trim="formData.password"
                clearable
            ></el-input>
          </el-form-item>
          <el-form-item label="">
            <el-space direction="vertical" alignment="left" style="width: 100%">
              <el-button @click="submitForm()" type="primary" style="width: 100%">登 录</el-button>
              <router-link tag="span" :to="{path:'register'}">
                <span style="float: right">没有账号？去注册</span>
              </router-link>
              <router-link tag="span" :to="{path:'retrievePassword'}">
                <span class="button" style="float: right">忘记密码？</span>
              </router-link>
            </el-space>
          </el-form-item>
        </el-form>
      </el-space>
    </el-card>
  </div>
</template>
<script setup>
import {ref} from 'vue';
import {ElMessage} from 'element-plus';
import http from "@/utils/http.js";
import {User, Lock} from "@element-plus/icons-vue";
import router from "@/router/index.js";

const formData = ref({
  username: '',
  password: '',
});

const formRef = ref(null);
const submitForm = () => {
  formRef.value.validate((valid) => {
    if (!valid) {
      return;
    }

    http.post("/common/login", formData.value).then(res => {
      if (!res) {
        ElMessage.error("登录响应为空");
        return;
      }

      if (res.code !== 200) {
        ElMessage.error(res.msg || "登录失败");
        return;
      }

      ElMessage({
        message: "登录成功，正在跳转",
        type: "success"
      });

      console.log("登录响应完整数据:", res);

      // 关键：Token在res.msg中
      const token = res.msg;
      console.log("获取到的Token:", token ? token.substring(0, 30) + "..." : "空");

      localStorage.setItem("token", token);
      console.log("Token已保存到localStorage");

      // 获取当前用户信息
      http.get("/common/currentUser").then(userRes => {
        if (userRes.code === 200) {
          let currentUser = userRes.data;
          localStorage.setItem("currentUser", JSON.stringify(currentUser));
          console.log("当前用户信息:", currentUser);
          router.push({path: "/admin"});
        } else {
          ElMessage.error("获取用户信息失败: " + userRes.msg);
        }
      }).catch(err => {
        console.error("获取用户失败:", err);
        ElMessage.error("获取用户信息异常");
      });

    }).catch(error => {
      console.error("登录请求失败:", error);
      ElMessage.error(error.msg || "登录请求失败");
    });
  });
};
</script>
<style scoped>
.main-context {
  height: 100vh; /* 使 .app 高度为视口高度 */
  background: url("../assets/2.jpg") no-repeat center center fixed;
  background-size: cover; /* 使用 cover 保持图片比例 */
  display: flex;
  justify-content: center;
  align-items: center;
  color: white; /* 根据背景图片调整文字颜色 */
}

.box-card {
  width: 300px;
  margin: 0 auto;
  text-align: center;
}
</style>