package com.bsd.say.controller;

import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.LoveLetter;
import com.bsd.say.service.LoveLetterService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("love-letter")
public class LoveLetterController extends BaseController<LoveLetterService, LoveLetter>{

    @Resource
    private LoveLetterService loveLetterService;

    @Override
    public LoveLetterService getBaseService() {
        return super.getBaseService();
    }

    @RequestMapping("save")
    public AjaxResult save(@RequestBody AjaxRequest ajaxRequest, HttpServletRequest request) {
        saveOrUpdate(ajaxRequest);
        return new AjaxResult();
    }

    /**
     * 生成情书
     */
    @RequestMapping(value = "/create-love-letter")
    @ResponseBody
    public AjaxResult createLoveLetter(@RequestBody AjaxRequest ajaxRequest){

        AjaxResult ajaxResult = new AjaxResult();
        try {
            ajaxResult = loveLetterService.createLoveLetter(ajaxRequest);
        } catch (Exception e) {
            e.printStackTrace();
            String errMsg = e.getMessage() != null ? e.getMessage() : "操作失败";
            ajaxResult.setRetcode(AjaxResult.FAILED);
            ajaxResult.setRetmsg(errMsg);
        }
        return ajaxResult;
    }

    /**
     * 查看情书
     */
    @RequestMapping(value = "/get-love-letter")
    @ResponseBody
    public AjaxResult getLoveLetter(@RequestBody AjaxRequest ajaxRequest){
        AjaxResult ajaxResult = new AjaxResult();
        try {
            ajaxResult = loveLetterService.getLoveLetter(ajaxRequest);
        } catch (Exception e) {
            e.printStackTrace();
            String errMsg = e.getMessage() != null ? e.getMessage() : "操作失败";
            ajaxResult.setRetcode(AjaxResult.FAILED);
            ajaxResult.setRetmsg(errMsg);
        }
        return ajaxResult;
    }
}
