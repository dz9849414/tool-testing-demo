package com.example.tooltestingdemo.mapper.template;

import com.example.tooltestingdemo.entity.template.TemplateJobLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 模板统计Mapper
 */
@Mapper
public interface TemplateStatisticsMapper {

    // ====================== 模板统计相关方法 ======================

    /**
     * 获取模板使用统计
     */
    @Select("SELECT template_id, COUNT(*) as usage_count, " +
            "SUM(success) as success_count, " +
            "AVG(duration_ms) as avg_duration " +
            "FROM pdm_tool_template_job_log " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY template_id")
    List<Map<String, Object>> getTemplateUsageStats(@Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 获取模板效率统计
     */
    @Select("SELECT DATE(create_time) as date, " +
            "COUNT(*) as job_count, " +
            "SUM(success) as success_count, " +
            "AVG(duration_ms) as avg_duration, " +
            "MAX(duration_ms) as max_duration, " +
            "MIN(duration_ms) as min_duration " +
            "FROM pdm_tool_template_job_log " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY date")
    List<Map<String, Object>> getTemplateEfficiencyStats(@Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 根据模板ID获取模板效率统计
     */
    @Select("SELECT DATE(create_time) as date, " +
            "COUNT(*) as job_count, " +
            "SUM(success) as success_count, " +
            "AVG(duration_ms) as avg_duration, " +
            "MAX(duration_ms) as max_duration, " +
            "MIN(duration_ms) as min_duration " +
            "FROM pdm_tool_template_job_log " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND template_id = #{templateId} " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY date")
    List<Map<String, Object>> getTemplateEfficiencyStatsByTemplateId(@Param("startTime") LocalDateTime startTime,
                                                                    @Param("endTime") LocalDateTime endTime,
                                                                    @Param("templateId") Long templateId);

    /**
     * 获取任务成功率统计
     */
    @Select("SELECT job_id, " +
            "COUNT(*) as total_count, " +
            "SUM(success) as success_count, " +
            "ROUND(SUM(success) * 100.0 / COUNT(*), 2) as success_rate " +
            "FROM pdm_tool_template_job_log " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY job_id")
    List<Map<String, Object>> getJobSuccessRateStats(@Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 获取执行时间趋势
     */
    @Select("SELECT DATE(create_time) as date, " +
            "AVG(duration_ms) as avg_duration " +
            "FROM pdm_tool_template_job_log " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND success = 1 " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY date")
    List<Map<String, Object>> getExecutionTimeTrend(@Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    // ====================== 批量任务统计相关方法 ======================

    /**
     * 获取批量任务执行统计
     */
    @Select("SELECT DATE(create_time) as date, " +
            "COUNT(*) as batch_count, " +
            "SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) as success_count, " +
            "ROUND(SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as success_rate " +
            "FROM pdm_tool_template_job_batch " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY date")
    List<Map<String, Object>> getBatchJobStats(@Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 获取批量任务执行详情
     */
    @Select("SELECT id, status, create_time, update_time " +
            "FROM pdm_tool_template_job_batch " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY create_time DESC")
    List<Map<String, Object>> getBatchJobDetails(@Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    // ====================== 统一执行统计相关方法 ======================

    /**
     * 获取统一执行统计（包含手动和定时任务）
     */
    @Select("SELECT execute_type, " +
            "COUNT(*) as total_count, " +
            "SUM(success) as success_count, " +
            "ROUND(SUM(success) * 100.0 / COUNT(*), 2) as success_rate, " +
            "AVG(duration_ms) as avg_duration, " +
            "MAX(duration_ms) as max_duration, " +
            "MIN(duration_ms) as min_duration " +
            "FROM pdm_tool_template_execute_log " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY execute_type")
    List<Map<String, Object>> getUnifiedExecutionStats(@Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 获取统一执行时间趋势
     */
    @Select("SELECT DATE(create_time) as date, " +
            "execute_type, " +
            "COUNT(*) as total_count, " +
            "SUM(success) as success_count, " +
            "AVG(duration_ms) as avg_duration " +
            "FROM pdm_tool_template_execute_log " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY DATE(create_time), execute_type " +
            "ORDER BY date, execute_type")
    List<Map<String, Object>> getUnifiedExecutionTrend(@Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 根据模板ID获取统一执行统计
     */
    @Select("SELECT execute_type, " +
            "COUNT(*) as total_count, " +
            "SUM(success) as success_count, " +
            "AVG(duration_ms) as avg_duration " +
            "FROM pdm_tool_template_execute_log " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND template_id = #{templateId} " +
            "GROUP BY execute_type")
    List<Map<String, Object>> getUnifiedExecutionStatsByTemplateId(@Param("startTime") LocalDateTime startTime,
                                                                  @Param("endTime") LocalDateTime endTime,
                                                                  @Param("templateId") Long templateId);

    // ====================== 任务日志统计相关方法 ======================

    /**
     * 根据任务ID列表获取每个任务的最新日志记录
     */
    @Select("<script>" +
            "SELECT t1.* FROM pdm_tool_template_job_log t1 " +
            "INNER JOIN (" +
            "    SELECT job_id, MAX(create_time) as max_create_time " +
            "    FROM pdm_tool_template_job_log " +
            "    WHERE job_id IN " +
            "    <foreach collection='jobIds' item='jobId' open='(' separator=',' close=')'>" +
            "        #{jobId}" +
            "    </foreach>" +
            "    GROUP BY job_id" +
            ") t2 ON t1.job_id = t2.job_id AND t1.create_time = t2.max_create_time " +
            "ORDER BY t1.create_time DESC" +
            "</script>")
    List<TemplateJobLog> selectLastLogsByJobIds(@Param("jobIds") List<Long> jobIds);

    /**
     * 根据任务ID获取最近的日志记录
     */
    @Select("SELECT * FROM pdm_tool_template_job_log " +
            "WHERE job_id = #{jobId} " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<TemplateJobLog> selectRecentByJobId(@Param("jobId") Long jobId, @Param("limit") Integer limit);

    /**
     * 获取任务执行统计
     */
    @Select("SELECT job_id, " +
            "COUNT(*) as total_count, " +
            "SUM(success) as success_count, " +
            "AVG(duration_ms) as avg_duration, " +
            "MAX(duration_ms) as max_duration, " +
            "MIN(duration_ms) as min_duration " +
            "FROM pdm_tool_template_job_log " +
            "WHERE job_id = #{jobId} " +
            "AND create_time BETWEEN #{startTime} AND #{endTime}")
    Map<String, Object> getJobExecutionStats(@Param("jobId") Long jobId,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 获取模板执行统计
     */
    @Select("SELECT template_id, " +
            "COUNT(*) as total_count, " +
            "SUM(success) as success_count, " +
            "AVG(duration_ms) as avg_duration " +
            "FROM pdm_tool_template_job_log " +
            "WHERE template_id = #{templateId} " +
            "AND create_time BETWEEN #{startTime} AND #{endTime}")
    Map<String, Object> getTemplateExecutionStats(@Param("templateId") Long templateId,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    // ====================== 每2小时响应时间统计 ======================

    /**
     * 获取每2小时平均响应时间统计（JOB_LOG数据源）
     */
    @Select("SELECT " +
            "DATE_FORMAT(create_time, '%Y-%m-%d %H:00:00') as time_slot, " +
            "FLOOR(HOUR(create_time) / 2) * 2 as hour_group, " +
            "COUNT(*) as execution_count, " +
            "AVG(duration_ms) as avg_duration, " +
            "MAX(duration_ms) as max_duration, " +
            "MIN(duration_ms) as min_duration " +
            "FROM pdm_tool_template_job_log " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND success = 1 " +
            "GROUP BY DATE(create_time), FLOOR(HOUR(create_time) / 2) * 2, time_slot " +
            "ORDER BY time_slot")
    List<Map<String, Object>> getHourlyResponseTimeStats(@Param("startTime") LocalDateTime startTime,
                                                        @Param("endTime") LocalDateTime endTime);

    /**
     * 获取每2小时平均响应时间统计（UNIFIED数据源）
     */
    @Select("SELECT " +
            "DATE_FORMAT(create_time, '%Y-%m-%d %H:00:00') as time_slot, " +
            "FLOOR(HOUR(create_time) / 2) * 2 as hour_group, " +
            "execute_type, " +
            "COUNT(*) as execution_count, " +
            "AVG(duration_ms) as avg_duration, " +
            "MAX(duration_ms) as max_duration, " +
            "MIN(duration_ms) as min_duration " +
            "FROM pdm_tool_template_execute_log " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND success = 1 " +
            "GROUP BY DATE(create_time), FLOOR(HOUR(create_time) / 2) * 2, execute_type, time_slot " +
            "ORDER BY time_slot")
    List<Map<String, Object>> getUnifiedHourlyResponseTimeStats(@Param("startTime") LocalDateTime startTime,
                                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 获取每2小时平均响应时间统计（BATCH数据源）
     */
    @Select("SELECT " +
            "DATE_FORMAT(create_time, '%Y-%m-%d %H:00:00') as time_slot, " +
            "FLOOR(HOUR(create_time) / 2) * 2 as hour_group, " +
            "batch_type, " +
            "COUNT(*) as execution_count, " +
            "AVG(response_time) as avg_response_time " +
            "FROM pdm_tool_template_job_batch " +
            "WHERE create_time BETWEEN ? AND ? " +
            "AND is_deleted = 0 " +
            "AND response_time IS NOT NULL " +
            "GROUP BY DATE(create_time), FLOOR(HOUR(create_time) / 2) * 2, time_slot, batch_type " +
            "ORDER BY DATE(create_time), hour_group")
    List<Map<String, Object>> getBatchHourlyResponseTimeStats(@Param("startTime") LocalDateTime startTime,
                                                            @Param("endTime") LocalDateTime endTime);

    // ====================== 周一到周日执行量统计 ======================

    /**
     * 获取周一到周日执行量统计（JOB_LOG数据源）
     */
    @Select("SELECT " +
            "DAYNAME(create_time) as day_name, " +
            "DAYOFWEEK(create_time) as day_of_week, " +
            "COUNT(*) as execution_count " +
            "FROM pdm_tool_template_job_log " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND success = 1 " +
            "GROUP BY DAYNAME(create_time), DAYOFWEEK(create_time) " +
            "ORDER BY DAYOFWEEK(create_time)")
    List<Map<String, Object>> getWeeklyExecutionStats(@Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 获取周一到周日执行量统计（UNIFIED数据源）
     */
    @Select("SELECT " +
            "DAYNAME(create_time) as day_name, " +
            "DAYOFWEEK(create_time) as day_of_week, " +
            "execute_type, " +
            "COUNT(*) as execution_count " +
            "FROM pdm_tool_template_execute_log " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND success = 1 " +
            "GROUP BY DAYNAME(create_time), DAYOFWEEK(create_time), execute_type " +
            "ORDER BY DAYOFWEEK(create_time)")
    List<Map<String, Object>> getUnifiedWeeklyExecutionStats(@Param("startTime") LocalDateTime startTime,
                                                           @Param("endTime") LocalDateTime endTime);

    // ====================== 成功率分析统计方法 ======================

    /**
     * 获取成功率分析统计（JOB_LOG数据源）
     */
    @Select("SELECT " +
            "COUNT(*) as total_count, " +
            "SUM(success) as success_count, " +
            "COUNT(*) - SUM(success) as failure_count, " +
            "ROUND(SUM(success) * 100.0 / COUNT(*), 2) as success_rate " +
            "FROM pdm_tool_template_job_log " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime}")
    Map<String, Object> getSuccessRateStats(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 获取成功率分析统计（UNIFIED数据源）
     */
    @Select("SELECT " +
            "COUNT(*) as total_count, " +
            "SUM(success) as success_count, " +
            "COUNT(*) - SUM(success) as failure_count, " +
            "ROUND(SUM(success) * 100.0 / COUNT(*), 2) as success_rate " +
            "FROM pdm_tool_template_execute_log " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime}")
    Map<String, Object> getUnifiedSuccessRateStats(@Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 获取成功率分析统计（BATCH数据源）
     */
    @Select("SELECT " +
            "COUNT(*) as total_count, " +
            "SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) as success_count, " +
            "COUNT(*) - SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) as failure_count, " +
            "ROUND(SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as success_rate " +
            "FROM pdm_tool_template_job_batch " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime}")
    Map<String, Object> getBatchSuccessRateStats(@Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    // ====================== 协议类型分布统计方法 ======================

    /**
     * 获取协议类型分布统计（按协议分类）
     */
    @Select("SELECT " +
            "pt.protocol_name as category, " +
            "COUNT(*) as usage_count, " +
            "SUM(CASE WHEN ptr.error_code = '200' THEN 1 ELSE 0 END) as success_count " +
            "FROM pdm_tool_protocol_test_record ptr " +
            "INNER JOIN pdm_tool_protocol_type pt ON ptr.protocol_id = pt.id " +
            "WHERE ptr.create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND ptr.is_deleted = 0 AND pt.is_deleted = 0 " +
            "GROUP BY pt.protocol_name " +
            "ORDER BY usage_count DESC")
    List<Map<String, Object>> getProtocolCategoryStats(@Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 获取协议类型分布统计（按具体协议）
     */
    @Select("SELECT " +
            "pt.protocol_code as protocol_code, " +
            "pt.protocol_name as protocol_name, " +
            "pt.protocol_name as category, " +
            "COUNT(*) as usage_count, " +
            "SUM(CASE WHEN ptr.response_code = '200' THEN 1 ELSE 0 END) as success_count " +
            "FROM pdm_tool_protocol_test_record ptr " +
            "INNER JOIN pdm_tool_protocol_type pt ON ptr.protocol_id = pt.id " +
            "WHERE ptr.create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND ptr.is_deleted = 0 AND pt.is_deleted = 0 " +
            "GROUP BY pt.protocol_code, pt.protocol_name " +
            "ORDER BY usage_count DESC")
    List<Map<String, Object>> getProtocolDetailStats(@Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 获取协议测试类型分布统计
     */
    @Select("SELECT " +
            "ptr.test_type as test_type, " +
            "COUNT(*) as test_count, " +
            "SUM(CASE WHEN ptr.response_code = '200' THEN 1 ELSE 0 END) as success_count " +
            "FROM pdm_tool_protocol_test_record ptr " +
            "WHERE ptr.create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND ptr.is_deleted = 0 " +
            "GROUP BY ptr.test_type " +
            "ORDER BY test_count DESC")
    List<Map<String, Object>> getProtocolTestTypeStats(@Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 获取前5的失败原因统计
     */
    @Select("SELECT " +
            "error_message as failure_reason, " +
            "COUNT(*) as failure_count, " +
            "error_code as error_code, " +
            "protocol_id, " +
            "(SELECT protocol_name FROM pdm_tool_protocol_type WHERE id = ptr.protocol_id) as protocol_name " +
            "FROM pdm_tool_protocol_test_record ptr " +
            "WHERE ptr.create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND ptr.is_deleted = 0 " +
            "AND ptr.error_code != '200' " +
            "AND error_message IS NOT NULL " +
            "AND error_message != '' " +
            "GROUP BY error_message, error_code, protocol_id " +
            "ORDER BY failure_count DESC " +
            "LIMIT 5")
    List<Map<String, Object>> getTopFailureReasons(@Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 获取批量任务的前5失败原因统计
     */
    @Select("SELECT " +
            "CASE " +
            "WHEN JSON_EXTRACT(result, '$.errorMessage') IS NOT NULL THEN JSON_UNQUOTE(JSON_EXTRACT(result, '$.errorMessage')) " +
            "WHEN JSON_EXTRACT(result, '$.error_message') IS NOT NULL THEN JSON_UNQUOTE(JSON_EXTRACT(result, '$.error_message')) " +
            "WHEN JSON_EXTRACT(result, '$.message') IS NOT NULL THEN JSON_UNQUOTE(JSON_EXTRACT(result, '$.message')) " +
            "ELSE '未知错误' " +
            "END as failure_reason, " +
            "COUNT(*) as failure_count, " +
            "CASE " +
            "WHEN JSON_EXTRACT(result, '$.errorCode') IS NOT NULL THEN JSON_UNQUOTE(JSON_EXTRACT(result, '$.errorCode')) " +
            "WHEN JSON_EXTRACT(result, '$.error_code') IS NOT NULL THEN JSON_UNQUOTE(JSON_EXTRACT(result, '$.error_code')) " +
            "WHEN JSON_EXTRACT(result, '$.code') IS NOT NULL THEN JSON_UNQUOTE(JSON_EXTRACT(result, '$.code')) " +
            "ELSE '未知错误码' " +
            "END as error_code, " +
            "NULL as protocol_id, " +
            "'批量任务' as protocol_name " +
            "FROM pdm_tool_template_job_batch tjb " +
            "WHERE tjb.create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND tjb.status = 'FAILED' " +
            "AND tjb.result IS NOT NULL " +
            "AND tjb.result != '' " +
            "GROUP BY failure_reason, error_code " +
            "ORDER BY failure_count DESC " +
            "LIMIT 5")
    List<Map<String, Object>> getBatchTopFailureReasons(@Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime);
}