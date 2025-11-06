package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 错题表 trouble_question
 * 
 * @author ruoyi
 */
public class TroubleQuestion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 错题ID */
    @Excel(name = "错题ID", cellType = Excel.ColumnType.NUMERIC)
    private Long questionId;

    /** 用户ID */
    @Excel(name = "用户ID", cellType = Excel.ColumnType.NUMERIC)
    private Long userId;

    /** 题目内容 */
    @Excel(name = "题目内容")
    private String questionContent;

    /** 题目图片 */
    @Excel(name = "题目图片")
    private String questionImages;

    /** 答案内容 */
    @Excel(name = "答案内容")
    private String answerContent;

    /** 答案图片 */
    @Excel(name = "答案图片")
    private String answerImages;

    /** 题目类型 */
    @Excel(name = "题目类型")
    private String questionType;

    /** 标签 */
    @Excel(name = "标签")
    private String tags;

    /** 重要性等级（1=低，2=中，3=高） */
    @Excel(name = "重要性", readConverterExp = "1=低,2=中,3=高")
    private Integer importance;

    /** 状态（0正常 1删除） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=删除")
    private String status;

    /** 是否收藏（0未收藏 1已收藏）- 非数据库字段 */
    private Integer isFavorite;

    public Long getQuestionId()
    {
        return questionId;
    }

    public void setQuestionId(Long questionId)
    {
        this.questionId = questionId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getQuestionContent()
    {
        return questionContent;
    }

    public void setQuestionContent(String questionContent)
    {
        this.questionContent = questionContent;
    }

    public String getQuestionImages()
    {
        return questionImages;
    }

    public void setQuestionImages(String questionImages)
    {
        this.questionImages = questionImages;
    }

    public String getAnswerContent()
    {
        return answerContent;
    }

    public void setAnswerContent(String answerContent)
    {
        this.answerContent = answerContent;
    }

    public String getAnswerImages()
    {
        return answerImages;
    }

    public void setAnswerImages(String answerImages)
    {
        this.answerImages = answerImages;
    }

    public String getQuestionType()
    {
        return questionType;
    }

    public void setQuestionType(String questionType)
    {
        this.questionType = questionType;
    }

    public String getTags()
    {
        return tags;
    }

    public void setTags(String tags)
    {
        this.tags = tags;
    }

    public Integer getImportance()
    {
        return importance;
    }

    public void setImportance(Integer importance)
    {
        this.importance = importance;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getIsFavorite()
    {
        return isFavorite;
    }

    public void setIsFavorite(Integer isFavorite)
    {
        this.isFavorite = isFavorite;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("questionId", getQuestionId())
            .append("userId", getUserId())
            .append("questionContent", getQuestionContent())
            .append("questionImages", getQuestionImages())
            .append("answerContent", getAnswerContent())
            .append("answerImages", getAnswerImages())
            .append("questionType", getQuestionType())
            .append("tags", getTags())
            .append("importance", getImportance())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
