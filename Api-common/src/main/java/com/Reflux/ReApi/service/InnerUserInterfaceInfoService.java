package com.Reflux.ReApi.service;

import com.Reflux.ReApi.model.entity.UserInterfaceInfo;

/**
 * 抽取出来的公共的接口，admin项目用于实现该接口， gateway项目用于调用实现的方法
 */

public interface InnerUserInterfaceInfoService {

    /**
     * 调用接口统计
     *
     * @param interfaceInfoId 接口ID
     * @param userId          用户ID
     * @return boolean 是否执行成功
     */
    boolean invokeCount(long interfaceInfoId, long userId);

    /**
     * 是否还有调用次数，其实就是在查对应接口用户关系表
     *
     * @param interfaceId 接口id
     * @param userId      用户id
     * @return UserInterfaceInfo 用户接口信息
     */
    UserInterfaceInfo hasLeftNum(Long interfaceId, Long userId);


    /**
     * 添加默认的用户接口信息
     *
     * @param interfaceId 接口id
     * @param userId      用户id
     * @return Boolean 是否添加成功
     */
    Boolean addDefaultUserInterfaceInfo(Long interfaceId, Long userId);

    /**
     * 检查用户是否有接口
     *
     * @param interfaceId 接口id
     * @param userId     用户id
     * @return UserInterfaceInfo 用户接口信息
     */

}
