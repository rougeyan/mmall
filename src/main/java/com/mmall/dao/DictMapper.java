package com.mmall.dao;

import com.mmall.pojo.Dict;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface DictMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Dict record);

    int insertSelective(Dict record);

    Dict selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Dict record);

    int updateByPrimaryKey(Dict record);

    List<Dict> getDictsTypes();

    int checkExistDict(@Param("dataType") String dataType, @Param("dataCode") String dataCode);
}