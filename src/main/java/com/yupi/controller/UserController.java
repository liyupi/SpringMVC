package com.yupi.controller;

import com.yupi.springmvc.annotation.ExtController;
import com.yupi.springmvc.annotation.ExtRequestMapping;

/**
 * 功能描述：UserController测试类
 *
 * @author Yupi Li
 * @date 2018/9/1 22:10
 */

@ExtController
@ExtRequestMapping("/user")
public class UserController {

    @ExtRequestMapping("/get")
    public String getWord() {
        System.out.println("测试手写SpringMVC");
        return "good";
    }
}
