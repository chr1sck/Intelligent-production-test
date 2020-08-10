package com.bsd.say.controller;

import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.AwardList;
import com.bsd.say.service.AwardListService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("award-list")
@CrossOrigin
public class AwardListController extends BaseController<AwardListService, AwardList>{
    @Resource
    private AwardListService awardListService;
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

    /**
     * 是否有资格进行抽奖
     */
    @RequestMapping(value = "/is-valid-lottery")
    @ResponseBody
    public AjaxResult isValidLottery(@RequestBody AjaxRequest ajaxRequest){
        AjaxResult ajaxResult = new AjaxResult();
        try {
            ajaxResult = awardListService.isValidLottery(ajaxRequest);
        } catch (Exception e) {
            e.printStackTrace();
            String errMsg = e.getMessage() != null ? e.getMessage() : "操作失败";
            ajaxResult.setRetcode(AjaxResult.FAILED);
            ajaxResult.setRetmsg(errMsg);
        }
        return ajaxResult;
    }

    @RequestMapping("award")
    public AjaxResult award(@RequestBody AjaxRequest ajaxRequest){
        AjaxResult ajaxResult = new AjaxResult();
        try {
            ajaxResult = awardListService.award(ajaxRequest);
        } catch (Exception e) {
            e.printStackTrace();
            String errMsg = e.getMessage() != null ? e.getMessage() : "操作失败";
            ajaxResult.setRetcode(AjaxResult.FAILED);
            ajaxResult.setRetmsg(errMsg);
        }
        return ajaxResult;
    }

    /**
     * 保存一等奖信息
     */
    @RequestMapping(value = "/save-award")
    @ResponseBody
    public AjaxResult saveAward(@RequestBody AjaxRequest ajaxRequest){
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult = awardListService.saveAward(ajaxRequest);
        return ajaxResult;
    }

    /**
     * 获取自己的抽奖和优惠券
     */
    @RequestMapping(value = "/get-award-list")
    @ResponseBody
    public AjaxResult getAwardList(@RequestBody AjaxRequest ajaxRequest){
        AjaxResult ajaxResult = new AjaxResult();
        try {
            ajaxResult = awardListService.getAwardList(ajaxRequest);
        } catch (Exception e) {
            e.printStackTrace();
            String errMsg = e.getMessage() != null ? e.getMessage() : "操作失败";
            ajaxResult.setRetcode(AjaxResult.FAILED);
            ajaxResult.setRetmsg(errMsg);
        }
        return ajaxResult;
    }
}
