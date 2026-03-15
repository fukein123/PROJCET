package com.community.modules.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.modules.audit.entity.AdminOperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminOperationLogMapper extends BaseMapper<AdminOperationLog> {
}
