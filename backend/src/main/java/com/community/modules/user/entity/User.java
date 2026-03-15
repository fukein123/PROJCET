package com.community.modules.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.community.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@Schema(description = "System user")
public class User extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String gender;
    private String avatar;
    private String role;
    private Integer points;
    private Integer status;
    private String realName;
    private Integer certified;

    @TableField(exist = false)
    private Long certificationId;
    @TableField(exist = false)
    private String certificationStatus;
    @TableField(exist = false)
    private String certificationRejectReason;
    @TableField(exist = false)
    private String certificationIdCardNo;
    @TableField(exist = false)
    private String certificationIdCardFrontUrl;
    @TableField(exist = false)
    private String certificationIdCardBackUrl;
    @TableField(exist = false)
    private java.time.LocalDateTime certificationSubmitTime;
    @TableField(exist = false)
    private java.time.LocalDateTime certificationAuditTime;
    @TableField(exist = false)
    private Long certificationAuditorId;
}
