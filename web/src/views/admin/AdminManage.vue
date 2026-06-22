<template>
  <div>
    <el-space direction="vertical" alignment="left" style="width: 100%">
      <el-card>
        <el-form ref="searchFormComponents" :model="searchForm" inline>
          <el-form-item label="用户名" prop="username">
            <el-input v-model="searchForm.username" clearable></el-input>
          </el-form-item>
          <el-form-item label="手机号" prop="tel">
            <el-input v-model="searchForm.tel" clearable></el-input>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 150px">
              <el-option label="启用" value="启用"/>
              <el-option label="禁用" value="禁用"/>
            </el-select>
          </el-form-item>
          <el-form-item label="">
            <el-button type="primary" :icon="Search" @click="search">搜索</el-button>
            <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
        <el-space>
          <el-button type="primary" @click="add" :icon="Plus">新增</el-button>
          <el-button type="danger" :icon="Delete" @click="batchDelete(null)" :disabled="selectionRows.length<=0">
            批量删除
          </el-button>
        </el-space>
      </el-card>
      <el-card>
        <el-table ref="tableComponents"
                  :data="listData"
                  tooltip-effect="dark"
                  style="width: 100%"
                  border
                  @selection-change="selectionChange">
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="id" label="ID" width="50"></el-table-column>
          <el-table-column prop="username" label="用户名称"></el-table-column>
          <el-table-column prop="nickname" label="昵称"></el-table-column>
          <el-table-column label="用户头像">
            <template #default="scope">
              <el-image style="width: 50px; height: 50px" :src="scope.row.avatarUrl"
                        :preview-src-list="[scope.row.avatarUrl]"
                        :preview-teleported="true"></el-image>
            </template>
          </el-table-column>
          <el-table-column prop="tel" label="电话"></el-table-column>
          <el-table-column prop="email" label="邮箱" width="150"></el-table-column>
          <el-table-column prop="status" label="状态">
            <template #default="scope">
              <el-tag type="success" v-if="scope.row.status==='启用'">启用</el-tag>
              <el-tag type="danger" v-if="scope.row.status==='禁用'">禁用</el-tag>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="高级操作" width="140">
            <template #default="scope">
              <el-button type="success" :icon="RefreshLeft" @click="resetPassword( scope.row)">重置密码</el-button>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="200">
            <template #default="scope">
              <el-button :icon="Edit" @click="edit(scope.$index, scope.row)">编辑</el-button>
              <el-button :icon="Delete" type="danger" @click="deleteOne(scope.$index, scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top: 20px">
          <el-pagination
              @current-change="currentChange"
              @size-change="sizeChange"
              :page-size="pageInfo.pageSize"
              :current-page="pageInfo.pageNum"
              background
              layout="total,sizes, prev, pager, next"
              :total="pageInfo.total">
          </el-pagination>
        </div>
      </el-card>
    </el-space>
    <el-dialog
        v-model="dialogOpen"
        v-if="dialogOpen"
        :title="formData.id?'编辑':'新增'"
        width="800px"
    >
      <el-form ref="formRef" :model="formData" label-width="100px" inline>
        <el-form-item label="头像" prop="avatarUrl" style="width: 100%"
                      :rules="[{required:true,message:'不能为空',trigger:[ 'blur','change']}]">
          <MyUpLoad type="imageCard" :limit="1" :files="formData.avatarUrl"
                    @setFiles="formData.avatarUrl =$event"></MyUpLoad>
        </el-form-item>
        <el-form-item label="用户名" prop="username"
                      :rules="[{required:true,message:'不能为空',trigger:[ 'blur','change']}]">
          <el-input v-model="formData.username" placeholder="用户名" :disabled="formData.id!=null"></el-input>
        </el-form-item>
        <el-form-item label="昵称" prop="nickname"
                      :rules="[{required:true,message:'不能为空',trigger:[ 'blur','change']}]">
          <el-input v-model="formData.nickname" placeholder="昵称"></el-input>
        </el-form-item>
        <el-form-item label="电话" prop="tel"
                      :rules="[{required:true,message:'不能为空',trigger:[ 'blur','change']}]">
          <el-input v-model="formData.tel" placeholder="电话"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email"
                      :rules="[{required:true,message:'不能为空',trigger:[ 'blur','change']}]">
          <el-input v-model="formData.email" placeholder="邮箱"></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status"
                      :rules="[{required:true,message:'不能为空',trigger:[ 'blur','change']}]">
          <el-radio-group v-model="formData.status">
            <el-radio label="启用"></el-radio>
            <el-radio label="禁用"></el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submit" :icon="Check">提交</el-button>
          <el-button @click="closeDialog" :icon="Close">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import request from "@/utils/http.js";
import {Check, Close, Delete, Edit, Refresh, Plus, Search, RefreshLeft} from '@element-plus/icons-vue'
import {ref, toRaw} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import MyUpLoad from "@/components/MyUpload.vue";
import MyEditor from "@/components/MyEditor.vue";

const searchFormComponents = ref();
const tableComponents = ref();

const listData = ref([]);
const pageInfo = ref({
  //当前页
  pageNum: 1,
  //分页大小
  pageSize: 10,
  //总条数
  total: 0
});
const searchForm = ref({});


getPageList()

/**
 * 获取分页数据
 */
function getPageList() {
  let data = Object.assign(toRaw(searchForm.value), toRaw(pageInfo.value))
  request.get("/admin/page", {
    params: data
  }).then(res => {
    listData.value = res.data.list
    pageInfo.value.total = res.data.total
  })
}

/**
 * 选择分页
 * @param e
 */
function currentChange(e) {
  pageInfo.value.pageNum = e
  getPageList()
}

/**
 * 改变分页数量
 * @param e
 */
function sizeChange(e) {
  pageInfo.value.pageSize = e
  getPageList()
  console.log(e)
}

/**
 * 搜索
 */
function search() {
  pageInfo.value.pageNum = 1
  getPageList()
}

/**
 * 重置搜索框
 */
function resetSearch() {
  searchFormComponents.value.resetFields();
  getPageList()
}


const dialogOpen = ref(false);
const formData = ref({});
const formRef = ref();

/**
 * 新增
 */
function add() {
  dialogOpen.value = true
  formData.value = {}
}

/**
 * 编辑
 * @param index
 * @param row
 */
function edit(index, row) {
  dialogOpen.value = true
  formData.value = Object.assign({}, row)
}

/**
 * 关闭弹框
 */
function closeDialog() {
  dialogOpen.value = false
}

/**
 * 提交数据
 */
function submit() {
  //新增
  formRef.value.validate((valid) => {
    if (!valid) {
      ElMessage({
        message: "验证失败，请检查必填项!",
        type: 'warning'
      });
      return
    }
    if (!formData.value.id) {
      request.post("/admin/add", formData.value).then(res => {
        if (!res) {
          return
        }
        dialogOpen.value = false
        ElMessage({
          message: res.msg + " 新增用户密码默认为：123456",
          type: 'success'
        });
        getPageList()
      })
    } else {
      //更新
      request.put("/admin/update", formData.value).then(res => {
        if (!res) {
          return
        }
        dialogOpen.value = false
        ElMessage({
          message: "操作成功",
          type: 'success'
        });
        getPageList()
      })
    }
  })
}


const selectionRows = ref([]);

/**
 * 多选
 * @param rows
 */
function selectionChange(rows) {
  selectionRows.value = rows
}

/**
 * 单个删除
 * @param index
 * @param row
 */
function deleteOne(index, row) {
  batchDelete([row])
}

/**
 * 批量删除
 * @param rows
 */
function batchDelete(rows) {
  if (!rows) {
    rows = selectionRows.value;
  }
  let ids = rows.map(item => item.id);
  ElMessageBox.confirm(`此操作将永久删除ID为[${ids}]的数据, 是否继续?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    center: true
  }).then(() => {
    request.delete("/admin/delBatch", {data: ids}).then(res => {
      if (!res) {
        return
      }
      ElMessage({
        message: "操作成功",
        type: 'success'
      });
      getPageList()

    })
  }).catch(() => {
    ElMessage({
      type: 'info',
      message: '已取消删除'
    });
    tableComponents.value.clearSelection();
  });
}

/**
 * 重置密码
 * @param row
 */
function resetPassword(row) {
  request.post("/common/resetPassword?type=ADMIN&id=" + row.id).then(res => {
    if (!res) {
      return
    }
    ElMessage({
      message: "操作成功",
      type: 'success'
    });
  })
}
</script>

<style scoped>
/* 主容器 */
:deep(.el-space) {
  width: 100%;
}

/* 卡片样式 */
:deep(.el-card) {
  border-radius: 12px;
  border: 1px solid rgba(129, 199, 132, 0.3);
  background: linear-gradient(135deg, #f8fff8 0%, #f0fdf0 100%);
  box-shadow: 0 4px 20px rgba(129, 199, 132, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  animation: fadeIn 0.5s ease;
}

:deep(.el-card):hover {
  box-shadow: 0 8px 30px rgba(102, 187, 106, 0.15);
  transform: translateY(-2px);
}

:deep(.el-card__header) {
  background: linear-gradient(90deg, #e8f5e9 0%, #f1f8e9 100%);
  border-bottom: 1px solid rgba(129, 199, 132, 0.2);
  padding: 16px 20px;
  font-weight: 600;
  color: #2e7d32;
}

/* 搜索表单 */
:deep(.el-form) {
  padding: 10px 0;
}

:deep(.el-form-item__label) {
  color: #388e3c;
  font-weight: 500;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper) {
  border-radius: 8px;
  border: 1px solid #c8e6c9;
  background-color: rgba(255, 255, 255, 0.9);
  transition: all 0.3s ease;
  box-shadow: 0 2px 6px rgba(129, 199, 132, 0.08);
}

:deep(.el-input__wrapper):hover,
:deep(.el-select__wrapper):hover {
  border-color: #81c784;
  box-shadow: 0 4px 12px rgba(129, 199, 132, 0.15);
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-select__wrapper.is-focus) {
  border-color: #4caf50;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.2);
}

/* 按钮样式 */
:deep(.el-button) {
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
  border-width: 1px;
  animation: fadeIn 0.5s ease;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #4caf50 0%, #66bb6a 100%);
  border-color: #4caf50;
  color: white;
}

:deep(.el-button--primary):hover {
  background: linear-gradient(135deg, #43a047 0%, #57a85a 100%);
  border-color: #43a047;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(76, 175, 80, 0.3);
}

:deep(.el-button--success) {
  background: linear-gradient(135deg, #81c784 0%, #a5d6a7 100%);
  border-color: #81c784;
  color: white;
}

:deep(.el-button--success):hover {
  background: linear-gradient(135deg, #6abf69 0%, #8bc38c 100%);
  border-color: #6abf69;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(129, 199, 132, 0.3);
}

:deep(.el-button--danger) {
  background: linear-gradient(135deg, #ef5350 0%, #ff867c 100%);
  border-color: #ef5350;
  color: white;
}

:deep(.el-button--danger):hover {
  background: linear-gradient(135deg, #d32f2f 0%, #ff6659 100%);
  border-color: #d32f2f;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(239, 83, 80, 0.3);
}

/* 表格样式 */
:deep(.el-table) {
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid rgba(129, 199, 132, 0.2);
  box-shadow: 0 4px 16px rgba(129, 199, 132, 0.08);
  animation: slideInUp 0.5s ease;
}

:deep(.el-table__header) {
  background: linear-gradient(90deg, #e8f5e9 0%, #f1f8e9 100%);
}

:deep(.el-table th) {
  background-color: transparent !important;
  color: #2e7d32;
  font-weight: 600;
  border-bottom: 2px solid #c8e6c9;
}

:deep(.el-table tr) {
  transition: all 0.3s ease;
}

:deep(.el-table tr:hover) {
  background-color: rgba(200, 230, 201, 0.3) !important;
  transform: translateX(3px);
}

:deep(.el-table td) {
  border-bottom: 1px solid rgba(200, 230, 201, 0.5);
  color: #424242;
}

:deep(.el-table .el-table__row--striped) {
  background-color: rgba(232, 245, 233, 0.3);
}

/* 标签样式 */
:deep(.el-tag) {
  border-radius: 12px;
  border-width: 0;
  font-weight: 500;
  padding: 4px 12px;
  transition: all 0.3s ease;
}

:deep(.el-tag--success) {
  background: linear-gradient(135deg, #4caf50 0%, #66bb6a 100%);
  color: white;
  box-shadow: 0 2px 6px rgba(76, 175, 80, 0.2);
}

:deep(.el-tag--danger) {
  background: linear-gradient(135deg, #ef5350 0%, #ff867c 100%);
  color: white;
  box-shadow: 0 2px 6px rgba(239, 83, 80, 0.2);
}

/* 图片预览 */
:deep(.el-image) {
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid rgba(200, 230, 201, 0.5);
  transition: all 0.3s ease;
}

:deep(.el-image:hover) {
  transform: scale(1.05);
  border-color: #4caf50;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.2);
}

/* 分页器 */
:deep(.el-pagination) {
  justify-content: center;
  margin-top: 20px;
  animation: fadeIn 0.8s ease;
}

:deep(.el-pagination.is-background .el-pager li) {
  border-radius: 6px;
  border: 1px solid #c8e6c9;
  background-color: white;
  color: #4caf50;
  transition: all 0.3s ease;
}

:deep(.el-pagination.is-background .el-pager li:hover) {
  color: #2e7d32;
  background-color: rgba(129, 199, 132, 0.1);
  transform: translateY(-2px);
}

:deep(.el-pagination.is-background .el-pager li.is-active) {
  background: linear-gradient(135deg, #4caf50 0%, #66bb6a 100%);
  border-color: #4caf50;
  color: white;
  box-shadow: 0 4px 10px rgba(76, 175, 80, 0.3);
}

/* 对话框样式 */
:deep(.el-dialog) {
  border-radius: 14px;
  border: 1px solid rgba(129, 199, 132, 0.3);
  background: linear-gradient(135deg, #f8fff8 0%, #f0fdf0 100%);
  box-shadow: 0 10px 40px rgba(129, 199, 132, 0.15);
  animation: dialogSlideIn 0.4s ease;
}

:deep(.el-dialog__header) {
  background: linear-gradient(90deg, #e8f5e9 0%, #f1f8e9 100%);
  border-bottom: 1px solid rgba(129, 199, 132, 0.2);
  padding: 16px 20px;
  margin: 0;
  border-radius: 14px 14px 0 0;
}

:deep(.el-dialog__title) {
  color: #2e7d32;
  font-weight: 600;
  font-size: 18px;
}

:deep(.el-dialog__body) {
  padding: 20px 24px;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid rgba(129, 199, 132, 0.2);
  padding: 16px 24px;
  background-color: rgba(232, 245, 233, 0.3);
  border-radius: 0 0 14px 14px;
}

/* 表单项目样式 */
:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-form-item__content) {
  margin-left: 0 !important;
}

/* 单选按钮组 */
:deep(.el-radio-group) {
  display: flex;
  gap: 20px;
}

:deep(.el-radio) {
  margin-right: 0;
}

:deep(.el-radio__inner) {
  border-color: #c8e6c9;
  background-color: white;
}

:deep(.el-radio__input.is-checked .el-radio__inner) {
  background-color: #4caf50;
  border-color: #4caf50;
}

:deep(.el-radio__label) {
  color: #424242;
  transition: color 0.3s ease;
}

:deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #4caf50;
  font-weight: 500;
}

/* 上传组件容器 */
:deep(.upload-container) {
  width: 100%;
  padding: 10px;
  border-radius: 8px;
  border: 1px dashed #c8e6c9;
  background-color: rgba(232, 245, 233, 0.2);
  transition: all 0.3s ease;
}

:deep(.upload-container:hover) {
  border-color: #81c784;
  background-color: rgba(129, 199, 132, 0.1);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes dialogSlideIn {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(-10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
  }
}

/* 滚动条美化 */
:deep(.el-table__body-wrapper::-webkit-scrollbar) {
  width: 6px;
}

:deep(.el-table__body-wrapper::-webkit-scrollbar-track) {
  background: rgba(200, 230, 201, 0.1);
  border-radius: 10px;
}

:deep(.el-table__body-wrapper::-webkit-scrollbar-thumb) {
  background: #c8e6c9;
  border-radius: 10px;
}

:deep(.el-table__body-wrapper::-webkit-scrollbar-thumb:hover) {
  background: #a5d6a7;
}

/* 响应式调整 */
@media (max-width: 768px) {
  :deep(.el-card) {
    margin: 8px;
    padding: 12px;
  }

  :deep(.el-table) {
    font-size: 14px;
  }

  :deep(.el-table__fixed-right) {
    position: static;
  }

  :deep(.el-form-item) {
    display: block;
    margin-bottom: 15px;
  }

  :deep(.el-form-item__label) {
    display: block;
    margin-bottom: 8px;
    width: 100% !important;
    text-align: left;
  }

  :deep(.el-form-item__content) {
    width: 100%;
  }
}

/* 特殊按钮效果 */
:deep(.el-button--success[icon="RefreshLeft"]) {
  position: relative;
  overflow: hidden;
}

:deep(.el-button--success[icon="RefreshLeft"]:hover::before) {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  animation: shine 1.5s infinite;
}

@keyframes shine {
  0% {
    left: -100%;
  }
  100% {
    left: 100%;
  }
}
</style>
