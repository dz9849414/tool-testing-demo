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
            "FROM template_job_log " +
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
            "FROM template_job_log " +
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
            "FROM template_job_log " +
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
            "FROM template_job_log " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY job_id")
    List<Map<String, Object>> getJobSuccessRateStats(@Param("startTime") LocalDateTime startTime, 
                                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 获取执行时间趋势
     */
    @Select("SELECT DATE(create_time) as date, " +
            "AVG(duration_ms) as avg_duration " +
            "FROM template_job_log " +
            "WHERE create_time BETWEEN #{startTime} AND #{endTime} " +
            "AND success = 1 " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY date")
    List<Map<String, Object>> getExecutionTimeTrend(@Param("startTime") LocalDateTime startTime, 
                                                   @Param("endTime") LocalDateTime endTime);

    // ====================== 任务日志统计相关方法 ======================

    /**
     * 根据任务ID列表获取每个任务的最新日志记录
     */
    @Select("<script>" +
            "SELECT t1.* FROM template_job_log t1 " +
            "INNER JOIN (" +
            "    SELECT job_id, MAX(create_time) as max_create_time " +
            "    FROM template_job_log " +
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
    @Select("SELECT * FROM template_job_log " +
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
            "FROM template_job_log " +
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
            "FROM template_job_log " +
            "WHERE template_id = #{templateId} " +
            "AND create_time BETWEEN #{startTime} AND #{endTime}")
    Map<String, Object> getTemplateExecutionStats(@Param("templateId") Long templateId, 
                                                 @Param("startTime") LocalDateTime startTime, 
                                                 @Param("endTime") LocalDateTime endTime);
}