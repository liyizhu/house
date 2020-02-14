package com.liyizhu.house.web.controller;


import com.liyizhu.house.biz.service.AgencyService;
import com.liyizhu.house.common.constants.CommonConstants;
import com.liyizhu.house.common.model.Agency;
import com.liyizhu.house.common.model.User;
import com.liyizhu.house.biz.service.UserService;
import com.liyizhu.house.common.result.ResultMsg;
import com.liyizhu.house.common.utils.HashUtils;
import com.liyizhu.house.web.interceptor.UserContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    AgencyService agencyService;

    @RequestMapping("users")
    @ResponseBody
    public List<User> getUsers(){
        return userService.getUsers();
    }

    /**
     * 注册提交:
     *      1.注册验证
     *      2 发送邮件
     *      3验证失败重定向到注册页面 注册页获取:根据account对象为依据判断是否注册页获取请求
     * @param account
     * @param modelMap
     * @return
     */
    @RequestMapping("/accounts/register")
    public String accountRegister(User account, ModelMap modelMap){
        if(account==null || account.getName()==null){
            List<Agency> allAgency = agencyService.getAllAgency();
            modelMap.put("agencyList",allAgency);
            return "/user/accounts/register";
        }
        //用户验证
        ResultMsg resultMsg = UserHelper.validate(account);
        if(resultMsg.isSuccess()&&userService.addAccount(account)){
            modelMap.addAttribute("email",account.getEmail());
            return "/user/accounts/registerSubmit";
        }else{
            return "redirect:/accounts/register?" + resultMsg.asUrlParams();
        }
    }

    @RequestMapping("/accounts/verify")
    public String verify(String key){
        boolean result = userService.enable(key);
        if(result){
            return "redirect:/index?" + ResultMsg.successMsg("激活成功").asUrlParams();
        }else{
            return "redirect:/accounts/register?" + ResultMsg.errorMsg("激活失败，请确认链接是否过期").asUrlParams();
        }
    }

    /* ======================== 登陆流程 ========================= */

    /**登陆
     * target为隐藏域，保存跳转前页面，登陆后，根据target跳转回该页面
     * @param req
     * @return
     */
    @RequestMapping("/accounts/signin")
    public String signin(HttpServletRequest req){
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String target = req.getParameter("target");
        if(username==null || password==null){
            req.setAttribute("target",target);
            return "user/accounts/signin";
        }
        User user = userService.auth(username,password);
        if(user==null){
            return "redirect:/accounts/signin?"+"target="+target+"&username="+username+"&"+
                    ResultMsg.errorMsg("用户名或密码错误").asUrlParams();
        }else{
            HttpSession session = req.getSession(true);
            session.setAttribute(CommonConstants.USER_ATTRIBUTE,user);
            return StringUtils.isNoneBlank(target) ? "redirect:" + target : "redirect:/index";
        }
    }

    @RequestMapping("/accounts/logout")
    public String logout(HttpServletRequest request){
        HttpSession session = request.getSession();
        if(session!=null){
            session.invalidate();
        }
        return "redirect:/index";
    }

    /* ======================== 个人信息页 ========================= */

    /**
     * 1、提供页面信息
     * 2、更新用户信息
     * @param req
     * @param updateUser
     * @return
     */
    @RequestMapping("accounts/profile")
    public String profile(HttpServletRequest req, User updateUser){
        if(updateUser.getEmail() == null){
            updateUser = userService.getUserByEmail(UserContext.getUser().getEmail());
            req.getSession(true).setAttribute("loginUser",updateUser);
            return "user/accounts/profile";
        }
        userService.updateUser(updateUser);
        User query = new User();
        query.setEmail(updateUser.getEmail());
        List<User> users = userService.getUserbyQuery(query);
        req.getSession(true).setAttribute(CommonConstants.USER_ATTRIBUTE,users.get(0));
        return "redirect:/accounts/profile?"+ ResultMsg.successMsg("更新成功").asUrlParams();
    }

    /**
     * 修改密码操作
     * @param email
     * @param password
     * @param newPassword
     * @param confirmPassword
     * @param modelMap
     * @return
     */
    @RequestMapping("accounts/changePassword")
    public String changePassword(String email,String password,String newPassword,
                                 String confirmPassword,ModelMap modelMap){
        User user = userService.auth(email,password);
        if(user==null || !confirmPassword.equals(newPassword)){
            return "redirect:/accounts/profile?"+ResultMsg.errorMsg("密码错误").asUrlParams();
        }
        User updateUser = new User();
        updateUser.setEmail(email);
        updateUser.setPasswd(HashUtils.encryPassword(newPassword));
        userService.updateUser(updateUser);
        return "redirect:/accounts/profile?"+ResultMsg.successMsg("更新成功").asUrlParams();
    }

    /**
     * 忘记密码
     * @param username
     * @param modelMap
     * @return
     */
    @RequestMapping("accounts/remember")
    public String remenber(String username,ModelMap modelMap){
        if(StringUtils.isBlank(username)){
            return "redirect:/accounts/signin?"+ResultMsg.errorMsg("邮箱不能为空").asUrlParams();
        }
        userService.resetNotify(username);
        modelMap.put("email",username);
        return "/user/accounts/remember";
    }

    @RequestMapping("accounts/reset")
    public String reset(String key,ModelMap modelMap){
        String email = userService.getResetEmail(key);
        if(StringUtils.isBlank(email)){
            return "redirect:/accounts/signin?"+ResultMsg.errorMsg("重置链接已经过期").asUrlParams();
        }
        modelMap.put("email",email);
        modelMap.put("success_key",key);
        return "/user/accounts/reset";
    }

    @RequestMapping("accounts/resetSubmit")
    public String resetSubmit(HttpServletRequest request,User user){
        ResultMsg retMsg = UserHelper.validateResetPassword(user.getKey(),user.getPasswd(),user.getConfirmPasswd());
        if(!retMsg.isSuccess()){
            String suffix = "";
            if(StringUtils.isNotBlank(user.getKey())){
                suffix = "email=" + userService.getResetEmail(user.getKey()) +"&key=" + user.getKey()+"&";
            }
            return "redirect:/accounts/reset?" + suffix + retMsg.asUrlParams();
        }
        User updateUser = userService.reset(user.getKey(),user.getPasswd());
        request.getSession(true).setAttribute(CommonConstants.USER_ATTRIBUTE,updateUser);
        return "redirect:/index?" + retMsg.asUrlParams();
    }

}
