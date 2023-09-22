package com.Reflux.ReApi.service;

import com.Reflux.ReApi.model.entity.InterfaceInfo;

/**
 *
 */

public interface InnerInterfaceInfoService {

    /**
     * 从数据库中查询模拟接口是否存在（请求路径、请求方法、请求参数）
     *
     * @return InterfaceInfo 接口信息
     */
    InterfaceInfo getInterfaceInfo(long id, String url, String method, String path);

}
