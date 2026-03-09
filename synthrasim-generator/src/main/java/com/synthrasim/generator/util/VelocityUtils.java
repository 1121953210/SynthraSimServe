package com.synthrasim.generator.util;

import com.synthrasim.generator.domain.GenTable;
import com.synthrasim.generator.domain.GenTableColumn;
import org.apache.velocity.VelocityContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Velocity模板工具类
 * 
 * 为Velocity模板引擎准备渲染上下文数据（变量绑定）。
 * 模板中通过 $!{tableName}、$!{columns} 等变量获取表和列的信息。
 */
public class VelocityUtils {

    /** Java基础类型，不需要import */
    private static final Set<String> JAVA_BASE_TYPES = new HashSet<>();
    static {
        JAVA_BASE_TYPES.add("String");
        JAVA_BASE_TYPES.add("Integer");
        JAVA_BASE_TYPES.add("Long");
        JAVA_BASE_TYPES.add("Double");
        JAVA_BASE_TYPES.add("Float");
        JAVA_BASE_TYPES.add("Boolean");
    }

    /**
     * 构建模板渲染上下文
     */
    public static VelocityContext prepareContext(GenTable table) {
        VelocityContext context = new VelocityContext();

        context.put("tableName", table.getTableName());
        context.put("tableComment", table.getTableComment());
        context.put("className", table.getClassName());
        context.put("classname", uncapitalize(table.getClassName()));
        context.put("packageName", table.getPackageName());
        context.put("moduleName", table.getModuleName());
        context.put("businessName", table.getBusinessName());
        context.put("functionName", table.getFunctionName());
        context.put("author", table.getFunctionAuthor());
        context.put("columns", table.getColumns());

        // 收集需要import的Java类型
        Set<String> importList = new HashSet<>();
        if (table.getColumns() != null) {
            for (GenTableColumn column : table.getColumns()) {
                String javaType = column.getJavaType();
                if (javaType != null && javaType.contains(".")) {
                    importList.add(javaType);
                }
            }
        }
        context.put("importList", importList);

        // 获取主键列
        if (table.getColumns() != null) {
            for (GenTableColumn column : table.getColumns()) {
                if ("1".equals(column.getIsPk())) {
                    context.put("pkColumn", column);
                    break;
                }
            }
        }

        return context;
    }

    /**
     * 获取需要生成的模板文件列表
     */
    public static List<String> getTemplateList() {
        List<String> templates = new ArrayList<>();
        templates.add("templates/vm/java/domain.java.vm");
        templates.add("templates/vm/java/mapper.java.vm");
        templates.add("templates/vm/java/service.java.vm");
        templates.add("templates/vm/java/serviceImpl.java.vm");
        templates.add("templates/vm/java/controller.java.vm");
        templates.add("templates/vm/xml/mapper.xml.vm");
        return templates;
    }

    /**
     * 根据模板路径获取生成的文件名
     */
    public static String getFileName(String template, GenTable table) {
        String className = table.getClassName();
        String businessName = table.getBusinessName();
        String packagePath = table.getPackageName().replace(".", "/");

        if (template.contains("domain.java.vm")) {
            return packagePath + "/domain/" + className + ".java";
        } else if (template.contains("mapper.java.vm")) {
            return packagePath + "/mapper/" + className + "Mapper.java";
        } else if (template.contains("service.java.vm")) {
            return packagePath + "/service/I" + className + "Service.java";
        } else if (template.contains("serviceImpl.java.vm")) {
            return packagePath + "/service/impl/" + className + "ServiceImpl.java";
        } else if (template.contains("controller.java.vm")) {
            return packagePath + "/controller/" + className + "Controller.java";
        } else if (template.contains("mapper.xml.vm")) {
            return "mapper/" + table.getModuleName() + "/" + className + "Mapper.xml";
        }
        return null;
    }

    private static String uncapitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }
}
