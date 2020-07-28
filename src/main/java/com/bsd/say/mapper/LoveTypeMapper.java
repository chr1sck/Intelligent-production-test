package com.bsd.say.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bsd.say.entities.LoveType;
import org.springframework.stereotype.Repository;

@Repository("loveTypeMapper")
public interface LoveTypeMapper extends BaseMapper<LoveType> {
}
