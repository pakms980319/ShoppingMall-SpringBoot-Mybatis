package com.example.app.shopping.domain.dto.common;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PageDto {
    //페이지정보(전체페이지,현재페이지)
    private int totalpage;			//총게시물건수 / amount
    private Criteria criteria;		//현재페이지,한페이지당 읽을 게시물의 건수가 저장되어있음

    //블럭정보
    private int pagePerBlock;		//블럭에 표시할 페이지개수(15건 지정)
    private int totalBlock;			//totalpage / pagePerBlock
    private int nowBlock;			//현재페이지번호 /pagePerBlock

    //표시할 페이지(블럭에표시할 시작페이지-마지막페이지)
    private int startPage;
    private int endPage;

    //NextPrev 버튼 표시여부
    private boolean prev,next;

    public PageDto(int totalcount,Criteria criteria) {


        this.criteria = criteria;

        //전체페이지 계산
        totalpage =(int)Math.ceil((1.0*totalcount)/criteria.getAmount());

        if (totalpage == 0) {totalpage=1;}

        //블럭계산
        pagePerBlock=5;
        totalBlock = (int)Math.ceil( (1.0*totalpage) / pagePerBlock );
        nowBlock =  (int)Math.ceil ((1.0*criteria.getPageno()) / pagePerBlock);

        //Next,Prev 버튼 활성화 유무
        prev=nowBlock>1;
        next=nowBlock<totalBlock;

        //블럭에 표시할 페이지 번호 계산
        this.endPage = (nowBlock * pagePerBlock<totalpage)?nowBlock * pagePerBlock:totalpage;
        this.startPage=nowBlock * pagePerBlock -pagePerBlock + 1;
    }
}
