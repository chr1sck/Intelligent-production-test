package com.bsd.say.service.impl;

import com.bsd.say.entities.LoveLetter;
import com.bsd.say.mapper.LoveLetterMapper;
import com.bsd.say.service.LoveLetterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("loveLetterService")
@Transactional
public class LoveLetterServiceImpl extends BaseServiceImpl<LoveLetterMapper, LoveLetter> implements LoveLetterService {
    @Autowired
    protected LoveLetterMapper loveLetterMapper;

    @Override
    public LoveLetterMapper getBaseMapper() {
        return this.loveLetterMapper;
    }
}
