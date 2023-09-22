package com.Reflux.ReApi.utils;

import lombok.Data;

@Data
public class JWTRespond {
    private String token;//设置token
    private Object LoginUserData;//把LoginUser传进data，就不用侵入源代码了
}
