<template>
  <div
    class="question-detail-overlay"
    @click.self="handleClose"
    style="z-index: 2000"
  >
    <div class="question-detail-dialog">
      <!-- 头部 -->
      <div class="detail-header">
        <div class="header-left">
          <div class="header-info">
            <el-tag
              :type="getTypeTagType(question.questionType)"
              size="medium"
              class="header-tag"
            >
              {{ question.questionType || "未区分" }}
            </el-tag>
            <span class="header-time">{{
              formatTime(question.createTime)
            }}</span>
          </div>
          <h2 class="header-title">题目详情</h2>
        </div>
        <div class="header-actions">
          <el-button
            type="success"
            size="small"
            icon="el-icon-chat-dot-round"
            @click="getAIAnswer"
            :loading="aiLoading"
            :disabled="aiLoading"
            class="ai-btn"
          >
            <span class="btn-text">AI答疑</span>
          </el-button>
          <el-button
            type="primary"
            size="small"
            icon="el-icon-edit"
            @click="handleEdit"
            class="edit-btn"
          >
            <span class="btn-text">编辑</span>
          </el-button>
          <el-button
            icon="el-icon-close"
            circle
            class="close-btn"
            @click="handleClose"
          />
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="detail-content-wrapper">
        <div class="detail-content-with-ai">
          <!-- 左侧：原始错题详情 -->
          <div class="original-content">
            <!-- 题目内容 -->
            <div class="detail-section">
              <div class="section-header">
                <div class="section-indicator blue"></div>
                <h3 class="section-title">题目内容</h3>
              </div>
              <div
                class="section-content question-content rendered-content"
                v-html="renderedQuestionContent"
              ></div>
            </div>

            <!-- 题目图片 -->
            <div v-if="hasImages" class="detail-section">
              <div class="section-header">
                <div class="section-indicator purple"></div>
                <h3 class="section-title">题目图片</h3>
              </div>
              <div class="image-viewer">
                <el-carousel
                  :interval="0"
                  :arrow="imageArray.length > 1 ? 'hover' : 'never'"
                  height="400px"
                  indicator-position="outside"
                >
                  <el-carousel-item v-for="(img, idx) in imageArray" :key="idx">
                    <el-image
                      :src="getImageUrl(img)"
                      fit="contain"
                      class="detail-image"
                      :preview-src-list="previewList"
                      :initial-index="idx"
                    />
                  </el-carousel-item>
                </el-carousel>
              </div>
            </div>

            <!-- 答案解析 -->
            <div class="detail-section">
              <div class="section-header">
                <div class="section-indicator green"></div>
                <h3 class="section-title">答案解析</h3>
              </div>
              <div
                class="section-content answer-content rendered-content"
                v-html="renderedAnswerContent"
              ></div>
            </div>

            <!-- 答案图片 -->
            <div v-if="hasAnswerImages" class="detail-section">
              <div class="section-header">
                <div class="section-indicator orange"></div>
                <h3 class="section-title">答案图片</h3>
              </div>
              <div class="image-viewer">
                <el-carousel
                  :interval="0"
                  :arrow="answerImageArray.length > 1 ? 'hover' : 'never'"
                  height="400px"
                  indicator-position="outside"
                >
                  <el-carousel-item
                    v-for="(img, idx) in answerImageArray"
                    :key="idx"
                  >
                    <el-image
                      :src="getImageUrl(img)"
                      fit="contain"
                      class="detail-image"
                      :preview-src-list="answerPreviewList"
                      :initial-index="idx"
                    />
                  </el-carousel-item>
                </el-carousel>
              </div>
            </div>

            <!-- 知识点标签 -->
            <div v-if="question.tags" class="detail-section">
              <div class="section-header">
                <div class="section-indicator orange"></div>
                <h3 class="section-title">知识点标签</h3>
              </div>
              <div class="tags-container">
                <el-tag
                  v-for="(tag, idx) in tagArray"
                  :key="idx"
                  type="warning"
                  size="medium"
                  class="detail-tag"
                >
                  <i class="el-icon-collection-tag"></i>
                  {{ tag }}
                </el-tag>
              </div>
            </div>

            <!-- 重要性和熟练度 -->
            <div class="detail-section">
              <div class="section-header">
                <div class="section-indicator red"></div>
                <h3 class="section-title">标签信息</h3>
              </div>
              <div class="tags-container">
                <el-tag
                  :type="getImportanceTagType(question.importance)"
                  size="medium"
                  class="detail-tag"
                >
                  <i class="el-icon-star-on"></i>
                  重要性：{{ getImportanceText(question.importance) }}
                </el-tag>
                <el-tag
                  :type="getProficiencyTagType(question.proficiency)"
                  size="medium"
                  class="detail-tag"
                >
                  <i class="el-icon-medal"></i>
                  熟练度：{{ getProficiencyText(question.proficiency) }}
                </el-tag>
                <el-tag
                  v-if="question.difficulty"
                  :type="getDifficultyTagType(question.difficulty)"
                  size="medium"
                  class="detail-tag"
                >
                  <i class="el-icon-data-line"></i>
                  难度：{{ getDifficultyText(question.difficulty) }}
                </el-tag>
              </div>
            </div>

            <!-- 错题来源、年级、错误类型 -->
            <div class="detail-section">
              <div class="section-header">
                <div class="section-indicator cyan"></div>
                <h3 class="section-title">其他信息</h3>
              </div>
              <div class="tags-container">
                <el-tag type="info" size="medium" class="detail-tag">
                  <i class="el-icon-document"></i>
                  来源：{{ question.questionSource || "未设置" }}
                </el-tag>
                <el-tag type="info" size="medium" class="detail-tag">
                  <i class="el-icon-school"></i>
                  年级：{{ question.grade || "未设置" }}
                </el-tag>
                <el-tag type="warning" size="medium" class="detail-tag">
                  <i class="el-icon-warning-outline"></i>
                  错误类型：{{ question.errorType || "未设置" }}
                </el-tag>
              </div>
            </div>
          </div>

          <!-- 右侧：AI答疑区域（桌面端） -->
          <div class="ai-answer-section desktop-ai">
            <div class="ai-header">
              <div class="section-header">
                <div class="section-indicator gradient"></div>
                <h3 class="section-title">
                  <i class="el-icon-chat-dot-round"></i>
                  AI智能答疑
                </h3>
              </div>
              <el-button
                type="text"
                size="small"
                @click="refreshAIAnswer"
                :loading="aiLoading"
                :disabled="aiLoading"
                class="refresh-btn"
              >
                <i class="el-icon-refresh"></i>
                重新生成
              </el-button>
            </div>

            <!-- AI回答区域 -->
            <div class="ai-content">
              <!-- 加载状态 -->
              <div v-if="aiLoading" class="ai-loading">
                <el-skeleton :rows="5" animated />
                <div class="loading-text">
                  <i class="el-icon-loading"></i>
                  AI正在思考中...
                </div>
              </div>

              <!-- 错误状态 -->
              <div v-else-if="aiError" class="ai-error">
                <el-alert
                  :title="aiError"
                  type="error"
                  :closable="true"
                  @close="clearAIError"
                />
                <el-button type="primary" @click="getAIAnswer" size="small">
                  <i class="el-icon-refresh-right"></i>
                  重试
                </el-button>
              </div>

              <!-- AI回答内容 -->
              <div v-else-if="aiAnswer" class="ai-answer">
                <div
                  class="ai-message rendered-content"
                  v-html="renderedAIAnswer"
                ></div>

                <!-- 对话历史 -->
                <div
                  v-if="conversationHistory.length > 1"
                  class="conversation-history"
                >
                  <div class="history-header">
                    <h4><i class="el-icon-chat-line-round"></i> 对话历史</h4>
                  </div>
                  <div class="history-items">
                    <div
                      v-for="(msg, index) in conversationHistory.slice(1)"
                      :key="index"
                      :class="['history-item', msg.role]"
                    >
                      <div class="history-role">
                        <i
                          :class="
                            msg.role === 'user' ? 'el-icon-user' : 'el-icon-cpu'
                          "
                        ></i>
                        {{ msg.role === "user" ? "我" : "AI" }}
                      </div>
                      <div
                        class="history-content rendered-content"
                        v-html="formatAIAnswer(msg.content)"
                      ></div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 初始状态 -->
              <div v-else class="ai-empty">
                <div class="empty-icon">
                  <i class="el-icon-chat-line-square"></i>
                </div>
                <p class="empty-text">点击"AI答疑"按钮获取智能解答</p>
                <el-button
                  type="primary"
                  @click="getAIAnswer"
                  :loading="aiLoading"
                  size="medium"
                >
                  <i class="el-icon-magic-stick"></i>
                  开始AI答疑
                </el-button>
              </div>
            </div>

            <!-- 继续对话输入框 -->
            <div
              v-if="aiAnswer || conversationHistory.length > 0"
              class="ai-input-container"
            >
              <div class="input-wrapper">
                <el-input
                  v-model="userMessage"
                  type="textarea"
                  :rows="3"
                  placeholder="继续向AI提问...（按Enter发送，Shift+Enter换行）"
                  resize="none"
                  @keydown.native="handleKeydown"
                  class="ai-textarea"
                />
                <el-button
                  type="primary"
                  @click="sendUserMessage"
                  :loading="aiLoading"
                  :disabled="!userMessage.trim()"
                  class="send-button"
                  icon="el-icon-s-promotion"
                >
                  发送
                </el-button>
              </div>
              <div class="input-tips">
                <i class="el-icon-info"></i>
                <small>按 Enter 发送，Shift+Enter 换行</small>
              </div>
            </div>
          </div>
        </div>

        <!-- 移动端AI答疑区域 -->
        <div class="mobile-ai-section" v-if="showMobileAI">
          <div class="mobile-ai-header">
            <div class="section-header">
              <div class="section-indicator gradient"></div>
              <h3 class="section-title">
                <i class="el-icon-chat-dot-round"></i>
                AI智能答疑
              </h3>
            </div>
            <div class="mobile-ai-actions">
              <el-button
                type="text"
                size="small"
                @click="refreshAIAnswer"
                :loading="aiLoading"
                :disabled="aiLoading"
              >
                <i class="el-icon-refresh"></i>
              </el-button>
              <el-button
                type="text"
                size="small"
                @click="closeMobileAI"
                class="close-mobile-ai"
              >
                <i class="el-icon-close"></i>
              </el-button>
            </div>
          </div>

          <!-- AI回答内容（移动端） -->
          <div class="ai-content mobile">
            <div v-if="aiLoading" class="ai-loading">
              <el-skeleton :rows="3" animated />
              <div class="loading-text">
                <i class="el-icon-loading"></i>
                思考中...
              </div>
            </div>

            <div v-else-if="aiError" class="ai-error">
              <el-alert
                :title="aiError"
                type="error"
                :closable="true"
                @close="clearAIError"
                size="small"
              />
              <el-button type="primary" @click="getAIAnswer" size="small">
                重试
              </el-button>
            </div>

            <div v-else-if="aiAnswer" class="ai-answer">
              <div
                class="ai-message rendered-content"
                v-html="renderedAIAnswer"
              ></div>

              <!-- 对话历史（移动端） -->
              <div
                v-if="conversationHistory.length > 1"
                class="conversation-history"
              >
                <div class="history-header">
                  <h4><i class="el-icon-chat-line-round"></i> 对话记录</h4>
                </div>
                <div class="history-items">
                  <div
                    v-for="(msg, index) in conversationHistory.slice(1)"
                    :key="index"
                    :class="['history-item', msg.role]"
                  >
                    <div class="history-role">
                      <i
                        :class="
                          msg.role === 'user' ? 'el-icon-user' : 'el-icon-cpu'
                        "
                      ></i>
                      {{ msg.role === "user" ? "我" : "AI" }}
                    </div>
                    <div
                      class="history-content rendered-content"
                      v-html="formatAIAnswer(msg.content)"
                    ></div>
                  </div>
                </div>
              </div>
            </div>

            <div v-else class="ai-empty">
              <p class="empty-text">获取AI解答</p>
              <el-button
                type="primary"
                @click="getAIAnswer"
                :loading="aiLoading"
                size="small"
              >
                <i class="el-icon-magic-stick"></i>
                开始答疑
              </el-button>
            </div>
          </div>

          <!-- 移动端对话输入 -->
          <div
            class="mobile-input-container"
            v-if="aiAnswer || conversationHistory.length > 0"
          >
            <div class="mobile-input-wrapper">
              <el-input
                v-model="userMessage"
                type="textarea"
                :rows="2"
                placeholder="继续提问..."
                resize="none"
                size="small"
                @keydown.native="handleKeydown"
                class="mobile-textarea"
              />
              <el-button
                type="primary"
                @click="sendUserMessage"
                :loading="aiLoading"
                :disabled="!userMessage.trim()"
                size="small"
                class="mobile-send-btn"
                icon="el-icon-s-promotion"
              >
              </el-button>
            </div>
            <div class="mobile-input-tips">
              <i class="el-icon-info"></i>
              <small>Enter发送 / Shift+Enter换行</small>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑对话框 -->
    <question-edit-dialog
      ref="editDialog"
      :question-id="question.questionId"
      @success="handleEditSuccess"
    />
  </div>
</template>

<script>
import QuestionEditDialog from "./QuestionEditDialog.vue";
import {
  getAIAnswer,
  continueAIConversation,
  buildQuestionInfo,
  buildAIRequest,
} from "@/api/trouble/ai";
import { renderMathContent } from "@/utils/mathRender";

export default {
  name: "QuestionDetail",
  components: {
    QuestionEditDialog,
  },
  props: {
    question: {
      type: Object,
      required: true,
    },
  },
  data() {
    return {
      editDialogVisible: false,
      aiLoading: false,
      aiAnswer: "",
      aiError: "",
      userMessage: "",
      conversationHistory: [],
      showMobileAI: false,
    };
  },
  computed: {
    hasImages() {
      return (
        this.question.questionImages &&
        this.question.questionImages.trim() !== ""
      );
    },
    imageArray() {
      if (!this.hasImages) return [];
      return this.question.questionImages
        .split(",")
        .filter((img) => img.trim());
    },
    previewList() {
      return this.imageArray.map((img) => this.getImageUrl(img));
    },
    hasAnswerImages() {
      return (
        this.question.answerImages && this.question.answerImages.trim() !== ""
      );
    },
    answerImageArray() {
      if (!this.hasAnswerImages) return [];
      return this.question.answerImages.split(",").filter((img) => img.trim());
    },
    answerPreviewList() {
      return this.answerImageArray.map((img) => this.getImageUrl(img));
    },
    tagArray() {
      if (!this.question.tags) return [];
      return this.question.tags.split(",").filter((tag) => tag.trim());
    },
    renderedQuestionContent() {
      return renderMathContent(this.question.questionContent || "暂无内容");
    },
    renderedAnswerContent() {
      return renderMathContent(this.question.answerContent || "暂无答案");
    },
    renderedAIAnswer() {
      return this.formatAIAnswer(this.aiAnswer);
    },
  },
  mounted() {
    this.checkMobileAI();
    window.addEventListener("resize", this.checkMobileAI);
  },
  beforeDestroy() {
    window.removeEventListener("resize", this.checkMobileAI);
  },
  methods: {
    handleClose() {
      this.$emit("close");
    },
    handleEdit() {
      this.editDialogVisible = true;
      this.$nextTick(() => {
        this.$refs.editDialog.open();
      });
    },
    handleEditSuccess() {
      this.$emit("refresh");
      this.$message.success("修改成功");
    },

    checkMobileAI() {
      const isMobile = window.matchMedia("(max-width: 991px)").matches;
      if (!isMobile) {
        this.showMobileAI = false;
      }
    },

    closeMobileAI() {
      this.showMobileAI = false;
    },

    // 处理文本输入框按键事件
    handleKeydown(event) {
      // 检测是否是Enter键且没有Shift键
      const isEnterKey = event.key === "Enter" || event.keyCode === 13;
      const hasShiftKey = event.shiftKey;

      if (isEnterKey && !hasShiftKey) {
        event.preventDefault();
        this.sendUserMessage();
      }
    },

    // AI答疑相关方法
    async getAIAnswer() {
      // 移动端显示AI区域
      const isMobile = window.matchMedia("(max-width: 991px)").matches;
      if (isMobile) {
        this.showMobileAI = true;
      }

      this.aiLoading = true;
      this.aiError = "";
      this.aiAnswer = "";

      try {
        // 构建错题信息
        const questionInfo = buildQuestionInfo(this.question);
        const requestData = buildAIRequest(questionInfo, []);

        const response = await getAIAnswer(requestData);

        if (response.success) {
          this.aiAnswer = response.answer;
          // 重置对话历史，只包含第一条AI回答
          this.conversationHistory = [
            { role: "assistant", content: response.answer },
          ];
        } else {
          throw new Error(response.error || "AI服务返回错误");
        }
      } catch (error) {
        console.error("获取AI解答失败:", error);
        this.aiError = `获取AI解答失败：${error.message}`;
      } finally {
        this.aiLoading = false;
      }
    },

    async sendUserMessage() {
      if (!this.userMessage.trim() || this.aiLoading) {
        return;
      }

      const userMessage = this.userMessage.trim();
      this.userMessage = "";

      // 添加用户消息到对话历史
      this.conversationHistory.push({ role: "user", content: userMessage });

      this.aiLoading = true;
      this.aiError = "";

      try {
        // 构建错题信息
        const questionInfo = buildQuestionInfo(this.question);
        const requestData = buildAIRequest(
          questionInfo,
          this.conversationHistory
        );

        const response = await continueAIConversation(requestData);

        if (response.success) {
          this.aiAnswer = response.answer;
          // 添加AI回复到对话历史
          this.conversationHistory.push({
            role: "assistant",
            content: response.answer,
          });
        } else {
          throw new Error(response.error || "AI服务返回错误");
        }
      } catch (error) {
        console.error("发送消息失败:", error);
        this.aiError = `发送消息失败：${error.message}`;
        // 移除最后一条用户消息
        this.conversationHistory.pop();
      } finally {
        this.aiLoading = false;
      }
    },

    refreshAIAnswer() {
      // 清空对话历史，重新获取AI解答
      this.conversationHistory = [];
      this.getAIAnswer();
    },

    clearAIError() {
      this.aiError = "";
    },

    // 格式化AI回答内容
    formatAIAnswer(text) {
      if (!text) return "";

      let processed = text;

      // 1. 保护 LaTeX
      const latexBlocks = [],
        latexInlines = [];
      processed = processed.replace(/\$\$([\s\S]*?)\$\$/g, (m) => {
        const idx = latexBlocks.length;
        latexBlocks.push(m);
        return `__LATEX_BLOCK_${idx}__`;
      });
      processed = processed.replace(/\$([^\$\n]+?)\$/g, (m) => {
        const idx = latexInlines.length;
        latexInlines.push(m);
        return `__LATEX_INLINE_${idx}__`;
      });
      processed = processed.replace(/\\\[([\s\S]*?)\\\]/g, (m) => {
        const idx = latexBlocks.length;
        latexBlocks.push(m);
        return `__LATEX_BLOCK_${idx}__`;
      });
      processed = processed.replace(/\\\(([\s\S]*?)\\\)/g, (m) => {
        const idx = latexInlines.length;
        latexInlines.push(m);
        return `__LATEX_INLINE_${idx}__`;
      });

      // 2. 处理代码块
      const codeBlocks = [];
      processed = processed.replace(/```([\s\S]*?)```/g, (m, c) => {
        const idx = codeBlocks.length;
        codeBlocks.push(`<pre><code>${c.trim()}</code></pre>`);
        return `__CODE_BLOCK_${idx}__`;
      });

      // 3. 清理 Markdown 符号（保留文本）
      processed = processed.replace(/^### (.+)$/gm, "$1");
      processed = processed.replace(/^## (.+)$/gm, "$1");
      processed = processed.replace(/^# (.+)$/gm, "$1");
      processed = processed.replace(/\*\*(.+?)\*\*/g, "$1");
      processed = processed.replace(/\*(.+?)\*/g, "$1");
      processed = processed.replace(/`([^`]+?)`/g, "$1");
      processed = processed.replace(/^\s*[-*]\s+(.+)$/gm, "$1");
      processed = processed.replace(/^\s*\d+\.\s+(.+)$/gm, "$1");

      // 4. 恢复 LaTeX
      processed = processed.replace(
        /__LATEX_BLOCK_(\d+)__/g,
        (_, i) => latexBlocks[parseInt(i)]
      );
      processed = processed.replace(
        /__LATEX_INLINE_(\d+)__/g,
        (_, i) => latexInlines[parseInt(i)]
      );

      // 5. 渲染 LaTeX
      processed = renderMathContent(processed);

      // 6. 恢复代码块
      processed = processed.replace(
        /__CODE_BLOCK_(\d+)__/g,
        (_, i) => codeBlocks[parseInt(i)]
      );

      // 7. 段落和换行处理：双换行 => 段落，单换行保留
      processed = processed.replace(/\n{2,}/g, "</p><p>");
      processed = `<p>${processed}</p>`; // 包裹最外层

      return processed;
    },

    // 辅助方法
    getImageUrl(imagePath) {
      if (!imagePath) return "";
      if (imagePath.startsWith("http")) {
        return imagePath;
      }
      const baseUrl = process.env.VUE_APP_BASE_API;
      return (
        baseUrl + (imagePath.startsWith("/") ? imagePath : "/" + imagePath)
      );
    },
    formatTime(time) {
      if (!time) return "";
      return this.parseTime(time, "{y}-{m}-{d} {h}:{i}:{s}");
    },
    getTypeTagType(type) {
      const typeMap = {
        选择题: "success",
        填空题: "warning",
        解答题: "danger",
        未区分: "info",
      };
      return typeMap[type] || "info";
    },
    getImportanceText(importance) {
      if (importance === null || importance === undefined) {
        return "未设置";
      }
      const importanceMap = {
        1: "低",
        2: "中",
        3: "高",
      };
      return importanceMap[importance] || "未设置";
    },
    getImportanceTagType(importance) {
      if (importance === 3) return "danger";
      if (importance === 2) return "warning";
      if (importance === 1) return "info";
      return "info";
    },
    getProficiencyText(proficiency) {
      if (proficiency === null || proficiency === undefined) {
        return "未设置";
      }
      const proficiencyMap = {
        0: "陌生",
        1: "一般",
        2: "较好",
        3: "熟练",
      };
      return proficiencyMap[proficiency] || "未设置";
    },
    getProficiencyTagType(proficiency) {
      if (proficiency === 3) return "success";
      if (proficiency === 2) return "primary";
      if (proficiency === 1) return "warning";
      if (proficiency === 0) return "danger";
      return "info";
    },
    getDifficultyText(difficulty) {
      if (difficulty === null || difficulty === undefined) {
        return "未设置";
      }
      const difficultyMap = {
        1: "简单",
        2: "中等",
        3: "困难",
      };
      return difficultyMap[difficulty] || "未设置";
    },
    getDifficultyTagType(difficulty) {
      if (difficulty === 3) return "danger";
      if (difficulty === 2) return "warning";
      if (difficulty === 1) return "success";
      return "info";
    },
    parseTime(time, pattern) {
      if (!time) return "";
      const date = new Date(time);
      const formatObj = {
        y: date.getFullYear(),
        m: date.getMonth() + 1,
        d: date.getDate(),
        h: date.getHours(),
        i: date.getMinutes(),
        s: date.getSeconds(),
      };
      const format = pattern || "{y}-{m}-{d} {h}:{i}:{s}";
      return format.replace(/{(\w+)}/g, (result, key) => {
        const value = formatObj[key];
        return value < 10 ? "0" + value : value;
      });
    },
  },
};
</script>

<style scoped>
/* ==================== 基础布局 ==================== */
.question-detail-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 20px;
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.question-detail-dialog {
  background: white;
  border-radius: 24px;
  max-width: 1400px;
  width: 100%;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease-out;
  display: flex;
  flex-direction: column;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ==================== 头部设计 ==================== */
.detail-header {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  padding: 24px 32px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
}

.header-left {
  flex: 1;
  min-width: 0;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.header-tag {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border: none;
  color: white;
  font-weight: 500;
}

.header-time {
  font-size: 14px;
  opacity: 0.9;
  white-space: nowrap;
}

.header-title {
  font-size: 24px;
  font-weight: bold;
  margin: 0;
  color: white;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.header-actions .el-button {
  transition: all 0.3s ease;
}

.close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: rotate(90deg);
}

/* ==================== 内容区域布局 ==================== */
.detail-content-wrapper {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.detail-content-with-ai {
  display: flex;
  padding: 32px;
  overflow-y: auto;
  flex: 1;
  gap: 32px;
}

.original-content {
  flex: 1;
  min-width: 0;
}

.ai-answer-section.desktop-ai {
  width: 480px;
  min-width: 480px;
  max-width: 480px;
  display: flex;
  flex-direction: column;
  border-left: 2px solid #e4e7ed;
  padding-left: 32px;
  background: linear-gradient(to bottom, #fafbfc 0%, #ffffff 100%);
  border-radius: 12px;
  padding: 24px;
  margin-left: 16px;
}

/* ==================== 详情区域样式 ==================== */
.detail-section {
  margin-bottom: 32px;
  animation: fadeInUp 0.4s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.detail-section:last-child {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.section-indicator {
  width: 4px;
  height: 24px;
  border-radius: 2px;
  flex-shrink: 0;
}

.section-indicator.blue {
  background: linear-gradient(135deg, #409eff 0%, #1d7ceb 100%);
}

.section-indicator.purple {
  background: linear-gradient(135deg, #9c27b0 0%, #7b1fa2 100%);
}

.section-indicator.green {
  background: linear-gradient(135deg, #67c23a 0%, #4caf50 100%);
}

.section-indicator.orange {
  background: linear-gradient(135deg, #e6a23c 0%, #ff9800 100%);
}

.section-indicator.red {
  background: linear-gradient(135deg, #f56c6c 0%, #f44336 100%);
}

.section-indicator.cyan {
  background: linear-gradient(135deg, #00bcd4 0%, #0097a7 100%);
}

.section-indicator.gradient {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-content {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 24px;
  line-height: 1.8;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.section-content:hover {
  border-color: #e4e7ed;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

/* 渲染内容样式 */
.rendered-content {
  font-size: 16px;
  color: #303133;
  word-break: break-word;
  line-height: 1.8;
}

.rendered-content >>> p {
  margin: 0 0 12px 0;
  line-height: 1.8;
}

.rendered-content >>> p:last-child {
  margin-bottom: 0;
}

.rendered-content >>> strong {
  color: #409eff;
  font-weight: 600;
}

.rendered-content >>> em {
  color: #606266;
  font-style: italic;
}

.rendered-content >>> code {
  background: #f5f7fa;
  padding: 2px 8px;
  border-radius: 4px;
  font-family: "Consolas", "Monaco", "Courier New", monospace;
  font-size: 14px;
  color: #e6a23c;
  border: 1px solid #e4e7ed;
}

.rendered-content >>> pre {
  background: #282c34;
  border-radius: 8px;
  padding: 16px;
  overflow-x: auto;
  margin: 12px 0;
}

.rendered-content >>> pre code {
  background: transparent;
  border: none;
  color: #abb2bf;
  font-size: 13px;
  padding: 0;
  line-height: 1.6;
}

.rendered-content >>> h2,
.rendered-content >>> h3,
.rendered-content >>> h4 {
  margin: 20px 0 12px;
  color: #303133;
  font-weight: 600;
}

.rendered-content >>> h2 {
  font-size: 20px;
  border-bottom: 2px solid #409eff;
  padding-bottom: 8px;
}

.rendered-content >>> h3 {
  font-size: 18px;
  color: #606266;
  border-left: 4px solid #409eff;
  padding-left: 12px;
}

.rendered-content >>> h4 {
  font-size: 16px;
  color: #909399;
}

.rendered-content >>> ul,
.rendered-content >>> ol {
  margin: 12px 0;
  padding-left: 24px;
}

.rendered-content >>> li {
  margin: 6px 0;
  line-height: 1.8;
}

.rendered-content >>> ul li {
  list-style-type: disc;
}

.rendered-content >>> ol li {
  list-style-type: decimal;
}

.rendered-content >>> blockquote {
  border-left: 4px solid #dcdfe6;
  padding-left: 16px;
  margin: 12px 0;
  color: #606266;
  font-style: italic;
}

.rendered-content >>> a {
  color: #409eff;
  text-decoration: none;
  transition: color 0.3s;
}

.rendered-content >>> a:hover {
  color: #66b1ff;
  text-decoration: underline;
}

.rendered-content >>> hr {
  border: none;
  border-top: 1px solid #e4e7ed;
  margin: 20px 0;
}

/* LaTeX数学公式专门样式 */
.rendered-content >>> .katex {
  font-size: 1.1em;
  color: #303133;
}

.rendered-content >>> .katex-display {
  margin: 20px 0;
  overflow-x: auto;
  overflow-y: hidden;
  padding: 12px 0;
  text-align: center;
}

.rendered-content >>> .katex-display > .katex {
  display: inline-block;
  text-align: left;
}

/* 确保行内公式与文字正确对齐 */
.rendered-content >>> .katex-html {
  display: inline-block;
  vertical-align: middle;
  line-height: 1;
}

/* MathJax公式样式（如果使用MathJax） */
.rendered-content >>> .MathJax {
  display: inline-block !important;
  vertical-align: middle;
}

.rendered-content >>> .MJXc-display {
  margin: 20px 0;
  text-align: center;
}

/* 确保h4标题在有公式的情况下也能正常显示 */
.rendered-content >>> h4 .katex {
  font-size: 0.95em;
}

/* 段落内的公式间距 */
.rendered-content >>> p .katex {
  margin: 0 2px;
}

/* 数学公式特殊样式 */
.rendered-content >>> .math {
  display: inline-block;
  margin: 0 2px;
}

.rendered-content >>> .math-display {
  display: block;
  margin: 16px 0;
  text-align: center;
  overflow-x: auto;
}

.question-content {
  background: linear-gradient(135deg, #e3f2fd 0%, #f0f9ff 100%);
  border: 1px solid #bbdefb;
}

.answer-content {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border: 1px solid #bae6fd;
}

/* ==================== 图片查看器 ==================== */
.image-viewer {
  border-radius: 12px;
  overflow: hidden;
  background: #1a1a1a;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.detail-image {
  width: 100%;
  height: 400px;
  display: block;
}

/* ==================== 标签容器 ==================== */
.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.detail-tag {
  padding: 10px 18px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
  border-radius: 8px;
  transition: all 0.3s ease;
  cursor: default;
}

.detail-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.detail-tag i {
  font-size: 16px;
}

/* ==================== AI答疑区域 ==================== */
.ai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid #e4e7ed;
}

.refresh-btn {
  color: #606266;
  transition: all 0.3s ease;
}

.refresh-btn:hover {
  color: #409eff;
}

.ai-content {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 20px;
  min-height: 300px;
  max-height: calc(90vh - 400px);
  padding-right: 8px;
}

.ai-content::-webkit-scrollbar {
  width: 6px;
}

.ai-content::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.ai-content::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

.ai-content.mobile {
  min-height: 200px;
  max-height: 400px;
}

/* ==================== AI状态样式 ==================== */
.ai-loading {
  padding: 24px;
  text-align: center;
}

.loading-text {
  margin-top: 16px;
  color: #606266;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.loading-text i {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.ai-error {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ai-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  text-align: center;
  color: #909399;
}

.empty-icon {
  font-size: 64px;
  color: #dcdfe6;
  margin-bottom: 16px;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 0.6;
  }
  50% {
    opacity: 1;
  }
}

.empty-text {
  margin-bottom: 24px;
  font-size: 16px;
  color: #606266;
}

.ai-answer {
  line-height: 1.6;
  animation: fadeIn 0.4s ease-out;
}

.ai-message {
  padding: 20px;
  background: white;
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* ==================== 对话历史 ==================== */
.conversation-history {
  margin-top: 24px;
  border-top: 2px solid #e4e7ed;
  padding-top: 20px;
}

.history-header h4 {
  margin: 0 0 16px 0;
  color: #606266;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.history-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.history-item {
  border-radius: 12px;
  padding: 16px;
  transition: all 0.3s ease;
  animation: slideInRight 0.3s ease-out;
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.history-item.user {
  background: linear-gradient(135deg, #e3f2fd 0%, #f0f9ff 100%);
  border: 1px solid #bbdefb;
  margin-left: 20px;
}

.history-item.assistant {
  background: linear-gradient(135deg, #f0f9ff 0%, #f6ffed 100%);
  border: 1px solid #b7eb8f;
  margin-right: 20px;
}

.history-item:hover {
  transform: translateX(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.history-role {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.history-role i {
  font-size: 16px;
}

.history-content {
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
}

/* ==================== 输入区域 ==================== */
.ai-input-container {
  margin-top: auto;
  padding-top: 16px;
  border-top: 2px solid #e4e7ed;
  background: white;
}

.input-wrapper {
  position: relative;
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.ai-textarea {
  flex: 1;
}

.ai-textarea >>> .el-textarea__inner {
  border-radius: 12px;
  border: 2px solid #dcdfe6;
  transition: all 0.3s ease;
  font-size: 14px;
  line-height: 1.6;
  padding: 12px 16px;
  resize: none;
}

.ai-textarea >>> .el-textarea__inner:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

.send-button {
  flex-shrink: 0;
  height: 96px;
  border-radius: 12px;
  padding: 0 24px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.send-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.send-button:active {
  transform: translateY(0);
}

.input-tips {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
  justify-content: flex-end;
}

.input-tips i {
  font-size: 14px;
}

/* ==================== 移动端AI区域 ==================== */
.mobile-ai-section {
  display: none;
}

/* ==================== 响应式设计 ==================== */
@media (max-width: 1399px) {
  .question-detail-dialog {
    max-width: 95%;
  }

  .ai-answer-section.desktop-ai {
    width: 420px;
    min-width: 420px;
    max-width: 420px;
  }
}

@media (max-width: 1199px) {
  .ai-answer-section.desktop-ai {
    width: 380px;
    min-width: 380px;
    max-width: 380px;
  }

  .detail-content-with-ai {
    gap: 24px;
  }
}

@media (max-width: 991px) {
  .detail-content-with-ai {
    flex-direction: column;
    padding-bottom: 100px;
  }

  .ai-answer-section.desktop-ai {
    display: none;
  }

  .mobile-ai-section {
    display: block;
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: white;
    border-top: 2px solid #e4e7ed;
    padding: 20px;
    box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
    z-index: 2001;
    max-height: 70vh;
    overflow-y: auto;
    animation: slideUpMobile 0.3s ease-out;
  }

  @keyframes slideUpMobile {
    from {
      transform: translateY(100%);
    }
    to {
      transform: translateY(0);
    }
  }

  .mobile-ai-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 2px solid #e4e7ed;
  }

  .mobile-ai-actions {
    display: flex;
    gap: 8px;
  }

  .close-mobile-ai {
    color: #909399;
  }

  .close-mobile-ai:hover {
    color: #f56c6c;
  }

  .mobile-input-container {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #e4e7ed;
  }

  .mobile-input-wrapper {
    display: flex;
    gap: 12px;
    align-items: flex-end;
  }

  .mobile-textarea {
    flex: 1;
  }

  .mobile-textarea >>> .el-textarea__inner {
    border-radius: 12px;
    border: 2px solid #dcdfe6;
    font-size: 14px;
    padding: 10px 14px;
  }

  .mobile-send-btn {
    flex-shrink: 0;
    height: 60px;
    width: 60px;
    border-radius: 12px;
    font-size: 18px;
  }

  .mobile-input-tips {
    text-align: center;
    margin-top: 8px;
    color: #909399;
    font-size: 11px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
  }
}

@media (max-width: 767px) {
  .question-detail-overlay {
    padding: 0;
    align-items: flex-start;
  }

  .question-detail-dialog {
    max-height: 100vh;
    height: 100vh;
    border-radius: 0;
    max-width: 100%;
  }

  .detail-header {
    padding: 16px 20px;
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .header-left {
    width: 100%;
  }

  .header-info {
    width: 100%;
    margin-bottom: 8px;
  }

  .header-title {
    font-size: 20px;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .header-actions .btn-text {
    display: none;
  }

  .header-actions .el-button {
    padding: 8px 12px;
  }

  .detail-content-with-ai {
    padding: 20px 16px;
    padding-bottom: 120px;
  }

  .section-header {
    margin-bottom: 12px;
  }

  .section-title {
    font-size: 16px;
  }

  .section-content {
    padding: 16px;
    font-size: 14px;
  }

  .rendered-content {
    font-size: 14px;
  }

  .detail-image {
    height: 250px;
  }

  .tags-container {
    gap: 8px;
  }

  .detail-tag {
    padding: 8px 14px;
    font-size: 13px;
  }

  .mobile-ai-section {
    max-height: 65vh;
    padding: 16px;
  }

  .mobile-ai-header {
    margin-bottom: 12px;
  }

  .section-title {
    font-size: 15px;
  }

  .ai-content.mobile {
    max-height: 300px;
  }
}

@media (max-width: 480px) {
  .detail-header {
    padding: 12px 16px;
  }

  .header-title {
    font-size: 18px;
  }

  .detail-content-with-ai {
    padding: 16px 12px;
  }

  .section-content {
    padding: 12px;
  }

  .mobile-ai-section {
    padding: 12px;
  }
}
</style>
