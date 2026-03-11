package com.community.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.modules.user.entity.CvsUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<CvsUser> {}

