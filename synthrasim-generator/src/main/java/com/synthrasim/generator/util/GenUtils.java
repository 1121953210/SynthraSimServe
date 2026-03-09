package com.synthrasim.generator.util;

import com.synthrasim.generator.domain.GenTableColumn;
import org.apache.commons.lang3.StringUtils;
import java.util.Arrays;
import java.util.Map;

/**
 * 代码生成工具类
 * 
 * 提供数据库类型到Java类型的映射、表名到类名的转换等方法。
 */
public class GenUtils {

    /**
     * 将数据库表名转换为Java类名
     * 规则：去掉表前缀 → 下划线转驼峰 → 首字母大写
     * 示例：biz_project → Project, sys_user → User, biz_model_template → ModelTemplate
     */
    public static String tableNameToClassName(String tableName) {
        // 去掉常见前缀
        String[] prefixes = {"sys_", "biz_", "gen_"};
        for (String prefix : prefixes) {
            if (tableName.startsWith(prefix)) {
                tableName = tableName.substring(prefix.length());
                break;
            }
        }
        return toCamelCase(tableName, true);
    }

    /**
     * 将数据库字段名转换为Java属性名
     * 规则：下划线转驼峰
     * 示例：project_name → projectName, is_deleted → isDeleted
     */
    public static String columnNameToJavaField(String columnName) {
        return toCamelCase(columnName, false);
    }

    /**
     * 将数据库字段类型映射为Java类型
     */
    public static String columnTypeToJavaType(String columnType) {
        if (columnType == null) return "String";
        String type = columnType.toLowerCase();
        if (type.contains("bigint")) return "Long";
        if (type.contains("int")) return "Integer";
        if (type.contains("decimal") || type.contains("numeric")) return "java.math.BigDecimal";
        if (type.contains("float") || type.contains("double")) return "Double";
        if (type.contains("datetime") || type.contains("timestamp")) return "java.util.Date";
        if (type.contains("date")) return "java.util.Date";
        if (type.contains("tinyint(1)")) return "Integer";
        if (type.contains("text") || type.contains("longtext")) return "String";
        if (type.contains("json")) return "String";
        return "String";
    }

    /**
     * 初始化列信息
     */
    public static GenTableColumn initColumn(Map<String, Object> column) {
        GenTableColumn col = new GenTableColumn();
        String columnName = (String) column.get("columnName");
        String columnType = (String) column.get("columnType");
        String columnComment = (String) column.get("columnComment");
        String columnKey = (String) column.get("columnKey");
        String extra = column.get("extra") != null ? column.get("extra").toString() : "";

        col.setColumnName(columnName);
        col.setColumnComment(columnComment);
        col.setColumnType(columnType);
        col.setJavaField(columnNameToJavaField(columnName));
        col.setJavaType(columnTypeToJavaType(columnType));
        col.setIsPk("PRI".equals(columnKey) ? "1" : "0");
        col.setIsIncrement(extra.contains("auto_increment") ? "1" : "0");
        col.setIsRequired("NO".equals(column.get("isNullable")) ? "1" : "0");
        col.setIsList("1");
        col.setIsQuery("0");
        col.setIsEdit("1");
        col.setQueryType("EQ");
        col.setHtmlType("input");

        if (col.getSort() == null && column.get("sort") != null) {
            col.setSort(Integer.parseInt(column.get("sort").toString()));
        }

        // 主键和公共字段不在编辑列表中
        String[] skipEditFields = {"id", "create_time", "update_time", "create_by", "update_by", "is_deleted"};
        if (Arrays.asList(skipEditFields).contains(columnName)) {
            col.setIsEdit("0");
            col.setIsList("0");
        }

        return col;
    }

    /**
     * 下划线命名转驼峰命名
     *
     * @param name            待转换的名称
     * @param capitalizeFirst 首字母是否大写（类名true，属性名false）
     */
    private static String toCamelCase(String name, boolean capitalizeFirst) {
        if (StringUtils.isEmpty(name)) return name;
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = capitalizeFirst;
        for (char c : name.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                result.append(capitalizeNext ? Character.toUpperCase(c) : c);
                capitalizeNext = false;
            }
        }
        return result.toString();
    }
}
