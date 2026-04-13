package com.example.tooltestingdemo.generator;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Property;
import com.baomidou.mybatisplus.generator.model.ClassAnnotationAttributes;
import lombok.Data;

import java.sql.Types;
import java.util.Collections;

public class MyFastAutoGenerator {
    public static final String URL = "jdbc:mysql://localhost:3306/tool_testing?useSSL=false&serverTimezone=Asia/Shanghai";
    public static final String UEERNAME = "root";
    public static final String PASSWORD = "123456";
    public static final String AUTHOR = "wanggang";
    public static final String OUTPUTDIR = "D://project/code";

    public static final String TABLENAME = "protocol_type";

    public static void main(String[] args) {
        // 使用 FastAutoGenerator 快速配置代码生成器
        FastAutoGenerator.create(URL, UEERNAME, PASSWORD)
                .globalConfig(builder -> {
                    builder.author(AUTHOR) // 设置作者
                            .outputDir(OUTPUTDIR) // 输出目录
                            .disableOpenDir(); // 禁止自动打开输出目录
                })
                .dataSourceConfig(builder ->
                        builder.typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {
                            int typeCode = metaInfo.getJdbcType().TYPE_CODE;
                            if (typeCode == Types.TINYINT) {
                                // 自定义类型转换
                                return DbColumnType.INTEGER;
                            }
                            return typeRegistry.getColumnType(metaInfo);
                        })
                )
                .packageConfig(builder ->
                        builder.parent("com.example.tooltestingdemo.generator") // 设置父包名
                                .moduleName("system") // 设置父包模块名
                                .pathInfo(Collections.singletonMap(OutputFile.xml, "D://project/code/com/example/tooltestingdemo/generator/system")) // 设置mapper.Xml生成路径
                )
                .strategyConfig(builder -> {
                    builder.addInclude(TABLENAME,"protocol_parameter_config","protocol_test_parameter","protocol_test_record","test_data_sample") // 设置需要生成的表名
                            // 实体类配置
                            .entityBuilder()
                            .enableFileOverride() // 启用实体文件覆盖
                            .enableLombok(new ClassAnnotationAttributes("@Data","lombok.Data")) //开启Lombok使用@Data注解
                            .enableTableFieldAnnotation() // 启用字段注解
                            .logicDeletePropertyName("isDeleted") // 逻辑删除属性名
                            // 属性自动填充
                            .addTableFills(
                                    new Property("createId", FieldFill.INSERT),
                                    new Property("createTime", FieldFill.INSERT),
                                    new Property("updateId", FieldFill.INSERT_UPDATE),
                                    new Property("updateTime", FieldFill.INSERT_UPDATE)
                            )
                            // Mapper配置
                            .mapperBuilder()
                            .enableFileOverride() // 覆盖 Mapper 接口及 XML
                            // Service配置
                            .serviceBuilder()
                            .enableFileOverride() // 覆盖 Service
                            // Controller配置
                            .controllerBuilder()
                            .enableFileOverride()// 覆盖 Controller
                            .enableRestStyle(); // 启用 REST 风格
                })
                .templateEngine(new FreemarkerTemplateEngine()) // 使用 Freemarker 模板引擎
                .execute(); // 执行生成
    }
}
