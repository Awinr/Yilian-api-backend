package com.Reflux.clientSdk.utils;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;

/**
 * 签名工具
 *
 */
public class SignUtils {
    /**
     * 生成签名
     * @param body 调用第三方接口所需要的该接口的参数
     * @param secretKey 密钥
     * @return
     */
    public static String genSign(String body, String secretKey) {
        Digester md5 = new Digester(DigestAlgorithm.SHA256);
        String content = body + "." + secretKey;
        return md5.digestHex(content);
    }
}
