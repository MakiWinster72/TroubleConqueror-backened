import request from "@/utils/request";

// 获取AI对错题的解答
export function getAIAnswer(data) {
  return request({
    url: "http://localhost:9001/api/ai/answer",
    method: "post",
    data: data,
    timeout: 60000,
  });
}

// 继续与AI对话
export function continueAIConversation(data) {
  return request({
    url: "http://localhost:9001/api/ai/continue",
    method: "post",
    data: data,
    timeout: 60000,
  });
}

// 测试AI服务连接
export function testAIConnection() {
  return request({
    url: "http://localhost:9001/api/ai/test",
    method: "get",
    timeout: 10000,
  });
}

// 构建错题信息对象
export function buildQuestionInfo(question) {
  return {
    question_content: question.questionContent || "",
    answer_content: question.answerContent || "",
    tags: question.tags || "",
    question_type: question.questionType || "",
    grade: question.grade || "",
    difficulty: question.difficulty || null,
  };
}

// 构建AI请求数据
export function buildAIRequest(questionInfo, conversationHistory = []) {
  return {
    question: questionInfo,
    conversation_history: conversationHistory,
    stream: false,
  };
}
