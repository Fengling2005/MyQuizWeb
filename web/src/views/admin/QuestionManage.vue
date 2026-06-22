<template>
  <div>
    <el-space direction="vertical" alignment="left" style="width: 100%">
      <el-card>
        <el-form ref="searchFormComponents" :model="searchForm" inline>
          <el-form-item label="题目" prop="content">
            <el-input v-model="searchForm.content" clearable placeholder="输入题目内容"></el-input>
          </el-form-item>
          <el-form-item label="题型" prop="questionType">
            <el-select v-model="searchForm.question_type" placeholder="请选择" clearable style="width: 150px">
              <el-option label="单选题" value="单选题"/>
              <el-option label="多选题" value="多选题"/>
              <el-option label="判断题" value="判断题"/>
            </el-select>
          </el-form-item>
          <el-form-item label="">
            <el-button type="primary" :icon="Search" @click="search">搜索</el-button>
            <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
        <el-space>
          <el-button type="primary" :icon="Plus" @click="add">新增</el-button>
          <el-button type="success" :icon="Upload" @click="handleImportClick">Excel导入题目</el-button>
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
          <el-table-column prop="questionId" label="题目ID" width="70"></el-table-column>
          <el-table-column prop="questionType" label="题型" width="70"></el-table-column>
          <el-table-column prop="content" label="题目"></el-table-column>
          <el-table-column prop="options" label="选项"></el-table-column>
          <el-table-column prop="answer" label="答案" width="90"></el-table-column>
          <el-table-column prop="explanation" label="解析" width="150"></el-table-column>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
        v-model="dialogOpen"
        v-if="dialogOpen"
        :title="formData.questionId?'编辑题目':'新增题目'"
        width="800px"
    >
      <el-form ref="formRef" :model="formData" label-width="100px" inline>
        <el-form-item label="题目" prop="content"
                      :rules="[{required:true,message:'不能为空',trigger:[ 'blur','change']}]">
          <el-input
              v-model="formData.content"
              placeholder="请输入题目内容"
              type="textarea"
              :rows="3"
              style="width: 600px;"
              resize="none"
          ></el-input>
        </el-form-item>
        <el-form-item label="题型" prop="questionType"
                      :rules="[{required:true,message:'不能为空',trigger:[ 'blur','change']}]">
          <el-radio-group v-model="formData.questionType">
            <el-radio label="单选题"></el-radio>
            <el-radio label="多选题"></el-radio>
            <el-radio label="判断题"></el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="选项" prop="options"
                      :rules="[{required:true,message:'不能为空',trigger:[ 'blur','change']}]">
          <el-input
              v-model="formData.options"
              placeholder="请输入选项内容"
              type="textarea"
              :rows="3"
              style="width: 600px;"
              resize="none"
          ></el-input>
          <div style="font-size: 12px; color: #666; margin-top: 5px;">
            提示：单选或多选题选项格式如：{"A":"选项一","B":"选项二","C":"选项三","D":"选项四"} ，判断题仅输入{}
          </div>
        </el-form-item>
        <el-form-item label="答案" prop="answer"
                      :rules="[{required:true,message:'不能为空',trigger:[ 'blur','change']}]">
          <el-input
              v-model="formData.answer"
              placeholder="请输入答案"
              style="width: 300px;"
          ></el-input>
          <div style="font-size: 12px; color: #666; margin-top: 5px;">
            提示：单选题输入如 A，多选题输入如 A,B,C，判断题输入 正确 或 错误
          </div>
        </el-form-item>
        <el-form-item label="解析" prop="explanation">
          <el-input
              v-model="formData.explanation"
              placeholder="请输入题目解析（可选）"
              type="textarea"
              :rows="4"
              style="width: 600px;"
              resize="none"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submit" :icon="Check">提交</el-button>
          <el-button @click="closeDialog" :icon="Close">取消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Excel导入对话框 -->
    <el-dialog
        v-model="importDialogVisible"
        title="Excel导入题目"
        width="600px"
        @close="handleImportClose"
    >
      <div>
        <div style="margin-bottom: 20px;">
          <el-alert
              title="导入说明"
              type="info"
              :closable="false"
              style="margin-bottom: 15px;"
          >
            <template #default>
              <div style="font-size: 12px;">
                请先下载模板，按格式填写后再上传
              </div>
            </template>
          </el-alert>

          <el-upload
              ref="uploadRef"
              class="upload-demo"
              drag
              :auto-upload="false"
              :on-change="handleFileChange"
              :show-file-list="true"
              accept=".xlsx,.xls"
              :limit="1"
              :on-exceed="handleExceed"
              :on-remove="handleFileRemove"
              name="file"
          >
            <el-icon class="el-icon--upload">
              <upload-filled />
            </el-icon>
            <div class="el-upload__text">
              拖拽文件到此处或 <em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                只能上传 Excel 文件（.xlsx, .xls），且不超过10MB
              </div>
              <div v-if="uploadResult" class="el-upload__result">
                <el-alert
                    :title="uploadResult.title"
                    :type="uploadResult.type"
                    :description="uploadResult.description"
                    show-icon
                    :closable="false"
                    style="margin-top: 10px;"
                />
                <div v-if="importData && importData.length > 0" style="margin-top: 10px;">
                  <el-descriptions :column="1" border size="small">
                    <el-descriptions-item label="预览数据（前3条）">
                      <div v-for="(item, index) in importData.slice(0, 3)" :key="index"
                           style="margin-bottom: 5px; padding: 5px; background: #f5f7fa; border-radius: 4px;">
                        {{ index + 1 }}. {{ item.content }} ({{ item.questionType }})
                      </div>
                      <div v-if="importData.length > 3" style="color: #909399; font-size: 12px;">
                        还有 {{ importData.length - 3 }} 条数据...
                      </div>
                    </el-descriptions-item>
                  </el-descriptions>
                </div>
              </div>
            </template>
          </el-upload>

          <div v-if="selectedFile" style="margin-top: 15px; text-align: center;">
            <el-button
                type="primary"
                :icon="Upload"
                @click="handleUpload"
                :loading="uploadLoading"
                :disabled="!selectedFile"
            >
              上传文件
            </el-button>
          </div>
        </div>

        <div style="margin-top: 20px;">
          <el-card>
            <template #header>
              <div style="font-weight: bold;">Excel模板说明</div>
            </template>
            <div style="font-size: 12px; color: #666;">
              <p><strong>模板已包含示例数据，请参考填写：</strong></p>
              <ul>
                <li><strong>题目</strong> - 题目内容（必填）</li>
                <li><strong>题型</strong> - 单选题/多选题/判断题（必填）</li>
                <li><strong>选项</strong> - 选项内容，格式：<strong>A.选项1,B.选项2,C.选项3</strong>（必填）</li>
                <li><strong>答案</strong> - 正确答案（必填）</li>
                <li><strong>解析</strong> - 题目解析（可选）</li>
              </ul>
              <p><strong>填写规范：</strong></p>
              <ul>
                <li>选项格式必须为：字母.内容，如：<strong>A.北京,B.上海,C.广州,D.深圳</strong></li>
                <li>多选题答案用逗号分隔，如：<strong>A,C</strong></li>
                <li>判断题答案：<strong>A</strong> 或 <strong>B</strong>（对应A.正确,B.错误）</li>
                <li>直接修改示例数据或新增行填写</li>
              </ul>
              <el-button type="primary" size="small" :icon="Download" @click="downloadTemplate"
                         style="margin-top: 10px;">
                下载模板
              </el-button>
            </div>
          </el-card>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button :icon="Close" @click="handleImportClose">关闭</el-button>
          <el-button type="primary" :icon="Check" @click="confirmImport"
                     :disabled="!importData || importData.length === 0"
                     :loading="confirmLoading">
            确认导入
          </el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import request from "@/utils/http.js";
import {Check, Close, Delete, Edit, Refresh, Plus, Search, Upload, Download} from '@element-plus/icons-vue'
import {ref, toRaw} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {UploadFilled} from '@element-plus/icons-vue'
import * as XLSX from 'xlsx';

const searchFormComponents = ref();
const tableComponents = ref();

const listData = ref([]);
const pageInfo = ref({
  pageNum: 1,
  pageSize: 10,
  total: 0
});
const searchForm = ref({});

getPageList()

/**
 * 获取分页数据
 */
function getPageList() {
  let data = Object.assign(toRaw(searchForm.value), toRaw(pageInfo.value))
  request.get("/question/page", {
    params: data
  }).then(res => {
    listData.value = res.data.list
    pageInfo.value.total = res.data.total
  })
}

/**
 * 选择分页
 */
function currentChange(e) {
  pageInfo.value.pageNum = e
  getPageList()
}

/**
 * 改变分页数量
 */
function sizeChange(e) {
  pageInfo.value.pageSize = e
  getPageList()
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
  formRef.value.validate((valid) => {
    if (!valid) {
      ElMessage({
        message: "验证失败，请检查必填项!",
        type: 'warning'
      });
      return
    }
    if (!formData.value.questionId) {
      request.post("/question/add", formData.value).then(res => {
        if (!res) {
          return
        }
        dialogOpen.value = false
        ElMessage({
          message: res.msg + "",
          type: 'success'
        });
        getPageList()
      })
    } else {
      request.put("/question/update", formData.value).then(res => {
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
 */
function selectionChange(rows) {
  selectionRows.value = rows
}

/**
 * 单个删除
 */
function deleteOne(index, row) {
  batchDelete([row])
}

/**
 * 批量删除
 */
function batchDelete(rows) {
  if (!rows) {
    rows = selectionRows.value;
  }
  let ids = rows.map(item => item.questionId);
  ElMessageBox.confirm(`此操作将永久删除ID为[${ids}]的数据, 是否继续?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    center: true
  }).then(() => {
    request.delete("/question/delBatch", {data: ids}).then(res => {
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
 * Excel导入功能
 */
const importDialogVisible = ref(false);
const importData = ref(null);
const uploadRef = ref();
const confirmLoading = ref(false);
const selectedFile = ref(null);
const uploadLoading = ref(false);
const uploadResult = ref(null);

// 点击导入按钮
function handleImportClick() {
  importDialogVisible.value = true;
  importData.value = null;
  selectedFile.value = null;
  uploadResult.value = null;
  if (uploadRef.value) {
    uploadRef.value.clearFiles();
  }
}

// 关闭导入对话框
function handleImportClose() {
  importDialogVisible.value = false;
  importData.value = null;
  selectedFile.value = null;
  uploadResult.value = null;
  confirmLoading.value = false;
  uploadLoading.value = false;
  if (uploadRef.value) {
    uploadRef.value.clearFiles();
  }
}

// 下载模板
function downloadTemplate() {
  try {
    // 创建数据
    const data = [
      ['题目', '题型', '选项', '答案', '解析'],
      ['示例：中国的首都是哪里？', '单选题', 'A.北京,B.上海,C.广州,D.深圳', 'A', '北京是中国的首都'],
      ['示例：以下哪些是水果？', '多选题', 'A.苹果,B.土豆,C.香蕉,D.黄瓜', 'A,C', '土豆和黄瓜是蔬菜'],
      ['示例：太阳从东边升起', '判断题', '正确,错误', '正确', '这是自然现象'],
      ['', '', '', '', ''],
      ['★ 填写说明 ★', '', '', '', ''],
      ['1. 题型：单选题、多选题、判断题', '', '', '', ''],
      ['2. 选项：用逗号分隔，例：A.北京,B.上海', '', '', '', ''],
      ['3. 多选题答案：用逗号分隔，例：A,C', '', '', '', ''],
      ['4. 判断题答案：正确 或 错误', '', '', '', '']
    ];

    // 创建工作簿和工作表
    const ws = XLSX.utils.aoa_to_sheet(data);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "模板");

    // 设置列宽
    ws['!cols'] = [
      { wch: 35 }, { wch: 10 },
      { wch: 45 }, { wch: 10 },
      { wch: 35 }
    ];

    // 生成并下载
    XLSX.writeFile(wb, '题目导入模板.xlsx');

    ElMessage.success('模板下载成功');

  } catch (error) {
    console.error('下载失败:', error);
    ElMessage.error('下载失败: ' + error.message);
  }
}

// 文件选择变化
function handleFileChange(file, fileList) {
  if (fileList.length > 0) {
    selectedFile.value = file.raw;

    // 校验文件
    const isExcel = selectedFile.value.type === 'application/vnd.ms-excel' ||
        selectedFile.value.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
        selectedFile.value.name.endsWith('.xlsx') ||
        selectedFile.value.name.endsWith('.xls');

    const isLt10M = selectedFile.value.size / 1024 / 1024 < 10;

    if (!isExcel) {
      ElMessage.error('只能上传Excel文件(.xlsx或.xls格式)!');
      selectedFile.value = null;
      uploadRef.value.clearFiles();
      return;
    }

    if (!isLt10M) {
      ElMessage.error('文件大小不能超过10MB!');
      selectedFile.value = null;
      uploadRef.value.clearFiles();
      return;
    }
  } else {
    selectedFile.value = null;
  }
}

// 文件超出限制
function handleExceed(files, fileList) {
  ElMessage.warning(`最多只能上传 1 个文件，已选择 ${files.length + fileList.length} 个文件`);
}

// 文件移除
function handleFileRemove(file, fileList) {
  selectedFile.value = null;
  importData.value = null;
  uploadResult.value = null;
}

// 上传文件到服务器
function handleUpload() {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件');
    return;
  }

  uploadLoading.value = true;
  uploadResult.value = null;
  importData.value = null;

  const formData = new FormData();
  formData.append('file', selectedFile.value);

  // 使用axios上传文件
  request.post('/question/importExcel', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then(res => {
    uploadLoading.value = false;

    if (res.code === 200) {
      uploadResult.value = {
        title: '上传成功',
        type: 'success',
        description: res.msg || `共解析到 ${res.data ? res.data.length : 0} 条题目数据`
      };
      importData.value = res.data;

      ElMessage.success('文件上传成功，请确认导入');
    } else {
      uploadResult.value = {
        title: '上传失败',
        type: 'error',
        description: res.msg || '文件上传失败'
      };
      ElMessage.error(res.msg || '文件上传失败');
    }
  }).catch(error => {
    uploadLoading.value = false;
    uploadResult.value = {
      title: '上传失败',
      type: 'error',
      description: error.response?.data?.msg || error.message || '文件上传失败'
    };
    console.error('上传失败:', error);
    ElMessage.error('文件上传失败: ' + (error.response?.data?.msg || error.message));
  });
}

// 确认导入（保存到数据库）
function confirmImport() {
  if (!importData.value || importData.value.length === 0) {
    ElMessage({
      message: '请先上传Excel文件',
      type: 'warning'
    });
    return;
  }

  confirmLoading.value = true;

  request.post('/question/confirmImport', importData.value).then(res => {
    confirmLoading.value = false;

    if (res.code === 200) {
      // 显示详细的导入结果
      const resultHtml = res.msg.replace(/\n/g, '<br>');
      ElMessageBox.alert(resultHtml, '导入完成', {
        confirmButtonText: '确定',
        type: 'success',
        showClose: false,
        dangerouslyUseHTMLString: true
      }).then(() => {
        // 关闭对话框并刷新列表
        handleImportClose();
        getPageList();
      });
    } else {
      ElMessage({
        message: res.msg || '导入失败',
        type: 'error'
      });
    }
  }).catch(error => {
    confirmLoading.value = false;
    console.error('导入失败:', error);
    ElMessage({
      message: '导入失败：' + (error.response?.data?.msg || error.message),
      type: 'error'
    });
  });
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

/* 上传组件 */
.upload-demo {
  width: 100%;
}

:deep(.el-upload-dragger) {
  width: 100%;
  min-height: 180px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  border: 2px dashed #c8e6c9;
  border-radius: 12px;
  background-color: rgba(232, 245, 233, 0.3);
  transition: all 0.3s ease;
}

:deep(.el-upload-dragger:hover) {
  border-color: #81c784;
  background-color: rgba(129, 199, 132, 0.1);
  transform: translateY(-2px);
}

:deep(.el-icon--upload) {
  color: #4caf50;
  font-size: 48px;
  margin-bottom: 10px;
}

.el-upload__text {
  color: #2e7d32;
  font-size: 14px;
}

.el-upload__text em {
  color: #4caf50;
  font-style: normal;
  font-weight: 500;
}

.el-upload__tip {
  margin-top: 10px;
  color: #66bb6a;
  font-size: 12px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.el-upload__result {
  margin-top: 15px;
  animation: fadeIn 0.5s ease;
}

:deep(.el-upload-list) {
  margin-top: 10px;
}

:deep(.el-alert) {
  margin-bottom: 10px;
  border-radius: 8px;
  border: 1px solid rgba(129, 199, 132, 0.2);
  animation: fadeIn 0.5s ease;
}

:deep(.el-alert--info) {
  background-color: rgba(129, 199, 132, 0.1);
  color: #2e7d32;
}

:deep(.el-descriptions) {
  margin-top: 10px;
  border-radius: 8px;
  overflow: hidden;
  animation: fadeIn 0.6s ease;
}

:deep(.el-descriptions__body) {
  background-color: rgba(232, 245, 233, 0.2);
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
}
</style>