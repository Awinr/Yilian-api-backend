package com.Reflux.clientSdk;


import com.Reflux.clientSdk.client.ReApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("reflux.client")
@Data
// 默认扫描位置是被标注的类所在的包及其子包 作用：扫描组件并加载到容器中
@ComponentScan
public class ReApiClientConfig {
    private String accessKey;

    private String secretKey;

    @Bean
    public ReApiClient ReApiClient() {
        return new ReApiClient(accessKey, secretKey);
    }

}
