package com.wisrc.excel;

import com.wisrc.entity.ColumnRelation;
import com.wisrc.entity.MainTable;
import com.wisrc.entity.SubTable;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope("prototype")
public class ParseETLXLSXTemplate {

    private final String HEADER_NAME = "ETL过程配置(有色背景区域禁止修改)";
    private final String ARGUMENT_NAME = "参数列表";
    private final String PROC_NAME = "程序名";
    private final String TARGET_NAME = "目标表名";
    private final String MAIN_TABLE_NAME = "主表";
    private final String SUB_TABLE_NAME = "子表";
    private final String FILTER_WHERE = "过滤条件where";
    private final String ETL_MAP_START_NAME = "ETL字段映射(此行内容不允许修改，也不允许在模板中其它地方重复)";
    private final String EXCEPTION_HANDLE_NAME = "ETL异常处理";
    private final Logger logger = LoggerFactory.getLogger(ParseETLXLSXTemplate.class);

    // 存储过程名称
    private String procName;

    // 参数列表
    private String argument;

    // 目标表名称
    private String targetTable;

    // 注释信息
    private Map<String, String> procComments;

    // 主表
    private MainTable mainTable;

    // 子表信息
    private List<SubTable> subTablesList;
    // where条件
    private String whereCondition;
    // 字段映射关系
    private List<ColumnRelation> columnRelationsList;

    @Autowired
    private GenSQL genSQL;

    public boolean checkHeader(String headName) {
        return HEADER_NAME.equals(headName);
    }

    public boolean setProcName(XSSFRow row) {
        String flag = row.getCell(0).toString();
        if (PROC_NAME.equals(flag)) {
            String name = row.getCell(1).toString();
            if (name == null || "".equals(name)) {
                logger.error("程序名称不能为空");
                return false;
            }
            this.procName = name;
            return true;
        }
        logger.error("第二行，第一个单元格名称应该是:{}。实际值是：{}，请检查ETL模板", PROC_NAME, flag);
        return false;
    }

    public boolean setTargetTable(XSSFRow row) {
        String flag = row.getCell(0).toString();
        if (TARGET_NAME.equals(flag)) {
            String name = row.getCell(1).toString();
            if (name == null || "".equals(name)) {
                logger.error("目标表不能为空");
                return false;
            }
            this.targetTable = name;
            return true;
        }
        logger.error("第三行，第一个单元格名称应该是:{}。实际值是：{}，请检查ETL模板", TARGET_NAME, flag);
        return false;
    }

    public void setArgument(XSSFRow row) {
        String flag = row.getCell(0).toString();
        if (ARGUMENT_NAME.equals(flag)) {
            String temp = row.getCell(1).toString();
            // 去掉换行符
            temp = temp.replaceAll("\n","");
            String[] tlist = temp.split(",");
            if (tlist.length > 0) {
                this.argument = "\t"+tlist[0].trim();
                for (int i = 1; i < tlist.length; i++) {
                    this.argument += "\n\t," +tlist[i].trim();
                }
            }

        }
    }

    public boolean setMainTable(XSSFSheet sheet) {
        Iterator<Row> iterator = sheet.rowIterator();
        while (iterator.hasNext()) {
            XSSFRow row = (XSSFRow) iterator.next();
            if (MAIN_TABLE_NAME.equals(row.getCell(0).toString())) {
                this.mainTable = new MainTable(row.getCell(1).toString(),
                        row.getCell(7).toString());
                return true;
            }
        }
        return false;
    }

    public boolean setSubTable(XSSFSheet sheet) {
        this.subTablesList = new ArrayList<>();
        Iterator<Row> iterator = sheet.rowIterator();
        while (iterator.hasNext()) {
            XSSFRow row = (XSSFRow) iterator.next();
            if (SUB_TABLE_NAME.equals(row.getCell(0).toString())) {

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
                this.subTablesList.add(subTable);
            }
            if (FILTER_WHERE.equals(row.getCell(0).toString())) {
                return true;
            }
        }
        return false;
    }

    public boolean setColumnRelation(XSSFSheet sheet) {
        this.columnRelationsList = new ArrayList<>();

        int maxRow = sheet.getPhysicalNumberOfRows();
        int index = 3;
        for (; index < maxRow; index++) {
            XSSFRow row = sheet.getRow(index);
            if (ETL_MAP_START_NAME.equals(row.getCell(0).toString())) {
                index = index + 3;
                break;
            }
        }
        for (; index < maxRow; index++) {
            XSSFRow row = sheet.getRow(index);
            if (!EXCEPTION_HANDLE_NAME.equals(row.getCell(0).toString())) {
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
                this.columnRelationsList.add(cr);
            } else {
                break;
            }
        }
        if (this.columnRelationsList.size() == 0) {
            logger.error("没有配置映射关系，请检查ETL配置模板");
            return false;
        }
        return true;
    }

    public String parse(XSSFSheet sheet) {

        XSSFRow r = sheet.getRow(0);
        if (!checkHeader(r.getCell(0).toString())) {
            logger.error("ETL 模板的文件头(第一行)不正确，请使用ETL Template编写数据映射规则");
            return null;
        }

        // 获取程序名称
        boolean flag = setProcName(sheet.getRow(1));
        if (!flag) {
            return null;
        }

        setArgument(sheet.getRow(2));

        // 获取目标表名称
        flag = setTargetTable(sheet.getRow(3));
        if (!flag) {
            return null;
        }

        // 获取程序注释信息
        setProcComments(sheet);

        // 获取主表信息
        flag = setMainTable(sheet);
        if (!flag) {
            return null;
        }

        flag = setSubTable(sheet);
        if (!flag) {
            return null;
        }

        setWhereCondition(sheet);

        flag = setColumnRelation(sheet);
        if (!flag) {
            return null;
        }
        return genSQL.getSQLScript(this);
    }

    public String getProcName() {
        return procName;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public Map<String, String> getProcComments() {
        return procComments;
    }

    public void setProcComments(XSSFSheet sheet) {
        this.procComments = new HashMap<>();
        int maxRow = sheet.getPhysicalNumberOfRows();
        for (int i = 3; i < maxRow; i++) {
            XSSFRow row = sheet.getRow(i);
            if (!MAIN_TABLE_NAME.equals(row.getCell(0).toString())) {
                this.procComments.put(row.getCell(1).toString(), row.getCell(2).toString());
            } else {
                break;
            }
        }
    }

    public MainTable getMainTable() {
        return mainTable;
    }

    public List<SubTable> getSubTablesList() {
        return subTablesList;
    }

    public String getWhereCondition() {
        return whereCondition;
    }

    public void setWhereCondition(XSSFSheet sheet) {
        Iterator<Row> iterator = sheet.rowIterator();
        while (iterator.hasNext()) {
            XSSFRow row = (XSSFRow) iterator.next();
            if (FILTER_WHERE.equals(row.getCell(0).toString())) {
                this.whereCondition = row.getCell(1).toString();
                return;
            }
        }
    }

    public String getArgument() {
        return argument;
    }

    public List<ColumnRelation> getColumnRelationsList() {
        return columnRelationsList;
    }

    @Override
    public String toString() {
        return "ParseETLXLSXTemplate{" +
                "procName='" + procName + '\'' +
                ", targetTable='" + targetTable + '\'' +
                ", procComments=" + procComments +
                ", mainTable='" + mainTable + '\'' +
                ", subTablesList=" + subTablesList +
                ", whereCondition='" + whereCondition + '\'' +
                ", columnRelationsList=" + columnRelationsList +
                '}';
    }
}
