package com.bsd.say.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bsd.say.entities.Users;
import org.springframework.stereotype.Repository;

@Repository("usersMapper")
public interface UsersMapper extends BaseMapper<Users> {
}
