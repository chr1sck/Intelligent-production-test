package com.bsd.say.service;

import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.LoveType;

public interface LoveTypeService extends BaseService<LoveType>{
    AjaxResult getLoveTypeList();
    AjaxResult getLoveTypeById(AjaxRequest ajaxRequest);
}
