package com.mmall.service.impl;

import com.alipay.demo.trade.model.GoodsDetail;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mmall.common.ServiceResponse;
import com.mmall.dao.DictMapper;
import com.mmall.pojo.Dict;
import com.mmall.pojo.OrderItem;
import com.mmall.service.IDictService;
import com.mmall.util.BigDecimalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.print.Doc;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service("iDictService")
public class DictServiceImpl implements IDictService {
    @Autowired
    private DictMapper dictMapper;

    /**
     * 添加字典
     * @param dict
     * @return
     */
    public ServiceResponse addDict(Dict dict){
        // 检查是否已经存在
        String existCode = dict.getDataCode();
        String existType = dict.getDataType();
        int existRow = dictMapper.checkExistDict(existType,existCode);

        if(existRow ==1){
            return ServiceResponse.createBySuccessMessage("添加字典失败,对应字典已存在");
        }
        // 添加
        int resultCount = dictMapper.insert(dict);
        if(resultCount == 1){
            return ServiceResponse.createByErrorMessage("添加字典成功");
        }
        return ServiceResponse.createBySuccessMessage("添加字典失败");
    }
    public ServiceResponse getDict(){
        List<Dict> allDcits = dictMapper.getDictsTypes();
        Set<String> typeSet = Sets.newHashSet();
        // 获取分类;
        for (Dict type :allDcits) {
            typeSet.add(type.getDataType());
        }

        // 遍历 n*m
        Map dictionary = Maps.newHashMap(); // 外层
        for (String setItem: typeSet){
            Map kv = Maps.newHashMap();
            for(Dict dictItem :allDcits){
                if(dictItem.getDataType().equals(setItem)){
                    kv.put(dictItem.getDataCode(),dictItem.getDataValue());
                }
            }
            dictionary.put(setItem,kv);
        }
        return ServiceResponse.createBySuccess(dictionary);
    }

//    /**
//     * 更新字典
//     * @param dict
//     * @return
//     */
//    public ServiceResponse update(Dict dict){
//        int resultCount = dictMapper.updateByPrimaryKey(dict);
//        if(resultCount == 0){
//            return ServiceResponse.createByErrorMessage("更新字典失败");
//        }
//        return ServiceResponse.createBySuccessMessage("更新字典失败");
//    }
//    /**
//     * 删除字典
//     * @param id
//     * @return
//     */
//    public ServiceResponse delete(int id){
//        int resultCount = dictMapper.updateByPrimaryKey(dict);
//        if(resultCount == 0){
//            return ServiceResponse.createByErrorMessage("更新字典失败");
//        }
//        return ServiceResponse.createBySuccessMessage("更新字典失败");
//    }
//
//    /**
//     * 检查是否已有相同data_code的key
//     * @return
//     */
//    public ServiceResponse checkDictDataCodeExist(String dataCode){
//        return null;
//    }
}
