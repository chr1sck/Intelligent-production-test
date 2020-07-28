package com.bsd.say.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bsd.say.entities.AwardList;
import org.springframework.stereotype.Repository;

@Repository("awardListMapper")
public interface AwardListMapper extends BaseMapper<AwardList> {
}
