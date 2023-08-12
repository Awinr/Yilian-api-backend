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
@ComponentScan
public class ReApiClientConfig {
    private String accessKey;

    private String secretKey;

    @Bean
    public ReApiClient ReApiClient() {
        return new ReApiClient(accessKey, secretKey);
    }

}
