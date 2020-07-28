package com.bsd.say.service.impl;

import com.bsd.say.entities.AwardList;
import com.bsd.say.mapper.AwardListMapper;
import com.bsd.say.service.AwardListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("awardListService")
@Transactional
public class AwardListServiceImpl extends BaseServiceImpl<AwardListMapper, AwardList> implements AwardListService {
    @Autowired
    protected AwardListMapper awardListMapper;

    @Override
    public AwardListMapper getBaseMapper() {
        return this.awardListMapper;
    }
}
