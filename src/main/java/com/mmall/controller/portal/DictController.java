package com.mmall.controller.portal;

import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Dict;
import com.mmall.service.IDictService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/dict/")
public class DictController {
    @Autowired
    private IDictService iDictService;

    @RequestMapping(value ="addDict.do",method = RequestMethod.POST)
    @ResponseBody()
    public ServiceResponse<String> addDict(Dict dict){
        String dataType = dict.getDataType();
        String dataCode = dict.getDataCode();
        String dataValue = dict.getDataValue();
        if(StringUtils.isNotBlank(dataType) || StringUtils.isNotBlank(dataCode) || StringUtils.isNotBlank(dataValue)){
            return iDictService.addDict(dict);
        }
        return ServiceResponse.createByErrorMessage("填写有误,重新填写");
    }

    @RequestMapping(value ="getDict.do",method = RequestMethod.GET)
    @ResponseBody()
    public ServiceResponse getDicts(){
        return iDictService.getDict();
    }
}
