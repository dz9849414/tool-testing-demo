package com.example.tooltestingdemo.utils;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * XML解析工具类
 */
@Slf4j
public class XmlParser {

    private static final XmlMapper xmlMapper = new XmlMapper();

    /**
     * 解析XML字符串为对象
     */
    public static <T> T parse(String xml, Class<T> clazz) {
        try {
            return xmlMapper.readValue(xml, clazz);
        } catch (Exception e) {
            log.error("XML解析失败", e);
            throw new RuntimeException("XML解析失败: " + e.getMessage());
        }
    }

    /**
     * 将对象转换为XML字符串
     */
    public static String toXml(Object obj) {
        try {
            return xmlMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("XML生成失败", e);
            throw new RuntimeException("XML生成失败: " + e.getMessage());
        }
    }

    /**
     * 格式化XML字符串
     */
    public static String formatXml(String xml) {
        try {
            Object obj = xmlMapper.readValue(xml, Object.class);
            return xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("XML格式化失败，返回原始内容", e);
            return xml;
        }
    }
}