package com.example.tooltestingdemo.entity.report;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

/**
 * XML模板结构
 */
@Data
@JacksonXmlRootElement(localName = "template")
public class TemplateXml {
    
    @JacksonXmlProperty(localName = "name")
    private String name;
    
    @JacksonXmlProperty(localName = "description")
    private String description;
    
    @JacksonXmlProperty(localName = "templateType")
    private String templateType;
    
    @JacksonXmlProperty(localName = "applicableScene")
    private String applicableScene;
    
    @JacksonXmlProperty(localName = "templateStructure")
    private String templateStructure;
    
    @JacksonXmlProperty(localName = "chapterStructure")
    private String chapterStructure;
    
    @JacksonXmlProperty(localName = "content")
    private String content;
    
    @JacksonXmlProperty(localName = "styleConfig")
    private String styleConfig;
    
    @JacksonXmlProperty(localName = "isSystemTemplate")
    private Boolean isSystemTemplate;
    
    @JacksonXmlProperty(localName = "isPublic")
    private Boolean isPublic;
    
    @JacksonXmlProperty(localName = "relatedBusinessType")
    private String relatedBusinessType;
    
    @JacksonXmlProperty(localName = "sortOrder")
    private Integer sortOrder;
    
    @JacksonXmlProperty(localName = "previewImage")
    private String previewImage;
    
    @JacksonXmlProperty(localName = "version")
    private String version;
    
    @JacksonXmlProperty(localName = "author")
    private String author;
    
    @JacksonXmlProperty(localName = "createTime")
    private String createTime;
    
    @JacksonXmlProperty(localName = "updateTime")
    private String updateTime;
}