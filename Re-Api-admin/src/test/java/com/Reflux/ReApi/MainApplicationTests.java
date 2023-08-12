package com.Reflux.ReApi;

import com.Reflux.ReApi.config.WxOpenConfig;
import javax.annotation.Resource;

import com.Reflux.ReApi.service.InnerUserInterfaceInfoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 主类测试
 *
 * @author Reflux
 */
@SpringBootTest
class MainApplicationTests {

    @Resource
    private WxOpenConfig wxOpenConfig;
    @Resource
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    @Test
    void contextLoads() {
        System.out.println(wxOpenConfig);
    }

    @Test
    void invokeCount(){
        Boolean aBoolean = innerUserInterfaceInfoService.invokeCount(1L, 1L);
        Assertions.assertTrue(aBoolean);
    }

}
