package com.wisrc.template;

import com.wisrc.entity.ColumnRelation;
import com.wisrc.entity.ExcelTemplateResult;
import com.wisrc.entity.SubTable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import org.thymeleaf.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Repository
public class GenOracleSQL {

    public String getSQLScript(ExcelTemplateResult template) {
        Resource resource = new ClassPathResource("templates/SQLTemplate.tpl");
        try {
            Path path = resource.getFile().toPath();
            byte[] tp = Files.readAllBytes(path);
            String temp = new String(tp);
            temp = temp.replaceFirst("%PROC_NAME%", template.getProcName());
            temp = temp.replaceFirst("%ARGUMENT%", template.getArgument());
            temp = temp.replaceFirst("%PROC_HEADER%", template.getProcHeader());
            temp = temp.replaceFirst("%PROC_FOOTER%", template.getProcFooter());
            temp = temp.replaceFirst("%PROC_VARIABLE%", template.getProcVariable());
            temp = temp.replaceFirst("%WHERE_CONDITION%", template.getWhereCondition());
            temp = temp.replaceFirst("%PROC_COMMENTS%", genProcComments(template.getProcComments()));
            temp = temp.replaceFirst("%TARGET_TABLE%", template.getTargetTable());
            temp = temp.replaceFirst("%MAIN_TABLE%", template.getMainTable().getTableName());
            temp = temp.replaceFirst("%MAIN_TABLE_ALIAS%", template.getMainTable().getTableAlias());
            temp = temp.replaceFirst("%TARGET_COLUMNS%", genTargetColumns(template.getColumnRelationsList()));
            temp = temp.replaceFirst("%EXPRESSION_COLUMNS%", genExpressionColumns(template.getColumnRelationsList()));
            temp = temp.replaceFirst("%SUB_TABLE_CONDITION%", genSubTable(template.getSubTablesList()));
            temp = temp.replaceFirst("%PROC_EXCEPTION%", template.getProcException());

            return temp;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    private String genProcComments(Map<String, String> map) {
        String comments = "\n";
        for (Map.Entry<String, String> e : map.entrySet()) {
            comments += "\t" + e.getKey() + "：\t" + e.getValue() + "\n";
        }
        return comments;
    }

    private String genTargetColumns(List<ColumnRelation> list) {
        String targetColumns = "\t";
        ColumnRelation c = list.get(0);
        String fc = c.getTargetColumn();
        if (fc.length() < 60) {
            fc = fc.concat(StringUtils.repeat(" ", 60 - fc.length()));
        }

        targetColumns += fc + "\t--" + c.getTargetComments();
        for (int i = 1; i < list.size(); i++) {
            String col = list.get(i).getTargetColumn();
            if (col.length() < 60) {
                col = col.concat(StringUtils.repeat(" ", 60 - col.length()));
            }
            targetColumns += "\n\t," + col + "\t--"
                    + list.get(i).getTargetComments().replaceAll("\n","");
        }
        return targetColumns;
    }

    private String genExpressionColumns(List<ColumnRelation> list) {
        String expColumns = "\t";
        ColumnRelation c = list.get(0);
        String exp = c.getExpression();
        exp = exp.replaceAll("\n","\n\t ");
        if (exp.length() < 60) {
            exp += StringUtils.repeat(" ",60 - exp.length());
        }
        expColumns += exp + "\t as " + c.getTargetColumn() + "\t--" + c.getExpressionComments();
        for (int i = 1; i < list.size(); i++) {
            exp = list.get(i).getExpression().replaceAll("\n","\n\t ");

            if (exp.contains("\n")) {
                int idx = exp.lastIndexOf("\n");
                if (exp.length() - idx < 60) {
                    exp += StringUtils.repeat(" ",60 - exp.length() + idx + 2);
                }
            }else {
                if (exp.length() < 60){
                    exp += StringUtils.repeat(" ",60 - exp.length());
                }
            }
            expColumns += "\n\t," + exp + "\t as " + list.get(i).getTargetColumn()
                    + "\t--" + list.get(i).getExpressionComments().replaceAll("\n","");
        }
        return expColumns;
    }

    private String genSubTable(List<SubTable> list) {
        String subTable = "\t";
        if (list.size() > 0) {
            SubTable first = list.get(0);
            subTable += first.getJoinType() + "  " + first.getTableName() + "  " + first.getTableAlias() + "  " + first.getCondition();

            for (int i = 1; i < list.size(); i++) {
                SubTable s = list.get(i);
                subTable += "\n\t" + s.getJoinType() + "  " + s.getTableName() + "  " + s.getTableAlias() + "  " + s.getCondition();
            }
        }
        return subTable;
    }
}
