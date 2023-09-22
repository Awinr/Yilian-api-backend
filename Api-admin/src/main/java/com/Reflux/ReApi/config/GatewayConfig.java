package com.Reflux.ReApi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "reflux.gateway")
@Data
//配置网关地址
public class GatewayConfig {
    private String host;
}
