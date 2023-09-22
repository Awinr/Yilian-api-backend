package com.Reflux.ReApi.mapper;


import com.Reflux.ReApi.model.entity.InterfaceInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Date;
import java.util.List;

/**
* @author Aaron
* @description 针对表【interface_info(接口信息)】的数据库操作Mapper
* @createDate 2023-07-21 16:18:19
* @Entity com.Reflux.ReApi.model.entity.InterfaceInfo
*/
public interface InterfaceInfoMapper extends BaseMapper<InterfaceInfo> {
    /**
     * 查询接口列表，排除已经删除的，接口开启的，
     * 要求：更新时间大于10天小于30天内的
     */
    List<InterfaceInfo> listInterfaceInfoWithDelete(Date tenDaysAgoDate);
}




