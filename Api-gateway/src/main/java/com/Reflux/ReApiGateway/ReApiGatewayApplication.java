package com.Reflux.ReApiGateway;

import com.Reflux.ReApi.TestDubboProvider.DemoService;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
@EnableDubbo
@Service
public class ReApiGatewayApplication {

    @DubboReference
    private DemoService demoService;

    public static void main(String[] args) {
        // 测试dubbo的接口调用
        ConfigurableApplicationContext context = SpringApplication.run(ReApiGatewayApplication.class, args);
        ReApiGatewayApplication application = context.getBean(ReApiGatewayApplication.class);
        String result = application.doSayHello("Dubbo");
        System.out.println("Hello:" + result);
    }

    public String doSayHello(String name) {
        return demoService.sayHello(name);
    }

}
