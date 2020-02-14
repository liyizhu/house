package com.liyizhu.house.web.controller;


import com.google.common.base.Objects;
import com.liyizhu.house.common.model.User;
import com.liyizhu.house.common.result.ResultMsg;
import org.apache.commons.lang3.StringUtils;

public class UserHelper {

    public static ResultMsg validate(User account){
        if(StringUtils.isBlank(account.getEmail())){
            return ResultMsg.errorMsg("Email 有误");
        }
        if(StringUtils.isBlank(account.getPasswd()) || StringUtils.isBlank(account.getConfirmPasswd()) || !account.getPasswd().equals(account.getConfirmPasswd())){
            return ResultMsg.errorMsg("请保证密码不为空且两次输入密码一致");
        }
        if(account.getPasswd().length()<6){
            return ResultMsg.errorMsg("密码长度至少为六位");
        }
        return ResultMsg.successMsg("");
    }


    public static ResultMsg validateResetPassword(String key, String password, String confirmPassword) {
        if (StringUtils.isBlank(key) || StringUtils.isBlank(password) || StringUtils.isBlank(confirmPassword)) {
            return ResultMsg.errorMsg("参数有误");
        }
        if (!Objects.equal(password, confirmPassword)) {
            return ResultMsg.errorMsg("密码必须与确认密码一致");
        }
        return ResultMsg.successMsg("");
    }
}
