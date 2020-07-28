package com.bsd.say.service.impl;

import com.bsd.say.entities.Users;
import com.bsd.say.mapper.UsersMapper;
import com.bsd.say.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("usersService")
@Transactional
public class UsersServiceImpl extends BaseServiceImpl<UsersMapper,Users> implements UsersService {
    @Autowired
    protected UsersMapper usersMapper;

    @Override
    public UsersMapper getBaseMapper() {
        return this.usersMapper;
    }
}
