package com.wisrc.template;

import com.wisrc.entity.ExcelTemplateResult;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;


@Component
public class LoadExcel {

    private final Logger logger = LoggerFactory.getLogger(LoadExcel.class);

    @Autowired
    private ParseXLSX parseXLSX;

    @Autowired
    private GenOracleSQL genOracleSQL;

    /**
     * 读取xlsx格式的文件
     * 适用于office 2007以及以后的版本
     *
     * @param path 打开的文件
     */
    private String xlsx(Path path) throws Exception {

        File file = path.toFile();

        try {

            XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file));

            logger.info("读取第一个sheet页面");
            XSSFSheet sheet = workbook.getSheetAt(0);

            ExcelTemplateResult excelTemplateResult = parseXLSX.parse(sheet);

            logger.info("模板内容解析完成，开始生成SQL语句");

            return genOracleSQL.getSQLScript(excelTemplateResult);

        } catch (IOException e) {
            logger.error("读取模板信息失败，请检查模板地址，错误信息是：{}", e.getMessage());
            throw new Exception("<div style='text-align:center'><h1>读取模板信息失败，" +
                    "请检查模板地址，</h1><h1>错误信息是</h1><h3>"
                    + e.getMessage() + "</h3></div>");
        }
    }

    /**
     * 读取xls根式文件
     * 适用于office template 97 - 2003 版本
     *
     * @param path 文件
     */
    private void xls(Path path) {

        File file = path.toFile();

        try {

            HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(file));

            HSSFSheet sheet = workbook.getSheetAt(0);

            // 获取实际行数
            // System.out.println(sheet.getPhysicalNumberOfRows());

            Iterator<Row> rows = sheet.rowIterator();

            while (rows.hasNext()) {
                HSSFRow row = (HSSFRow) rows.next();
                Iterator<Cell> cells = row.cellIterator();
                while (cells.hasNext()) {
                    HSSFCell cell = (HSSFCell) cells.next();

                    System.out.print(cell.toString() + ",");
                }
                System.out.println("");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取csv文件
     * 适用于csv文件格式的数据文件
     *
     * @param path 文件路径
     */
    public void csv(Path path) {
        try {
            List<String> list = Files.readAllLines(path);
            for (String m : list) {
                System.out.println(m);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取指定路径的文件
     * 目前只支持3种格式的数据文件，分别是xlsx，xls，csv
     *
     * @param url 文件路径
     */
    public String load(String url) throws Exception {
        if (url.endsWith("xlsx")) {
            logger.info("开始解析Excel xlsx模板->: {}",url);
            return xlsx(Paths.get(url));
        } else {
            logger.info("不支持的数据文件格式，目前只支持数据文件类型：xlsx");
            throw new Exception("<div style='text-align:center'><h1>不支持的数据文件格式，目前只支持数据文件类型：xlsx</h1></div>");
        }
//        else if (url.endsWith("xls")) {
//            return xls(Paths.get(url));
//        } else if (url.endsWith("csv")) {
//            return csv(Paths.get(url));
//        }
    }
}
