package com.example.app.shopping.domain.service.productInquiryBoard;

import com.example.app.shopping.domain.dto.common.Criteria;
import com.example.app.shopping.domain.dto.common.PageDto;
import com.example.app.shopping.domain.mapper.ProductInquiryBoardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class productInquiryBoardServiceImpl implements productInquiryBoardService{

    @Autowired
    private ProductInquiryBoardMapper productInquiryBoardMapper;

    @Override
    @Transactional
    public Map<String, Object> getproductInquiryBoards(Criteria criteria) throws Exception {
        // 검색 결과로 나오는 게시글 수 확인
        int count = productInquiryBoardMapper.findProductInquiryBoardsCount(criteria);
        PageDto pageDto = new PageDto(count, criteria);

        // 시작 게시물 번호 구하기
        int offset = (criteria.getPageno() - 1) * criteria.getAmount();

        List<Map<String, Object>> list = productInquiryBoardMapper.findProductInquiryBoards(criteria, offset);

        Map<String, Object> returnVal = new HashMap<>();
        returnVal.put("list", list);
        returnVal.put("pageDto", pageDto);

        return returnVal;
    }

    // id 를 전달받아 상품 문의 게시판을 상세 조회한 결과를 반환한다
    @Override
    @Transactional
    public Map<String, Object> getproductInquiryBoardDetail(Integer id) throws Exception {
        return productInquiryBoardMapper.findProductInquiryBoardById(id);
    }
}