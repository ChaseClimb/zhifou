package com.wenda.service;

import com.wenda.dao.UserDao;
import com.wenda.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    UserDao userDao;

    public User getUserById(int id){
        return userDao.getUserById(id);
    };
}
