package com.example.app.shopping.controller;

import com.example.app.shopping.config.auth.PrincipalDetails;
import com.example.app.shopping.domain.service.cart.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequestMapping(value = "cart")
public class CartController {
    @Autowired
    private CartService cartService;

    //장바구니에 담은 물건을 보여주는 로직
    @GetMapping("")
    public String cartForm(Authentication authentication, Model model) throws Exception {
        if (authentication == null) {
            return "user/loginForm";
        } else {
            PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
            String userId = principal.getUserDto().getId();
            List<Map<String, Object>> cartList = cartService.cartList(userId);
            model.addAttribute("carts", cartList);
            return "cart/cartForm";
        }
    }
    //장바구니 클릭하면 물건이 장바구니에 담기는 로직
    @PostMapping("")
    @ResponseBody
    public String cartAdd(@RequestBody Map<String, Integer> result, Authentication authentication) {
        Integer productId = result.get("productId");
        Integer quantity = result.get("quantity");

        //로그인 안한 유저가 장바구니에 물건을 담을 때 동작할 서비스
        if (authentication == null) {
           return "FAILURE_LOGIN";
        } else {
            PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();

            String userId = principal.getUsername();

           return cartService.cartAddLoignedUser(productId, quantity, userId);
        }
    }
    //장바구니에서 수량을 변경하면 DB에 상품이 변경되는 로직
    @PostMapping("AddAmount")
    @ResponseBody
    public void addAmount(@RequestBody Map<String, Object> result) {
        int amountValue = Integer.parseInt((String) result.get("amountValue"));
        int cartId = Integer.parseInt((String) result.get("cartId"));
        int productId = Integer.parseInt((String) result.get("productId"));
        cartService.updateCartItemQuantity(amountValue, productId, cartId);
    }

    //장바구니에서 삭제 누르면 장바구니 아이템 삭제
    @DeleteMapping("delete")
    @ResponseBody
    public String deleteCartItem(@RequestBody Map<String, Object> result) {
        int cartId = Integer.parseInt((String) result.get("cartId"));
        int productId = Integer.parseInt((String) result.get("productId"));
        System.out.println(cartId);
        System.out.println(productId);
        return cartService.deleteCartItem(cartId, productId);
    }
}
