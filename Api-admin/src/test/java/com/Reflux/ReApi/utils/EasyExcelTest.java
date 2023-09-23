package com.Reflux.ReApi.utils;

import cn.hutool.core.lang.UUID;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import io.jsonwebtoken.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * EasyExcel 测试
 *
 * @author Aaron
 */
@SpringBootTest
public class EasyExcelTest {

    @Test
    public void doImport() throws FileNotFoundException {
        File file = ResourceUtils.getFile("classpath:test_excel.xlsx");
        List<Map<Integer, String>> list = EasyExcel.read(file)
                .excelType(ExcelTypeEnum.XLSX)
                .sheet()
                .headRowNumber(0)
                .doReadSync();
        System.out.println(list);
    }

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
        System.out.println(body.getSubject());    }

}