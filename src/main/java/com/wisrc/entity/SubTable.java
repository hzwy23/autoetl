package com.wisrc.entity;

public class SubTable {
    // 子表名
    private String tableName;

    // 子表别名
    private String tableAlias;

    // 关联方式
    private String joinType;

    // 关联条件
    private String condition;

    public SubTable(String tableName, String tableAlias, String joinType, String condition) {
        this.tableName = tableName;
        this.tableAlias = tableAlias;
        this.joinType = joinType;
        this.condition = condition;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getJoinType() {
        return joinType;
    }

    public void setJoinType(String joinType) {
        this.joinType = joinType;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public void setTableAlias(String tableAlias) {
        this.tableAlias = tableAlias;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "SubTable{" +
                "tableName='" + tableName + '\'' +
                ", tableAlias='" + tableAlias + '\'' +
                ", joinType='" + joinType + '\'' +
                ", condition='" + condition + '\'' +
                '}';
    }
}
