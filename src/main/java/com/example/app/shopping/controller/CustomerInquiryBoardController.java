package com.example.app.shopping.controller;

import com.example.app.shopping.domain.dto.common.Criteria;
import com.example.app.shopping.domain.service.customerInquiryBoard.CustomerInquiryBoardServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class CustomerInquiryBoardController {

    @Autowired
    private CustomerInquiryBoardServiceImpl service;

    @GetMapping("/customerInquiryBoardList")
    public String CustomerInquiryBoardList(@ModelAttribute Criteria criteria, Model model) {
        System.out.println("CustomerInquiryBoardController's customerInquiryBoardList");

        if (criteria.getPageno() == null) {
            criteria.setPageno(1);
        }

        if (criteria.getAmount() == null) {
            criteria.setAmount(10);
        }

        try {
            Map<String, Object> serviceReturnVal = service.getCustomerInquiryBoards(criteria);
            model.addAttribute("success", true);
            model.addAttribute("list", serviceReturnVal.get("list"));  // 상품 리스트 정보
            model.addAttribute("pageDto", serviceReturnVal.get("pageDto"));  // 페이징 처리를 위한 정보

            System.out.println(serviceReturnVal.get("pageDto"));

        } catch (Exception e) {
            model.addAttribute("success", false);
        }

        return "CustomerInquiryBoard/boardList";
    }

    @GetMapping("/customerInquiryBoard")
    public String customerInquiryBoardList(
            @RequestParam(name = "id", defaultValue = "0", required = false) Integer id,
            Model model) {
        System.out.println("CustomerInquiryBoardController's customerInquiryBoardList id: " + id + " model: " + model);

        Map<String, Object> response = null;  // 반환할 데이터를 담을 변수

        try {
            response = service.getCustomerInquiryBoardDetail(id);
            response.put("success", true);
        } catch (Exception e) {
            response = new HashMap<>();
            response.put("success", false);
        }

        model.addAttribute("response", response);

        return "/customerInquiryBoard/boardDetail";
    }
}
