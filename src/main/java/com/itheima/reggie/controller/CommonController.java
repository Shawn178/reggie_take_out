package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

// 通用类：主要做文件的上传、下载处理
@RestController
@Slf4j
// 因为前端传来的是common/upload.html
@RequestMapping("/common")
public class CommonController {

    // 声明一个静态变量，用于存放上传的文件
    @Value("${reggie.path}")
    private String basePath;

    /*
    一、文件上传的方法
     */
    @PostMapping("/upload")
    // 要接收上传的文件，需要添加MultipartFile参数, 这个参数名要和前端的name属性一致
    public R<String> upload(MultipartFile file){
        // 上传的file文件是一个在C盘的临时文件，需要转存到指定位置，否则本次请求结束即删除
        log.info("文件上传为：{}",file.toString());

        // 保证上传的是原始文件名
        String originalFilename = file.getOriginalFilename();
        // 文件原始的后缀类型也要动态截取，拼接到UUID后面
            // lastIndexOf(".") 是 Java String 的方法，表示“查找指定字符(串)在字符串中最后一次出现的位置”，返回的是索引下标（从 0 开始）。找不到则返回 -1。
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 但是使用UUID重新生成文件名，可以避免文件名重复
            // 将UUID与后缀拼接
        String fileName = UUID.randomUUID().toString() + suffix;


        // 判断要转存文件的目录是否存在
          // 创建一个目录对象(创建目标位置的目录对象)
        File dir = new File(basePath);
          // 判断目录是否存在
        if (!dir.exists()){
            // 不存在，直接创建目录
            dir.mkdirs();
        }

        // 指定转存文件的位置
        try {
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return R.success(fileName);
    }


    /*
    二、文件下载的方法

    不需要返回值，通过输出流向浏览器页面写回数据
    需要Response对象获取输出流
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){

        try {
            // 1.输入流：通过文件名获取文件输入流
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));

            // 2.输出流：通过输出流将文件写回浏览器，在浏览器展示图片
                // 通过Response响应对象获取输出流
            ServletOutputStream outputStream = response.getOutputStream();

            // 响应头设置：告诉浏览器这是一张图片
            response.setContentType("image/jpeg");

            // 3.将输入流的数据，全部写入输出流
                // 法一：使用 Apache Commons IO 的工具方法，把输入流的数据按内部缓冲循环拷贝到输出流，返回拷贝的字节数。
            IOUtils.copy(fileInputStream,outputStream);

                // 法二：每次读最多 1024 字节到缓冲区 bytes，read 返回本次实际读取的长度 len；当 len 为 -1 表示读完；每次把 bytes 数组中 [0, len) 部分写到输出流。
            /*int len = 0;
            byte[] bytes = new byte[1024];
            while((len = fileInputStream.read(bytes)) != -1){
                outputStream.write(bytes,0,len);
                outputStream.flush(); // 刷新缓冲区，确保数据及时写出
            }*/
            // 关闭资源
            outputStream.close();

            // 捕获异常对象
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
