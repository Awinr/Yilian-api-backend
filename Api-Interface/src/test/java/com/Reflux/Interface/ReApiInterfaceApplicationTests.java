package com.Reflux.Interface;

import com.Reflux.clientSdk.client.ReApiClient;
import com.Reflux.clientSdk.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * 测试类
 */
@SpringBootTest
class ReApiInterfaceApplicationTests {

    @Resource
    private ReApiClient reApiClient;

    /**
     * 当增加新的接口，可以直接通过本单元，来测试apiClient能否调用接口成功（先绕过admin和网关）
     */
    @Test
    void contextInterface() {
        String result1 = reApiClient.getNameByGet("Reflux");
        String result2 = reApiClient.getNameByPost("Aaron");
        User user = new User();
        user.setUsername("作_者 Aaron");
        String usernameByPost = reApiClient.getUsernameByPost(user);
        System.out.println(result1);
        System.out.println(result2);
        System.out.println(usernameByPost);
    }

}
