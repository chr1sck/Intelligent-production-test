package com.bsd.say.service.impl;

import com.bsd.say.entities.Record;
import com.bsd.say.mapper.RecordMapper;
import com.bsd.say.service.RecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("recordService")
@Transactional
public class RecordServiceImpl extends BaseServiceImpl<RecordMapper,Record> implements RecordService {
}
