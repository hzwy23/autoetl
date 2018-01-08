package com.wisrc.excel;

import com.wisrc.entity.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope("prototype")
public class ParseXLSXTemplate {

    private final Logger logger = LoggerFactory.getLogger(ParseXLSXTemplate.class);

    private ExcelTemplateResult excelTemplateResult;

    public ParseXLSXTemplate(){
        this.excelTemplateResult = new ExcelTemplateResult();
    }

    public ExcelTemplateResult parse(XSSFSheet sheet) {

        XSSFRow r = sheet.getRow(0);
        if (!checkHeader(r.getCell(0).toString())) {
            logger.error("ETL 模板的文件头(第一行)不正确，请使用ETL Template编写数据映射规则");
            return null;
        }

        // 获取程序名称
        boolean flag = parseProcName(sheet.getRow(1));
        if (!flag) {
            return null;
        }

        parseArgument(sheet.getRow(2));

        // 获取目标表名称
        flag = parseTargetTable(sheet.getRow(3));
        if (!flag) {
            return null;
        }

        parseProcHeader(sheet);
        parseProcVariable(sheet);
        parseProcFooter(sheet);
        parseProcException(sheet);
        // 获取程序注释信息
        parseProcComments(sheet);

        // 获取主表信息
        flag = parseMainTable(sheet);
        if (!flag) {
            return null;
        }

        flag = parseSubTable(sheet);
        if (!flag) {
            return null;
        }

        parseWhereCondition(sheet);

        flag = parseColumnRelation(sheet);
        if (!flag) {
            return null;
        }
        return this.excelTemplateResult;
    }

    public ExcelTemplateResult getExcelTemplateResult() {
        return excelTemplateResult;
    }

    private void parseProcException(String procException) {
        excelTemplateResult.setProcException(procException);
    }

    private void parseProcHeader(String procHeader) {
        excelTemplateResult.setProcHeader(procHeader);
    }

    private void parseProcFooter(String procFooter) {
        excelTemplateResult.setProcFooter(procFooter);
    }

    private boolean checkHeader(String headName) {
        return ExcelTemplateFlag.HEADER_NAME.equals(headName);
    }

    private boolean parseProcName(XSSFRow row) {
        String flag = row.getCell(0).toString();
        if (ExcelTemplateFlag.PROC_NAME.equals(flag)) {
            String name = row.getCell(1).toString();
            if (name == null || "".equals(name)) {
                logger.error("程序名称不能为空");
                return false;
            }
            excelTemplateResult.setProcName(name);
            return true;
        }
        logger.error("第二行，第一个单元格名称应该是:{}。实际值是：{}，请检查ETL模板", ExcelTemplateFlag.PROC_NAME, flag);
        return false;
    }

    private void parseProcHeader(XSSFSheet sheet){
        Iterator<Row> iterator = sheet.rowIterator();
        while (iterator.hasNext()) {
            XSSFRow row = (XSSFRow) iterator.next();
            if (ExcelTemplateFlag.PROC_HEADER.equals(row.getCell(0).toString())) {

                excelTemplateResult.setProcHeader(row.getCell(1).toString());
                return ;
            }
        }
    }

    private void parseProcFooter(XSSFSheet sheet){
        Iterator<Row> iterator = sheet.rowIterator();
        while (iterator.hasNext()) {
            XSSFRow row = (XSSFRow) iterator.next();
            if (ExcelTemplateFlag.PROC_FOOTER.equals(row.getCell(0).toString())) {
                excelTemplateResult.setProcFooter(row.getCell(1).toString());
                return ;
            }
        }
    }

    private void parseProcVariable(XSSFSheet sheet){
        Iterator<Row> iterator = sheet.rowIterator();
        while (iterator.hasNext()) {
            XSSFRow row = (XSSFRow) iterator.next();
            if (ExcelTemplateFlag.PROC_VARIABLE.equals(row.getCell(0).toString())) {
                String mvar =  row.getCell(1).toString().trim();
                // 如果没有定义变量，则直接推出变量处理过程
                if(mvar.isEmpty()){
                    excelTemplateResult.setProcVariable("");
                    return;
                }
                // 如果变量没有以分号结尾，则追加分号
                if (!mvar.endsWith(";")) {
                    mvar += ";";
                }

                mvar = "\t" + mvar.replaceAll("\n","");
                mvar = mvar.replaceAll(";",";\n\t");
                excelTemplateResult.setProcVariable(mvar);
                return ;
            }
        }
    }


    private void parseProcException(XSSFSheet sheet){
        Iterator<Row> iterator = sheet.rowIterator();
        while (iterator.hasNext()) {
            XSSFRow row = (XSSFRow) iterator.next();
            if (ExcelTemplateFlag.EXCEPTION_HANDLE_NAME.equals(row.getCell(0).toString())) {
                String exception = row.getCell(1).toString();
                if (exception != null && !exception.isEmpty()) {
                    String ret = "Exception\n"+exception.replaceAll("\n","\n\t");
                    excelTemplateResult.setProcException(ret);
                } else {
                    excelTemplateResult.setProcException("-- no exception handle");
                }
                return;
            }
        }
    }

    private boolean parseTargetTable(XSSFRow row) {
        String flag = row.getCell(0).toString();
        if (ExcelTemplateFlag.TARGET_NAME.equals(flag)) {
            String name = row.getCell(1).toString();
            if (name == null || name.isEmpty()) {
                logger.error("目标表不能为空");
                return false;
            } else if (name.endsWith(".0")) {
                name = name.substring(0,name.length()-2);
            }
            excelTemplateResult.setTargetTable(name);
            return true;
        }
        logger.error("第三行，第一个单元格名称应该是:{}。实际值是：{}，请检查ETL模板", ExcelTemplateFlag.TARGET_NAME, flag);
        return false;
    }

    private void parseArgument(XSSFRow row) {
        String argument = "";
        String flag = row.getCell(0).toString();
        if (ExcelTemplateFlag.ARGUMENT_NAME.equals(flag)) {
            String temp = row.getCell(1).toString();
            // 去掉换行符
            temp = temp.replaceAll("\n","");
            String[] tlist = temp.split(",");
            if (tlist.length > 0) {
                argument = "\t"+tlist[0].trim();
                for (int i = 1; i < tlist.length; i++) {
                    argument += "\n\t," +tlist[i].trim();
                }
            }
        }
        excelTemplateResult.setArgument(argument);
    }

    private boolean parseMainTable(XSSFSheet sheet) {
        Iterator<Row> iterator = sheet.rowIterator();
        while (iterator.hasNext()) {
            XSSFRow row = (XSSFRow) iterator.next();
            if (ExcelTemplateFlag.MAIN_TABLE_NAME.equals(row.getCell(0).toString())) {
                excelTemplateResult.setMainTable(new MainTable(row.getCell(1).toString(),
                        row.getCell(7).toString()));
                return true;
            }
        }
        return false;
    }

    private boolean parseSubTable(XSSFSheet sheet) {
        List<SubTable> subTablesList = new ArrayList<>();
        Iterator<Row> iterator = sheet.rowIterator();
        while (iterator.hasNext()) {
            XSSFRow row = (XSSFRow) iterator.next();
            if (ExcelTemplateFlag.SUB_TABLE_NAME.equals(row.getCell(0).toString())) {

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
            if (ExcelTemplateFlag.FILTER_WHERE.equals(row.getCell(0).toString())) {
                excelTemplateResult.setSubTablesList(subTablesList);
                return true;
            }
        }
        return false;
    }

    private boolean parseColumnRelation(XSSFSheet sheet) {
        List<ColumnRelation> columnRelationsList = new ArrayList<>();

        int maxRow = sheet.getPhysicalNumberOfRows();
        int index = 3;
        for (; index < maxRow; index++) {
            XSSFRow row = sheet.getRow(index);
            if (ExcelTemplateFlag.ETL_MAP_START_NAME.equals(row.getCell(0).toString())) {
                index = index + 3;
                break;
            }
        }
        for (; index < maxRow; index++) {
            XSSFRow row = sheet.getRow(index);
            if (!ExcelTemplateFlag.PROC_FOOTER.equals(row.getCell(0).toString())) {
                String targetColumn = row.getCell(0).toString();
                String targetComments = row.getCell(1).toString();
                String expression = row.getCell(2).toString();
                if (expression.endsWith(".0")){
                    expression = expression.substring(0,expression.length()-2);
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
        excelTemplateResult.setColumnRelationsList(columnRelationsList);
        return true;
    }

    // 获取存储过程注释项
    private void parseProcComments(XSSFSheet sheet) {
        Map<String,String> procComments = new HashMap<>();
        int maxRow = sheet.getPhysicalNumberOfRows();
        for (int i = 4; i < maxRow; i++) {
            XSSFRow row = sheet.getRow(i);
            if (!ExcelTemplateFlag.MAIN_TABLE_NAME.equals(row.getCell(0).toString())) {
                String key = row.getCell(1).toString();
                if (key.endsWith(".0")) {
                    key = key.substring(0,key.length()-2);
                }

                String value = row.getCell(2).toString();
                if (value.endsWith(".0")){
                    value = value.substring(0,value.length()-2);
                }
                procComments.put(key,value);
            } else {
                break;
            }
        }
        excelTemplateResult.setProcComments(procComments);
    }

    private void parseWhereCondition(XSSFSheet sheet) {
        Iterator<Row> iterator = sheet.rowIterator();
        while (iterator.hasNext()) {
            XSSFRow row = (XSSFRow) iterator.next();
            if (ExcelTemplateFlag.FILTER_WHERE.equals(row.getCell(0).toString())) {
                excelTemplateResult.setWhereCondition(row.getCell(1).toString());
                return;
            }
        }
    }
}
