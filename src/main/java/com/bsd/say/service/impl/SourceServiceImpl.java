package com.bsd.say.service.impl;

import com.bsd.say.entities.Source;
import com.bsd.say.mapper.SourceMapper;
import com.bsd.say.service.SourceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("sourceService")
@Transactional
public class SourceServiceImpl extends BaseServiceImpl<SourceMapper,Source> implements SourceService {
}
