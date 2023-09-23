package com.Reflux.ReApi;

import cn.hutool.core.lang.UUID;
import com.Reflux.ReApi.config.WxOpenConfig;
import javax.annotation.Resource;

import com.Reflux.ReApi.service.InnerUserInterfaceInfoService;
import io.jsonwebtoken.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

/**
 * 主类测试
 *
 * @author Aaron
 */
@SpringBootTest
class MainApplicationTests {
    @Test
    public void jwt() {
        JwtBuilder jwtBuilder = Jwts.builder();
        String jwtToken = jwtBuilder
                // header
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                // payload
                .claim("username", "Aaron")
                .claim("role", "admin")
                .setSubject("jwtTest")
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .setId(UUID.randomUUID().toString())
                // signature
                .signWith(SignatureAlgorithm.HS256, "admin")
                .compact();
        System.out.println(jwtToken);
// eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.
// eyJ1c2VybmFtZSI6IkFhcm9uIiwicm9sZSI6ImFkbWluIiwic3ViIjoiand0VGVzdCIsImV4cCI6MTY5MjQ1Mzg5NCwianRpIjoiMTZiM2YwZDEtNDE2Mi00OWI5LTk5MDgtODJmM2QzNjIyN2JlIn0.
// nLgH_pDD0jGCtSnvj3LK0xEc1DH76jmb4CnwNLbrloY
    }

    @Test
    public void parseJWT() {
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6IkFhcm9uIiwicm9sZSI6ImFkbWluIiwic3ViIjoiand0VGVzdCIsImV4cCI6MTY5MjQ1Mzg5NCwianRpIjoiMTZiM2YwZDEtNDE2Mi00OWI5LTk5MDgtODJmM2QzNjIyN2JlIn0.nLgH_pDD0jGCtSnvj3LK0xEc1DH76jmb4CnwNLbrloY";
        JwtParser jwtParser = Jwts.parser();
        // 通过签名对token进行解析
        Jws<Claims> claims = jwtParser.setSigningKey("admin").parseClaimsJws(token);
        Claims body = claims.getBody();
        System.out.println(body.get("username"));
        System.out.println(body.get("role"));
        System.out.println(body.getId());
        System.out.println(body.getSubject());
    }

}
