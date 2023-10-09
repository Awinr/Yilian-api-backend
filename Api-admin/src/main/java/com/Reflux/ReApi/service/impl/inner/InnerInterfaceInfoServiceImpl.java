package com.Reflux.ReApi.service.impl.inner;


import com.Reflux.ReApi.model.entity.InterfaceInfo;
import com.Reflux.ReApi.service.InterfaceInfoService;
import com.Reflux.ReApi.service.InnerInterfaceInfoService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
* @author Aaron
*/
@DubboService
public class InnerInterfaceInfoServiceImpl implements InnerInterfaceInfoService {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Override
    public InterfaceInfo getInterfaceInfo(long id,String url, String method,String path) {
        return interfaceInfoService.lambdaQuery()
                // 看接口是否存在
                .eq(InterfaceInfo::getId, id)
                // 看接口参数是否正确，下面三个参数是为了调用第三方接口的（我们本地不存在的接口）
                .eq(InterfaceInfo::getUrl,url)
                .eq(InterfaceInfo::getMethod,method)
                .eq(InterfaceInfo::getPath,path)
                .one();
    }
}




