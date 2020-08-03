package com.mmall.service;

import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Dict;

public interface IDictService {
    ServiceResponse addDict(Dict dict);

    ServiceResponse getDict();
}
