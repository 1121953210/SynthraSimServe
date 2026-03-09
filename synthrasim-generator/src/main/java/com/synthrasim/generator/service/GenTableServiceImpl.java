package com.synthrasim.generator.service;

import com.synthrasim.common.exception.ServiceException;
import com.synthrasim.generator.domain.GenTable;
import com.synthrasim.generator.domain.GenTableColumn;
import com.synthrasim.generator.mapper.GenTableColumnMapper;
import com.synthrasim.generator.mapper.GenTableMapper;
import com.synthrasim.generator.util.GenUtils;
import com.synthrasim.generator.util.VelocityUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成业务服务
 * 
 * 核心功能：
 * 1. 查询数据库中的所有表信息
 * 2. 读取表的字段信息
 * 3. 使用Velocity模板引擎生成Entity/Mapper/Service/Controller代码
 * 4. 将生成的代码打包为ZIP下载
 */
@Service
public class GenTableServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(GenTableServiceImpl.class);

    @Autowired
    private GenTableMapper genTableMapper;

    @Autowired
    private GenTableColumnMapper genTableColumnMapper;

    /**
     * 查询数据库中的所有表
     */
    public List<Map<String, Object>> selectDbTableList() {
        return genTableMapper.selectDbTableList();
    }

    /**
     * 根据表名查询表的字段信息
     */
    public List<GenTableColumn> selectDbColumnsByTableName(String tableName) {
        List<Map<String, Object>> dbColumns = genTableColumnMapper.selectDbColumnsByTableName(tableName);
        List<GenTableColumn> columns = new ArrayList<>();
        for (Map<String, Object> dbColumn : dbColumns) {
            columns.add(GenUtils.initColumn(dbColumn));
        }
        return columns;
    }

    /**
     * 生成代码（返回ZIP字节数组，供前端下载）
     * 
     * @param tableName       表名
     * @param packageName     生成的包路径
     * @param moduleName      模块名
     * @param functionAuthor  作者
     * @return ZIP压缩包的字节数组
     */
    public byte[] generateCode(String tableName, String packageName, String moduleName, String functionAuthor) {
        // 1. 读取表信息
        Map<String, Object> tableInfo = genTableMapper.selectDbTableByName(tableName);
        if (tableInfo == null) {
            throw new ServiceException("表'" + tableName + "'不存在");
        }

        // 2. 构建GenTable对象
        GenTable table = new GenTable();
        table.setTableName(tableName);
        table.setTableComment((String) tableInfo.get("tableComment"));
        table.setClassName(GenUtils.tableNameToClassName(tableName));
        table.setPackageName(packageName != null ? packageName : "com.synthrasim.system");
        table.setModuleName(moduleName != null ? moduleName : "system");
        table.setBusinessName(getBusinessName(tableName));
        table.setFunctionName(table.getTableComment() != null ? table.getTableComment() : table.getClassName());
        table.setFunctionAuthor(functionAuthor != null ? functionAuthor : "SynthraSim");

        // 3. 读取字段信息
        table.setColumns(selectDbColumnsByTableName(tableName));

        // 4. 初始化Velocity
        initVelocity();

        // 5. 生成代码并打包为ZIP
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(outputStream)) {
            VelocityContext context = VelocityUtils.prepareContext(table);
            List<String> templates = VelocityUtils.getTemplateList();

            for (String templatePath : templates) {
                StringWriter sw = new StringWriter();
                Template tpl = Velocity.getTemplate(templatePath, "UTF-8");
                tpl.merge(context, sw);

                String fileName = VelocityUtils.getFileName(templatePath, table);
                if (fileName != null) {
                    zip.putNextEntry(new ZipEntry(fileName));
                    zip.write(sw.toString().getBytes(StandardCharsets.UTF_8));
                    zip.flush();
                    zip.closeEntry();
                }
            }
        } catch (IOException e) {
            log.error("代码生成失败", e);
            throw new ServiceException("代码生成失败：" + e.getMessage());
        }

        return outputStream.toByteArray();
    }

    /**
     * 预览代码（返回各文件的代码内容，不下载）
     */
    public Map<String, String> previewCode(String tableName) {
        Map<String, Object> tableInfo = genTableMapper.selectDbTableByName(tableName);
        if (tableInfo == null) {
            throw new ServiceException("表'" + tableName + "'不存在");
        }

        GenTable table = new GenTable();
        table.setTableName(tableName);
        table.setTableComment((String) tableInfo.get("tableComment"));
        table.setClassName(GenUtils.tableNameToClassName(tableName));
        table.setPackageName("com.synthrasim.system");
        table.setModuleName("system");
        table.setBusinessName(getBusinessName(tableName));
        table.setFunctionName(table.getTableComment() != null ? table.getTableComment() : table.getClassName());
        table.setFunctionAuthor("SynthraSim");
        table.setColumns(selectDbColumnsByTableName(tableName));

        initVelocity();

        Map<String, String> codeMap = new LinkedHashMap<>();
        VelocityContext context = VelocityUtils.prepareContext(table);
        List<String> templates = VelocityUtils.getTemplateList();
        for (String templatePath : templates) {
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(templatePath, "UTF-8");
            tpl.merge(context, sw);
            codeMap.put(templatePath, sw.toString());
        }
        return codeMap;
    }

    /** 初始化Velocity引擎 */
    private void initVelocity() {
        Properties p = new Properties();
        p.setProperty("resource.loaders", "class");
        p.setProperty("resource.loader.class.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        p.setProperty("input.encoding", "UTF-8");
        Velocity.init(p);
    }

    /** 从表名中提取业务名（去掉前缀后的最后一段） */
    private String getBusinessName(String tableName) {
        String name = tableName;
        String[] prefixes = {"sys_", "biz_", "gen_"};
        for (String prefix : prefixes) {
            if (name.startsWith(prefix)) {
                name = name.substring(prefix.length());
                break;
            }
        }
        int lastIndex = name.lastIndexOf("_");
        return lastIndex > 0 ? name.substring(lastIndex + 1) : name;
    }
}
