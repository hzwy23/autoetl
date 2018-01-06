package com.wisrc.entity;

import org.springframework.stereotype.Repository;

@Repository
public class MainTable {
    private String tableName;
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
