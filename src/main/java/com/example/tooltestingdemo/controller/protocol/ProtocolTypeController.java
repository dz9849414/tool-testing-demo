package com.example.tooltestingdemo.controller.protocol;

import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.entity.protocol.ProtocolType;
import com.example.tooltestingdemo.entity.template.TemplateFolder;
import com.example.tooltestingdemo.service.protocol.IProtocolTypeService;
import com.example.tooltestingdemo.service.template.TemplateFolderService;
import com.example.tooltestingdemo.vo.TemplateFolderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 协议类型主表 前端控制器
 * </p>
 *
 * @author wanggang
 * @since 2026-04-11
 */
@Slf4j
@RestController
@RequestMapping("api/protocol/protocolType")
@RequiredArgsConstructor
public class ProtocolTypeController {

    private final IProtocolTypeService protocolTypeService;


    /**
     * 新增协议类型
     *
     * 接口地址：POST /api/protocol/protocolType
     *
     */
    @PostMapping
    public Result<ProtocolType> createProtocolType(@RequestBody ProtocolType protocolType) {
        ProtocolType vo = protocolTypeService.createProtocolTyp(protocolType);
        return Result.success("创建成功", vo);
    }

    /**
     * 获取协议类型列表
     *
     * 接口地址：get /api/protocol/protocolType/list
     *
     */
    @GetMapping("/list")
    public Result<List<ProtocolType>> getProtocolTypeList(ProtocolType protocolType) {
        List<ProtocolType> protocolTypeList = protocolTypeService.getProtocolTypeList(protocolType);
        return Result.success(protocolTypeList);
    }

}
