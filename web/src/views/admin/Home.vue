<template>
  <div class="home-container">
    <div class="background-overlay"></div>
    <div class="content-wrapper">
      <el-row :gutter="20" justify="center" align="middle">
        <el-col :span="24" class="text-center">
          <div class="main-text">
            让自己的生命成为一件不可混淆的艺术品
          </div>
          <div class="sub-text">
            Make your life an unmistakable work of art
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
// 可以在这里添加逻辑
</script>

<style scoped>
.home-container {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

.home-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('../src/assets/2.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  z-index: 1;
}

.background-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.2); /* 深色半透明遮罩，增强文字可读性 */
  z-index: 2;
}

.content-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3;
}

.text-center {
  text-align: center;
}

.main-text {
  font-size: 3rem;
  font-weight: bold;
  color: #ffffff;
  line-height: 1.3;
  margin-bottom: 1rem;
  text-shadow: 2px 2px 8px rgba(0, 0, 0, 0.5);
  letter-spacing: 2px;
  font-family: 'SimSun', 'NSimSun', 'FangSong', serif;
}

.sub-text {
  font-size: 1.5rem;
  color: rgba(255, 255, 255, 0.85);
  font-style: italic;
  text-shadow: 1px 1px 4px rgba(0, 0, 0, 0.3);
  letter-spacing: 1px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-text {
    font-size: 2rem;
    padding: 0 1rem;
  }

  .sub-text {
    font-size: 1.2rem;
    padding: 0 1rem;
  }
}

@media (max-width: 480px) {
  .main-text {
    font-size: 1.5rem;
  }

  .sub-text {
    font-size: 1rem;
  }
}
</style>