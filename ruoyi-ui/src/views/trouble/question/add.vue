<template>
  <div class="app-container">
    <transition name="fade-slide">
      <el-card class="box-card">
        <div slot="header" class="clearfix header-row">
          <span class="card-title">添加错题</span>
          <div class="header-buttons">
            <el-button
              style="padding: 3px 0"
              type="text"
              @click="goToDashboard"
            >
              <i class="el-icon-house"></i> 返回主页
            </el-button>
          </div>
        </div>

        <el-form
          ref="form"
          :model="form"
          :rules="rules"
          :label-width="labelWidth"
          class="form-layout"
        >
          <!-- 题目内容 -->
          <el-row :gutter="20">
            <el-col :xs="24" :sm="24" :md="24">
              <el-form-item label="题目内容" prop="questionContent">
                <el-input
                  v-model="form.questionContent"
                  type="textarea"
                  :rows="6"
                  placeholder="请输入题目内容，支持拍照识别"
                  show-word-limit
                  maxlength="2000"
                />
                <!-- OCR 按钮 -->
                <el-button
                  size="mini"
                  type="primary"
                  @click="handleOCR('question')"
                  style="margin-top: 6px;"
                >
                  OCR
                </el-button>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 图片上传 -->
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="12">
              <el-form-item label="题目图片">
                <image-upload
                  v-model="form.questionImages"
                  :limit="1"
                  class="image-upload-full"
                />
                <div class="upload-tip">支持拍照上传，最多1张图片</div>
              </el-form-item>
            </el-col>

          </el-row>

          <!-- 答案内容 -->
          <el-row :gutter="20">
            <el-col :xs="24" :sm="24" :md="24">
              <el-form-item label="答案内容">
                <el-input
                  v-model="form.answerContent"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入答案内容或解析"
                  show-word-limit
                  maxlength="2000"
                />
                <!-- OCR 按钮 -->
                <el-button
                  size="mini"
                  type="primary"
                  @click="handleOCR('answer')"
                  style="margin-top: 6px;"
                >
                  OCR
                </el-button>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :xs="24" :sm="12" :md="12">
              <el-form-item label="答案图片">
                <image-upload
                  v-model="form.answerImages"
                  :limit="1"
                  class="image-upload-full"
                />
                <div class="upload-tip">支持拍照上传，最多1张图片</div>
              </el-form-item>
            </el-col>
          </el-row>
          <!-- 类型与重要性 -->
          <el-row :gutter="20" class="row-inline">
            <el-col :xs="24" :sm="12" :md="12">
              <el-form-item label="题目类型" prop="questionType">
                <el-select
                  v-model="form.questionType"
                  placeholder="请选择题目类型"
                  style="width: 100%"
                >
                  <el-option label="未区分" value="未区分" />
                  <el-option label="选择题" value="选择题" />
                  <el-option label="填空题" value="填空题" />
                  <el-option label="解答题" value="解答题" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :xs="24" :sm="12" :md="12">
              <el-form-item label="重要性" prop="importance">
                <el-radio-group v-model="form.importance">
                  <el-radio :label="3">高</el-radio>
                  <el-radio :label="2">中</el-radio>
                  <el-radio :label="1">低</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 标签 -->
          <el-row :gutter="20" class="row-inline">
            <el-col :xs="24" :sm="24" :md="24">
              <el-form-item label="标签">
                <el-select
                  v-model="selectedTags"
                  multiple
                  filterable
                  allow-create
                  default-first-option
                  placeholder="请选择或输入标签"
                  style="width: 100%"
                  @change="handleTagsChange"
                >
                  <el-option
                    v-for="tag in defaultTags"
                    :key="tag"
                    :label="tag"
                    :value="tag"
                  />
                </el-select>
                <div class="tag-tip">可以选择默认标签或自定义标签</div>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 备注 -->
          <el-row :gutter="20">
            <el-col :xs="24" :sm="24" :md="24">
              <el-form-item label="备注">
                <el-input
                  v-model="form.remark"
                  type="textarea"
                  :rows="2"
                  placeholder="请输入备注信息（可选）"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 收藏选项 -->
          <el-row :gutter="20">
            <el-col :xs="24" :sm="24" :md="24">
              <el-form-item label="收藏">
                <el-checkbox v-model="shouldFavorite" class="favorite-checkbox">
                  <i class="el-icon-star-on"></i> 同时收藏此错题
                </el-checkbox>
                <div class="favorite-tip">勾选后，此错题将自动添加到我的收藏</div>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 操作按钮 -->
          <el-row class="action-row" :gutter="12">
            <el-col :xs="24" :sm="24" :md="24" class="action-col">
              <transition name="button-press">
                <el-button
                  type="primary"
                  @click="submitForm"
                  :loading="submitLoading"
                  :style="buttonStyle"
                >
                  保存错题
                </el-button>
              </transition>
              <el-button @click="resetForm" :style="buttonStyle">
                重置
              </el-button>
              <el-button @click="goBack" :style="buttonStyle">
                取消
              </el-button>
            </el-col>
          </el-row>
        </el-form>
      </el-card>
    </transition>
  </div>
</template>

<script>
import { addQuestion, favoriteQuestion } from "@/api/trouble/question";
export default {
  name: "QuestionAdd",
  data() {
    return {
      shouldFavorite: false, // 是否收藏错题
      form: {
        questionContent: "",
        questionImages: "",
        answerContent: "",
        answerImages: "",
        questionType: "未区分",
        tags: "",
        importance: 2,
        remark: "",
      },
      rules: {
        questionContent: [
          { required: true, message: "题目内容不能为空", trigger: "blur" },
        ],
      },
      submitLoading: false,
      isMobile: false,
      selectedTags: [],
      defaultTags: ["语文", "数学", "英语", "物理", "化学", "生物", "政治", "历史", "地理"],
      formChanged: false, // 表单是否有修改
    };
  },
  computed: {
    labelWidth() {
      return this.isMobile ? "90px" : "100px";
    },
    buttonStyle() {
      return this.isMobile
        ? { width: "100%", marginBottom: "10px", transition: "all 0.3s" }
        : { marginLeft: "8px", transition: "all 0.3s" };
    },
  },
  created() {
    this.checkIsMobile();
    window.addEventListener("resize", this.checkIsMobile);

    // 监听表单变化
    this.$watch('form', () => {
      this.formChanged = true;
    }, { deep: true });
  },
  beforeDestroy() {
    window.removeEventListener("resize", this.checkIsMobile);
  },
  beforeRouteLeave(to, from, next) {
    // 如果表单有修改且未提交，提示用户
    if (this.formChanged) {
      this.$confirm('表单内容尚未保存，确定要离开吗？', '提示', {
        confirmButtonText: '确定离开',
        cancelButtonText: '留下',
        type: 'warning'
      }).then(() => {
        next();
      }).catch(() => {
        next(false);
      });
    } else {
      next();
    }
  },
  methods: {
    checkIsMobile() {
      this.isMobile = window.matchMedia("(max-width: 767px)").matches;
    },
    async handleOCR(target) {
      // target = 'question' 或 'answer'
      const fileInput = document.createElement('input');
      fileInput.type = 'file';
      fileInput.accept = 'image/*';
      fileInput.onchange = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        const formData = new FormData();
        formData.append('file', file);

        try {
          // 动态获取当前页面 host
          const host = window.location.hostname; // 手机访问时就是电脑局域网 IP
          const port = 9000; // OCR 服务端口

          // 上传到 OCR 后端
          const res = await fetch(`http://${host}:${port}/ocr/upload`, {
            method: 'POST',
            body: formData,
          });

          if (!res.ok) {
            throw new Error(`HTTP错误 ${res.status}`);
          }

          const data = await res.json();
          if (!data.text) {
            throw new Error('OCR返回内容为空');
          }

          // 填入输入框
          if (target === 'question') {
            this.form.questionContent = data.text;
          } else if (target === 'answer') {
            this.form.answerContent = data.text;
          }

          this.$message.success('OCR识别成功');
        } catch (err) {
          this.$message.error('OCR识别失败');
          if (process.env.NODE_ENV === 'development') {
            console.error('OCR识别错误:', err);
          }
        }
      };

      fileInput.click();
    },
    submitForm() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          this.submitLoading = true;
          addQuestion(this.form)
            .then((response) => {
              // 如果勾选了收藏，则调用收藏接口
              if (this.shouldFavorite && response.data) {
                const questionId = response.data.questionId || response.data;
                favoriteQuestion(questionId).then(() => {
                  this.submitLoading = false;
                  this.$modal.msgSuccess("错题添加并收藏成功");
                  this.handleSuccessCallback();
                }).catch(() => {
                  this.submitLoading = false;
                  this.$modal.msgSuccess("错题添加成功，但收藏失败");
                  this.handleSuccessCallback();
                });
              } else {
                this.submitLoading = false;
                this.$modal.msgSuccess("错题添加成功");
                this.handleSuccessCallback();
              }
            })
            .catch(() => {
              this.submitLoading = false;
            });
        }
      });
    },

    // 处理成功回调
    handleSuccessCallback() {
      // 清除表单修改标记（已成功保存）
      this.formChanged = false;

      // 成功后询问用户是否继续添加
      this.$confirm('是否继续添加错题？', '提示', {
        confirmButtonText: '继续添加',
        cancelButtonText: '返回列表',
        type: 'success'
      }).then(() => {
        // 继续添加：重置表单
        this.resetForm();
      }).catch(() => {
        // 返回列表
        this.goBack();
      });
    },
    resetForm() {
      this.form = {
        questionContent: "",
        questionImages: "",
        answerContent: "",
        answerImages: "",
        questionType: "未区分",
        tags: "",
        importance: 2,
        remark: "",
      };
      this.selectedTags = [];
      this.shouldFavorite = false; // 重置收藏选项
      this.formChanged = false; // 重置表单修改标记
      this.$nextTick(() => {
        if (this.$refs.form) this.$refs.form.resetFields();
      });
    },
    goBack() {
      // 返回系统首页
      this.$router.push("/");
    },
    goToDashboard() {
      // 返回系统首页
      this.$router.push("/");
    },
    handleTagsChange(value) {
      if (Array.isArray(value)) {
        const maxTags = 8;
        if (value.length > maxTags) {
          value = value.slice(0, maxTags);
          this.selectedTags = value;
          this.$message.info(`标签数量已限制为 ${maxTags} 个`);
        }
        this.form.tags = value.join(",");
      }
    },
  },
};
</script>

<style scoped>
/* 蓝色系美化 - 添加错题页面 */
::v-deep .app-container {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 50%, #90caf9 100%);
  min-height: calc(100vh - 50px);
  padding: 20px;
  position: relative;
  overflow: hidden;
}

/* 背景装饰元素 */
::v-deep .app-container::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(33, 150, 243, 0.1) 0%, transparent 70%);
  animation: rotate 30s linear infinite;
  z-index: 0;
}

::v-deep .app-container > * {
  position: relative;
  z-index: 1;
}

@keyframes rotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.box-card {
  margin: 20px auto;
  max-width: 1200px;
  box-sizing: border-box;
  border-radius: 16px;
  border: none;
  box-shadow: 0 8px 32px rgba(42, 82, 152, 0.15);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  animation: slideIn 0.6s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.box-card:hover {
  box-shadow: 0 12px 48px rgba(42, 82, 152, 0.2);
  transform: translateY(-2px);
}

/* 头部样式 */
::v-deep .box-card .el-card__header {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  border-bottom: none;
  padding: 20px 24px;
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.card-title {
  font-size: 20px;
  font-weight: 600;
  color: #ffffff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-buttons .el-button {
  color: rgba(255, 255, 255, 0.95);
  font-weight: 500;
  transition: all 0.3s;
}

.header-buttons .el-button:hover {
  color: #ffffff;
  transform: scale(1.05);
}

/* 表单布局 */
::v-deep .box-card .el-card__body {
  padding: 32px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fbff 100%);
}

.form-layout {
  max-width: 100%;
}

.form-layout .el-form-item {
  margin-bottom: 24px;
  transition: all 0.3s ease;
}

::v-deep .el-form-item__label {
  color: #2c3e50;
  font-weight: 500;
  font-size: 14px;
}

/* 输入框美化 */
::v-deep .el-input__inner,
::v-deep .el-textarea__inner {
  border-radius: 8px;
  border: 1px solid #d4e8f7;
  transition: all 0.3s;
  background: #ffffff;
}

::v-deep .el-input__inner:focus,
::v-deep .el-textarea__inner:focus {
  border-color: #2a5298;
  box-shadow: 0 0 0 2px rgba(42, 82, 152, 0.1);
}

::v-deep .el-input__inner:hover,
::v-deep .el-textarea__inner:hover {
  border-color: #4a9ff5;
}

/* 选择框美化 */
::v-deep .el-select {
  width: 100%;
}

::v-deep .el-select .el-input__inner {
  background: #ffffff;
}

/* 标签选择美化 */
::v-deep .el-tag {
  border-radius: 6px;
  border: none;
  background: linear-gradient(135deg, #e8f4f8 0%, #d4e8f7 100%);
  color: #2a5298;
  font-weight: 500;
  transition: all 0.3s;
}

::v-deep .el-tag:hover {
  background: linear-gradient(135deg, #d4e8f7 0%, #c5dff5 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(42, 82, 152, 0.15);
}

::v-deep .el-tag .el-tag__close {
  color: #2a5298;
}

::v-deep .el-tag .el-tag__close:hover {
  background-color: #2a5298;
  color: #ffffff;
}

/* 上传组件美化 */
.image-upload-full {
  display: block;
  width: 100%;
}

::v-deep .el-upload {
  border-radius: 8px;
  border: 2px dashed #d4e8f7;
  transition: all 0.3s;
}

::v-deep .el-upload:hover {
  border-color: #2a5298;
  background: #f8fbff;
}

.upload-tip,
.tag-tip,
.favorite-tip {
  font-size: 13px;
  color: #7a8a9a;
  margin-top: 8px;
  transition: color 0.3s;
  line-height: 1.6;
}

.upload-tip:hover,
.tag-tip:hover,
.favorite-tip:hover {
  color: #2a5298;
}

/* 收藏复选框美化 */
.favorite-checkbox {
  font-size: 15px;
  font-weight: 500;
  color: #2c3e50;
}

.favorite-checkbox i {
  color: #f39c12;
  margin-right: 4px;
  font-size: 16px;
}

::v-deep .favorite-checkbox .el-checkbox__input.is-checked .el-checkbox__inner {
  background-color: #2a5298;
  border-color: #2a5298;
}

::v-deep .favorite-checkbox .el-checkbox__input.is-checked + .el-checkbox__label {
  color: #2a5298;
}

::v-deep .favorite-checkbox:hover .el-checkbox__inner {
  border-color: #2a5298;
}

/* 按钮美化 */
.action-row {
  margin-top: 32px;
  text-align: center;
  padding-top: 24px;
  border-top: 2px solid #e8f1f8;
}

.action-col {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

::v-deep .el-button {
  border-radius: 8px;
  font-weight: 500;
  padding: 12px 32px;
  transition: all 0.3s;
  min-width: 120px;
}

::v-deep .el-button--primary {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  border: none;
}

::v-deep .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(42, 82, 152, 0.3);
}

::v-deep .el-button--default {
  background: #ffffff;
  border: 1px solid #d4e8f7;
  color: #5a6c7d;
}

::v-deep .el-button--default:hover {
  background: #f8fbff;
  border-color: #2a5298;
  color: #2a5298;
  transform: translateY(-2px);
}

/* 动画与响应式 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.4s ease;
}

.fade-slide-enter,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.button-press-enter-active {
  transition: transform 0.15s;
}

.button-press-enter {
  transform: scale(0.98);
}

/* 小屏优化 */
@media (max-width: 767px) {
  ::v-deep .app-container {
    padding: 12px;
  }

  .box-card {
    margin: 10px auto;
    border-radius: 12px;
  }

  ::v-deep .box-card .el-card__body {
    padding: 20px;
  }

  .card-title {
    font-size: 18px;
  }

  .el-form-item__label {
    font-size: 13px;
  }

  ::v-deep .el-button {
    width: 100%;
    margin-bottom: 8px;
    min-width: auto;
  }

  .action-col {
    flex-direction: column;
    width: 100%;
  }

  .upload-tip,
  .tag-tip {
    margin-top: 8px;
    font-size: 12px;
  }
}

/* 桌面端 */
@media (min-width: 768px) {
  ::v-deep .el-button + .el-button {
    margin-left: 0;
  }
}
</style>
