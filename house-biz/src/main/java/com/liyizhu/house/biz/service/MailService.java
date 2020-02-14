package com.liyizhu.house.biz.service;

import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.liyizhu.house.biz.mapper.UserMapper;
import com.liyizhu.house.common.model.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${domain.name}")
    private String domainName;

    @Autowired
    private UserMapper userMapper;

    private final Cache<String,String > registerCache =
            CacheBuilder.newBuilder().maximumSize(100).expireAfterAccess(15, TimeUnit.MINUTES)
                .removalListener(new RemovalListener<String, String>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, String> notification) {
                        String email = notification.getValue();
                        User user = new User();
                        user.setEmail(email);
                        List<User> targetUser = userMapper.selectUserByQuery(user);
                        if(!targetUser.isEmpty() && Objects.equal(targetUser.get(0).getEnable(),0)){
                            userMapper.delete(email);//在删除前判断用户是否已经激活，未激活的用户进行删除
                        }
                    }
                }).build();
    private final Cache<String, String> resetCache =  CacheBuilder.newBuilder().maximumSize(100).
                                    expireAfterAccess(15, TimeUnit.MINUTES).build();


    /**
     * 1.缓存key-email的关系
     * 2.借助spring mail 发送邮件
     * 3.借助异步框架进行异步操作
     * @param email
     */
    @Async
    public void registerNotify(String email) {
        String randomKey = RandomStringUtils.randomAlphabetic(10);
        registerCache.put(randomKey,email);
        String url = "http://" + domainName + "/accounts/verify?key=" + randomKey;
        sendEmail("房产销售平台账号激活邮件",url,email);
    }

    @Async
    public void sendEmail(String title, String url, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setSubject(title);
        message.setTo(email);
        message.setText(url);
        mailSender.send(message);
    }

    public boolean enable(String key) {
        String email = registerCache.getIfPresent(key);
        if(StringUtils.isBlank(email)){
            return false;
        }
        User updateUser = new User();
        updateUser.setEmail(email);
        updateUser.setEnable(1);
        userMapper.update(updateUser);
        registerCache.invalidate(key);
        return true;
    }

    @Async
    public void resetNotify(String username) {
        String randomKey = RandomStringUtils.randomAlphabetic(10);
        resetCache.put(randomKey,username);
        String url = "http://" + domainName + "/accounts/reset?key=" + randomKey;
        sendEmail("房产销售平台重置密码邮件",url,username);
    }

    public String getResetEmail(String key) {
        return resetCache.getIfPresent(key);
    }

    public void invalidateResetKey(String key) {
        resetCache.invalidate(key);
    }
}
