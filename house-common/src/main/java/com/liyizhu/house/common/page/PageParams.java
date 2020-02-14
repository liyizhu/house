package com.liyizhu.house.common.page;

public class PageParams {
    public static final Integer PAGE_SIZE = 5;

    private Integer pageSize;
    private Integer pageNum;
    private Integer offset;
    private Integer limit;

    public PageParams(){

    }

    public PageParams(Integer pageSize,Integer pageNum){
        this.pageSize = pageSize;
        this.pageNum = pageNum;
        this.offset = pageSize * (pageNum-1);
        this.limit = pageSize;
    }

    public static PageParams build(Integer pageSize,Integer pageNum){
        if(pageNum==null){
            pageNum=1;
        }
        if(pageSize==null){
            pageSize = PAGE_SIZE;
        }
        return new PageParams(pageSize,pageNum);
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
