package com.example.app.shopping.controller;

import com.example.app.shopping.config.auth.PrincipalDetails;
import com.example.app.shopping.domain.dto.common.Criteria;
import com.example.app.shopping.domain.service.productReviewBoard.ProductReviewBoardServiceImpl;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class ProductReviewBoardController {
    @Autowired
    private ProductReviewBoardServiceImpl service;

    // 상품 조회에서 상품 리뷰 뿌려주는 rest API, 기존 게시판과 구분을 위해 url 끝에 API 를 작성하였습니다.
    @PostMapping("/productReviewBoardListAPI")
    public @ResponseBody Map<String, Object> productReviewBoardListAPI(@RequestBody ProductReviewBoardAPI request) {
        System.out.println("ProductReviewBoardController's productReviewBoardListAPI");
        System.out.println("request: " + request);
        Criteria criteria = request.getCriteria();

        Integer pId = request.getPId();

        if (criteria.getPageno() == null) {
            criteria.setPageno(1);
        }

        if (criteria.getAmount() == null) {
            criteria.setAmount(10);
        }

        System.out.println(request);
        System.out.println(criteria);

        Map<String, Object> returnVal = new HashMap<>();
        
        try {
            returnVal = service.getproductReviewBoards(criteria, pId);
            returnVal.put("success", true);
        } catch (Exception e) {
            returnVal.put("success", false);
        }

        return returnVal;
    }

    // 악의적인 리뷰 혹은 광고 등을 검열하기 위한 개발자 뷰를 위한 메서드입니다. (일반 유저는 볼 이유가 없어 접근 제한 필요할 듯 합니다)
    @GetMapping("/productReviewBoardList")
    public String productReviewBoardList(
            @ModelAttribute Criteria criteria,
            Model model
    ) {
        System.out.println("ProductReviewBoardController's productReviewBoardList " +
                "criteria: " + criteria + " model: " + model);

        if (criteria.getPageno() == null) {
            criteria.setPageno(1);
        }

        if (criteria.getAmount() == null) {
            criteria.setAmount(10);
        }

        try {
            Map<String, Object> serviceReturnVal = service.getproductReviewBoards(criteria, null);  // pId 는 criteria 의 keyword 로 대체
            model.addAttribute("success", true);
            model.addAttribute("list", serviceReturnVal.get("list"));  // 상품 리스트 정보
            model.addAttribute("pageDto", serviceReturnVal.get("pageDto"));  // 페이징 처리를 위한 정보
        } catch (Exception e) {
            model.addAttribute("success", false);
        }

        return "/productReviewBoard/boardList";
    }

    @GetMapping("/productReviewBoard")
    public String productReviewBoard (
            @RequestParam(name = "id", defaultValue = "0", required = false) Integer id,
            Model model) {
        System.out.println("ProductReviewBoardController's productReviewBoard id: " + id + " model: " + model);

        Map<String, Object> response = null;  // 반환할 데이터를 담을 변수

        try {
            response = service.getproductReviewBoardDetail(id);
            response.put("success", true);
        } catch (Exception e) {
            response = new HashMap<>();
            response.put("success", false);
        }

        model.addAttribute("response", response);
        response = null;

        return "/productReviewBoard/boardDetail";
    }

    // 내 작성 리뷰 뿌리기

    @GetMapping("/myProductReviewBoardList")
    public String myProductReviewBoardList(
            @ModelAttribute Criteria criteria,
            Model model,
            Authentication authentication
    ) {
        System.out.println("ProductReviewBoardController's myProductReviewBoardList " +
                "criteria: " + criteria + " model: " + model);

        if (criteria.getPageno() == null) {
            criteria.setPageno(1);
        }

        criteria.setAmount(6);

        if (authentication == null) {
            String error = "로그인 정보가 없습니다.";
            model.addAttribute("error", error);

            return "/error/error";
        }

        String uId = ((PrincipalDetails) authentication.getPrincipal()).getUsername();

        try {
            Map<String, Object> serviceReturnVal = service.getMyProductReviewBoards(criteria, uId);
            model.addAttribute("success", true);
            model.addAttribute("list", serviceReturnVal.get("list"));  // 상품 리스트 정보
            model.addAttribute("pageDto", serviceReturnVal.get("pageDto"));  // 페이징 처리를 위한 정보
        } catch (Exception e) {
            model.addAttribute("success", false);
        }

        return "/myPage/myReviewPage";
    }

    @Data
    private static class ProductReviewBoardAPI {
        @JsonProperty("criteria")
        Criteria criteria;
        @JsonProperty("pId")
        Integer pId;  // 상품 ID
    }
}