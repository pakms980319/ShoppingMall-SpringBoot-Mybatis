package com.example.app.shopping.controller;

import com.example.app.shopping.domain.dto.UserDto;
import com.example.app.shopping.domain.service.user.UserService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserService userService;


    private PortOneTokenResponse portOneTokenResponse;
    private String security;

    @Value("${spring.portOne.imp_key}")
    private String imp_key;
    @Value("${spring.portOne.imp_secret}")
    private String imp_secret;

    @GetMapping("loginForm")
    public String getLoginForm(@RequestParam(value = "exception", required = false) String exception,
                             Authentication authentication, Model model) {
        System.out.println("로그인!!!");
        if (authentication != null) {
            return "redirect:/";
        }
        model.addAttribute("exception", exception);
        return "user/loginForm";
    }

    //엑세스 토큰 받기
    @GetMapping("token")
    public @ResponseBody void AccessToken(){
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
    // 인증된 사용자 정보 가져오기
    @GetMapping(value = "AuthInfo/{imp_uid}",produces = MediaType.APPLICATION_JSON_VALUE) //인증하면 imp_uid를 받아올 수 있음 받아온 값을 사용
    public @ResponseBody PortOneAuthInfoResponse AuthInfo(@PathVariable("imp_uid")String imp_uid){
        AccessToken();// 엑세스 토큰 가져오기
        log.info("Get/portOne/AuthInfo" + imp_uid);
        String url = "https://api.iamport.kr/certifications/"+imp_uid;
        //header
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization","Bearer "+portOneTokenResponse.getResponse().getAccess_token());
        //params
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        //Entity
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        //request
        RestTemplate rt = new RestTemplate();

        //response
        ResponseEntity<PortOneAuthInfoResponse> response = rt.exchange(url, HttpMethod.GET,entity,PortOneAuthInfoResponse.class);
        System.out.println(response.getBody());

        security = "security"; // 회원가입 보안설정

        return response.getBody();
    }
    //인증 객체
    @Data
    private static class AuthInfoResponse{
        public int birth;
        public String birthday;
        public boolean certified;
        public int certified_at;
        public boolean foreigner;
        public Object foreigner_v2;
        public Object gender;
        public String imp_uid;
        public String merchant_uid;
        public String name;
        public String origin;
        public String pg_provider;
        public String pg_tid;
        public String phone;
        public Object unique_in_site;
        public String unique_key;
    }
    @Data
    private static class PortOneAuthInfoResponse{
        public int code;
        public Object message;
        public AuthInfoResponse response;
    }

    //회원가입 폼으로 이동
    @GetMapping("joinForm")
    public String JoinForm(Authentication authentication){
        if (authentication != null){ // 로그인이 되어있다면
            return "index";
        }
        return "user/joinForm"; //로그인이 안되어있다면
    }

    //회원가입
    @PostMapping("")
    @ResponseBody
    public ResponseEntity<String> join(@Valid @RequestBody UserDto userDto, BindingResult bindingResult){
        System.out.println(userDto);
        if (bindingResult.hasErrors() || security == null) {
            // 유효성 검사 실패 시 오류 메시지 알려줌
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            System.out.println(errors);
            return new ResponseEntity<>("FAIL", HttpStatus.BAD_GATEWAY);
        }
        System.out.println(bindingResult.hasErrors());
        return new ResponseEntity<>(userService.userJoin(userDto), HttpStatus.OK);
    }
    //회원가입시 본인인증 후 중복된 회원인지 확인

    @PostMapping("duplicateUserCheck")
    @ResponseBody
    public String duplicateUserCheck(@RequestBody UserDto userDto){
        return userService.duplicateUserCheck(userDto.getName(), userDto.getPhone());
    }


    // 유저 등록시 유저 중복확인
    @GetMapping(value = "confirmUserId")
    @ResponseBody
    public String getConfirmUserId(@RequestParam(value = "id") String id) {
        return userService.confirmUserId(id);
    }

    // 유저 아이디 찾기 폼으로 이동
    @GetMapping("findUserIdForm")
    public void findUserIdForm(){
        
    }
    //유저 페스워드 찾기 폼으로 이동
    @GetMapping("findUserPasswordForm")
    public void findUserPasswordForm(){

    }
    // 유저 이메일로 아이디 찾기
    @PostMapping("findUserIdByEmail")
    @ResponseBody
    public String findUserIdByEmail(@RequestBody UserDto userDto){
        return userService.findUserIdByEmailAndUserName(userDto);
    }
    // 유저 휴대폰번호로 아이디 찾기
    @PostMapping("findUserIdByPhone")
    @ResponseBody
    public String findUserIdByPhone(@RequestBody UserDto userDto){
        return userService.findUserIdByPhoneAndUserName(userDto);
    }
    //유저 이메일로 랜덤값 전달해서 비밀번호 전송받기
    @PostMapping("findUserPasswordByEmailAsRandomValue")
    @ResponseBody
    public String findUserPasswordByEmail(@RequestBody UserDto userDto){
        return userService.findUserPasswordByEmailAndUserIdAsRandomValue(userDto);
    }
    // 유저 이메일 랜덤값 전달받아서 인증된 유저에게 비밀번호 코드 전달
    @PostMapping("passwordAuthenticationCodeCheck")
    public ResponseEntity<String> checkPasswordAuthenticationCode(@RequestBody Map<String, String> request) {
        String passwordCheck = request.get("passwordCheck");

        if (passwordCheck == null || passwordCheck.isEmpty()) {
            return new ResponseEntity<>("패스워드 인증코드를 입력하세요", HttpStatus.BAD_REQUEST);
        } else if (passwordCheck.length() != 6) {
            return new ResponseEntity<>("패스워드 인증코드는 6자리 입니다.", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(userService.checkPasswordAuthenticationCode(request), HttpStatus.OK);
    }
    //본인인증 통과한 유저에게 이메일로 랜덤값 보내기
    @PostMapping("findUserPasswordByAuthentication")
    @ResponseBody
    public String findUserPasswordByAuthentication(@RequestBody UserDto userDto){
        return userService.findUserPasswordByAuthentication(userDto);
    }

}
