package com.wisrc.webapp.controller;

import com.wisrc.excel.LoadExcel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class Index {
    private final Logger logger = LoggerFactory.getLogger(Index.class);

    @Autowired
    private LoadExcel loadExcel;

    @RequestMapping(value = "/")
    public String index(HttpServletRequest request) {
        String url = request.getParameter("fileScript");
        logger.info("读取文件路径是->: {}", url);
        String scriptFile = loadExcel.load(url);
        String outfile = url.replaceAll("xlsx$","sql");
        Path path = Paths.get(outfile);
        try {
            if (!Files.exists(path)) {
                path = Files.createFile(path);
            }
            Files.write(path, scriptFile.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "<div style='text-align:center'><h1>测试模板，页面仅供参考</h1><h1>生成的sql文件路径是：</h1><h2>".concat(outfile).concat("</h2></div>");
    }
}
