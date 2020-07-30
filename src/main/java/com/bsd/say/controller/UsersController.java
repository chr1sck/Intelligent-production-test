package com.bsd.say.controller;

import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.Users;
import com.bsd.say.service.UsersService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("users")
@CrossOrigin
public class UsersController extends BaseController<UsersService, Users>{
    @Resource
    private UsersService usersService;

    @Override
    public UsersService getBaseService() {
        return super.getBaseService();
    }

    @RequestMapping(value = "/send-note")
    @ResponseBody
    public AjaxResult sendNote(@RequestBody AjaxRequest ajaxRequest){

        AjaxResult ajaxResult = new AjaxResult();
        try {
            ajaxResult = usersService.sendNote(ajaxRequest);
        } catch (Exception e) {
            e.printStackTrace();
            String errMsg = e.getMessage() != null ? e.getMessage() : "操作失败";
            ajaxResult.setRetcode(AjaxResult.FAILED);
            ajaxResult.setRetmsg(errMsg);
        }
        return ajaxResult;
    }

}
