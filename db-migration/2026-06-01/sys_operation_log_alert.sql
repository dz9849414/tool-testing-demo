ALTER TABLE pdm_tool_sys_operation_log
    MODIFY COLUMN method_json LONGBLOB COMMENT '方法调用链JSON（Gzip压缩）';