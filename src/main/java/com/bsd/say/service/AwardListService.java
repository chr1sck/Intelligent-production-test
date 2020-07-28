package com.bsd.say.service;

import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.AwardList;

public interface AwardListService extends BaseService<AwardList>{
    AjaxResult award(AjaxRequest ajaxRequest);
}
