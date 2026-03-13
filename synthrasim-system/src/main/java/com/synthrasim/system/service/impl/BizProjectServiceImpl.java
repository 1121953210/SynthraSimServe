package com.synthrasim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.synthrasim.system.domain.BizProject;
import com.synthrasim.system.mapper.BizProjectMapper;
import com.synthrasim.system.service.IBizProjectService;
import org.springframework.stereotype.Service;

/**
 * 项目业务实现
 */
@Service
public class BizProjectServiceImpl extends ServiceImpl<BizProjectMapper, BizProject> implements IBizProjectService {
}

