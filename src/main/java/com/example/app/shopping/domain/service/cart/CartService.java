package com.example.app.shopping.domain.service.cart;

import com.example.app.shopping.domain.dto.CartDto;
import com.example.app.shopping.domain.dto.UserDto;
import com.example.app.shopping.domain.mapper.CartItemMapper;
import com.example.app.shopping.domain.mapper.CartMapper;
import com.example.app.shopping.domain.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CartService {
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private CartItemMapper cartItemMapper;

    public String cartAddNotUserLogin(Integer productId, Integer quantity) {
        cartMapper.insertCart();
        return null;
//        Integer findById = cartMapper.findById();
//        cartMapper.insertCartItem(productId, quantity);
    }

    public void addCartItem(Integer productId, Integer quantity) {

    }

    //    @Transactional
    public String cartAddLoignedUser(Integer productId, Integer quantity, String userId) {
        try {

            Integer cartId = cartMapper.findCartIdByUserId(userId);

            if (cartId == null) {  // Cart DB 에 유저 정보가 없다면
                // Cart DB 에 유저 정보 등록
                cartMapper.insertCartByUserId(userId);

                // 등록한 유저의 카트 ID 조회
                cartId = cartMapper.findCartIdByUserId(userId);

                // 가져온 상품을 cart_item DB 에 등록한다 ( 카드 ID, 제품 ID, 선택한 수량 )
                cartItemMapper.insertCartItem(cartId, productId, quantity);

                // TODOLIST: 리턴값을 다르게 주어 유저아이디가 없을 땐 그냥 장바구니에 담고 유저아이디가 있을 땐 장바구니로 이동하면 함께 주문된다고 알림주기 나중에 시간있을 때
            }
            else if (cartId > 0) {  // Cart DB 에 유저 정보가 조회가 된다면

                // 등록하려는 상품 ID 가 cart_item DB 에 등록 되어 있는 상태에 대한 조건 분기 로직
                Integer cartItemId = cartItemMapper.findIdByProductIdAndCartId(productId, cartId);  // 해당 변수는 cart_item 의 id 를 가진다

                if (cartItemId == null) {  // 장바구니에 등록된 적 없는 상품이라면
                    // cart_item DB 에 상품을 등록한다. ( 유저 정보가 담긴 cart 의 cart Id, 등록할 product Id, 등록할 수량 )
                    cartItemMapper.insertCartItem(cartId, productId, quantity);
                } else {
                    // cart_item DB 에 등록 된 적 있는 상품이라면
                    Integer productQuantity =
                            cartItemMapper.findQuantityByProductIdAndCardId(productId, cartId);  // 유저 정보가 담긴 cartId 와 productId 를 사용하여 cart_item 에서 등록된 상품의 수량을 가져온다
                    productQuantity = productQuantity == null ? 0 : productQuantity;


                    int sumQuantity = productQuantity + quantity; // 기존에 등록된 수량에 추가 수량을 더한다

                }
            }

        } catch (Exception e) { // 오류가 발생시 롤백
            System.out.println("오류 발생" + e.getMessage());
            throw e; // 예외 던져 트랜잭션 롤백
        }

        return "SUCCESS";
    }

    public List<Map<String, Object>> cartList(String userId) throws Exception {
        return cartItemMapper.findProductListById(userId);
    }

    public void updateCartItemQuantity(int amountValue, int productId, int cartId) {
        cartItemMapper.updateQuantityByCartIdAndProductId(cartId, productId, amountValue);
    }

    public String deleteCartItem(int cartId, int productId) {
        cartItemMapper.deleteCartItemByCartIdAndProductId(cartId, productId);
        return "SUCCESS";
    }
}
