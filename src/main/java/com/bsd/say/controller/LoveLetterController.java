package com.bsd.say.controller;

import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.LoveLetter;
import com.bsd.say.service.LoveLetterService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("love-letter")
public class LoveLetterController extends BaseController<LoveLetterService, LoveLetter>{

    @Override
    public LoveLetterService getBaseService() {
        return super.getBaseService();
    }

    @RequestMapping("save")
    public AjaxResult save(@RequestBody AjaxRequest ajaxRequest, HttpServletRequest request) {
        saveOrUpdate(ajaxRequest);
        return new AjaxResult();
    }
}
