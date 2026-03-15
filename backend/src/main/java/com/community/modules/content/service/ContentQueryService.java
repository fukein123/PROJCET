package com.community.modules.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.common.exception.BusinessException;
import com.community.common.util.SecurityUtil;
import com.community.common.web.PageResult;
import com.community.modules.content.entity.BannerInfo;
import com.community.modules.content.entity.CommentInfo;
import com.community.modules.content.entity.ExchangeOrder;
import com.community.modules.content.entity.FavoriteActivity;
import com.community.modules.content.entity.ForumCategory;
import com.community.modules.content.entity.ForumPost;
import com.community.modules.content.entity.InfoDynamic;
import com.community.modules.content.entity.MallProduct;
import com.community.modules.content.entity.NoticeInfo;
import com.community.modules.content.mapper.BannerInfoMapper;
import com.community.modules.content.mapper.CommentInfoMapper;
import com.community.modules.content.mapper.ExchangeOrderMapper;
import com.community.modules.content.mapper.FavoriteActivityMapper;
import com.community.modules.content.mapper.ForumCategoryMapper;
import com.community.modules.content.mapper.ForumPostMapper;
import com.community.modules.content.mapper.InfoDynamicMapper;
import com.community.modules.content.mapper.MallProductMapper;
import com.community.modules.content.mapper.NoticeInfoMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ContentQueryService {

    private final InfoDynamicMapper infoDynamicMapper;
    private final NoticeInfoMapper noticeInfoMapper;
    private final BannerInfoMapper bannerInfoMapper;
    private final ForumCategoryMapper forumCategoryMapper;
    private final ForumPostMapper forumPostMapper;
    private final CommentInfoMapper commentInfoMapper;
    private final FavoriteActivityMapper favoriteActivityMapper;
    private final MallProductMapper mallProductMapper;
    private final ExchangeOrderMapper exchangeOrderMapper;

    public PageResult<InfoDynamic> pageDynamics(long current, long size, String type, String keyword, boolean onlyPublished) {
        LambdaQueryWrapper<InfoDynamic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(type), InfoDynamic::getType, type)
                .and(StringUtils.hasText(keyword), q -> q.like(InfoDynamic::getTitle, keyword)
                        .or().like(InfoDynamic::getContent, keyword))
                .eq(onlyPublished, InfoDynamic::getStatus, 1)
                .orderByDesc(InfoDynamic::getPublishTime);
        PageHelper.startPage((int) current, (int) size);
        List<InfoDynamic> records = infoDynamicMapper.selectList(wrapper);
        PageInfo<InfoDynamic> pageInfo = new PageInfo<>(records);
        return new PageResult<>(pageInfo.getTotal(), current, size, records);
    }

    public List<InfoDynamic> hotDynamicsTop5() {
        return infoDynamicMapper.selectList(new LambdaQueryWrapper<InfoDynamic>()
                .eq(InfoDynamic::getStatus, 1)
                .orderByDesc(InfoDynamic::getViews)
                .last("limit 5"));
    }

    public InfoDynamic detailDynamic(Long id) {
        InfoDynamic dynamic = infoDynamicMapper.selectById(id);
        if (dynamic == null || (!isAdmin() && !Objects.equals(dynamic.getStatus(), 1))) {
            throw new BusinessException(404, "Dynamic not found");
        }
        return dynamic;
    }

    public PageResult<NoticeInfo> pageNotices(long current, long size, boolean onlyPublished) {
        LambdaQueryWrapper<NoticeInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(onlyPublished, NoticeInfo::getStatus, 1)
                .orderByDesc(NoticeInfo::getPublishTime);
        PageHelper.startPage((int) current, (int) size);
        List<NoticeInfo> records = noticeInfoMapper.selectList(wrapper);
        PageInfo<NoticeInfo> pageInfo = new PageInfo<>(records);
        return new PageResult<>(pageInfo.getTotal(), current, size, records);
    }

    public NoticeInfo detailNotice(Long id) {
        NoticeInfo notice = noticeInfoMapper.selectById(id);
        if (notice == null || (!isAdmin() && !Objects.equals(notice.getStatus(), 1))) {
            throw new BusinessException(404, "Notice not found");
        }
        return notice;
    }

    public List<BannerInfo> listBanners() {
        return bannerInfoMapper.selectList(new LambdaQueryWrapper<BannerInfo>()
                .eq(BannerInfo::getStatus, 1)
                .orderByAsc(BannerInfo::getSort)
                .orderByDesc(BannerInfo::getCreateTime));
    }

    public List<BannerInfo> listBannersForAdmin() {
        return bannerInfoMapper.selectList(new LambdaQueryWrapper<BannerInfo>()
                .orderByAsc(BannerInfo::getSort)
                .orderByDesc(BannerInfo::getCreateTime));
    }

    public List<ForumCategory> listForumCategories() {
        return forumCategoryMapper.selectList(new LambdaQueryWrapper<ForumCategory>()
                .eq(ForumCategory::getStatus, 1)
                .orderByAsc(ForumCategory::getSort)
                .orderByDesc(ForumCategory::getCreateTime));
    }

    public List<ForumCategory> listForumCategoriesForAdmin() {
        return forumCategoryMapper.selectList(new LambdaQueryWrapper<ForumCategory>()
                .orderByAsc(ForumCategory::getSort)
                .orderByDesc(ForumCategory::getCreateTime));
    }

    public PageResult<ForumPost> pageForumPosts(long current, long size, String keyword, String status, boolean onlyMine, boolean onlyApproved) {
        Long userId = SecurityUtil.currentUserId();
        String role = SecurityUtil.currentRole();
        boolean mineAndAuthenticated = onlyMine && userId != null;
        boolean allowQueryNonApproved = "ADMIN".equalsIgnoreCase(role) || mineAndAuthenticated;
        LambdaQueryWrapper<ForumPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), ForumPost::getTitle, keyword)
                .eq(mineAndAuthenticated, ForumPost::getUserId, userId)
                .orderByDesc(ForumPost::getCreateTime);

        if (allowQueryNonApproved) {
            if (StringUtils.hasText(status)) {
                wrapper.eq(ForumPost::getStatus, status);
            } else if (onlyApproved) {
                wrapper.eq(ForumPost::getStatus, "APPROVED");
            }
        } else {
            wrapper.eq(ForumPost::getStatus, "APPROVED");
        }

        PageHelper.startPage((int) current, (int) size);
        List<ForumPost> records = forumPostMapper.selectList(wrapper);
        PageInfo<ForumPost> pageInfo = new PageInfo<>(records);
        return new PageResult<>(pageInfo.getTotal(), current, size, records);
    }

    public ForumPost detailForumPost(Long id) {
        ForumPost post = forumPostMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(404, "Post not found");
        }

        Long userId = SecurityUtil.currentUserId();
        boolean canView = isAdmin()
                || "APPROVED".equalsIgnoreCase(post.getStatus())
                || (userId != null && userId.equals(post.getUserId()));
        if (!canView) {
            throw new BusinessException(404, "Post not found");
        }
        return post;
    }

    public PageResult<CommentInfo> pageComments(long current,
                                                long size,
                                                boolean onlyMine,
                                                String targetType,
                                                Long targetId,
                                                boolean includeTestData) {
        Long userId = SecurityUtil.currentUserId();
        LambdaQueryWrapper<CommentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(onlyMine && userId != null, CommentInfo::getUserId, userId)
                .eq(StringUtils.hasText(targetType), CommentInfo::getTargetType, targetType)
                .eq(targetId != null, CommentInfo::getTargetId, targetId)
                .eq(CommentInfo::getStatus, 1)
                .orderByDesc(CommentInfo::getCreateTime);
        if (!includeTestData) {
            wrapper.and(q -> q.isNull(CommentInfo::getTestDataTag).or().eq(CommentInfo::getTestDataTag, ""));
        }
        PageHelper.startPage((int) current, (int) size);
        List<CommentInfo> records = commentInfoMapper.selectList(wrapper);
        PageInfo<CommentInfo> pageInfo = new PageInfo<>(records);
        return new PageResult<>(pageInfo.getTotal(), current, size, records);
    }

    public List<FavoriteActivity> myFavorites() {
        return favoriteActivityMapper.selectList(new LambdaQueryWrapper<FavoriteActivity>()
                .eq(FavoriteActivity::getUserId, SecurityUtil.currentUserId())
                .orderByDesc(FavoriteActivity::getPriority)
                .orderByDesc(FavoriteActivity::getCreateTime));
    }

    public PageResult<FavoriteActivity> pageFavorites(long current, long size) {
        PageHelper.startPage((int) current, (int) size);
        List<FavoriteActivity> records = favoriteActivityMapper.selectList(new LambdaQueryWrapper<FavoriteActivity>()
                .eq(FavoriteActivity::getUserId, SecurityUtil.currentUserId())
                .orderByDesc(FavoriteActivity::getPriority)
                .orderByDesc(FavoriteActivity::getCreateTime));
        PageInfo<FavoriteActivity> pageInfo = new PageInfo<>(records);
        return new PageResult<>(pageInfo.getTotal(), current, size, records);
    }

    public PageResult<MallProduct> pageMallProducts(long current, long size, String keyword, Integer status, boolean onlyEnabled) {
        LambdaQueryWrapper<MallProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), MallProduct::getName, keyword)
                .eq(status != null, MallProduct::getStatus, status)
                .eq(onlyEnabled, MallProduct::getStatus, 1)
                .orderByDesc(MallProduct::getCreateTime);
        PageHelper.startPage((int) current, (int) size);
        List<MallProduct> records = mallProductMapper.selectList(wrapper);
        PageInfo<MallProduct> pageInfo = new PageInfo<>(records);
        return new PageResult<>(pageInfo.getTotal(), current, size, records);
    }

    public PageResult<ExchangeOrder> pageMyExchangeOrders(long current, long size, String status) {
        LambdaQueryWrapper<ExchangeOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExchangeOrder::getUserId, SecurityUtil.currentUserId())
                .eq(StringUtils.hasText(status), ExchangeOrder::getStatus, status)
                .orderByDesc(ExchangeOrder::getCreateTime);
        PageHelper.startPage((int) current, (int) size);
        List<ExchangeOrder> records = exchangeOrderMapper.selectList(wrapper);
        PageInfo<ExchangeOrder> pageInfo = new PageInfo<>(records);
        return new PageResult<>(pageInfo.getTotal(), current, size, records);
    }

    public PageResult<ExchangeOrder> pageAdminExchangeOrders(long current,
                                                             long size,
                                                             String orderNo,
                                                             String productKeyword,
                                                             String userKeyword,
                                                             String status) {
        LambdaQueryWrapper<ExchangeOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(orderNo), ExchangeOrder::getOrderNo, orderNo)
                .like(StringUtils.hasText(productKeyword), ExchangeOrder::getProductName, productKeyword)
                .and(StringUtils.hasText(userKeyword), q -> q.like(ExchangeOrder::getUserName, userKeyword)
                        .or().like(ExchangeOrder::getRealName, userKeyword))
                .eq(StringUtils.hasText(status), ExchangeOrder::getStatus, status)
                .orderByDesc(ExchangeOrder::getCreateTime);
        PageHelper.startPage((int) current, (int) size);
        List<ExchangeOrder> records = exchangeOrderMapper.selectList(wrapper);
        records.forEach(this::maskOrderForAdminList);
        PageInfo<ExchangeOrder> pageInfo = new PageInfo<>(records);
        return new PageResult<>(pageInfo.getTotal(), current, size, records);
    }

    public ExchangeOrder detailAdminExchangeOrder(Long id) {
        ExchangeOrder order = exchangeOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "Exchange order not found");
        }
        return order;
    }

    private boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(SecurityUtil.currentRole());
    }

    private void maskOrderForAdminList(ExchangeOrder order) {
        order.setReceiverPhone(maskPhone(order.getReceiverPhone()));
        order.setReceiverAddress(maskAddress(order.getReceiverAddress()));
    }

    private String maskPhone(String value) {
        if (!StringUtils.hasText(value) || value.length() <= 7) {
            return value;
        }
        return value.substring(0, 3) + "****" + value.substring(value.length() - 4);
    }

    private String maskAddress(String value) {
        if (!StringUtils.hasText(value) || value.length() <= 10) {
            return value;
        }
        return value.substring(0, 6) + "****" + value.substring(value.length() - 4);
    }
}
