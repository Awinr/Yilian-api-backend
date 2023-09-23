package com.Reflux.ReApi.service;


import com.Reflux.ReApi.model.request.InterfaceInfo.InterfaceInfoEditRequest;
import com.Reflux.ReApi.model.request.InterfaceInfo.InterfaceInfoInvokeRequest;
import com.Reflux.ReApi.model.request.InterfaceInfo.InterfaceInfoQueryRequest;
import com.Reflux.ReApi.model.entity.InterfaceInfo;
import com.Reflux.ReApi.model.vo.InterfaceInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


public interface InterfaceInfoService extends IService<InterfaceInfo> {
     void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);
     QueryWrapper<InterfaceInfo> getQueryWrapper(InterfaceInfoQueryRequest interfaceInfoQueryRequest);



    /**
     * 获取接口信息封装
     *
     * @param interfaceInfo
     * @return
     */
    InterfaceInfoVO getInterfaceInfoVO(InterfaceInfo interfaceInfo);

    /**
     * 分页获取接口信息封装
     *
     * @param interfaceInfoPage
     * @param request
     * @return
     */
    Page<InterfaceInfoVO> getInterfaceInfoVOPage(Page<InterfaceInfo> interfaceInfoPage, HttpServletRequest request);

    /**
     * 修改接口信息
     *
     * @param interfaceInfoUpdateRequest 接口信息修改请求
     * @return 是否成功
     */
    boolean updateInterfaceInfo(InterfaceInfoEditRequest interfaceInfoUpdateRequest);

    /**
     * 根据用户ID 分页获取接口信息封装
     *
     * @param interfaceInfoPage 接口信息分页
     * @param request           当前会话
     * @return 接口信息分页
     */
    Page<InterfaceInfoVO> getInterfaceInfoVOByUserIdPage(Page<InterfaceInfo> interfaceInfoPage, HttpServletRequest request);

    /**
     * 使用事务，删除接口和对应接口关系表
     * @param id
     * @return
     */
    boolean removeByIdTranslator(long id);

    /**
     * 使用事务，批量删除接口，以及对应接口关系表
     * @param ids
     * @return
     */
    boolean removeByIdsTranslator(List<Long> ids);

    /**
     * 创建接口，创建者直接开通该接口
     * @param interfaceInfo
     * @return
     */
    boolean addInterface(InterfaceInfo interfaceInfo);

    String getInvokeResult(InterfaceInfoInvokeRequest interfaceInfoInvokeRequest, HttpServletRequest request, InterfaceInfo oldInterfaceInfo);

    boolean offlineInterface(long id);
}
