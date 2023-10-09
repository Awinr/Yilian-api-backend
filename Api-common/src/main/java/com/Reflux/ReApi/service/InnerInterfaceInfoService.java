package com.Reflux.ReApi.service;

import com.Reflux.ReApi.model.entity.InterfaceInfo;

/**
 * 抽取出来的公共的接口，admin项目用于实现该接口， gateway项目用于调用实现的方法
 */

public interface InnerInterfaceInfoService {

    /**
     * 从数据库中查询模拟接口是否存在（请求路径、请求方法、请求参数）
     *
     * @return InterfaceInfo 接口信息
     */
    InterfaceInfo getInterfaceInfo(long id, String url, String method, String path);

}
