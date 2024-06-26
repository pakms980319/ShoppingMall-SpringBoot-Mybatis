package com.example.app.shopping.controller;

import com.example.app.shopping.config.auth.PrincipalDetails;
import com.example.app.shopping.domain.dto.CustomerInquiryBoardDto;
import com.example.app.shopping.domain.dto.common.Criteria;
import com.example.app.shopping.domain.service.customerInquiryBoard.CustomerInquiryBoardServiceImpl;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Controller
public class CustomerInquiryBoardController {

    @Autowired
    private CustomerInquiryBoardServiceImpl service;

    @GetMapping("customerInquiryBoardList")
    public String customerInquiryBoardList (@ModelAttribute Criteria criteria, Model model) {
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

        return "customerInquiryBoard/boardList";
    }

    @GetMapping("customerInquiryBoard")
    public String customerInquiryBoard(
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

        return "customerInquiryBoard/boardDetail";
    }

    @GetMapping("myCustomerInquiryBoardList")
    public String myCustomerInquiryBoardList(@ModelAttribute Criteria criteria, Authentication authentication, Model model) {
        model.addAttribute("menu", "myCustomerInquiryBoardList");

        if (criteria.getPageno() == null) {
            criteria.setPageno(1);
        }

        if (authentication == null) {
            String error = "로그인 정보가 없습니다.";
            model.addAttribute("error", error);

            return "error/error";
        }

        criteria.setAmount(6);

        String uId = ((PrincipalDetails) authentication.getPrincipal()).getUsername();

        try {
            Map<String, Object> serviceReturnVal = service.getMyCustomerInquiryBoards(criteria, uId);
            model.addAttribute("success", true);
            model.addAttribute("list", serviceReturnVal.get("list"));  // 상품 리스트 정보
            model.addAttribute("pageDto", serviceReturnVal.get("pageDto"));  // 페이징 처리를 위한 정보

            System.out.println(serviceReturnVal.get("pageDto"));

        } catch (Exception e) {
            model.addAttribute("success", false);
        }

        return "myPage/myCustomerInquiryPage";
    }

    
    /*
        고객 문의 페이지로 이동시켜준다
    */
    @GetMapping("customerInquiry")
    public String getCustomerInquiry(Model model, Authentication authentication) {
        System.out.println("CustomerInquiryBoardController's getCustomerInquiry");

        return "customerInquiryBoard/addCustomerInquiry";
    }


    /*
        POST    /customerInquiry

        고객 문의글에 작성된 내용을 받아 DB에 저장합니다.
        성공 시
        ("success", true)
        ("msg", "성공 메시지")
        실패 시
        ("success", false)
        ("msg", "실패 메시지")
        가 Map 형태로 전달됩니다.
    */
    @PostMapping("customerInquiry")
    public @ResponseBody Map<String, Object> postCustomerInquiry(@ModelAttribute postCustomerInquiryDto postDto, Authentication authentication)
    {
        System.out.println("CustomerInquiryBoardController's postCustomerInquiry dto: " + postDto);

        Map<String, Object> response = new HashMap<>();

        if(authentication == null) {
            response.put("success", false);
            response.put("msg", "비회원은 작성할 수 없습니다.");

            return response;
        }

        CustomerInquiryBoardDto boardDto = new CustomerInquiryBoardDto();

        boardDto.setUid(authentication.getName());
        boardDto.setTitle(postDto.getTitle());
        boardDto.setContent(postDto.getContent());

        MultipartFile file = postDto.getImage();

        try {
            response = service.postCustomerInquiryServ(boardDto, file);
        } catch (Exception e) {
            response.put("success", false);
            response.put("msg", "상품 등록에 실패하였습니다.");
        }

        return response;
    }

    @Data
    private static class postCustomerInquiryDto {
        @JsonProperty("title")
        private String title;
        @JsonProperty("content")
        private String content;
        @JsonProperty("image")
        private MultipartFile image;
    }

    /*
        고객 문의 게시글 수정 페이지로 이동시킵니다.
        비회원 이거나 게시글 정보를 찾을 수 없을때는 그에대한 메시지를 Model 에 담습니다.
    */
    @GetMapping("customerInquiry/edit")
    public String customerInquiryEditPage(@RequestParam("id") Integer id, Model model, Authentication authentication) {
        System.out.println("CustomerInquiryBoardController's customerInquiryEditPage id: " + id);

        String userName = "";

        if (authentication != null) {
            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
            userName = principalDetails.getUsername();
        }

        try {
            // 서비스로부터 게시글 정보를 받아온다
            Map<String, Object> result = service.getCustomerInquiryBoardDetail(id);

            // 게시글 작성자와 수정자가 일치하는지 확인한다
            if(!userName.equals( (String) result.get("Uid") )) {
                model.addAttribute("success", false);
                model.addAttribute("boardId", id);
                model.addAttribute("msg", "본인의 게시글만 수정할 수 있습니다.");
            } else {
                // 게시글 작성자와 수정자가 일치한다
                model.addAttribute("success", true);
                model.addAttribute("board", result);
            }
        } catch (NullPointerException e) {
            // 게시글을 찾지 못한 상황
            model.addAttribute("success", false);
            model.addAttribute("boardId", id);
            model.addAttribute("msg", "해당 게시물을 찾을수 없습니다.");
        } catch (Exception e) {
            // DB 통신 에러처리
            model.addAttribute("success", false);
            model.addAttribute("boardId", id);
            model.addAttribute("msg", "데이터베이스 연동에 실패하였습니다.");
        }

        return "customerInquiryBoard/edit";
    }

    /*
        게시글 수정 요청을 서비스를 수행합니다.
    */
    @PutMapping("customerInquiry")
    public @ResponseBody Map<String, Object> putCustomerInquiry (
            @ModelAttribute PutCustomerInquiryDto putDto, Authentication authentication) {
        System.out.println("CustomerInquiryBoardController's putCustomerInquiry dto: " + putDto);

        Map<String, Object> response = new HashMap<>();

        if (authentication == null) {
            response.put("success", false);
            response.put("msg", "비회원은 수정할 수 없습니다.");
            return response;
        }

        CustomerInquiryBoardDto boardDto = new CustomerInquiryBoardDto();
        boardDto.setId(putDto.getId());
        boardDto.setUid(authentication.getName());
        boardDto.setTitle(putDto.getTitle());
        boardDto.setContent(putDto.getContent());

        MultipartFile file = putDto.getImage();
        boolean deleteImage = putDto.isDeleteImage();

        try {
            response = service.putCustomerInquiryServ(boardDto, file, deleteImage);
        } catch (Exception e) {
            response.put("success", false);
            response.put("msg", "문의 수정에 실패하였습니다.");
        }

        return response;
    }

    @Data
    private static class PutCustomerInquiryDto {
        @JsonProperty("id")
        private Long id;
        @JsonProperty("title")
        private String title;
        @JsonProperty("content")
        private String content;
        @JsonProperty("image")
        private MultipartFile image;
        @JsonProperty("deleteImage")
        private boolean deleteImage;
    }

    /*
        게시글 삭제 요청 처리
        관리자, 작성자 본인만 삭제 가능
    */
    @DeleteMapping("customerInquiry")
    public @ResponseBody Map<String, Object> deleteCustomerInquiry(@RequestParam("id") Integer id, Authentication authentication) {
        System.out.println("CustomerInquiryBoardController's deleteCustomerInquiry id: " + id);

        Map<String, Object> response = new HashMap<>();

        boolean isAdmin = false;

        if (authentication == null) {
            // 만약 비회원 상태라면
            response.put("success", false);
            response.put("msg", "작성한 회원만 삭제 요청을 할 수 있습니다.");

            return response;
        }

        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        String userName = principalDetails.getUsername();
        Collection<? extends GrantedAuthority> authorities = principalDetails.getAuthorities();
        isAdmin = authorities.stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        try {
            Map<String, Object> boardInfo = service.getCustomerInquiryBoardDetail(id);

            if (!isAdmin && !userName.equals(boardInfo.get("Uid"))) {
                response.put("success", false);
                response.put("msg", "본인이 작성한 게시글만 삭제할 수 있습니다.");
            } else {
                // 제거 요청에 대한 서비스 호출
                response = service.deleteCustomerInquiryServ(Long.valueOf(id));
            }
        } catch (NullPointerException e) {
            response.put("success", false);
            response.put("msg", "게시글 정보를 찾을 수 없습니다.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("msg", "데이터베이스 에러");
        }

        return response;
    }
}
