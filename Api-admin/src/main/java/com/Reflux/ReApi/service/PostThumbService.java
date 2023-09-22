package com.Reflux.ReApi.service;


import com.Reflux.ReApi.model.entity.PostThumb;
import com.Reflux.ReApi.model.vo.LoginUserVO;
import com.baomidou.mybatisplus.extension.service.IService;


/**
 * 帖子点赞服务
 *
 * @author Aaron
 */
public interface PostThumbService extends IService<PostThumb> {

    /**
     * 点赞
     *
     * @param postId
     * @param loginUser
     * @return
     */
    int doPostThumb(long postId, LoginUserVO loginUser);

    /**
     * 帖子点赞（内部服务）
     *
     * @param userId
     * @param postId
     * @return
     */
    int doPostThumbInner(long userId, long postId);
}
