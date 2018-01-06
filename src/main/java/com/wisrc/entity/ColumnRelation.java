package com.wisrc.entity;

import org.springframework.stereotype.Repository;

@Repository
public class ColumnRelation {
    private String targetColumn;
    private String targetComments;
    private String expression;
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
