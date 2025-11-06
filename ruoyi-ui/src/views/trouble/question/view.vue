<template>
  <div class="question-view-page">
    <!-- 顶部搜索栏 -->
    <div class="view-header">
      <div class="header-inner">
        <div class="header-left">
          <h1 class="page-title">我的错题本</h1>
          <p class="page-subtitle">共 {{ total }} 道题目</p>
        </div>
        <div class="header-right">
          <el-input
            v-model="queryParams.questionContent"
            placeholder="搜索题目内容..."
            prefix-icon="el-icon-search"
            clearable
            @keyup.enter.native="handleQuery"
            @clear="handleQuery"
            class="search-input"
          />
          <el-button
            type="text"
            class="back-btn"
            @click="goToDashboard"
          >
            <i class="el-icon-house"></i> 返回主页
          </el-button>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-inner">
        <div class="filter-group">
          <span class="filter-label">题目类型：</span>
          <el-radio-group v-model="queryParams.questionType" @change="handleQuery" class="filter-radios">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="选择题">选择题</el-radio-button>
            <el-radio-button label="填空题">填空题</el-radio-button>
            <el-radio-button label="解答题">解答题</el-radio-button>
            <el-radio-button label="其他">其他</el-radio-button>
          </el-radio-group>
        </div>
        <div class="filter-group">
          <span class="filter-label">标签：</span>
          <el-select
            v-model="selectedTags"
            multiple
            placeholder="选择标签筛选"
            clearable
            @change="handleTagFilter"
            class="tag-select"
          >
            <el-option
              v-for="tag in availableTags"
              :key="tag"
              :label="tag"
              :value="tag"
            />
          </el-select>
        </div>
        <div class="filter-group">
          <span class="filter-label">重要性：</span>
          <el-radio-group v-model="queryParams.importance" @change="handleQuery" class="filter-radios">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button :label="3">高</el-radio-button>
            <el-radio-button :label="2">中</el-radio-button>
            <el-radio-button :label="1">低</el-radio-button>
          </el-radio-group>
        </div>
        <div class="view-mode-group">
          <span class="filter-label">视图：</span>
          <el-radio-group v-model="viewMode" @change="handleViewModeChange" class="view-mode-radios">
            <el-radio-button label="list">
              <i class="el-icon-menu"></i> 列表
            </el-radio-button>
            <el-radio-button label="card">
              <i class="el-icon-grid"></i> 卡片
            </el-radio-button>
            <el-radio-button label="compact">
              <i class="el-icon-document"></i> 紧凑
            </el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </div>

    <!-- 题目列表 -->
    <div class="view-container">
      <!-- 列表视图 -->
      <div v-if="viewMode === 'list'" v-loading="loading" class="questions-list">
        <question-list-item
          v-for="question in questionList"
          :key="question.questionId"
          :question="question"
          @click="handleQuestionClick"
          @view="handleView"
          @edit="handleEdit"
          @favorite="handleFavorite"
          @delete="handleDelete"
        />
      </div>

      <!-- 卡片视图 -->
      <div v-else-if="viewMode === 'card'" v-loading="loading" class="questions-grid">
        <question-card
          v-for="question in questionList"
          :key="question.questionId"
          :question="question"
          @click="handleQuestionClick"
          @view="handleView"
          @edit="handleEdit"
          @favorite="handleFavorite"
          @delete="handleDelete"
        />
      </div>

      <!-- 紧凑视图 -->
      <div v-else-if="viewMode === 'compact'" v-loading="loading" class="questions-compact">
        <question-compact-item
          v-for="question in questionList"
          :key="question.questionId"
          :question="question"
          @click="handleQuestionClick"
          @view="handleView"
          @edit="handleEdit"
          @favorite="handleFavorite"
          @delete="handleDelete"
        />
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && questionList.length === 0" class="empty-state">
        <i class="el-icon-document-delete"></i>
        <p>没有找到相关题目</p>
      </div>

      <!-- 分页 -->
      <pagination
        v-show="total > 0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
        class="pagination-wrapper"
      />
    </div>

    <!-- 详情弹窗 -->
    <question-detail
      v-if="selectedQuestion"
      :question="selectedQuestion"
      @close="selectedQuestion = null"
      @refresh="handleQuestionRefresh"
    />

    <!-- 编辑对话框 -->
    <question-edit-dialog
      ref="editDialog"
      :question-id="selectedQuestion ? selectedQuestion.questionId : null"
      @success="handleEditSuccess"
    />
  </div>
</template>

<script>
import { listQuestion, delQuestion, favoriteQuestion, unfavoriteQuestion, getQuestion } from "@/api/trouble/question";
import QuestionCard from "./components/QuestionCard.vue";
import QuestionListItem from "./components/QuestionListItem.vue";
import QuestionCompactItem from "./components/QuestionCompactItem.vue";
import QuestionDetail from "./components/QuestionDetail.vue";
import QuestionEditDialog from "./components/QuestionEditDialog.vue";
import { mapGetters } from "vuex";

export default {
  name: "QuestionView",
  components: {
    QuestionCard,
    QuestionListItem,
    QuestionCompactItem,
    QuestionDetail,
    QuestionEditDialog
  },
  computed: {
    ...mapGetters(['roles'])
  },
  data() {
    return {
      loading: true,
      questionList: [],
      total: 0,
      selectedQuestion: null,
      selectedTags: [],
      availableTags: [],
      viewMode: 'list', // 默认列表视图: 'list', 'card', 'compact'
      queryParams: {
        pageNum: 1,
        pageSize: 12,
        questionContent: null,
        questionType: null,
        tags: null,
        importance: ""
      }
    };
  },
  created() {
    // 从localStorage读取视图模式
    const savedViewMode = localStorage.getItem('questionViewMode');
    if (savedViewMode && ['list', 'card', 'compact'].includes(savedViewMode)) {
      this.viewMode = savedViewMode;
    }
    // 根据视图模式设置每页数量
    if (this.viewMode === 'list') {
      this.queryParams.pageSize = 20;
    } else if (this.viewMode === 'compact') {
      this.queryParams.pageSize = 30;
    }
    
    // 检查是否有查询参数（从其他页面跳转过来时可能带有 id）
    if (this.$route.query.id) {
      // 如果有 id，先加载列表，然后自动打开详情
      this.getList().then(() => {
        const question = this.questionList.find(q => q.questionId == this.$route.query.id);
        if (question) {
          this.selectedQuestion = question;
        }
      });
    } else {
      this.getList();
    }
    this.loadTags();
  },
  methods: {
    /** 查询错题列表 */
    getList() {
      this.loading = true;
      // 如果有选中的标签，添加到查询参数
      if (this.selectedTags.length > 0) {
        this.queryParams.tags = this.selectedTags.join(',');
      } else {
        this.queryParams.tags = null;
      }
      
      // 处理重要性参数：空字符串转为null
      const queryParams = { ...this.queryParams };
      if (queryParams.importance === "") {
        queryParams.importance = null;
      }
      
      return listQuestion(queryParams).then(response => {
        const questions = response.rows || [];
        // 检查每个错题是否已收藏（这里需要根据实际API返回的数据来判断）
        // 如果API返回了isFavorite字段，则直接使用；否则需要额外查询
        this.questionList = questions;
        this.total = response.total || 0;
        this.loading = false;
        return response;
      }).catch(() => {
        this.questionList = [];
        this.total = 0;
        this.loading = false;
        return Promise.reject();
      });
    },
    /** 加载所有可用标签 */
    loadTags() {
      listQuestion({ pageNum: 1, pageSize: 1000 }).then(response => {
        const allQuestions = response.rows || [];
        const tagSet = new Set();
        allQuestions.forEach(q => {
          if (q.tags) {
            q.tags.split(',').forEach(tag => {
              const trimmedTag = tag.trim();
              if (trimmedTag) {
                tagSet.add(trimmedTag);
              }
            });
          }
        });
        this.availableTags = Array.from(tagSet).sort();
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 标签筛选 */
    handleTagFilter() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 视图模式切换 */
    handleViewModeChange() {
      // 保存视图模式到localStorage
      localStorage.setItem('questionViewMode', this.viewMode);
      // 根据视图模式调整每页数量
      if (this.viewMode === 'list') {
        this.queryParams.pageSize = 20;
      } else if (this.viewMode === 'compact') {
        this.queryParams.pageSize = 30;
      } else {
        this.queryParams.pageSize = 12;
      }
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 题目点击 */
    handleQuestionClick(question) {
      this.selectedQuestion = question;
    },
    handleQuestionRefresh() {
      // 刷新列表
      this.getList();
      // 重新加载详情
      if (this.selectedQuestion) {
        const questionId = this.selectedQuestion.questionId;
        this.getList().then(() => {
          const updatedQuestion = this.questionList.find(q => q.questionId == questionId);
          if (updatedQuestion) {
            this.selectedQuestion = updatedQuestion;
          }
        });
      }
    },

    handleEditSuccess() {
      this.getList();
      if (this.selectedQuestion) {
        const questionId = this.selectedQuestion.questionId;
        this.getList().then(() => {
          const updatedQuestion = this.questionList.find(q => q.questionId == questionId);
          if (updatedQuestion) {
            this.selectedQuestion = updatedQuestion;
          }
        });
      }
    },
    /** 返回主页 */
    goToDashboard() {
      this.$router.push("/index");
    },
    /** 查看详情 */
    handleView(question) {
      this.selectedQuestion = question;
    },
    /** 编辑 */
    handleEdit(question) {
      this.selectedQuestion = question;
      this.$nextTick(() => {
        if (this.$refs.editDialog) {
          this.$refs.editDialog.open();
        }
      });
    },
    /** 收藏/取消收藏 */
    handleFavorite(question) {
      const isFavorite = question.isFavorite;
      const action = isFavorite ? unfavoriteQuestion : favoriteQuestion;
      const actionText = isFavorite ? '取消收藏' : '收藏';
      
      action(question.questionId).then(() => {
        this.$message.success(`${actionText}成功`);
        // 更新本地状态
        question.isFavorite = !isFavorite;
        // 刷新列表
        this.getList();
      }).catch(() => {
        this.$message.error(`${actionText}失败`);
      });
    },
    /** 删除 */
    handleDelete(question) {
      this.$modal.confirm('确认要删除该错题吗？').then(() => {
        return delQuestion(question.questionId);
      }).then(() => {
        this.$message.success('删除成功');
        this.getList();
      }).catch(() => {});
    }
  }
};
</script>

<style scoped>
.question-view-page {
  min-height: 100vh;
  background: #f5f5f5;
  position: relative;
  padding: 0;
}

.view-header {
  background: #ffffff;
  border-bottom: 1px solid #e0e0e0;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: none;
  border-radius: 0;
}

.header-inner {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.header-left {
  flex-shrink: 0;
}

.page-title {
  font-size: 24px;
  font-weight: 500;
  color: #212121;
  margin: 0 0 4px 0;
  letter-spacing: -0.5px;
}

.page-subtitle {
  color: #757575;
  font-size: 14px;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  justify-content: flex-end;
}

.search-input {
  width: 320px;
}

.back-btn {
  color: #757575;
  padding: 0;
}

.back-btn:hover {
  color: #212121;
}

.filter-bar {
  background: #ffffff;
  border-bottom: 1px solid #e0e0e0;
  box-shadow: none;
  border-radius: 0;
}

.filter-inner {
  max-width: 1400px;
  margin: 0 auto;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  gap: 32px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.view-mode-group {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
  flex-wrap: nowrap;
}

.view-mode-radios {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 0;
}

.view-mode-radios >>> .el-radio-button__inner {
  border: 1px solid #e0e0e0;
  background: #ffffff;
  color: #616161;
  padding: 6px 12px;
  border-radius: 4px;
  margin-right: 8px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 4px;
}

.view-mode-radios >>> .el-radio-button__inner:hover {
  border-color: #2196f3;
  color: #2196f3;
}

.view-mode-radios >>> .el-radio-button__orig-radio:checked + .el-radio-button__inner {
  background: #2196f3;
  border-color: #2196f3;
  color: #ffffff;
  box-shadow: none;
}

.filter-label {
  font-size: 14px;
  color: #616161;
  font-weight: 500;
  white-space: nowrap;
}

.filter-radios {
  margin: 0;
}

.filter-radios >>> .el-radio-button__inner {
  border: 1px solid #e0e0e0;
  background: #ffffff;
  color: #616161;
  padding: 8px 16px;
  border-radius: 4px;
  margin-right: 8px;
  transition: all 0.2s;
}

.filter-radios >>> .el-radio-button__inner:hover {
  border-color: #2196f3;
  color: #2196f3;
}

.filter-radios >>> .el-radio-button__orig-radio:checked + .el-radio-button__inner {
  background: #2196f3;
  border-color: #2196f3;
  color: #ffffff;
  box-shadow: none;
}

.tag-select {
  width: 200px;
}

.view-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  box-sizing: border-box;
}

/* 列表视图 */
.questions-list {
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 0;
  margin-bottom: 24px;
  overflow: hidden;
  box-shadow: none;
  border-top: none;
}

/* 卡片视图 */
.questions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.questions-grid .flat-card {
  background: #ffffff;
  box-shadow: none;
  border: 1px solid #e0e0e0;
}

/* 紧凑视图 */
.questions-compact {
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 0;
  margin-bottom: 24px;
  overflow: hidden;
  box-shadow: none;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #9e9e9e;
}

.empty-state i {
  font-size: 64px;
  display: block;
  margin-bottom: 16px;
  opacity: 0.4;
}

.empty-state p {
  font-size: 16px;
  margin: 0;
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: center;
  padding: 16px;
  background: #ffffff;
  border-radius: 0;
  box-shadow: none;
  border: 1px solid #e0e0e0;
}

@media (max-width: 768px) {
  .header-inner {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .header-right {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
  }

  .search-input {
    width: 100%;
  }

  .filter-inner {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .filter-group {
    width: 100%;
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-radios {
    width: 100%;
  }

  .tag-select {
    width: 100%;
  }

  .view-mode-group {
    width: 100%;
    margin-left: 0;
  }

  .questions-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .questions-list {
    border: none;
  }

  .questions-compact {
    border: none;
  }

  .view-container {
    padding: 16px;
  }
}
</style>
