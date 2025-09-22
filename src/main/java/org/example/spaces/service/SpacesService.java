package org.example.spaces.service;

import org.example.spaces.model.FileMetadata;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;


/*
* 定义文件存储相关操作*/
public interface SpacesService {
    /*
    * 上传文件
    * @param file 要上传的文件
    * @param folder 目标文件夹（可选）
    * @return 文件元数据
    * */
    FileMetadata uploadFile(MultipartFile file, String folder);

    /*
     * 下载文件
     * @param key 文件键
     * @return 文件输入流
     * */
    InputStream downloadFile(String key);

    /*
     * 获取文件元数据
     * @param key 文件键
     * @return 文件元数据
     * */
    FileMetadata getFileMetadata(String key);

    /*
     * 列出所有文件
     * @param prefix 前缀过滤（可选）
     * @return 文件元数据列表
     * */
    List<FileMetadata> listFiles(String prefix);

    /*
     * 删除文件
     * @param key 文件键
     * @return 是否删除成功
     * */
    boolean deleteFile(String key);

    /*
     * 检查文件是否存在
     * @param key 文件键
     * @return 是否存在
     * */
    boolean fileExists(String key);

    /*
     * 生产文件的公共URL
     * @param key 文件键
     * @return 公共访问URL
     * */
    String generatePublicUrl(String key);
}
