package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServiceResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
    //  引入日志
    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);
    //    注入mapper
    @Autowired
    private CategoryMapper categoryMapper;

    // 添加品类
    public ServiceResponse addCategory(String categoryName,Integer parentId){
        if (parentId == null || StringUtils.isBlank(categoryName)){
            return ServiceResponse.createByErrorMessage("添加品类错误,参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true); // 刚开始添加的分类可用;

        int rowCount = categoryMapper.insert(category);
        if(rowCount>0){
            return ServiceResponse.createBySuccess("添加品类成功");
        }
        return ServiceResponse.createByErrorMessage("添加品类失败");
    }
    // 更新品类
    public ServiceResponse updateCategoryName(Integer categoryId, String categoryName){
        if (categoryId == null || StringUtils.isBlank(categoryName)){
            return ServiceResponse.createByErrorMessage("更新品类参数错误");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);

        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(rowCount>0){
            return ServiceResponse.createBySuccess("更新品类名字成功");
        }
        return ServiceResponse.createByErrorMessage("更新品类名字失败");
    }

    // 查询子节点的category信息, 并且不递归 保持平级
    public ServiceResponse<List<Category>> getChildrenParalleCategory(Integer categoryId){
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if(CollectionUtils.isEmpty(categoryList)){
            logger.info("未找到当前分类的子类");
        }
        return ServiceResponse.createBySuccess(categoryList);
    }

    /**
     * 递归方法:
     * 递归查询本节点id 以及孩子节点id;
     *  查询当前节点的id和递归子节点的id;
     *  0 -> 10000 -> 100000
     *  传0 要 100000 100000
     *  传10000  获取的只有100000
     *
     * @param categoryId
     * @return
     */
    public ServiceResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId){
        // 这个Sets是guawa里面提供的方法;
        // 集合处理比较强大
        Set<Category> categorySet = Sets.newHashSet();
        // 调用递归算法;
        findChildCategory(categorySet,categoryId);
        // Lists 也是guawa里面提供的方法;
        List<Integer> categoryList = Lists.newArrayList();
        if(categoryId != null){
            for (Category categoryItem : categorySet){
                categoryList.add(categoryItem.getId());
            }
        }
        return ServiceResponse.createBySuccess(categoryList);

    }
    // Set排重;
    // Set<Category> 排重 需要重写equal 和hasCode
    // 处理set的 对象的时候
    // 假如需要去重 需要重写pojo对象的 equals和hasCode 方法;
    // 递归算法自己调用自己
    private Set<Category> findChildCategory(Set<Category> categorySet, Integer categoryId){
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if(category != null){
            categorySet.add(category);
        }
        // 查找子节点
        // 递归算法必须要有一个退出条件
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        for (Category categoryItem: categoryList){
            // 调用自己;
            findChildCategory(categorySet,categoryItem.getId());
        }
        return  categorySet;

    }
}
