package com.bsd.say.service;

import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.LoveLetter;

public interface LoveLetterService extends BaseService<LoveLetter>{
    AjaxResult createLoveLetter(AjaxRequest ajaxRequest);
}
