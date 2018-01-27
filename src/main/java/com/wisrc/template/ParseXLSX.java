package com.wisrc.template;

import com.wisrc.entity.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope("prototype")
public class ParseXLSX {

    private final Logger logger = LoggerFactory.getLogger(ParseXLSX.class);

    private ExcelTemplateResult indexResult = new ExcelTemplateResult();

    private Set<String> withViewSet = new HashSet<>();

    public ExcelTemplateResult parse(XSSFWorkbook workbook) throws Exception {

        logger.info("获取Excel映射文档主体sheet页面，sheet页名称是：index");
        XSSFSheet sheet = workbook.getSheet("index");

        logger.info("开始校验ETL模板头信息");
        XSSFRow r = sheet.getRow(0);
        if (r == null
                || !checkHeader(r.getCell(0).toString())) {
            logger.error("ETL 模板的文件头(第一行)不正确，请使用系统给定的模板编写数据映射规则");
            throw new Exception("ETL 模板的文件头(第一行)不正确，请使用系统给定的模板编写数据映射规则");
        }
        logger.info("模板头信息校验完成，success");

        // 获取程序名称
        boolean flag = parseProcName(sheet.getRow(1));
        if (!flag) {
            logger.error("模板中程序名称解析失败");
            throw new Exception("模板中程序名称解析失败");
        }

        // 获取存储过程参数信息
        parseArgument(sheet.getRow(2));

        // 获取目标表名称
        flag = parseTargetTable(sheet.getRow(3));
        if (!flag) {
            logger.error("读取模板中的表名失败");
            throw new Exception("读取模板中的表名失败");
        }

        // 获取存储过程头部预处理信息
        parseProcHeader(sheet);

        // 获取存储过程变量定义信息
        parseProcVariable(sheet);

        // 获取存储过程尾部处理信息
        parseProcFooter(sheet);

        // 获取存储过程异常处理部分信息
        parseProcException(sheet);

        // 获取程序注释信息
        parseProcComments(sheet);

        // 获取主表信息
        flag = parseMainTable(sheet,indexResult);
        if (!flag) {
            logger.error("获取主表名称失败");
            throw new Exception("获取主表名称失败");
        }
        String mainTableName = indexResult.getMainTable().getTableName();
        parseWithViews(workbook,mainTableName);


        // 获取存储过程子表信息
        flag = parseSubTable(sheet,indexResult);
        if (!flag) {
            logger.error("获取子表信息失败");
            throw new Exception("获取子表信息失败");
        }
        for (SubTable s : indexResult.getSubTablesList()){
            String tb = s.getTableName();
            parseWithViews(workbook,tb);
        }

        // 获取where过滤信息
        parseWhereCondition(sheet,indexResult);

        // 获取字段映射信息
        flag = parseColumnRelation(sheet,indexResult);
        if (!flag) {
            logger.error("解析字段新设失败");
            throw new Exception("解析字段新设失败");
        }
        return this.indexResult;
    }

    private void parseWithViews(XSSFWorkbook workbook,String mainTableName){
        if (withViewSet.contains(mainTableName)) {
            // 不能重复解析
            logger.info("{} 已经被解析过了，不能重复解析",mainTableName);
            return;
        }
        XSSFSheet mainSheet = workbook.getSheet(mainTableName);
        if(mainSheet != null){
            // 家产主表是否是with view临时视图
            logger.info("解析主表临时视图, 临时视图名称是：{}",mainTableName);
            WithView wv = new WithView();
            wv.setTargetTable(mainTableName);
            // 获取主表信息
            parseMainTable(mainSheet,wv);
            parseSubTable(mainSheet,wv);
            parseWhereCondition(mainSheet,wv);
            parseColumnRelation(mainSheet,wv);
            withViewSet.add(mainTableName);
            indexResult.getWithViewStack().add(wv);

            // 读取下一个主表是否为临时表
            mainTableName = wv.getMainTable().getTableName();
            parseWithViews(workbook,mainTableName);

            //读取子表是否是临时表
            for (SubTable s : wv.getSubTablesList()) {
                String tb = s.getTableName();
                parseWithViews(workbook,tb);
            }
        }
    }

    public ExcelTemplateResult getIndexResult() {
        return indexResult;
    }

    private void parseProcException(String procException) {
        indexResult.setProcException(procException);
    }

    private void parseProcHeader(String procHeader) {
        indexResult.setProcHeader(procHeader);
    }

    private void parseProcFooter(String procFooter) {
        indexResult.setProcFooter(procFooter);
    }

    private boolean checkHeader(String headName) {
        return TemplateLabel.HEADER_NAME.equals(headName);
    }

    private boolean parseProcName(XSSFRow row) {
        String flag = row.getCell(0).toString();
        if (TemplateLabel.PROC_NAME.equals(flag)) {
            String name = row.getCell(1).toString();
            if (name == null || "".equals(name)) {
                logger.error("程序名称不能为空");
                return false;
            }
            indexResult.setProcName(name);
            return true;
        }
        logger.error("第二行，第一个单元格名称应该是:{}。实际值是：{}，请检查ETL模板", TemplateLabel.PROC_NAME, flag);
        return false;
    }

    private void parseProcHeader(XSSFSheet sheet) {
        Iterator<Row> iterator = sheet.rowIterator();
        while (iterator.hasNext()) {
            XSSFRow row = (XSSFRow) iterator.next();
            if (TemplateLabel.PROC_HEADER.equals(row.getCell(0).toString())) {

                indexResult.setProcHeader(row.getCell(1).toString());
                return;
            }
        }
    }

    private void parseProcFooter(XSSFSheet sheet) {
        Iterator<Row> iterator = sheet.rowIterator();
        while (iterator.hasNext()) {
            XSSFRow row = (XSSFRow) iterator.next();
            if (TemplateLabel.PROC_FOOTER.equals(row.getCell(0).toString())) {
                indexResult.setProcFooter(row.getCell(1).toString());
                return;
            }
        }
    }

    private void parseProcVariable(XSSFSheet sheet) {
        Iterator<Row> iterator = sheet.rowIterator();
        while (iterator.hasNext()) {
            XSSFRow row = (XSSFRow) iterator.next();
            if (TemplateLabel.PROC_VARIABLE.equals(row.getCell(0).toString())) {
                String mvar = row.getCell(1).toString().trim();
                // 如果没有定义变量，则直接推出变量处理过程
                if (mvar.isEmpty()) {
                    indexResult.setProcVariable("");
                    return;
                }
                // 如果变量没有以分号结尾，则追加分号
                if (!mvar.endsWith(";")) {
                    mvar += ";";
                }

                mvar = "\t" + mvar.replaceAll("\n", "");
                mvar = mvar.replaceAll(";", ";\n\t");
                indexResult.setProcVariable(mvar);
                return;
            }
        }
    }


    private void parseProcException(XSSFSheet sheet) {
        Iterator<Row> iterator = sheet.rowIterator();
        while (iterator.hasNext()) {
            XSSFRow row = (XSSFRow) iterator.next();
            if (TemplateLabel.EXCEPTION_HANDLE_NAME.equals(row.getCell(0).toString())) {
                String exception = row.getCell(1).toString();
                if (exception != null && !exception.isEmpty()) {
                    String ret = "Exception\n" + exception.replaceAll("\n", "\n\t");
                    indexResult.setProcException(ret);
                } else {
                    indexResult.setProcException("-- no exception handle");
                }
                return;
            }
        }
    }

    private boolean parseTargetTable(XSSFRow row) {
        String flag = row.getCell(0).toString();
        if (TemplateLabel.TARGET_NAME.equals(flag)) {
            String name = row.getCell(1).toString();
            if (name == null || name.isEmpty()) {
                logger.error("目标表不能为空");
                return false;
            } else if (name.endsWith(".0")) {
                name = name.substring(0, name.length() - 2);
            }
            indexResult.setTargetTable(name);
            return true;
        }
        logger.error("第三行，第一个单元格名称应该是:{}。实际值是：{}，请检查ETL模板", TemplateLabel.TARGET_NAME, flag);
        return false;
    }

    private void parseArgument(XSSFRow row) {
        String argument = "";
        String flag = row.getCell(0).toString();
        if (TemplateLabel.ARGUMENT_NAME.equals(flag)) {
            String temp = row.getCell(1).toString();
            // 去掉换行符
            temp = temp.replaceAll("\n", "");
            String[] tlist = temp.split(",");
            if (tlist.length > 0) {
                argument = "\t" + tlist[0].trim();
                for (int i = 1; i < tlist.length; i++) {
                    argument += "\n\t," + tlist[i].trim();
                }
            }
        }
        indexResult.setArgument(argument);
    }

    private boolean parseMainTable(XSSFSheet sheet,WithView result) {
        Iterator<Row> iterator = sheet.rowIterator();
        while (iterator.hasNext()) {
            XSSFRow row = (XSSFRow) iterator.next();
            if (TemplateLabel.MAIN_TABLE_NAME.equals(row.getCell(0).toString())) {
                result.setMainTable(new MainTable(row.getCell(1).toString(),
                        row.getCell(7).toString()));
                return true;
            }
        }
        return false;
    }

    private boolean parseSubTable(XSSFSheet sheet, WithView result) {
        List<SubTable> subTablesList = new ArrayList<>();
        Iterator<Row> iterator = sheet.rowIterator();
        while (iterator.hasNext()) {
            XSSFRow row = (XSSFRow) iterator.next();
            if (TemplateLabel.SUB_TABLE_NAME.equals(row.getCell(0).toString())) {

                String tableName = row.getCell(1).toString();
                String tableAlias = row.getCell(3).toString();
                String joinType = row.getCell(5).toString();
                String condition = row.getCell(7).toString();

                if (tableAlias.isEmpty()
                        || tableName.isEmpty()
                        || joinType.isEmpty()
                        || condition.isEmpty()) {
                    continue;
                }

                SubTable subTable = new SubTable(
                        tableName, tableAlias, joinType, condition
                );
                subTablesList.add(subTable);
            }
            if (TemplateLabel.FILTER_WHERE.equals(row.getCell(0).toString())) {
                result.setSubTablesList(subTablesList);
                return true;
            }
        }
        return false;
    }

    private boolean parseColumnRelation(XSSFSheet sheet,WithView result) {
        List<ColumnRelation> columnRelationsList = new ArrayList<>();


        int maxRow = sheet.getPhysicalNumberOfRows();
        int index = 3;
        for (; index < maxRow; index++) {
            XSSFRow row = sheet.getRow(index);
            if (TemplateLabel.ETL_MAP_START_NAME.equals(row.getCell(0).toString())) {
                index = index + 3;
                break;
            }
        }

        for (; index < maxRow; index++) {
            XSSFRow row = sheet.getRow(index);
            XSSFCell col = row.getCell(0);
            if (col == null) {
                break;
            }
            if (!TemplateLabel.PROC_FOOTER.equals(col.toString())) {
                String targetColumn = row.getCell(0).toString();
                String targetComments = row.getCell(1).toString();
                String expression = row.getCell(2).toString();
                if (expression.endsWith(".0")) {
                    expression = expression.substring(0, expression.length() - 2);
                }
                String expressionComments = row.getCell(6).toString();
                ColumnRelation cr = new ColumnRelation(
                        targetColumn, targetComments, expression, expressionComments
                );
                columnRelationsList.add(cr);
            } else {
                break;
            }
        }

        if (columnRelationsList.size() == 0) {
            logger.error("没有配置映射关系，请检查ETL配置模板");
            return false;
        }
        result.setColumnRelationsList(columnRelationsList);
        return true;
    }

    // 获取存储过程注释项
    private void parseProcComments(XSSFSheet sheet) {
        List<Comments> procComments = new ArrayList<>();
        int maxRow = sheet.getPhysicalNumberOfRows();
        for (int i = 4; i < maxRow; i++) {
            XSSFRow row = sheet.getRow(i);
            if (!TemplateLabel.MAIN_TABLE_NAME.equals(row.getCell(0).toString())) {
                String key = row.getCell(1).toString();
                if (key.endsWith(".0")) {
                    key = key.substring(0, key.length() - 2);
                }

                String value = row.getCell(2).toString();
                if (value.endsWith(".0")) {
                    value = value.substring(0, value.length() - 2);
                }
                procComments.add(new Comments(key, value));
            } else {
                break;
            }
        }
        indexResult.setProcComments(procComments);
    }

    private void parseWhereCondition(XSSFSheet sheet, WithView result) {
        Iterator<Row> iterator = sheet.rowIterator();
        while (iterator.hasNext()) {
            XSSFRow row = (XSSFRow) iterator.next();
            if (TemplateLabel.FILTER_WHERE.equals(row.getCell(0).toString())) {
                result.setWhereCondition(row.getCell(1).toString());
                return;
            }
        }
    }
}
