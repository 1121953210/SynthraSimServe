package com.synthrasim.generator.controller;

import com.synthrasim.common.annotation.RequiresPermissions;
import com.synthrasim.common.core.domain.AjaxResult;
import com.synthrasim.generator.service.GenTableServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 代码生成控制器
 * 
 * 提供代码自动生成功能的接口：
 * 1. 查询数据库所有表
 * 2. 查询表的字段信息
 * 3. 预览生成的代码
 * 4. 下载生成的代码（ZIP压缩包）
 */
@Api(tags = "代码生成")
@RestController
@RequestMapping("/tool/gen")
public class GenController {

    @Autowired
    private GenTableServiceImpl genTableService;

    /**
     * 查询数据库中的所有表
     * 用于代码生成页面展示可选择的表列表
     */
    @ApiOperation("查询数据库表列表")
    @GetMapping("/db/list")
    public AjaxResult dbTableList() {
        List<Map<String, Object>> tables = genTableService.selectDbTableList();
        return AjaxResult.success(tables);
    }

    /**
     * 查询指定表的字段信息
     */
    @ApiOperation("查询表字段信息")
    @GetMapping("/column/{tableName}")
    public AjaxResult columnList(@PathVariable String tableName) {
        return AjaxResult.success(genTableService.selectDbColumnsByTableName(tableName));
    }

    /**
     * 预览代码
     * 返回各文件的生成内容（不下载，前端展示预览）
     */
    @ApiOperation("预览代码")
    @GetMapping("/preview/{tableName}")
    public AjaxResult preview(@PathVariable String tableName) {
        Map<String, String> codeMap = genTableService.previewCode(tableName);
        return AjaxResult.success(codeMap);
    }

    /**
     * 生成代码并下载ZIP
     */
    @ApiOperation("生成代码下载")
    @GetMapping("/download/{tableName}")
    public void download(HttpServletResponse response,
                         @PathVariable String tableName,
                         @RequestParam(required = false) String packageName,
                         @RequestParam(required = false) String moduleName,
                         @RequestParam(required = false) String author) throws IOException {
        byte[] data = genTableService.generateCode(tableName, packageName, moduleName, author);
        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=\"" + tableName + "_code.zip\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");
        response.getOutputStream().write(data);
    }
}
