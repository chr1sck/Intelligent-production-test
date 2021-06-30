package com.bsd.say.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bsd.say.entities.LoveLetter;
import org.springframework.stereotype.Repository;

@Repository("loveLetterMapper")
public interface LoveLetterMapper extends BaseMapper<LoveLetter> {
}
