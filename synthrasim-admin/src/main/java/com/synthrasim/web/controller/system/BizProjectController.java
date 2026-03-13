package com.synthrasim.web.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.synthrasim.common.annotation.Log;
import com.synthrasim.common.annotation.RequiresPermissions;
import com.synthrasim.common.core.domain.AjaxResult;
import com.synthrasim.common.core.page.TableDataInfo;
import com.synthrasim.framework.security.service.LoginUser;
import com.synthrasim.system.domain.BizProject;
import com.synthrasim.system.service.IBizProjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 项目管理控制器
 *
 * 参照 database_design.md 的 3.1 项目表（biz_project）设计实现：
 * - 新增项目
 * - 修改项目
 * - 删除项目（逻辑删除）
 * - 分页查询项目列表
 * - 上传项目封面图（返回相对路径，存入 cover_image 字段）
 */
@Api(tags = "项目管理")
@RestController
@RequestMapping("/system/project")
public class BizProjectController {

    @Autowired
    private IBizProjectService projectService;

    /**
     * 分页查询项目列表
     */
    @ApiOperation("查询项目列表")
    @RequiresPermissions("system:project:list")
    @GetMapping("/list")
    public TableDataInfo list(BizProject project,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<BizProject> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizProject> wrapper = new LambdaQueryWrapper<BizProject>()
                .like(StringUtils.hasText(project.getProjectName()),
                        BizProject::getProjectName, project.getProjectName())
                .eq(project.getStatus() != null,
                        BizProject::getStatus, project.getStatus())
                .orderByDesc(BizProject::getUpdateTime);
        IPage<BizProject> result = projectService.page(page, wrapper);
        return new TableDataInfo(result.getRecords(), result.getTotal());
    }

    /**
     * 根据ID查询项目详情
     */
    @ApiOperation("查询项目详情")
    @RequiresPermissions("system:project:query")
    @GetMapping("/{projectId}")
    public AjaxResult getInfo(@PathVariable Long projectId) {
        BizProject project = projectService.getById(projectId);
        return AjaxResult.success(project);
    }

    /**
     * 新增项目
     */
    @ApiOperation("新增项目")
    @RequiresPermissions("system:project:add")
    @Log(title = "项目管理", businessType = "INSERT")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody BizProject project) {
        // 从当前认证信息中获取登录用户ID，并设置为项目所有者
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            if (loginUser.getUser() != null) {
                project.setOwnerId(loginUser.getUser().getId());
            }
        }
        project.setId(null);
        return projectService.save(project) ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     * 修改项目
     */
    @ApiOperation("修改项目")
    @RequiresPermissions("system:project:edit")
    @Log(title = "项目管理", businessType = "UPDATE")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody BizProject project) {
        // 更新时不允许修改 ownerId
        project.setOwnerId(null);
        return projectService.updateById(project) ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     * 删除项目（逻辑删除）
     */
    @ApiOperation("删除项目")
    @RequiresPermissions("system:project:remove")
    @Log(title = "项目管理", businessType = "DELETE")
    @DeleteMapping("/{projectIds}")
    public AjaxResult remove(@PathVariable Long[] projectIds) {
        for (Long projectId : projectIds) {
            projectService.removeById(projectId);
        }
        return AjaxResult.success();
    }

    /**
     * 上传项目封面图
     *
     * 前端调用步骤示例：
     * 1）调用本接口上传图片，拿到返回的相对路径 url（如 /uploads/project/xxxx.jpg）
     * 2）在新增/修改项目时，将该相对路径赋值给 BizProject.coverImage 一并提交
     */
    @ApiOperation("上传项目封面图")
    @RequiresPermissions("system:project:edit")
    @PostMapping("/uploadCover")
    public AjaxResult uploadCover(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return AjaxResult.error("上传文件不能为空");
        }

        // 上传根目录：{user.dir}/uploads/project/
        String userDir = System.getProperty("user.dir");
        Path uploadRoot = Paths.get(userDir, "uploads", "project");

        try {
            Files.createDirectories(uploadRoot);

            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
            }

            String filename = UUID.randomUUID().toString().replaceAll("-", "") + ext;
            Path targetPath = uploadRoot.resolve(filename);
            file.transferTo(targetPath.toFile());

            // 相对路径（供前端和 cover_image 字段使用）
            String relativePath = "/uploads/project/" + filename;

            AjaxResult result = AjaxResult.success();
            result.put("url", relativePath);
            return result;
        } catch (IOException e) {
            return AjaxResult.error("上传封面图失败：" + e.getMessage());
        }
    }
}

