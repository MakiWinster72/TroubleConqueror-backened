"""DeepSeek AI 答疑服务

功能：
- 接收前端POST请求，包含错题信息和对话历史
- 调用DeepSeek API获取AI解答
- 支持多轮对话
- 返回AI解答内容

运行：
uvicorn deepseek:app --host 0.0.0.0 --port 9001
"""

import asyncio
import logging
import os
import time
from typing import Any, Dict, List, Optional

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from openai import OpenAI
from pydantic import BaseModel

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="DeepSeek AI答疑服务", description="为错题征服者提供AI答疑功能")

# 允许跨域
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 允许所有前端访问
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# DeepSeek API配置
DEEPSEEK_API_KEY = "sk-013ac2eb6a5549a49f96581e42b0d68e"
DEEPSEEK_BASE_URL = "https://api.deepseek.com"
DEEPSEEK_MODEL = "deepseek-chat"  # deepseek-chat或deepseek-reasoner

# 初始化OpenAI客户端
client = OpenAI(api_key=DEEPSEEK_API_KEY, base_url=DEEPSEEK_BASE_URL)

# 系统提示词 - 定义AI的角色和行为
SYSTEM_PROMPT = """你是一位专业的中小学教育助手，专门帮助学生分析和解答错题。
你的任务是：
1. 分析学生提供的错题，理解题目内容和学生可能的困惑点
2. 提供清晰、详细的解答步骤和思路
3. 指出错误原因和常见误区
4. 给出相关知识点和举一反三的练习建议
5. 用耐心、鼓励的语气与学生交流

请按照以下格式回答问题：
1. **题目分析**：简要分析题目考察的知识点和难点
2. **详细解答**：逐步展示解题过程
3. **错误分析**：分析可能出错的原因
4. **知识点总结**：总结相关的知识点
5. **练习建议**：提供类似题目或巩固练习的建议

请使用中文回答，语言要亲切、易懂，适合中小学生理解。"""


# 请求数据模型
class QuestionInfo(BaseModel):
    """错题信息"""

    question_content: str  # 题目内容
    answer_content: Optional[str] = ""  # 答案内容（可选）
    tags: Optional[str] = ""  # 标签（可选）
    question_type: Optional[str] = ""  # 题目类型
    grade: Optional[str] = ""  # 年级
    difficulty: Optional[int] = None  # 难度等级


class ConversationMessage(BaseModel):
    """对话消息"""

    role: str
    content: str


class AIRequest(BaseModel):
    """AI答疑请求"""

    question: QuestionInfo
    conversation_history: Optional[List[ConversationMessage]] = []  # 对话历史
    stream: Optional[bool] = False


class AIResponse(BaseModel):
    """AI响应"""

    success: bool
    answer: str
    conversation_id: Optional[str] = None  # 用于多轮对话的会话ID
    tokens_used: Optional[int] = None  # 使用的token数量
    error: Optional[str] = None


def build_messages(
    question: QuestionInfo, conversation_history: List[ConversationMessage] = None
) -> List[Dict[str, str]]:
    """
    构建发送给AI的消息列表
    """
    messages = [{"role": "system", "content": SYSTEM_PROMPT}]

    # 如果有对话历史，添加到消息中
    if conversation_history:
        for msg in conversation_history[-10:]:  # 只保留最近10条历史记录
            messages.append({"role": msg.role, "content": msg.content})
    else:
        # 第一次对话，构建初始问题
        user_message = f"""请帮我分析这道错题：

题目：{question.question_content}

"""

        if question.answer_content:
            user_message += f"我的答案：{question.answer_content}\n\n"

        if question.tags:
            user_message += f"相关标签：{question.tags}\n"

        if question.question_type:
            user_message += f"题目类型：{question.question_type}\n"

        if question.grade:
            user_message += f"年级：{question.grade}\n"

        if question.difficulty:
            difficulty_map = {1: "简单", 2: "中等", 3: "困难"}
            user_message += f"难度：{difficulty_map.get(question.difficulty, '未知')}\n"

        user_message += "\n请帮我分析这道题，告诉我正确的解题思路和我的错误在哪里。"
        messages.append({"role": "user", "content": user_message})

    return messages


@app.get("/")
async def root():
    """健康检查端点"""
    return {
        "service": "DeepSeek AI答疑服务",
        "status": "running",
        "model": DEEPSEEK_MODEL,
        "version": "1.0.0",
    }


@app.post("/api/ai/answer", response_model=AIResponse)
async def get_ai_answer(request: AIRequest):
    """
    获取AI对错题的解答
    """
    try:
        logger.info(
            f"收到AI答疑请求，题目长度：{len(request.question.question_content)}"
        )

        # 构建消息
        messages = build_messages(request.question, request.conversation_history)

        # 调用DeepSeek API
        start_time = time.time()

        if request.stream:
            # 流式响应
            response = client.chat.completions.create(
                model=DEEPSEEK_MODEL,
                messages=messages,
                stream=False,
                max_tokens=2000,
                temperature=0.7,
                timeout=60.0,
            )
        else:
            # 非流式响应
            response = client.chat.completions.create(
                model=DEEPSEEK_MODEL,
                messages=messages,
                stream=False,
                max_tokens=2000,
                temperature=0.7,
                timeout=60.0,
            )

        elapsed_time = time.time() - start_time
        logger.info(f"DeepSeek API调用完成，耗时：{elapsed_time:.2f}秒")

        # 提取回答内容
        answer = response.choices[0].message.content

        return AIResponse(
            success=True,
            answer=answer,
            tokens_used=response.usage.total_tokens
            if hasattr(response, "usage")
            else None,
        )

    except Exception as e:
        logger.error(f"调用DeepSeek API失败：{str(e)}")
        return AIResponse(success=False, answer="", error=f"AI服务暂时不可用：{str(e)}")


@app.post("/api/ai/continue", response_model=AIResponse)
async def continue_conversation(request: AIRequest):
    """
    继续对话 - 基于已有的对话历史继续交流
    """
    try:
        if not request.conversation_history:
            return AIResponse(
                success=False, answer="", error="需要提供对话历史才能继续对话"
            )

        # 确保最后一条消息是用户的
        if (
            not request.conversation_history
            or request.conversation_history[-1].role != "user"
        ):
            return AIResponse(
                success=False, answer="", error="对话历史的最后一条消息应该是用户的提问"
            )

        # 构建消息
        messages = [{"role": "system", "content": SYSTEM_PROMPT}]

        for msg in request.conversation_history[-20:]:  # 限制历史长度
            messages.append({"role": msg.role, "content": msg.content})

        # 调用DeepSeek API
        response = client.chat.completions.create(
            model=DEEPSEEK_MODEL,
            messages=messages,
            stream=False,
            max_tokens=1500,
            temperature=0.7,
            timeout=60.0,
        )

        answer = response.choices[0].message.content

        return AIResponse(
            success=True,
            answer=answer,
            tokens_used=response.usage.total_tokens
            if hasattr(response, "usage")
            else None,
        )

    except Exception as e:
        logger.error(f"继续对话失败：{str(e)}")
        return AIResponse(success=False, answer="", error=f"继续对话失败：{str(e)}")


@app.get("/api/ai/test")
async def test_connection():
    """
    测试DeepSeek API连接
    """
    try:
        test_message = [{"role": "user", "content": "你好，请回复'AI服务连接正常'"}]

        response = client.chat.completions.create(
            model=DEEPSEEK_MODEL,
            messages=test_message,
            stream=False,
            max_tokens=100,
            timeout=30.0,
        )

        return {
            "success": True,
            "message": "DeepSeek API连接正常",
            "response": response.choices[0].message.content,
            "model": DEEPSEEK_MODEL,
        }

    except Exception as e:
        return {
            "success": False,
            "message": f"DeepSeek API连接失败：{str(e)}",
            "model": DEEPSEEK_MODEL,
        }


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=9001)
