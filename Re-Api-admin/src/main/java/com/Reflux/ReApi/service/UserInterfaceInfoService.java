package com.Reflux.ReApi.service;


import com.Reflux.ReApi.model.request.UserInterfaceInfo.UserInterfaceInfoQueryRequest;
import com.Reflux.ReApi.model.entity.UserInterfaceInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Mr.Reflux
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service
* @createDate 2023-07-22 17:02:20
*/
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {
     void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add);
     QueryWrapper<UserInterfaceInfo> getQueryWrapper(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest);

    List<UserInterfaceInfo> listTopInvokeInterfaceInfo(int limit);

    Boolean addUserInterface(UserInterfaceInfo userInterfaceInfo);

}
