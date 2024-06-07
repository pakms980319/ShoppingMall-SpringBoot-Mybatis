package com.example.app.shopping.controller;

import com.example.app.shopping.config.auth.PrincipalDetails;
import com.example.app.shopping.domain.dto.PaymentDto;
import com.example.app.shopping.domain.service.PaymentService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Controller
@Slf4j
@RequestMapping("/payment")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;
    private PortOneTokenResponse portOneTokenResponse;

    @GetMapping("")
    public String payment(Authentication authentication) {
        return "/payment/paymentForm";
    }

    @GetMapping("/paymentForm")
    public String payment_search(Authentication authentication, Model model) {
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        String id = principal.getUserDto().getId();
        List<PaymentDto> paymentDtos = paymentService.searchPayment(id);
        model.addAttribute("payment", paymentDtos);
        return "payment/paymentList";
    }

    //결제 결과값을 db에 저장
    @PostMapping("/save")
    @ResponseBody
    public void payment_save(@RequestBody PaymentDto paymentDto, Authentication authentication) {
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        String id = principal.getUserDto().getId();
        System.out.println(paymentDto);

        paymentService.payResultSave(paymentDto, id);
    }

    //엑세스 토큰 받기
    @GetMapping("/token")
    public @ResponseBody void AccessToken(){

        String imp_key = "6257186181622002";
        String imp_secret = "LFmdkrDK2syh8Z4YCr7XoiVvDs5IRSAMHYAS43i4Jdy7FVSMKxGKCYMcYf5C7OWpsYXkdQUUufqHWz33";

        String url = "https://api.iamport.kr/users/getToken";
        //HEADER
        HttpHeaders headers = new HttpHeaders();

        //PARAM
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("imp_key",imp_key);
        params.add("imp_secret",imp_secret);
        //Entity
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        //ENTITY
        //REQUEST
        RestTemplate rt = new RestTemplate();
        ResponseEntity<PortOneTokenResponse> response = rt.exchange(url, HttpMethod.POST, entity, PortOneTokenResponse.class);
        System.out.println(response.getBody());
        //RESPONSE
        this.portOneTokenResponse = response.getBody(); //엑세스 토큰 객체를 넣어줌
    }
    //access토큰 객체
    @Data
    private static class TokenResponse{
        public String access_token;
        public int now;
        public int expired_at;
    }
    @Data
    private static class PortOneTokenResponse{
        public int code;
        public Object message;
        public TokenResponse response;
    }
    //access토큰 객체

//    결제 취소 요청 // axios로 받기 + 서비스단 구현!!!!!
    @PostMapping("/cancel")
    public @ResponseBody void cancel(@RequestParam("imp_uid") String imp_uid, @RequestParam("merchant_uid") String merchant_uid){
        AccessToken();
        log.info("Post /payment/cancel..");
        // access-token 받기

        //URL
        String url = "https://api.iamport.kr/payments/cancel";

        //Request Header
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization","Bearer " + portOneTokenResponse.getResponse().getAccess_token());
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        //Request Body
        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        params.add("imp_uid",imp_uid);
        params.add("merchant_uid", merchant_uid);

        //Hader+Body
        HttpEntity<MultiValueMap<String,String>> entity = new HttpEntity(params,headers);

        //요청
        RestTemplate restTemplate = new RestTemplate();

        //반환값확인
        ResponseEntity<String> resp =  restTemplate.exchange(url, HttpMethod.POST,entity,String.class);

        System.out.println(resp);
        System.out.println(resp.getBody());
    }


}