package com.example.tooltestingdemo.service.impl.template;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.template.TemplateEnvironment;
import com.example.tooltestingdemo.mapper.template.TemplateEnvironmentMapper;
import com.example.tooltestingdemo.service.template.TemplateEnvironmentService;
import com.example.tooltestingdemo.util.TemplateConverter;
import com.example.tooltestingdemo.vo.TemplateEnvironmentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 模板环境配置 Service 实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateEnvironmentServiceImpl extends ServiceImpl<TemplateEnvironmentMapper, TemplateEnvironment> 
        implements TemplateEnvironmentService {

    private final TemplateEnvironmentMapper environmentMapper;

    @Override
    public TemplateEnvironmentVO getEnvironmentById(Long id) {
        return TemplateConverter.toVO(getById(id));
    }

    @Override
    public List<TemplateEnvironmentVO> getEnvironmentsByTemplateId(Long templateId) {
        return TemplateConverter.toEnvironmentVOList(environmentMapper.selectByTemplateId(templateId));
    }

    @Override
    public TemplateEnvironmentVO getDefaultEnvironment(Long templateId) {
        return TemplateConverter.toVO(environmentMapper.selectDefaultByTemplateId(templateId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateEnvironmentVO createEnvironment(TemplateEnvironment environment) {
        handleDefaultEnvironment(environment);
        save(environment);
        log.info("创建环境配置成功: id={}, name={}", environment.getId(), environment.getEnvName());
        return TemplateConverter.toVO(environment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateEnvironment(TemplateEnvironment environment) {
        handleDefaultEnvironment(environment);
        boolean result = updateById(environment);
        if (result) log.info("更新环境配置成功: id={}", environment.getId());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteEnvironment(Long id) {
        boolean result = removeById(id);
        if (result) log.info("删除环境配置成功: id={}", id);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultEnvironment(Long templateId, Long envId) {
        environmentMapper.clearDefaultByTemplateId(templateId);
        
        TemplateEnvironment env = new TemplateEnvironment();
        env.setId(envId);
        env.setIsDefault(1);
        boolean result = updateById(env);
        
        if (result) log.info("设置默认环境成功: templateId={}, envId={}", templateId, envId);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateEnvironmentVO cloneEnvironment(Long sourceEnvId, String newName) {
        TemplateEnvironment source = Optional.ofNullable(getById(sourceEnvId))
            .orElseThrow(() -> new RuntimeException("源环境配置不存在"));
        
        TemplateEnvironment clone = new TemplateEnvironment();
        BeanUtils.copyProperties(source, clone, "id", "isDefault");
        clone.setEnvName(newName);
        clone.setIsDefault(0);
        
        save(clone);
        log.info("克隆环境配置成功: newId={}, sourceId={}, name={}", clone.getId(), sourceEnvId, newName);
        return TemplateConverter.toVO(clone);
    }

    private void handleDefaultEnvironment(TemplateEnvironment environment) {
        if (Integer.valueOf(1).equals(environment.getIsDefault())) {
            environmentMapper.clearDefaultByTemplateId(environment.getTemplateId());
        }
    }
}
