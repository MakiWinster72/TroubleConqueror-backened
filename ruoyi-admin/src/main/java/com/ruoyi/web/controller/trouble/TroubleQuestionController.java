package com.ruoyi.web.controller.trouble;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.TroubleQuestion;
import com.ruoyi.system.service.ITroubleQuestionService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.service.ITroubleQuestionFavoriteService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 错题Controller
 * 
 * @author ruoyi
 */
@Api(tags = "错题管理")
@RestController
@RequestMapping("/trouble/question")
public class TroubleQuestionController extends BaseController
{
    @Autowired
    private ITroubleQuestionService troubleQuestionService;

    @Autowired
    private ITroubleQuestionFavoriteService troubleQuestionFavoriteService;

    /**
     * 查询错题列表
     */
    @ApiOperation("查询错题列表")
    @PreAuthorize("@ss.hasPermi('trouble:question:list')")
    @GetMapping("/list")
    public TableDataInfo list(TroubleQuestion troubleQuestion)
    {
        // 只查询当前用户的错题
        Long userId = SecurityUtils.getUserId();
        troubleQuestion.setUserId(userId);
        startPage();
        List<TroubleQuestion> list = troubleQuestionService.selectTroubleQuestionList(troubleQuestion);
        
        // 标记每个错题是否已收藏
        for (TroubleQuestion question : list) {
            boolean isFavorited = troubleQuestionFavoriteService.isFavorited(question.getQuestionId(), userId);
            question.setIsFavorite(isFavorited ? 1 : 0);
        }
        
        return getDataTable(list);
    }

    /**
     * 导出错题列表
     */
    @PreAuthorize("@ss.hasPermi('trouble:question:export')")
    @Log(title = "错题", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TroubleQuestion troubleQuestion)
    {
        troubleQuestion.setUserId(SecurityUtils.getUserId());
        List<TroubleQuestion> list = troubleQuestionService.selectTroubleQuestionList(troubleQuestion);
        ExcelUtil<TroubleQuestion> util = new ExcelUtil<>(TroubleQuestion.class);
        util.exportExcel(response, list, "错题数据");
    }

    /**
     * 获取错题详细信息
     */
    @PreAuthorize("@ss.hasPermi('trouble:question:query')")
    @GetMapping(value = "/{questionId}")
    public AjaxResult getInfo(@PathVariable("questionId") Long questionId)
    {
        TroubleQuestion question = troubleQuestionService.selectTroubleQuestionByQuestionId(questionId);
        // 验证是否属于当前用户
        if (question != null && !question.getUserId().equals(SecurityUtils.getUserId())) {
            return error("无权限访问该错题");
        }
        return success(question);
    }

    /**
     * 新增错题
     */
    @ApiOperation("新增错题")
    @PreAuthorize("@ss.hasPermi('trouble:question:add')")
    @Log(title = "错题", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TroubleQuestion troubleQuestion)
    {
        // 设置当前用户ID
        troubleQuestion.setUserId(SecurityUtils.getUserId());
        // 设置默认题目类型
        if (troubleQuestion.getQuestionType() == null || troubleQuestion.getQuestionType().isEmpty()) {
            troubleQuestion.setQuestionType("未区分");
        }
        return toAjax(troubleQuestionService.insertTroubleQuestion(troubleQuestion));
    }

    /**
     * 修改错题
     */
    @PreAuthorize("@ss.hasPermi('trouble:question:edit')")
    @Log(title = "错题", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TroubleQuestion troubleQuestion)
    {
        // 验证是否属于当前用户
        TroubleQuestion existingQuestion = troubleQuestionService.selectTroubleQuestionByQuestionId(troubleQuestion.getQuestionId());
        if (existingQuestion == null || !existingQuestion.getUserId().equals(SecurityUtils.getUserId())) {
            return error("无权限修改该错题");
        }
        troubleQuestion.setUserId(SecurityUtils.getUserId());
        return toAjax(troubleQuestionService.updateTroubleQuestion(troubleQuestion));
    }

    /**
     * 删除错题
     */
    @PreAuthorize("@ss.hasPermi('trouble:question:remove')")
    @Log(title = "错题", businessType = BusinessType.DELETE)
	@DeleteMapping("/{questionIds}")
    public AjaxResult remove(@PathVariable Long[] questionIds)
    {
        // 验证所有错题是否属于当前用户
        for (Long questionId : questionIds) {
            TroubleQuestion question = troubleQuestionService.selectTroubleQuestionByQuestionId(questionId);
            if (question == null || !question.getUserId().equals(SecurityUtils.getUserId())) {
                return error("无权限删除该错题");
            }
        }
        return toAjax(troubleQuestionService.deleteTroubleQuestionByQuestionIds(questionIds));
    }

    /**
     * 收藏错题
     */
    @ApiOperation("收藏错题")
    @Log(title = "收藏错题", businessType = BusinessType.INSERT)
    @PostMapping("/favorite/{questionId}")
    public AjaxResult favoriteQuestion(@PathVariable("questionId") Long questionId)
    {
        // 验证错题是否存在且属于当前用户
        TroubleQuestion question = troubleQuestionService.selectTroubleQuestionByQuestionId(questionId);
        if (question == null) {
            return error("错题不存在");
        }
        Long userId = SecurityUtils.getUserId();
        if (!question.getUserId().equals(userId)) {
            return error("无权限收藏该错题");
        }

        // 收藏错题
        int result = troubleQuestionFavoriteService.favoriteQuestion(questionId, userId);
        if (result > 0) {
            return success("收藏成功");
        } else {
            return error("该错题已收藏");
        }
    }

    /**
     * 取消收藏错题
     */
    @ApiOperation("取消收藏错题")
    @Log(title = "取消收藏错题", businessType = BusinessType.DELETE)
    @PostMapping("/unfavorite/{questionId}")
    public AjaxResult unfavoriteQuestion(@PathVariable("questionId") Long questionId)
    {
        Long userId = SecurityUtils.getUserId();
        int result = troubleQuestionFavoriteService.unfavoriteQuestion(questionId, userId);
        if (result > 0) {
            return success("取消收藏成功");
        } else {
            return error("取消收藏失败");
        }
    }

    /**
     * 查询收藏的错题列表
     */
    @ApiOperation("查询收藏的错题列表")
    @GetMapping("/favorite/list")
    public TableDataInfo listFavoriteQuestions(TroubleQuestion troubleQuestion)
    {
        Long userId = SecurityUtils.getUserId();
        troubleQuestion.setUserId(userId);
        startPage();
        List<TroubleQuestion> list = troubleQuestionService.selectFavoriteQuestionList(troubleQuestion);
        
        // 标记所有收藏的错题的收藏状态为已收藏
        for (TroubleQuestion question : list) {
            question.setIsFavorite(1);
        }
        
        return getDataTable(list);
    }

    /**
     * 更新错题重要性
     */
    @ApiOperation("更新错题重要性")
    @PreAuthorize("@ss.hasPermi('trouble:question:edit')")
    @Log(title = "更新错题重要性", businessType = BusinessType.UPDATE)
    @PutMapping("/importance/{questionId}")
    public AjaxResult updateImportance(@PathVariable("questionId") Long questionId, @RequestParam("importance") Integer importance)
    {
        // 验证重要性参数
        if (importance == null || (importance < 1 || importance > 3)) {
            return error("重要性等级必须为1（低）、2（中）或3（高）");
        }
        
        // 验证错题是否存在且属于当前用户
        TroubleQuestion existingQuestion = troubleQuestionService.selectTroubleQuestionByQuestionId(questionId);
        if (existingQuestion == null) {
            return error("错题不存在");
        }
        if (!existingQuestion.getUserId().equals(SecurityUtils.getUserId())) {
            return error("无权限修改该错题");
        }
        
        // 更新重要性
        TroubleQuestion updateQuestion = new TroubleQuestion();
        updateQuestion.setQuestionId(questionId);
        updateQuestion.setImportance(importance);
        updateQuestion.setUserId(SecurityUtils.getUserId());
        
        int result = troubleQuestionService.updateTroubleQuestion(updateQuestion);
        if (result > 0) {
            return success("更新重要性成功");
        } else {
            return error("更新重要性失败");
        }
    }
}



