package com.synthrasim.web.controller.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.synthrasim.common.core.domain.AjaxResult;
import com.synthrasim.common.core.page.TableDataInfo;
import com.synthrasim.framework.security.service.LoginUser;
import com.synthrasim.framework.security.service.TokenService;
import com.synthrasim.system.domain.SysLoginLog;
import com.synthrasim.system.service.ISysLoginLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 登录日志控制器
 * 
 * 对应原型中"用户信息-登录日志"页签，
 * 展示当前用户的登录/注销操作记录。
 */
@Api(tags = "登录日志")
@RestController
@RequestMapping("/system/loginLog")
public class SysLoginLogController {

    @Autowired
    private ISysLoginLogService loginLogService;

    @Autowired
    private TokenService tokenService;

    /**
     * 查询当前用户的登录日志（分页）
     * 按操作时间倒序排列
     */
    @ApiOperation("查询登录日志")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        LoginUser loginUser = tokenService.getLoginUser();
        IPage<SysLoginLog> page = loginLogService.selectLoginLogPage(
                loginUser.getUser().getId(), pageNum, pageSize);
        return new TableDataInfo(page.getRecords(), page.getTotal());
    }
}
