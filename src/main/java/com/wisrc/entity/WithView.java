package com.wisrc.entity;

import java.util.List;

public class WithView {
    // 目标表名称
    private String targetTable = "";
    // 主表
    private MainTable mainTable;
    // 子表信息
    private List<SubTable> subTablesList;
    // where条件
    private String whereCondition = "";
    // 字段映射关系
    private List<ColumnRelation> columnRelationsList;

    public String getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
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

    @Override
    public String toString() {
        return "WithView{" +
                "targetTable='" + targetTable + '\'' +
                ", mainTable=" + mainTable +
                ", subTablesList=" + subTablesList +
                ", whereCondition='" + whereCondition + '\'' +
                ", columnRelationsList=" + columnRelationsList +
                '}';
    }
}
