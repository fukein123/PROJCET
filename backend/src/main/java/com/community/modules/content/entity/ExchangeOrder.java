package com.community.modules.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.community.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exchange_order")
public class ExchangeOrder extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long productId;
    private String userName;
    private String realName;
    private String productName;
    private String productImage;
    private String productSummary;
    private Integer quantity;
    private Integer pointsPerItem;
    private Integer totalPoints;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String requestKey;
    private String status;
    private String statusReason;
    private java.time.LocalDateTime shippedTime;
    private java.time.LocalDateTime receivedTime;
    private java.time.LocalDateTime cancelledTime;
}
