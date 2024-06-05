package com.example.app.shopping.domain.service.productReviewBoard;

import com.example.app.shopping.domain.dto.common.Criteria;
import com.example.app.shopping.domain.dto.common.PageDto;
import com.example.app.shopping.domain.mapper.ProductReviewBoardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductReviewBoardServiceImpl implements ProductReviewBoardService {

    @Autowired
    private ProductReviewBoardMapper mapper;

    @Override
    public Map<String, Object> getproductReviewBoards(Criteria criteria, Integer pId) throws Exception {
        // 검색 결과로 나오는 게시글 수 확인
        int count = mapper.findProductReviewBoardsCount(criteria, pId);
        PageDto pageDto = new PageDto(count, criteria);

        // 시작 게시물 번호 구하기
        int offset = (criteria.getPageno() - 1) * criteria.getAmount();

        List<Map<String, Object>> list = mapper.findProductReviewBoards(criteria, offset, pId);

        Map<String, Object> returnVal = new HashMap<>();

        returnVal.put("list", list);
        returnVal.put("pageDto", pageDto);

        return returnVal;
    }

    @Override
    public Map<String, Object> getproductReviewBoardDetail(Integer id) throws Exception {
        return mapper.findProductReviewBoardById(id);
    }

    @Override
    public Map<String, Object> getMyProductReviewBoards(Criteria criteria, String uId) throws Exception {
        // 검색 결과로 나오는 게시글 수 확인
        int count = mapper.findMyProductReviewBoardsCount(criteria, uId);
        PageDto pageDto = new PageDto(count, criteria);

        // 시작 게시물 번호 구하기
        int offset = (criteria.getPageno() - 1) * criteria.getAmount();

        List<Map<String, Object>> list = mapper.findMyProductReviewBoards(criteria, offset, uId);

        Map<String, Object> returnVal = new HashMap<>();

        returnVal.put("list", list);
        returnVal.put("pageDto", pageDto);

        return returnVal;
    }
}
