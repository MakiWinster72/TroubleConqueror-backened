-- 为错题表添加重要性字段

-- 添加重要性字段
ALTER TABLE trouble_question 
ADD COLUMN importance INT NOT NULL DEFAULT 2 
COMMENT '重要性等级（1=低，2=中，3=高）' 
AFTER tags;

-- 为重要性字段添加索引（可选，用于提高查询性能）
CREATE INDEX idx_importance ON trouble_question(importance);

