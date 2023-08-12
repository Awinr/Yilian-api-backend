package com.Reflux.ReApi.model.request.file;

import java.io.Serializable;
import lombok.Data;

/**
 * 文件上传请求
 *
 * @author Reflux
 */
@Data
public class UploadFileRequest implements Serializable {

    /**
     * 业务
     */
    private String biz;

    /**
     * 上传者的ID
     */
    private Long useId;

    private static final long serialVersionUID = 1L;
}