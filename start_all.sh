#!/bin/bash

pids=()

cleanup() {
  echo ">>> 停止所有服务..."
  for pid in "${pids[@]}"; do
    kill "$pid" 2>/dev/null
  done
  echo ">>> 全部已停止"
  exit 0
}
trap cleanup INT

# 检查并启动 Redis
echo ">>> 检查 Redis..."
if ! pgrep -x "redis-server" >/dev/null; then
  echo "Redis 未运行，启动 Redis..."
  redis-server --daemonize yes
else
  echo "Redis 已在运行"
fi

# 检查并启动 MariaDB
echo ">>> 检查 MariaDB..."
if ! pgrep -x "mysqld" >/dev/null; then
  echo "MariaDB 未运行，启动 MariaDB..."
  sudo systemctl start mariadb
else
  echo "MariaDB 已在运行"
fi

# 1. 根目录 python http server
echo ">>> 启动 Python http server..."
python -m http.server &
pids+=($!)

# 2. ruoyi-admin
echo ">>> 启动 RuoYi-Admin..."
cd ruoyi-admin
mvn spring-boot:run &
pids+=($!)
cd ..

# 3. OCR 服务
echo ">>> 启动 OCR 服务..."
cd ocr
source venv/bin/activate
uvicorn xfyun:app --host 0.0.0.0 --port 9000 &
pids+=($!)
cd ..

# 4. ruoyi-ui
echo ">>> 启动 RuoYi-UI..."
cd ruoyi-ui
npm run dev &
pids+=($!)
cd ..

# 5. AI
echo ">>> 启动 AI 服务..."
cd ocr
source venv/bin/activate
uvicorn uvicorn deepseek:app --host 0.0.0.0 --port 9001 --reload
pids+=($!)
cd ..

echo ">>> 所有服务已启动，按 Ctrl+C 停止"
wait
