package com.liyizhu.house.web.controller;

import com.liyizhu.house.common.model.User;
import com.liyizhu.house.biz.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class HelloController {

    @Autowired
    UserService userService;

    @RequestMapping("hello")
    public String hello(ModelMap modelMap){
        List<User> users = userService.getUsers();
        User user = users.get(0);
        /*if(user != null){
            throw new IllegalArgumentException("by user eq null");
        }*/
        modelMap.addAttribute("user",user);
        return "hello";
    }

}
