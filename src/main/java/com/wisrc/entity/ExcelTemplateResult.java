package com.wisrc.entity;

import java.util.List;
import java.util.Map;

public class ExcelTemplateResult {
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
    // 变量列表
    private String procVariable;
    // 程序头部
    private String procHeader;
    // 程序尾部
    private String procFooter;
    // 异常处理
    private String procException;


    public String getProcName() {
        return procName;
    }

    public void setProcName(String procName) {
        this.procName = procName;
    }

    public String getArgument() {
        return argument;
    }

    public void setArgument(String argument) {
        this.argument = argument;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    public Map<String, String> getProcComments() {
        return procComments;
    }

    public void setProcComments(Map<String, String> procComments) {
        this.procComments = procComments;
    }

    public MainTable getMainTable() {
        return mainTable;
    }

    public void setMainTable(MainTable mainTable) {
        this.mainTable = mainTable;
    }

    public List<SubTable> getSubTablesList() {
        return subTablesList;
    }

    public void setSubTablesList(List<SubTable> subTablesList) {
        this.subTablesList = subTablesList;
    }

    public String getWhereCondition() {
        return whereCondition;
    }

    public void setWhereCondition(String whereCondition) {
        this.whereCondition = whereCondition;
    }

    public List<ColumnRelation> getColumnRelationsList() {
        return columnRelationsList;
    }

    public void setColumnRelationsList(List<ColumnRelation> columnRelationsList) {
        this.columnRelationsList = columnRelationsList;
    }

    public String getProcVariable() {
        return procVariable;
    }

    public void setProcVariable(String procVariable) {
        this.procVariable = procVariable;
    }

    public String getProcHeader() {
        return procHeader;
    }

    public void setProcHeader(String procHeader) {
        this.procHeader = procHeader;
    }

    public String getProcFooter() {
        return procFooter;
    }

    public void setProcFooter(String procFooter) {
        this.procFooter = procFooter;
    }

    public String getProcException() {
        return procException;
    }

    public void setProcException(String procException) {
        this.procException = procException;
    }

    @Override
    public String toString() {
        return "ExcelTemplateResult{" +
                "procName='" + procName + '\'' +
                ", argument='" + argument + '\'' +
                ", targetTable='" + targetTable + '\'' +
                ", procComments=" + procComments +
                ", mainTable=" + mainTable +
                ", subTablesList=" + subTablesList +
                ", whereCondition='" + whereCondition + '\'' +
                ", columnRelationsList=" + columnRelationsList +
                ", procVariable='" + procVariable + '\'' +
                ", procHeader='" + procHeader + '\'' +
                ", procFooter='" + procFooter + '\'' +
                ", procException='" + procException + '\'' +
                '}';
    }
}
