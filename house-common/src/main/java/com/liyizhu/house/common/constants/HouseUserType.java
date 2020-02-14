package com.liyizhu.house.common.constants;

public enum  HouseUserType {

    // 1、售卖 2、收藏
    SALE(1),BOOKMARK(2);

    public final Integer value;

    private HouseUserType(Integer value){
        this.value = value;
    }
}
