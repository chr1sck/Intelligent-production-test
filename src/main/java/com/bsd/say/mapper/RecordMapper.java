package com.bsd.say.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bsd.say.entities.Record;
import org.springframework.stereotype.Repository;

@Repository("recordMapper")
public interface RecordMapper extends BaseMapper<Record> {
}
