package com.liyizhu.house.web.controller;

import com.liyizhu.house.biz.service.*;
import com.liyizhu.house.common.constants.CommonConstants;
import com.liyizhu.house.common.constants.HouseUserType;
import com.liyizhu.house.common.model.*;
import com.liyizhu.house.common.page.PageData;
import com.liyizhu.house.common.page.PageParams;
import com.liyizhu.house.common.result.ResultMsg;
import com.liyizhu.house.web.interceptor.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class HouseController {

    @Autowired
    private HouseService houseService;

    @Autowired
    private CityService cityService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private RecommendService recommendService;

    @Autowired
    private AgencyService agencyService;


    /**
     * 1.实现分页
     * 2.支持小区搜索、类型搜索
     * 3.支持排序
     * 4.支持展示图片、价格、标题、地址等信息
     * @param pageSize
     * @param pageNum
     * @param query
     * @param modelMap
     * @return
     */
    @RequestMapping("house/list")
    public String houseList(Integer pageSize, Integer pageNum, House query, ModelMap modelMap){
        PageParams pageParams = PageParams.build(pageSize,pageNum);
        PageData<House> ps = houseService.queryHouse(query,pageParams);
        //热门推荐待写
        List<House> hotHouses = recommendService.getHotHouse(CommonConstants.RECOM_SIZE);
        modelMap.put("ps",ps);
        modelMap.put("vo",query);
        modelMap.put("recomHouses",hotHouses);
        return "house/listing";
    }

    /**
     * 1、查询出所有城市，以及小区
     * 2、转到房产添加页面
     * @param modelMap
     * @return
     */
    @RequestMapping("house/toAdd")
    public String toAdd(ModelMap modelMap){
        modelMap.put("citys",cityService.getAllCitys());
        modelMap.put("communitys",houseService.getAllCommunitys());
        return "house/add";
    }

    @RequestMapping("house/add")
    public String doAdd(House house){
        User user = UserContext.getUser();
        house.setState(CommonConstants.HOUSE_STATE_UP);
        houseService.addHouse(house,user);
        return "redirect:/house/ownlist";
    }

    @RequestMapping("house/ownlist")
    public String ownlist(House house,Integer pageNum,Integer pageSize,ModelMap modelMap){
        User user = UserContext.getUser();
        house.setUserId(user.getId());
        house.setBookmarked(false);
        modelMap.put("pageType","own");
        modelMap.put("ps",houseService.queryHouse(house,PageParams.build(pageSize,pageNum)));
        return "house/ownlist";
    }

    /**
     * 查询房屋详情
     * 查询关联经纪人
     * @param id
     * @param modelMap
     * @return
     */
    @RequestMapping("house/detail")
    public String houseDetail(Long id,ModelMap modelMap){
        House house = houseService.queryOneHouse(id);
        HouseUser houseUser = houseService.getHouseUser(id);
        recommendService.increase(id);
        List<Comment> comments = commentService.getHouseComments(id,8);
        if (houseUser.getUserId() != null && !houseUser.getUserId().equals(0)) {
            modelMap.put("agent", agencyService.getAgentDeail(houseUser.getUserId()));
        }
        List<House> rcHouses =  recommendService.getHotHouse(CommonConstants.RECOM_SIZE);
        modelMap.put("recomHouses", rcHouses);
        modelMap.put("house", house);
        modelMap.put("commentList", comments);
        return "/house/detail";
    }

    @RequestMapping("house/leaveMsg")
    public String houseMsg(UserMsg userMsg){
        houseService.addUserMsg(userMsg);
        return "redirect:/house/detail?id=" + userMsg.getHouseId() + ResultMsg.successMsg("留言成功").asUrlParams();
    }

    // 评分
    @ResponseBody
    @RequestMapping("house/rating")
    public ResultMsg houseRate(Double rating,Long id){
        houseService.updateRating(id,rating);
        return ResultMsg.successMsg("ok");
    }

    // 收藏
    @ResponseBody
    @RequestMapping("house/bookmark")
    public ResultMsg bookmark(Long id){
        User user = UserContext.getUser();
        houseService.bindUser2House(id,user.getId(),true);
        return ResultMsg.successMsg("ok");
    }

    // 取消收藏
    @ResponseBody
    @RequestMapping("house/unbookmark")
    public ResultMsg unbookmark(Long id){
        User user = UserContext.getUser();
        houseService.unbindUser2House(id,user.getId(),HouseUserType.BOOKMARK);
        return ResultMsg.successMsg("ok");
    }

    // 删除房产
    @RequestMapping("house/del")
    public String delsale(Long id,String pageType){
        User user = UserContext.getUser();
        houseService.unbindUser2House(id,user.getId(),pageType.equals("own")? HouseUserType.SALE:HouseUserType.BOOKMARK);
        return "redirect:/house/ownlist";
    }

    // 收藏列表
    @RequestMapping("house/bookmarked")
    public String bookmarked(House house,Integer pageNum,Integer pageSize,ModelMap modelMap){
        User user = UserContext.getUser();
        house.setBookmarked(true);
        house.setUserId(user.getId());
        modelMap.put("ps",houseService.queryHouse(house,PageParams.build(pageSize,pageNum)));
        modelMap.put("pageType","book");
        return "/house/ownlist";
    }
}
