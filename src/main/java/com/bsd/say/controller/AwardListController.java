package com.bsd.say.controller;

import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.AwardList;
import com.bsd.say.service.AwardListService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("award-list")
public class AwardListController extends BaseController<AwardListService, AwardList>{
    @Override
    public AwardListService getBaseService() {
        return super.getBaseService();
    }

    @RequestMapping("save")
    public AjaxResult save(@RequestBody AjaxRequest ajaxRequest, HttpServletRequest request) {
        saveOrUpdate(ajaxRequest);
        return new AjaxResult();
    }

    @RequestMapping("page")
    public AjaxResult page(@RequestBody AjaxRequest ajaxRequest) {

        final AjaxResult page1 = super.page(ajaxRequest);
        AjaxResult ajaxResult = page1;
        System.out.println(ajaxResult);
        return ajaxResult;
    }
}
