<template>
  <div class="app-container">
    <el-card class="welcome-card">
      <div slot="header" class="clearfix header-row">
        <span class="welcome-title">🎯 错题征服者 - 智能错题管理系统</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="refreshData">
          <i class="el-icon-refresh"></i> 刷新 (Refresh)
        </el-button>
      </div>

      <div class="welcome-content">
        <p class="welcome-desc">
          专为中小学生设计的智能错题管理系统，帮助用户高效管理错题、提升学习效率。
          支持文本输入和拍照识别添加错题，智能分类管理，多维度统计分析。
        </p>

        <!-- 统计卡片：响应式列设置 -->
        <el-row :gutter="16" class="stats-row">
          <el-col :xs="24" :sm="12" :md="6" v-for="(item, idx) in statItems" :key="idx">
            <el-card class="stat-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-number">{{ item.value }}</div>
                <div class="stat-label">{{ item.label }}</div>
                <i :class="item.icon" class="stat-icon"></i>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 功能按钮区域：响应式 -->
    <el-row :gutter="16" class="function-row">
      <el-col :xs="24" :sm="12" :md="8">
        <el-card class="function-card" shadow="hover" @click.native="goToAddQuestion">
          <div class="function-content">
            <div class="function-icon"><i class="el-icon-edit-outline"></i></div>
            <div class="function-title">添加错题 (Add)</div>
            <div class="function-desc">手动输入题目内容，支持文本和图片</div>
            <el-button type="primary" size="medium" class="function-btn" @click.stop="goToAddQuestion">
              <i class="el-icon-edit"></i> 立即添加 (Add Now)
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="8">
        <el-card class="function-card" shadow="hover" @click.native="goToQuestionList">
          <div class="function-content">
            <div class="function-icon"><i class="el-icon-view"></i></div>
            <div class="function-title">查看错题 (List)</div>
            <div class="function-desc">浏览和管理已添加的错题</div>
            <el-button type="success" size="medium" class="function-btn" @click.stop="goToQuestionList">
              <i class="el-icon-view"></i> 查看列表 (View)
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="8">
        <el-card class="function-card" shadow="hover" @click.native="goToCameraAdd">
          <div class="function-content">
            <div class="function-icon"><i class="el-icon-camera"></i></div>
            <div class="function-title">拍照添加 (Camera)</div>
            <div class="function-desc">拍照识别题目，智能提取内容</div>
            <el-button type="warning" size="medium" class="function-btn" @click.stop="goToCameraAdd">
              <i class="el-icon-camera"></i> 拍照识别 (Scan)
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快速操作区域 -->
    <el-card class="quick-actions">
      <div slot="header" class="clearfix">
        <span>🚀 快速操作 (Quick Actions)</span>
      </div>

      <el-row :gutter="12" class="quick-row">
        <el-col :xs="12" :sm="6"><el-button type="primary" icon="el-icon-edit" size="medium" @click="goToAddQuestion" block>手动添加 (Add)</el-button></el-col>
        <el-col :xs="12" :sm="6"><el-button type="success" icon="el-icon-view" size="medium" @click="goToQuestionList" block>查看列表 (List)</el-button></el-col>
        <el-col :xs="12" :sm="6"><el-button type="warning" icon="el-icon-camera" size="medium" @click="goToCameraAdd" block>拍照识别 (Camera)</el-button></el-col>
        <el-col :xs="12" :sm="6"><el-button type="info" icon="el-icon-download" size="medium" @click="exportQuestions" block>导出数据 (Export)</el-button></el-col>
      </el-row>
    </el-card>

    <!-- 最近错题展示：桌面显示表格，移动端显示卡片列表 -->
    <el-card class="recent-questions">
      <div slot="header" class="clearfix">
        <span>📚 最近添加的错题 (Recent)</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="goToQuestionList">
          查看全部 (View All) <i class="el-icon-arrow-right"></i>
        </el-button>
      </div>

      <div v-if="recentQuestions.length === 0" class="empty-state">
        <i class="el-icon-document"></i>
        <p>还没有添加错题，点击上方按钮开始添加吧！</p>
      </div>

      <div v-else>
        <!-- 手机端卡片列表 -->
        <div v-if="isMobile" class="mobile-list">
          <el-row :gutter="12">
            <el-col :span="24" v-for="q in recentQuestions" :key="q.questionId">
              <el-card class="mobile-question-card" shadow="never">
                <div class="mobile-card-header">
                  <div class="mobile-card-title">{{ truncate(q.questionContent, 120) }}</div>
                  <div class="mobile-card-meta">{{ parseTime(q.createTime, '{y}-{m}-{d} {h}:{i}') }}</div>
                </div>
                <div class="mobile-card-body">
                  <el-tag :type="getTypeTagType(q.questionType)" size="mini">{{ q.questionType }}</el-tag>
                  <div class="mobile-tags">
                    <el-tag v-for="tag in getTagsArray(q.tags)" :key="tag" size="mini">{{ tag }}</el-tag>
                  </div>
                </div>
                <div class="mobile-card-actions">
                  <el-button size="mini" type="text" @click="viewQuestion(q)">查看 (View)</el-button>
                  <el-button size="mini" type="text" @click="editQuestion(q)">编辑 (Edit)</el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 桌面端表格 -->
        <div v-else class="desktop-table-wrapper">
          <el-table :data="recentQuestions" style="width: 100%" :stripe="true" :border="true">
            <el-table-column prop="questionContent" label="题目内容" :show-overflow-tooltip="true">
              <template #default="{ row }">
                <div class="question-preview">
                  {{ row.questionContent.length > 80 ? row.questionContent.substring(0, 80) + '...' : row.questionContent }}
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="questionType" label="类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getTypeTagType(row.questionType)" size="small">{{ row.questionType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="tags" label="标签" width="200">
              <template #default="{ row }">
                <el-tag v-for="tag in getTagsArray(row.tags)" :key="tag" size="mini" style="margin-right: 5px;">{{ tag }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="添加时间" width="180">
              <template #default="{ row }">
                <span>{{ parseTime(row.createTime, '{y}-{m}-{d} {h}:{i}') }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140">
              <template #default="{ row }">
                <el-button size="mini" type="text" @click="viewQuestion(row)">查看 (View)</el-button>
                <el-button size="mini" type="text" @click="editQuestion(row)">编辑 (Edit)</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-card>

    <!-- 拍照识别对话框（宽度和布局在小屏优化） -->
    <el-dialog title="📷 拍照识别题目 (Camera Scan)" :visible.sync="cameraDialogVisible" :width="cameraDialogWidth" :close-on-click-modal="false">
      <div class="camera-section">
        <div class="camera-tip">
          <i class="el-icon-camera"></i>
          <p>点击下方按钮调用手机相机拍照</p>
          <p class="tip-text">支持识别数学公式、文字内容等</p>
        </div>

        <el-upload
          class="camera-uploader"
          :action="uploadUrl"
          :headers="uploadHeaders"
          :show-file-list="false"
          :on-success="handleCameraSuccess"
          :before-upload="beforeCameraUpload"
          accept="image/*"
          capture="camera"
        >
          <el-button type="primary" size="large" icon="el-icon-camera">点击拍照识别 (Take Photo)</el-button>
        </el-upload>

        <div v-if="cameraResult" class="camera-result">
          <h4>识别结果 (Result)：</h4>
          <el-input type="textarea" :rows="4" v-model="cameraResult" placeholder="识别结果将显示在这里..."></el-input>
          <div style="margin-top: 10px;">
            <el-button type="primary" @click="useCameraResult">使用识别结果 (Use)</el-button>
            <el-button @click="cameraResult = ''">重新识别 (Retry)</el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listQuestion } from "@/api/trouble/question";
import { getTroubleStatistics } from "@/api/trouble/statistics";
import { getToken } from "@/utils/auth";

export default {
  name: "TroubleDashboard",
  data() {
    return {
      // 统计数据
      stats: {
        totalQuestions: 0,
        todayQuestions: 0,
        thisWeekQuestions: 0,
        tagsCount: 0
      },
      // 最近错题
      recentQuestions: [],
      // 拍照对话框
      cameraDialogVisible: false,
      cameraResult: '',
      // 上传配置
      uploadUrl: process.env.VUE_APP_BASE_API + "/common/upload",
      uploadHeaders: {
        Authorization: "Bearer " + getToken()
      },
      // 响应式标志
      isMobile: false
    };
  },
  computed: {
    statItems() {
      return [
        { label: "总错题数", value: this.stats.totalQuestions, icon: "el-icon-document" },
        { label: "今日新增", value: this.stats.todayQuestions, icon: "el-icon-plus" },
        { label: "本周新增", value: this.stats.thisWeekQuestions, icon: "el-icon-date" },
        { label: "标签数量", value: this.stats.tagsCount, icon: "el-icon-collection-tag" }
      ];
    },
    // 弹窗宽度根据屏幕切换
    cameraDialogWidth() {
      return this.isMobile ? "95%" : "600px";
    }
  },
  created() {
    this.loadData();
    this.checkIsMobile();
    window.addEventListener("resize", this.checkIsMobile);
  },
  beforeDestroy() {
    window.removeEventListener("resize", this.checkIsMobile);
  },
  methods: {
    /** 检测移动端：使用 matchMedia 判定 */
    checkIsMobile() {
      // 视口宽度小于 768 视为移动端
      this.isMobile = window.matchMedia("(max-width: 767px)").matches;
    },

    /** 加载数据 */
    loadData() {
      this.loadStats();
      this.loadRecentQuestions();
    },

    /** 加载统计数据 */
    loadStats() {
      getTroubleStatistics().then(response => {
        if (response.code === 200) {
          this.stats = {
            totalQuestions: response.data.totalQuestions || 0,
            todayQuestions: response.data.todayQuestions || 0,
            thisWeekQuestions: response.data.thisWeekQuestions || 0,
            tagsCount: response.data.tagsCount || 0
          };
        }
      }).catch(() => {
        this.stats = { totalQuestions: 0, todayQuestions: 0, thisWeekQuestions: 0, tagsCount: 0 };
      });
    },

    /** 加载最近错题 */
    loadRecentQuestions() {
      listQuestion({ pageNum: 1, pageSize: 5 }).then(response => {
        this.recentQuestions = response.rows || [];
      }).catch(() => {
        this.recentQuestions = [];
      });
    },

    refreshData() {
      this.loadData();
      this.$message.success('数据已刷新 (Refreshed)');
    },

    goToAddQuestion() {
      this.$router.push('/trouble/question/add');
    },

    goToQuestionList() {
      this.$router.push('/trouble/question/view');
    },

    goToCameraAdd() {
      this.cameraDialogVisible = true;
      this.cameraResult = '';
    },

    viewQuestion(row) {
      this.$router.push({ path: '/trouble/question/view', query: { id: row.questionId } });
    },

    editQuestion(row) {
      this.$router.push({ path: '/trouble/question', query: { edit: row.questionId } });
    },

    exportQuestions() {
      this.$router.push('/trouble/question');
      this.$nextTick(() => {
        this.$message.info('请在错题列表页面点击导出按钮 (Please use Export on list)');
      });
    },

    beforeCameraUpload(file) {
      const isImage = file.type.indexOf('image/') === 0;
      const isLt10M = file.size / 1024 / 1024 < 10;

      if (!isImage) {
        this.$message.error('只能上传图片文件! (Only images allowed)');
        return false;
      }
      if (!isLt10M) {
        this.$message.error('上传图片大小不能超过 10MB! (Max 10MB)');
        return false;
      }
      return true;
    },

    getTypeTagType(type) {
      const typeMap = {
        '选择题': 'success',
        '填空题': 'warning',
        '解答题': 'danger',
        '未区分': 'info'
      };
      return typeMap[type] || 'info';
    },

    getTagsArray(tags) {
      if (!tags) return [];
      return tags.split(',').filter(tag => tag.trim());
    },

    handleCameraSuccess(response, file) {
      // 假设后端返回 { code:200, data:{ text: '识别文本' } }
      if (response && response.code === 200) {
        this.cameraResult = response.data && response.data.text ? response.data.text : '';
      } else if (typeof response === 'string') {
        // 某些后端直接返回文本
        this.cameraResult = response;
      } else {
        this.$message.error('识别失败 (Recognition failed)');
      }
    },

    useCameraResult() {
      // 将识别结果带到添加错题页面（示例做法）
      this.cameraDialogVisible = false;
      this.$router.push({ path: '/trouble/question/add', query: { content: this.cameraResult } });
    },

    truncate(text, n = 100) {
      if (!text) return '';
      return text.length > n ? text.substring(0, n) + '...' : text;
    },

    // 时间格式化函数：保持与现有 parseTime 调用一致（如果项目已有全局工具可替换）
    parseTime(time, cFormat) {
      if (!time) return '';
      const format = cFormat || '{y}-{m}-{d} {h}:{i}:{s}';
      let date = typeof time === 'object' ? time : new Date(time);
      const formatObj = {
        y: date.getFullYear(),
        m: date.getMonth() + 1,
        d: date.getDate(),
        h: date.getHours(),
        i: date.getMinutes(),
        s: date.getSeconds(),
        a: date.getDay()
      };
      return format.replace(/{([ymdhisa])+}/g, (result, key) => {
        let value = formatObj[key];
        if (key === 'a') return ['日', '一', '二', '三', '四', '五', '六'][value];
        return value < 10 ? '0' + value : value;
      });
    }
  }
};
</script>

<style scoped>
/* 基础布局 - 蓝色渐变背景 */
.app-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  box-sizing: border-box;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8f1f8 50%, #dbe7f2 100%);
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 欢迎卡片美化 */
::v-deep .welcome-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(42, 82, 152, 0.12);
  background: linear-gradient(135deg, #ffffff 0%, #f8fbff 100%);
  margin-bottom: 24px;
  overflow: hidden;
}

::v-deep .welcome-card .el-card__header {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  border-bottom: none;
  padding: 20px 24px;
}

.welcome-title {
  font-size: 24px;
  font-weight: 600;
  color: #ffffff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.header-row .el-button {
  color: rgba(255, 255, 255, 0.95);
  font-weight: 500;
  transition: all 0.3s;
}

.header-row .el-button:hover {
  color: #ffffff;
  transform: scale(1.05);
}

/* 欢迎描述 */
.welcome-content {
  padding: 24px;
}

.welcome-desc {
  font-size: 15px;
  color: #5a6c7d;
  line-height: 1.8;
  margin-bottom: 24px;
  text-align: center;
}

.stats-row {
  margin-top: 20px;
}

/* 统计卡片风格 - 蓝色系渐变 */
::v-deep .stat-card {
  text-align: left;
  border: none;
  border-radius: 12px;
  transition: all 0.3s ease;
  padding: 20px;
  background: linear-gradient(135deg, #ffffff 0%, #f0f7ff 100%);
  box-shadow: 0 2px 12px rgba(42, 82, 152, 0.08);
  position: relative;
  overflow: hidden;
}

::v-deep .stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
}

::v-deep .stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(42, 82, 152, 0.18);
}

::v-deep .stat-card .el-card__body {
  padding: 0;
}

.stat-content {
  position: relative;
  padding-right: 50px;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #6b7c8d;
  font-weight: 500;
}

.stat-icon {
  position: absolute;
  top: 50%;
  right: 12px;
  transform: translateY(-50%);
  font-size: 36px;
  color: rgba(42, 82, 152, 0.15);
}

/* 功能卡片 - 蓝色系主题 */
.function-row {
  margin-top: 24px;
  margin-bottom: 24px;
}

::v-deep .function-card {
  cursor: pointer;
  border: none;
  border-radius: 16px;
  transition: all 0.3s ease;
  background: #ffffff;
  box-shadow: 0 4px 16px rgba(42, 82, 152, 0.1);
  height: 100%;
}

::v-deep .function-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(42, 82, 152, 0.2);
}

::v-deep .function-card .el-card__body {
  padding: 0;
}

.function-content {
  text-align: center;
  padding: 32px 24px;
}

.function-icon {
  font-size: 48px;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  transition: transform 0.3s;
}

::v-deep .function-card:hover .function-icon {
  transform: scale(1.1);
}

.function-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #2c3e50;
}

.function-desc {
  font-size: 14px;
  color: #7a8a9a;
  margin-bottom: 20px;
  line-height: 1.6;
}

.function-btn {
  width: 100%;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s;
}

::v-deep .function-btn.el-button--primary {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  border: none;
}

::v-deep .function-btn.el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(42, 82, 152, 0.3);
}

::v-deep .function-btn.el-button--success {
  background: linear-gradient(135deg, #0a74da 0%, #4a9ff5 100%);
  border: none;
}

::v-deep .function-btn.el-button--success:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(74, 159, 245, 0.3);
}

::v-deep .function-btn.el-button--warning {
  background: linear-gradient(135deg, #3a7bd5 0%, #00d2ff 100%);
  border: none;
}

::v-deep .function-btn.el-button--warning:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(58, 123, 213, 0.3);
}

/* 快速操作 */
::v-deep .quick-actions {
  margin-top: 24px;
  margin-bottom: 24px;
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(42, 82, 152, 0.12);
  background: #ffffff;
}

::v-deep .quick-actions .el-card__header {
  background: linear-gradient(135deg, #f8fbff 0%, #ffffff 100%);
  border-bottom: 2px solid #e8f1f8;
  padding: 16px 20px;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.quick-row .el-button {
  margin-bottom: 8px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s;
}

::v-deep .quick-row .el-button--primary {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  border: none;
}

::v-deep .quick-row .el-button--success {
  background: linear-gradient(135deg, #0a74da 0%, #4a9ff5 100%);
  border: none;
}

::v-deep .quick-row .el-button--warning {
  background: linear-gradient(135deg, #3a7bd5 0%, #00d2ff 100%);
  border: none;
}

::v-deep .quick-row .el-button--info {
  background: linear-gradient(135deg, #5a6c7d 0%, #7a8a9a 100%);
  border: none;
}

/* 最近错题卡片 */
::v-deep .recent-questions {
  border-radius: 16px;
  border: none;
  box-shadow: 0 4px 20px rgba(42, 82, 152, 0.12);
  background: #ffffff;
}

::v-deep .recent-questions .el-card__header {
  background: linear-gradient(135deg, #f8fbff 0%, #ffffff 100%);
  border-bottom: 2px solid #e8f1f8;
  padding: 16px 20px;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

/* 最近错题 - 桌面表格 */
.desktop-table-wrapper {
  overflow-x: auto;
}

::v-deep .desktop-table-wrapper .el-table {
  border-radius: 8px;
}

::v-deep .desktop-table-wrapper .el-table th {
  background: linear-gradient(135deg, #f8fbff 0%, #ffffff 100%);
  color: #2c3e50;
  font-weight: 600;
}

::v-deep .desktop-table-wrapper .el-table--striped .el-table__body tr.el-table__row--striped td {
  background: #f8fbff;
}

.question-preview {
  color: #5a6c7d;
  line-height: 1.6;
}

/* 移动端卡片列表 */
.mobile-list {
  padding: 8px 0;
}

::v-deep .mobile-question-card {
  margin-bottom: 12px;
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(42, 82, 152, 0.08);
  transition: all 0.3s;
}

::v-deep .mobile-question-card:hover {
  box-shadow: 0 4px 16px rgba(42, 82, 152, 0.15);
}

.mobile-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
  gap: 8px;
}

.mobile-card-title {
  font-weight: 600;
  word-break: break-word;
  color: #2c3e50;
}

.mobile-card-meta {
  color: #7a8a9a;
  font-size: 12px;
  white-space: nowrap;
}

.mobile-card-body {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
  margin-bottom: 8px;
}

.mobile-card-actions {
  text-align: right;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 48px 12px;
  color: #7a8a9a;
}

.empty-state i {
  font-size: 64px;
  margin-bottom: 16px;
  display: block;
  color: rgba(42, 82, 152, 0.3);
}

.empty-state p {
  font-size: 15px;
  margin: 0;
}

/* 相机区 */
.camera-section {
  text-align: center;
  padding: 24px;
}

.camera-tip {
  margin-bottom: 20px;
}

.camera-tip i {
  font-size: 48px;
  color: #2a5298;
  margin-bottom: 12px;
  display: block;
}

.camera-tip p {
  margin: 8px 0;
  color: #5a6c7d;
}

.tip-text {
  font-size: 13px;
  color: #7a8a9a;
}

.camera-result {
  margin-top: 20px;
  text-align: left;
}

.camera-result h4 {
  margin-bottom: 12px;
  color: #2c3e50;
}

/* 响应式微调 */
@media (max-width: 767px) {
  .app-container {
    padding: 12px;
  }

  .welcome-title {
    font-size: 18px;
  }

  .welcome-content {
    padding: 16px;
  }

  .stat-number {
    font-size: 24px;
  }

  .function-icon {
    font-size: 40px;
  }

  .function-content {
    padding: 24px 16px;
  }

  .desktop-table-wrapper {
    display: none;
  }

  .mobile-list {
    display: block;
  }
}

@media (min-width: 768px) {
  .mobile-list {
    display: none;
  }
}
</style>
