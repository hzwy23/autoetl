package com.wisrc.template;

import com.wisrc.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Stack;

@Component
public class GenOracleSQL {

    private final Logger logger = LoggerFactory.getLogger(GenOracleSQL.class);

    public String getSQLScript(ExcelTemplateResult template) throws Exception {

        // 读取SQL模板
        String sqlTemplate = getSQLTemplate();
        logger.debug("开始解析SQL模板");
        // 解析SQL模板
        return sqlTemplate.replace("%PROC_NAME%", template.getProcName())
                .replace("%ARGUMENT%", template.getArgument())
                .replace("%PROC_HEADER%", template.getProcHeader())
                .replace("%PROC_FOOTER%", template.getProcFooter())
                .replace("%PROC_VARIABLE%", template.getProcVariable())
                .replace("%WHERE_CONDITION%", genWhereCond(template.getWhereCondition(),0))
                .replace("%PROC_COMMENTS%", genProcComments(template.getProcComments()))
                .replace("%TARGET_TABLE%", template.getTargetTable())
                .replace("%MAIN_TABLE%", template.getMainTable().getTableName())
                .replace("%MAIN_TABLE_ALIAS%", template.getMainTable().getTableAlias())
                .replace("%TARGET_COLUMNS%", genTargetColumns(template.getColumnRelationsList()))
                .replace("%EXPRESSION_COLUMNS%", genExpressionColumns(template.getColumnRelationsList()))
                .replace("%WITH_VIEWS%", genWithViews(template.getWithViewStack()))
                .replace("%SUB_TABLE_CONDITION%", genSubTable(template.getSubTablesList()))
                .replace("%PROC_EXCEPTION%", template.getProcException());
    }

    private String genWithViews(Stack<WithView> stack){
        StringBuffer strBuf = new StringBuffer("\nwith ");
        WithView wv = null;

        int i = 0;
        while (!stack.empty()){
            wv = stack.pop();
            if (i > 0) {
                strBuf.append(", ");
            }
            strBuf.append(wv.getTargetTable())
                    .append(" as (\n  select\n")
                    .append(genExpressionColumns(wv.getColumnRelationsList()))
                    .append("\n  from ")
                    .append(wv.getMainTable().getTableName())
                    .append(" ")
                    .append(wv.getMainTable().getTableAlias())
                    .append(genSubTable(wv.getSubTablesList()))
                    .append(genWhereCond(wv.getWhereCondition(),2))
                    .append("\n)");
            i++;
        }

        if (strBuf.toString().equals("\nwith ")) {
            return "";
        } else {
            return strBuf.toString();
        }
    }

    private String genWhereCond(String val,int tabSize){
        if (val == null || val.isEmpty()) {
            return "";
        } else {
            String empty = StringUtils.repeat(" ",tabSize);
            if (val.toUpperCase().startsWith("WHERE ")) {
                return "\n".concat(empty).concat(val);
            } else {
                return new StringBuffer("\n")
                        .append(empty)
                        .append("where ")
                        .append(val)
                        .toString();
            }
        }
    }

    private String getSQLTemplate() throws IOException {
        Resource resource = new ClassPathResource("templates/SQLTemplate.tpl");
        Path path = resource.getFile().toPath();
        byte[] tp = Files.readAllBytes(path);
        return new String(tp);
    }

    private String genProcComments(List<Comments> list) {
        StringBuffer comments = new StringBuffer("\n");
        for (Comments c : list) {
            String val = c.getValue().replace("\n", "\n\t\t\t\t\t  ");
            comments.append("\t" + c.getKey() + "：\t" + val + "\n");
        }
        return comments.toString();
    }

    private String genTargetColumns(List<ColumnRelation> list) {
        StringBuffer targetColumns = new StringBuffer("\t");
        ColumnRelation c = list.get(0);
        String fc = c.getTargetColumn();
        if (fc.length() < 60) {
            fc = fc.concat(StringUtils.repeat(" ", 60 - fc.length()));
        }

        targetColumns.append(fc + "\t--" + c.getTargetComments());
        for (int i = 1; i < list.size(); i++) {
            String col = list.get(i).getTargetColumn();
            if (col.length() < 60) {
                col = col.concat(StringUtils.repeat(" ", 60 - col.length()));
            }
            targetColumns.append("\n\t," + col + "\t--"
                    + list.get(i).getTargetComments().replaceAll("\n", ""));
        }
        return targetColumns.toString();
    }

    private String genExpressionColumns(List<ColumnRelation> list) {
        StringBuffer expColumns = new StringBuffer("\t");
        ColumnRelation c = list.get(0);
        String exp = c.getExpression();
        exp = exp.replaceAll("\n", "\n\t ");
        if (exp.length() < 60) {
            exp += StringUtils.repeat(" ", 60 - exp.length());
        }
        expColumns.append(exp + "\t as " + c.getTargetColumn() + "\t--" + c.getExpressionComments());
        for (int i = 1; i < list.size(); i++) {
            exp = list.get(i).getExpression().replaceAll("\n", "\n\t ");

            if (exp.contains("\n")) {
                int idx = exp.lastIndexOf("\n");
                if (exp.length() - idx < 60) {
                    exp += StringUtils.repeat(" ", 60 - exp.length() + idx + 2);
                }
            } else {
                if (exp.length() < 60) {
                    exp += StringUtils.repeat(" ", 60 - exp.length());
                }
            }

            expColumns.append("\n\t," + exp + "\t as " + list.get(i).getTargetColumn()
                    + "\t--" + list.get(i).getExpressionComments().replaceAll("\n", ""));
        }
        return expColumns.toString();
    }

    private String genSubTable(List<SubTable> list) {
        StringBuffer subTable = new StringBuffer("\n\t");
        if (list.size() > 0) {
            SubTable first = list.get(0);
            String cond = first.getCondition().trim();
            cond = cond.replaceAll("\n", "").replaceAll(" and ", "\n\t\tand ");
            subTable.append(first.getJoinType() + "  " + first.getTableName() + "  " + first.getTableAlias() + "\n\t\ton " + cond);

            for (int i = 1; i < list.size(); i++) {
                SubTable s = list.get(i);
                cond = s.getCondition().trim();
                cond = cond.replaceAll("\n", "").replaceAll(" and ", "\n\t\tand ");
                subTable.append("\n\t" + s.getJoinType() + "  " + s.getTableName() + "  " + s.getTableAlias() + "\n\t\ton " + cond);
            }
        }
        if (subTable.toString().equals("\n\t")) {
            return "";
        }else {
            return subTable.toString();
        }
    }
}
