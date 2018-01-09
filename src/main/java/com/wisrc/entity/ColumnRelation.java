package com.wisrc.entity;

public class ColumnRelation {
    // 目标字段
    private String targetColumn;
    // 目标字段描述
    private String targetComments;
    // 计算公式或字段
    private String expression;
    // 计算公式说明
    private String expressionComments;

    public ColumnRelation(String targetColumn,
                          String targetComments,
                          String expression,
                          String expressionComments) {
        this.targetColumn = targetColumn;
        this.targetComments = targetComments;
        this.expression = expression;
        this.expressionComments = expressionComments;
    }


    public String getTargetColumn() {
        return targetColumn;
    }

    public void setTargetColumn(String targetColumn) {
        this.targetColumn = targetColumn;
    }

    public String getTargetComments() {
        return targetComments;
    }

    public void setTargetComments(String targetComments) {
        this.targetComments = targetComments;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getExpressionComments() {
        return expressionComments;
    }

    public void setExpressionComments(String expressionComments) {
        this.expressionComments = expressionComments;
    }

    @Override
    public String toString() {
        return "ColumnRelation{" +
                "targetColumn='" + targetColumn + '\'' +
                ", targetComments='" + targetComments + '\'' +
                ", expression='" + expression + '\'' +
                ", expressionComments='" + expressionComments + '\'' +
                '}';
    }
}
