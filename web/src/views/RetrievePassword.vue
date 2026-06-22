<template>
  <div class="main-context">
    <el-card class="box-card">
      <el-space direction="vertical" style="width: 100%" size="large">
        <el-space>
          <img src="../assets/logo.png" width="100%" style="width: 55px">
          <el-space direction="vertical" style="width: 100%" size="small">
            <h2 style="font-style: oblique">答题训练系统</h2>
            <span style="font-style: oblique;font-size: 15px">MyQuizWeb.com</span>
          </el-space>
        </el-space>
        <el-form :model="formData" label-width="80px" :rules="rules" ref="formRef" style="width: 100%">
          <el-form-item label="用户名" prop="username"
                        :rules="[{required:true,message:'请输入用户名',trigger:['blur','change']}]">
            <el-input
                style="width: 180px"
                placeholder="请输入用户名"
                v-model.trim="formData.username"
                clearable
            >
            </el-input>
          </el-form-item>
          <el-form-item label="手机号" prop="tel"
                        :rules="[
                          {required:true,message:'请输入手机号',trigger:['blur','change']}
                        ]">
            <el-input
                style="width: 180px"
                placeholder="请输入手机号"
                v-model.trim="formData.tel"
                clearable
            >
            </el-input>
          </el-form-item>
          <el-form-item label="新密码" prop="password"
                        :rules="[
                          {required:true,message:'请输入新密码',trigger:['blur','change']},
                          {min:6,message:'密码长度至少6位',trigger:['blur','change']}
                        ]">
            <el-input
                style="width: 180px"
                placeholder="请输入新密码"
                show-password
                v-model.trim="formData.password"
                clearable
            >
            </el-input>
          </el-form-item>
          <el-form-item label="" style="width: 100%">
            <el-space direction="vertical" alignment="left" style="width: 100%">
              <el-button @click="submitForm()" type="success" style="width: 100%">重置密码</el-button>
              <router-link tag="span" :to="{path:'login'}">
                <el-button type="text" class="button" style="float: right">返回登录</el-button>
              </router-link>
            </el-space>
          </el-form-item>
        </el-form>
      </el-space>
    </el-card>
  </div>
</template>

<script setup>
import {ref, reactive} from 'vue';
import {ElMessage} from 'element-plus';
import http from "@/utils/http.js";
import router from "@/router/index.js";

const formRef = ref(null);
const formData = ref({
  username: '',
  tel: '',
  password: ''
});

// 表单验证规则
const rules = reactive({
  username: [
    {required: true, message: '请输入用户名', trigger: ['blur', 'change']}
  ],
  tel: [
    {required: true, message: '请输入手机号', trigger: ['blur', 'change']}
  ],
  password: [
    {required: true, message: '请输入新密码', trigger: ['blur', 'change']},
    {min: 6, message: '密码长度至少6位', trigger: ['blur', 'change']}
  ]
});

const submitForm = () => {
  formRef.value.validate((valid) => {
    if (!valid) {
      ElMessage({
        message: "请正确填写表单信息",
        type: "warning"
      });
      return;
    }

    http.post("/common/retrievePassword", formData.value).then(res => {
      if (!res) {
        return;
      }
      ElMessage({
        message: "密码重置成功，请使用新密码登录",
        type: "success"
      });
      // 清空表单
      formData.value = {
        username: '',
        tel: '',
        password: ''
      };
      // 跳转到登录页面
      setTimeout(() => {
        router.push({path: "/login"});
      }, 1500);
    }).catch(error => {
      ElMessage({
        message: error.message || "重置失败，请检查用户名和手机号是否正确",
        type: "error"
      });
    });
  });
}
</script>

<style scoped>
.main-context {
  height: 100vh; /* 使 .app 高度为视口高度 */
  background: url("../assets/1.jpg") no-repeat center center fixed;
  background-size: cover; /* 使用 cover 保持图片比例 */
  display: flex;
  justify-content: center;
  align-items: center;
  color: white; /* 根据背景图片调整文字颜色 */
}

.box-card {
  width: 350px;
  margin: 0 auto;
  text-align: center;
}
</style>