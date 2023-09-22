package com.Reflux.ReApi.controller;

import com.Reflux.ReApi.common.BaseResponse;
import com.Reflux.ReApi.common.ErrorCode;
import com.Reflux.ReApi.common.ResultUtils;
import com.Reflux.ReApi.exception.BusinessException;
import com.Reflux.ReApi.model.request.postthumb.PostThumbAddRequest;

import com.Reflux.ReApi.service.PostThumbService;
import com.Reflux.ReApi.service.UserService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.Reflux.ReApi.model.vo.LoginUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 帖子点赞接口
 *
 * @author Aaron
 */
@RestController
@RequestMapping("/post_thumb")
@Slf4j
public class PostThumbController {

    @Resource
    private PostThumbService postThumbService;

    @Resource
    private UserService userService;

    /**
     * 点赞 / 取消点赞
     *
     * @param postThumbAddRequest
     * @param request
     * @return resultNum 本次点赞变化数
     */
    @PostMapping("/")
    public BaseResponse<Integer> doThumb(@RequestBody PostThumbAddRequest postThumbAddRequest,
            HttpServletRequest request) {
        if (postThumbAddRequest == null || postThumbAddRequest.getPostId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能点赞
        //final User loginUser = userService.getLoginUserBySession(request);
        final LoginUserVO loginUser =userService.getLoginUserByThreadLocal();
        long postId = postThumbAddRequest.getPostId();
        int result = postThumbService.doPostThumb(postId, loginUser);
        return ResultUtils.success(result);
    }

}
