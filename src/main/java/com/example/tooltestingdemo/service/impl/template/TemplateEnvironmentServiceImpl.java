package com.example.tooltestingdemo.service.impl.template;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.template.TemplateEnvironment;
import com.example.tooltestingdemo.mapper.template.TemplateEnvironmentMapper;
import com.example.tooltestingdemo.service.template.TemplateEnvironmentService;
import com.example.tooltestingdemo.util.TemplateConverter;
import com.example.tooltestingdemo.vo.TemplateEnvironmentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 模板环境配置 Service 实现类
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/service/impl/template/TemplateEnvironmentServiceImpl.java
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateEnvironmentServiceImpl extends ServiceImpl<TemplateEnvironmentMapper, TemplateEnvironment> 
        implements TemplateEnvironmentService {

    @Override
    public TemplateEnvironmentVO getEnvironmentById(Long id) {
        TemplateEnvironment environment = getById(id);
        return TemplateConverter.toVO(environment);
    }

    @Override
    public List<TemplateEnvironmentVO> getEnvironmentsByTemplateId(Long templateId) {
        List<TemplateEnvironment> environments = baseMapper.selectByTemplateId(templateId);
        return TemplateConverter.toEnvironmentVOList(environments);
    }

    @Override
    public TemplateEnvironmentVO getDefaultEnvironment(Long templateId) {
        TemplateEnvironment environment = baseMapper.selectDefaultByTemplateId(templateId);
        return TemplateConverter.toVO(environment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateEnvironmentVO createEnvironment(TemplateEnvironment environment) {
        // 如果设置为默认环境，先取消其他默认环境
        if (Integer.valueOf(1).equals(environment.getIsDefault())) {
            baseMapper.clearDefaultByTemplateId(environment.getTemplateId());
        }
        
        save(environment);
        log.info("创建环境配置成功: id={}, name={}", environment.getId(), environment.getEnvName());
        return TemplateConverter.toVO(environment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateEnvironment(TemplateEnvironment environment) {
        // 如果设置为默认环境，先取消其他默认环境
        if (Integer.valueOf(1).equals(environment.getIsDefault())) {
            baseMapper.clearDefaultByTemplateId(environment.getTemplateId());
        }
        
        boolean result = updateById(environment);
        if (result) {
            log.info("更新环境配置成功: id={}", environment.getId());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteEnvironment(Long id) {
        boolean result = removeById(id);
        if (result) {
            log.info("删除环境配置成功: id={}", id);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultEnvironment(Long templateId, Long envId) {
        // 先取消所有默认环境
        baseMapper.clearDefaultByTemplateId(templateId);
        
        // 设置新的默认环境
        TemplateEnvironment environment = new TemplateEnvironment();
        environment.setId(envId);
        environment.setIsDefault(1);
        boolean result = updateById(environment);
        
        if (result) {
            log.info("设置默认环境成功: templateId={}, envId={}", templateId, envId);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateEnvironmentVO cloneEnvironment(Long sourceEnvId, String newName) {
        TemplateEnvironment source = getById(sourceEnvId);
        if (source == null) {
            throw new RuntimeException("源环境配置不存在");
        }
        
        TemplateEnvironment clone = new TemplateEnvironment();
        clone.setTemplateId(source.getTemplateId());
        clone.setEnvName(newName);
        clone.setEnvCode(source.getEnvCode());
        clone.setBaseUrl(source.getBaseUrl());
        clone.setHeaders(source.getHeaders());
        clone.setVariables(source.getVariables());
        clone.setAuthType(source.getAuthType());
        clone.setAuthConfig(source.getAuthConfig());
        clone.setProxyEnabled(source.getProxyEnabled());
        clone.setProxyHost(source.getProxyHost());
        clone.setProxyPort(source.getProxyPort());
        clone.setProxyUsername(source.getProxyUsername());
        clone.setProxyPassword(source.getProxyPassword());
        clone.setIsDefault(0); // 克隆的环境不是默认环境
        clone.setDescription(source.getDescription());
        
        save(clone);
        log.info("克隆环境配置成功: newId={}, sourceId={}, name={}", clone.getId(), sourceEnvId, newName);
        return TemplateConverter.toVO(clone);
    }
}
