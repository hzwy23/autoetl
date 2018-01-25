package com.wisrc.webapp.controller;

import com.wisrc.template.LoadExcel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@Scope("prototype")
public class Index {
    private final Logger logger = LoggerFactory.getLogger(Index.class);

    @Autowired
    private LoadExcel loadExcel;

    @RequestMapping(value = "/")
    public String index(HttpServletRequest request) {
        String url = request.getParameter("fileScript");
        logger.info("读取文件路径是->: {}", url);

        // 解析excel模板文件，生成字符串
        String scriptFile = null;
        try {
            scriptFile = loadExcel.load(url);
            logger.info("SQL存储过程生成完成，生成的程序路径是->: {}", url);
        } catch (Exception e) {
            return e.getMessage();
        }
        // outfile 生成脚本的名称，将模板名称后缀替换成.sql类型
        String outfile = url.replaceAll("xlsx$", "sql");
        Path path = Paths.get(outfile);

        try {
            if (!Files.exists(path)) {
                path = Files.createFile(path);
            }
            Files.write(path, scriptFile.getBytes());
        } catch (IOException e) {
            logger.error("创建输出文件失败：{}", e.getMessage());
            return "<div style='text-align:center'><h1>创建输出文件失败：失败原因是：</h1><h2>" + e.getMessage() + "</h2></div>";
        }
        return "<div style='text-align:center'><h1>测试模板，页面仅供参考</h1><h1>生成的sql文件路径是：</h1><h2>".concat(outfile).concat("</h2></div>");
    }
}
