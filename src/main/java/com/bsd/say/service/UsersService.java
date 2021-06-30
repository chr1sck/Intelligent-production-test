package com.bsd.say.service;

import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.Users;

public interface UsersService extends BaseService<Users>{

    AjaxResult sendNote(AjaxRequest ajaxRequest);

    AjaxResult isSubscribe(AjaxRequest ajaxRequest);

    AjaxResult getUserInfoByOpenId(AjaxRequest ajaxRequest);
}
