package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProducetService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("iProducetService")
public class ProductServiceImpl implements IProducetService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ICategoryService iCategoryService;


    // ============================  后台系统部分 ============================
    /**
     *  新增/ 更新产品
     * @param product
     * @return
     */
    public ServiceResponse saveOrUpdateProduct(Product product){
        if(product !=null){
            // 判定子图是不是空
            if(StringUtils.isNoneBlank(product.getSubImages())){
                // 把子图的第一个设置为主图
                String[] subImageArray = product.getSubImages().split(",");
                if(subImageArray.length>0){
                    product.setMainImage(subImageArray[0]);
                }
            }
            // 更新
            if(product.getId() != null){
                int rowCount =  productMapper.updateByPrimaryKeySelective(product);
                if(rowCount>0){
                    return ServiceResponse.createBySuccess("更新产品成功");
                }
                return ServiceResponse.createByErrorMessage("更新产品失败");
            }else{
            // 新增
                int rowCount = productMapper.insert(product);
                if(rowCount>0){
                    return ServiceResponse.createBySuccess("新增产品成功");
                }
                return ServiceResponse.createByErrorMessage("新增产品失败");
            }
        }
        return ServiceResponse.createByErrorMessage("新增或者更新产品参数不正确");
    }

    /**
     * 设置产品上下架 status;
     * @param productId
     * @param status
     * @return
     */
    public ServiceResponse<String> setSaleStatus(Integer productId,Integer status){
        // 拿不到产品id / 状态错误
        if(productId == null || status == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUEMENT.getCode(),ResponseCode.ILLEGAL_ARGUEMENT.getDesc());
        }
        // new一个Product对象 => mapper.update
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);

        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if(rowCount>0){
            return ServiceResponse.createBySuccessMessage("修改产品销售状态成功");
        }
        return ServiceResponse.createByErrorMessage("修改产品销售状态失败");
    }

    /**
     * 获取产品详情
     *
     * 我们需要一个简化的product对象projectDetailVo (先在vo 创建ProductDetailVo对象类)
     * 通过 assembleProductDetailVo 方法进行组装赋值;
     *
     * @param productId
     * @return
     */
    public ServiceResponse<ProductDetailVo> manageProductDetail(Integer productId){
        // 判定产品id非空;
        if(productId == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUEMENT.getCode(),ResponseCode.ILLEGAL_ARGUEMENT.getDesc());
        }

        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null){
            return ServiceResponse.createByErrorMessage("产品已下架或者删除");
        }

        // vo 对象 value object
        // pojo -> business object ->vo view object
        // 通过方法组装productVo 组装
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);

        return ServiceResponse.createBySuccess(productDetailVo);
    }

     // 创建 ProductDetailVo 对象 为了 manageProductDetail方法返回vo对象

    /**
     * 通过 product 组装ProductDetailVo 方法
     *
     * 1. 注意事项: 读取配置
     * @param product
     * @return
     */
    public ProductDetailVo assembleProductDetailVo(Product product){
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategroyId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());

        // 图片host 头: imageHost => 读取配置文件
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));

        // 设置 parentCategoryID(先获取parentCategoryId)
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if(category == null){
            productDetailVo.setParentCategoryId(0); // 默认根节点
        }else{
            productDetailVo.setParentCategoryId(category.getParentId());
        }


        // 设置: createTime
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        // 设置: updateTime
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));

        return productDetailVo;
    }

    /**
     * 获取产品列表
     * 附带: 分页功能和动态排序;
     *
     * pageHelper 使用方法:
     * 1. 一定要startPage -- start 记录一个开始
     * 2. 填充自己的sql 查询逻辑
     * 3. pagehelper -end 收尾
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServiceResponse getProductList(int pageNum,int pageSize){
        // pageHelper - start
        PageHelper.startPage(pageNum,pageSize);

        // 获取sql获得的对象
        List<Product> productList = productMapper.selectList();

        // 为么进行分页 同时创建 一个 包含ProductListVo 的List
        List<ProductListVo> productListVoList = Lists.newArrayList();

        // 循环塞入 assembleProductListVo 过滤不要的数据;
        for (Product productItem: productList){
            // 组装 productListVo;
            ProductListVo productListVo = assembleProductListVo(productItem);
            // 塞入过滤的数据item;
            productListVoList.add(productListVo);
        }
        // 这里是收尾
        PageInfo pageResult = new PageInfo(productListVoList);

        // 这里才是把过滤后的数据返回给前端
        pageResult.setList(productListVoList);
        return ServiceResponse.createBySuccess(pageResult);



    }

    /**
     * 组装产品列表函数
     * @param product
     * @return
     */
    private ProductListVo assembleProductListVo(Product product){

        ProductListVo productListVo = new ProductListVo();

        productListVo.setId(product.getId());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setPrice(product.getPrice());
        productListVo.setMainImage(product.getMainImage());
        productListVo.setCategroyId(product.getCategoryId());
        productListVo.setName(product.getName());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        productListVo.setStatus(product.getStatus());

        return  productListVo;
    }

    /**
     * 根据产品ID / 产品名称(模糊搜索) 具体产品
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServiceResponse<PageInfo> searchProduct(String productName,Integer productId, int pageNum, int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        // 若不为空的时候作为条件查询;
        if(StringUtils.isNoneBlank(productName)){
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
//        同理创建一个 productList和  productListVoList
        // 一个为了遍历 一个为了过滤数据 然后返回 productListVoList
        List<Product> productList= productMapper.selectByNameAndProductId(productName,productId);
        List<ProductListVo> productListVoList = Lists.newArrayList();

        for (Product productItem: productList){
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        PageInfo pageResult = new PageInfo(productListVoList);
        return ServiceResponse.createBySuccess(pageResult);
    }



    // ============================  客户端client部分 ============================

    /**
     * client端: 根据具体产品ID 获取产品详情
     * @param productId
     * @return
     */
    public ServiceResponse<ProductDetailVo> getProductDetail(Integer productId){
        if(productId == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUEMENT.getCode(),ResponseCode.ILLEGAL_ARGUEMENT.getDesc());
        }

        Product product = productMapper.selectByPrimaryKey(productId);
        // 搜不出来 或者 不等于在售状态
        if(product == null || product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()){
            return ServiceResponse.createByErrorMessage("产品已下架或者删除");
        }
        // vo 对象 value object
        // pojo -> business object ->vo view object
        // 通过方法组装productVo 组装
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);

        return ServiceResponse.createBySuccess(productDetailVo);
    }

    /**
     * client端: 根据关键字 和categoryId(分类) 进行搜索
     * 1. 注意这里可能传的是大类
     * @param keyword
     * @param categoryId
     * @param pageNum
     * @param pageSize
     * @param orderby
     * @return
     */
    public ServiceResponse<PageInfo> getProductByKeywordCategory(String keyword, Integer categoryId, int pageNum, int pageSize, String orderby){
        //  参数校验 若关键字和category未空 则为错误;
        if(StringUtils.isBlank(keyword) && categoryId == null){
            // 参数错误;
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUEMENT.getCode(),ResponseCode.ILLEGAL_ARGUEMENT.getDesc());
        }

        // 假如它传入的是个大分类  电子产品 =>(手机,电视机,电脑...) ==> 小米手机,小米电视机,华为手机/ 华硕电脑/...
        // 运用递归算法把所有的具体产品都匹配出来

        // 获取分类Id列表
        List<Integer> categoryIdList =new ArrayList<Integer>();

        // 判定是否有传入具体分类ID
        if(categoryId != null){
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            System.out.println(category.toString());
            // 可能存在不存在此分类 但是又具备关键字;
            if(category == null && StringUtils.isBlank(keyword)){
                // 没有该分类 还没有关键字, 这时候返回一个空的结果集  但不报错
                PageHelper.startPage(pageNum,pageSize);
                List<ProductDetailVo> productDetailVoList = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productDetailVoList);
                return  ServiceResponse.createBySuccess(pageInfo);
            }
            // 根据category 分类ID 去搜 递归下的分类List
            categoryIdList = iCategoryService.selectCategoryAndChildrenById(category.getId()).getData();
        }
        // 判定关键字是否不为空
        if (StringUtils.isNoneBlank(keyword)){
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }

        // PageHelper start:
        PageHelper.startPage(pageNum,pageSize);

        // 排序处理
        if(StringUtils.isNoneBlank(orderby)){
            // 动态排序
            if (Const.ProductListOrderBy.Price_ASC_DESC.contains(orderby)){
                String[] orderbyArray = orderby.split("_");
                PageHelper.orderBy(orderbyArray[0]+" "+orderbyArray[1]);
            }
        }

        // Dao层搜索 productMapper
        List<Product> productList =productMapper.selectByNameAndCategoryIds(
                StringUtils.isBlank(keyword)?null:keyword,
                categoryIdList.size()==0?null:categoryIdList);

        // 与上文雷同
        List<ProductListVo> productListVoList = Lists.newArrayList();

        for (Product product: productList){
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }
        // 进行分页
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);

        return ServiceResponse.createBySuccess(pageInfo);

    }


}
