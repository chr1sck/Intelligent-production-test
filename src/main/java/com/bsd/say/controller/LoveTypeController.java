package com.bsd.say.controller;

import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.LoveType;
import com.bsd.say.service.LoveTypeService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("love-type")
public class LoveTypeController extends BaseController<LoveTypeService, LoveType>{
    @Resource
    private LoveTypeService loveTypeService;

    @Override
    public LoveTypeService getBaseService() {
        return super.getBaseService();
    }

    @RequestMapping("save")
    public AjaxResult save(@RequestBody AjaxRequest ajaxRequest, HttpServletRequest request) {
        saveOrUpdate(ajaxRequest);
        return new AjaxResult();
    }

    @RequestMapping("list")
    public AjaxResult list(){
        AjaxResult ajaxResult = new AjaxResult();
        try {
            ajaxResult = loveTypeService.getLoveTypeList();
        } catch (Exception e) {
            e.printStackTrace();
            String errMsg = e.getMessage() != null ? e.getMessage() : "操作失败";
            ajaxResult.setRetcode(AjaxResult.FAILED);
            ajaxResult.setRetmsg(errMsg);
        }
        return ajaxResult;
    }

    @RequestMapping("select-id")
    public AjaxResult getLoveTypeById(@RequestBody AjaxRequest ajaxRequest){
        AjaxResult ajaxResult = new AjaxResult();
        try {
            ajaxResult = loveTypeService.getLoveTypeById(ajaxRequest);
        } catch (Exception e) {
            e.printStackTrace();
            String errMsg = e.getMessage() != null ? e.getMessage() : "操作失败";
            ajaxResult.setRetcode(AjaxResult.FAILED);
            ajaxResult.setRetmsg(errMsg);
        }
        return ajaxResult;
    }
}
