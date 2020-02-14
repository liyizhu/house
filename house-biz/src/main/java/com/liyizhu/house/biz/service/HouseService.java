package com.liyizhu.house.biz.service;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.liyizhu.house.biz.mapper.HouseMapper;
import com.liyizhu.house.common.constants.HouseUserType;
import com.liyizhu.house.common.model.*;
import com.liyizhu.house.common.page.PageData;
import com.liyizhu.house.common.page.PageParams;
import com.liyizhu.house.common.utils.BeanHelper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HouseService {

    @Autowired
    HouseMapper houseMapper;

    @Value("${file.prefix}")
    private String imgPrefix;

    @Autowired
    FileService fileService;

    @Autowired
    AgencyService agencyService;

    @Autowired
    MailService mailService;

    /**
     * 1.查询小区
     * 2.添加图片服务器地址前缀
     * 3.构建分页结果
     * @param query
     * @param pageParams
     * @return
     */
    public PageData<House> queryHouse(House query, PageParams pageParams) {
        List<House> houses = Lists.newArrayList();
        if(!Strings.isNullOrEmpty(query.getName())){
            Community community = new Community();
            community.setName(query.getName());
            List<Community> communities = houseMapper.selectCommunity(community);
            if(!communities.isEmpty()){
                query.setCommunityId(communities.get(0).getId());
            }
        }
        houses = queryAndSetImg(query,pageParams);//添加图片服务器前缀
        Long count = houseMapper.selectPageCount(query);
        return PageData.buildPage(houses,count,pageParams.getPageSize(),pageParams.getPageNum());

    }

    public List<House> queryAndSetImg(House query, PageParams pageParams) {
        List<House> houses = houseMapper.selectPageHouses(query,pageParams);
        houses.forEach(h->{
            h.setFirstImg(imgPrefix + h.getImages().split(",")[0]);
            h.setImageList(Arrays.asList(h.getImages().split(",")).stream().map(img -> imgPrefix + img).collect(Collectors.toList()));
            h.setFloorPlanList(Arrays.asList(h.getFloorPlan().split(",")).stream().map(img -> imgPrefix + img).collect(Collectors.toList()));
        });
        return houses;
    }

    public List<Community> getAllCommunitys() {
        Community community = new Community();
        return houseMapper.selectCommunity(community);
    }

    /**
     * 添加房屋图片
     * 添加户型图片
     * 插入房产信息
     * 绑定用户到房产的关系
     * @param house
     * @param user
     */
    public void addHouse(House house, User user) {
        if(CollectionUtils.isNotEmpty(house.getHouseFiles())){
            String images = Joiner.on(",").join(fileService.getImgPaths(house.getHouseFiles()));
            house.setImages(images);
        }
        if(CollectionUtils.isNotEmpty(house.getFloorPlanFiles())){
            String images = Joiner.on(",").join(fileService.getImgPaths(house.getFloorPlanFiles()));
            house.setFloorPlan(images);
            BeanHelper.onInsert(house);
            houseMapper.insert(house);
            bindUser2House(house.getId(),user.getId(),false);
        }
    }

    public void bindUser2House(Long houseId, Long userId, boolean collect) {
        HouseUser existhouseUser = houseMapper.selectHouseUser(userId,houseId,collect ? HouseUserType.BOOKMARK.value:HouseUserType.SALE.value);
        if(existhouseUser!=null){
            return ;
        }
        HouseUser houseUser = new HouseUser();
        houseUser.setHouseId(houseId);
        houseUser.setUserId(userId);
        houseUser.setType(collect ? HouseUserType.BOOKMARK.value:HouseUserType.SALE.value);
        BeanHelper.setDefaultProp(houseUser,HouseUser.class);
        BeanHelper.onInsert(houseUser);
        houseMapper.insertHouseUser(houseUser);
    }

    public HouseUser getHouseUser(Long houseId) {
        HouseUser houseUser = houseMapper.selectSaleHouseUser(houseId);
        return houseUser;
    }

    public House queryOneHouse(Long id) {
        House query = new House();
        query.setId(id);
        List<House> houses = queryAndSetImg(query, PageParams.build(1, 1));
        if (!houses.isEmpty()) {
            return houses.get(0);
        }
        return null;
    }

    public void addUserMsg(UserMsg userMsg) {
        BeanHelper.onInsert(userMsg);
        houseMapper.insertUserMsg(userMsg);
        User agency = agencyService.getAgentDeail(userMsg.getAgentId());
        mailService.sendEmail("来自用户" + userMsg.getEmail() + "的留言",userMsg.getMsg(),agency.getEmail());
    }

    public void updateRating(Long id, Double rating) {
        House house = queryOneHouse(id);
        Double oldRating = house.getRating();
        Double newRating = oldRating.equals(0D) ? rating :Math.min((oldRating+rating)/2,5);
        House updateHouse = new House();
        updateHouse.setId(id);
        updateHouse.setRating(newRating);
        BeanHelper.onInsert(updateHouse);
        houseMapper.updateHouse(updateHouse);
    }

    public void unbindUser2House(Long houseId, Long userId, HouseUserType type) {
        if(type.equals(HouseUserType.SALE)){
            houseMapper.downHouse(houseId);
        }else{
            houseMapper.deleteHouseUser(houseId,userId,type.value);
        }
    }
}
