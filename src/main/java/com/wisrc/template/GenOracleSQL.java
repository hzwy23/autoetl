package com.wisrc.template;

import com.wisrc.entity.ColumnRelation;
import com.wisrc.entity.Comments;
import com.wisrc.entity.ExcelTemplateResult;
import com.wisrc.entity.SubTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import org.thymeleaf.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Repository
public class GenOracleSQL {

    private final Logger logger = LoggerFactory.getLogger(GenOracleSQL.class);

    public String getSQLScript(ExcelTemplateResult template) throws Exception {
        Resource resource = new ClassPathResource("templates/SQLTemplate.tpl");
        try {
            Path path = resource.getFile().toPath();
            byte[] tp = Files.readAllBytes(path);
            String temp = new String(tp);
            return temp.replace("%PROC_NAME%", template.getProcName())
                    .replace("%ARGUMENT%", template.getArgument())
                    .replace("%PROC_HEADER%", template.getProcHeader())
                    .replace("%PROC_FOOTER%", template.getProcFooter())
                    .replace("%PROC_VARIABLE%", template.getProcVariable())
                    .replace("%WHERE_CONDITION%", template.getWhereCondition())
                    .replace("%PROC_COMMENTS%", genProcComments(template.getProcComments()))
                    .replace("%TARGET_TABLE%", template.getTargetTable())
                    .replace("%MAIN_TABLE%", template.getMainTable().getTableName())
                    .replace("%MAIN_TABLE_ALIAS%", template.getMainTable().getTableAlias())
                    .replace("%TARGET_COLUMNS%", genTargetColumns(template.getColumnRelationsList()))
                    .replace("%EXPRESSION_COLUMNS%", genExpressionColumns(template.getColumnRelationsList()))
                    .replace("%SUB_TABLE_CONDITION%", genSubTable(template.getSubTablesList()))
                    .replace("%PROC_EXCEPTION%", template.getProcException());

        } catch (IOException e) {
            logger.error("根据模板生成Oracle脚本失败，错误信息是：{}", e.getMessage());
            throw new Exception("根据模板生成Oracle脚本失败，错误信息是：".concat(e.getMessage()));
        }
    }

    private String genProcComments(List<Comments> list) {
        StringBuffer comments = new StringBuffer("\n");
        for (Comments c : list) {
            String val = c.getValue().replace("\n","\n\t\t\t\t\t  ");
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
        StringBuffer subTable = new StringBuffer("\t");
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
        return subTable.toString();
    }
}
