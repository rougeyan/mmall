package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Category;

import java.util.List;

public interface ICategoryService {
    ServiceResponse addCategory(String categoryName, Integer parentId);

    ServiceResponse updateCategory(Category category);

    ServiceResponse<PageInfo> getChildrenParalleCategory(Integer categoryId,int pageNum, int pageSize);

    ServiceResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId);
}
