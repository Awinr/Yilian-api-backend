package com.Reflux.ReApi.service;

import com.Reflux.ReApi.model.entity.User;

/**
 * 抽取出来的公共的接口，admin项目用于实现该接口， gateway项目用于调用实现的方法
 */
public interface InnerUserService {

    /**
     * 数据库中查是否已分配给用户秘钥（accessKey）
     * @param accessKey accessKey
     * @return User 用户信息
     */
    User getInvokeUser(String accessKey);
}
