package com.bsd.say.service;

import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.AwardList;
import com.bsd.say.exception.AreadyAwardException;

public interface AwardListService extends BaseService<AwardList>{
    AjaxResult award(AjaxRequest ajaxRequest) throws AreadyAwardException;

    AjaxResult isValidLottery(AjaxRequest ajaxRequest);

    AjaxResult saveAward(AjaxRequest ajaxRequest);

    AjaxResult getAwardList(AjaxRequest ajaxRequest);
}
