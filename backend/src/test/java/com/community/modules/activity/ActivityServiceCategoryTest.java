package com.community.modules.activity;

import com.community.common.exception.BusinessException;
import com.community.modules.activity.mapper.ActivityApplicationMapper;
import com.community.modules.activity.mapper.ActivityCategoryMapper;
import com.community.modules.activity.mapper.ActivityCheckRecordMapper;
import com.community.modules.activity.mapper.ActivityMapper;
import com.community.modules.activity.service.ActivityQueryService;
import com.community.modules.activity.service.ActivityService;
import com.community.modules.activity.service.ActivityCommandService;
import com.community.modules.content.mapper.FavoriteActivityMapper;
import com.community.modules.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityServiceCategoryTest {

    @Mock
    private ActivityCategoryMapper categoryMapper;
    @Mock
    private ActivityMapper activityMapper;
    @Mock
    private ActivityApplicationMapper applicationMapper;
    @Mock
    private ActivityCheckRecordMapper checkRecordMapper;
    @Mock
    private FavoriteActivityMapper favoriteActivityMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ActivityQueryService activityQueryService;
    @Mock
    private ActivityCommandService activityCommandService;

    @InjectMocks
    private ActivityService activityService;

    @Test
    void deleteCategory_shouldThrowWhenCategoryIsReferenced() {
        Long categoryId = 100L;
        when(activityMapper.selectCount(any())).thenReturn(2L);

        BusinessException ex = assertThrows(BusinessException.class, () -> activityService.deleteCategory(categoryId));

        assertEquals("该分类下仍有活动，请先删除相关活动", ex.getMessage());
        verifyNoInteractions(categoryMapper);
    }

    @Test
    void deleteCategory_shouldDeleteWhenCategoryNotReferenced() {
        Long categoryId = 101L;
        when(activityMapper.selectCount(any())).thenReturn(0L);

        activityService.deleteCategory(categoryId);

        verify(categoryMapper, times(1)).deleteById(categoryId);
    }

    @Test
    void batchDeleteCategories_shouldSkipNullAndDuplicateIds() {
        when(activityMapper.selectCount(any())).thenReturn(0L);

        activityService.batchDeleteCategories(Arrays.asList(201L, null, 201L, 202L));

        verify(categoryMapper, times(1)).deleteById(201L);
        verify(categoryMapper, times(1)).deleteById(202L);
    }

    @Test
    void batchDeleteCategories_shouldThrowWhenAnyReferenced() {
        when(activityMapper.selectCount(any())).thenReturn(0L, 1L);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> activityService.batchDeleteCategories(List.of(301L, 302L))
        );

        assertEquals("该分类下仍有活动，请先删除相关活动", ex.getMessage());
        verify(categoryMapper, times(1)).deleteById(301L);
        verify(categoryMapper, never()).deleteById(302L);
    }
}
