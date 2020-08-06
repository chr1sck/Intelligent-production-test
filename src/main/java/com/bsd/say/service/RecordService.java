package com.bsd.say.service;

import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.Record;

public interface RecordService  extends BaseService<Record>{
    AjaxResult createRecord(AjaxRequest ajaxRequest);

}
