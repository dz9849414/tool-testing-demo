package com.example.tooltestingdemo.util;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用工具类：根据 ID 查询/更新，忽略逻辑删除（3.5.11 可用）
 */
@Component
public class MpIgnoreDeleteUtil {

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    /**
     * 通用 getById，不带 is_deleted=0
     */
    @SuppressWarnings("unchecked")
    public <T> T getByIdIgnoreLogicDelete(Class<T> entityClass, Object id) {
        TableInfo table = TableInfoHelper.getTableInfo(entityClass);
        String sql = "SELECT * FROM " + table.getTableName() + " WHERE " + table.getKeyColumn() + " = #{id}";

        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectOne(sql, id);
        }
    }

    /**
     * 通用 updateById，不带 is_deleted=0
     */
    public void updateByIdIgnoreLogicDelete(Object mapper, Object entity) {
        try {
            Class<?> entityClass = entity.getClass();
            TableInfo table = TableInfoHelper.getTableInfo(entityClass);
            
            if (table == null) {
                throw new RuntimeException("无法获取表信息: " + entityClass.getName());
            }

            // 构建 UPDATE SQL
            StringBuilder sql = new StringBuilder("UPDATE ");
            sql.append(table.getTableName());
            sql.append(" SET ");

            // 收集字段值
            List<Object> values = new ArrayList<>();
            Object idValue = null;
            boolean first = true;
            
            for (Field field : entityClass.getDeclaredFields()) {
                field.setAccessible(true);
                Object fieldValue = field.get(entity);
                
                // 跳过主键字段（在WHERE条件中使用）
                if (table.getKeyProperty().equals(field.getName())) {
                    idValue = fieldValue;
                    continue;
                }
                
                // 使用驼峰转下划线获取列名（MyBatis-Plus 默认行为）
                String columnName = camelToUnderscore(field.getName());
                
                if (!first) {
                    sql.append(", ");
                }
                sql.append(columnName).append(" = ?");
                values.add(fieldValue);
                first = false;
            }

            sql.append(" WHERE ").append(table.getKeyColumn()).append(" = ?");
            values.add(idValue);

            // 使用原生 JDBC 执行 SQL
            try (SqlSession session = sqlSessionFactory.openSession(true);
                 java.sql.Connection conn = session.getConnection();
                 java.sql.PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                
                // 设置参数
                for (int i = 0; i < values.size(); i++) {
                    stmt.setObject(i + 1, values.get(i));
                }
                
                // 执行更新
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            throw new RuntimeException("更新操作失败: " + e.getMessage(), e);
        }
    }

    /**
     * 驼峰转下划线
     */
    private String camelToUnderscore(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toLowerCase(str.charAt(0)));
        for (int i = 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append("_");
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}