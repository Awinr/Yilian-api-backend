package com.Reflux.Interface;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;
//如果引入的Knife4j的jar包较低，还需要添加@EnableSwagger2WebMvc注解
@EnableSwagger2WebMvc
@SpringBootApplication
public class RefluxInterfaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RefluxInterfaceApplication.class, args);
    }

}
