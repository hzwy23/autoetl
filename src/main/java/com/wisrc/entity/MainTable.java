package com.wisrc.entity;

public class MainTable {
    // 主表名
    private String tableName;
    // 主表别名
    private String tableAlias;

    public MainTable(String tableName, String tableAlias) {
        this.tableAlias = tableAlias;
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public void setTableAlias(String tableAlias) {
        this.tableAlias = tableAlias;
    }
}
