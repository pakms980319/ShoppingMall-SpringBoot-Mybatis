package com.example.app.shopping.controller;

import com.example.app.shopping.domain.dto.MiddleCategoryDto;
import com.example.app.shopping.domain.dto.ProductDto;
import com.example.app.shopping.domain.mapper.MiddleCategoryMapper;
import com.example.app.shopping.domain.service.admin.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Controller
@Slf4j
@RequestMapping(value = "admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    // 관리자 페이지 GET
    @GetMapping("")
    public String getAdminPage() {
        log.info("GET AdminController's getAdminPage");

        return "admin/admin";
    }

    // 제품 등록 Get
    @GetMapping("addProduct")
    public void getAddProduct(Model model) {
        // 제품 메인 카테고리 List 가져오기
        List<String> majorCategoryList = adminService.getMajorCategoryList();

        // 제품 서브 카테고리 List 가져오기
        List<MiddleCategoryDto> middleCategoryList = adminService.getMiddleCategoryList();

        model.addAttribute("majorCategoryList", majorCategoryList);
        model.addAttribute("middleCategoryList", middleCategoryList);
    }

    // 제품 등록 Post
    @PostMapping("product")
    public ResponseEntity<String> postAddProduct(@RequestPart(value = "mainImage") MultipartFile mainImage,
                                                 @RequestPart(value = "subImage") MultipartFile subImage,
                                                 ProductDto productDto) throws IOException {
        // 제품 등록
        adminService.addProduct(mainImage, subImage, productDto);
        return ResponseEntity.ok("제품 등록이 완료 되었습니다.");
    }

    // 제품 수정 Get
    @GetMapping("modifyProduct")
    public void getModifyProduct(@RequestParam(value = "productName", required = false) String productName,
                                 Model model) {
        // 제품 메인 카테고리 List 가져오기
        List<String> majorCategoryList = adminService.getMajorCategoryList();

        // 제품 서브 카테고리 List 가져오기
        List<MiddleCategoryDto> middleCategoryList = adminService.getMiddleCategoryList();

        // 제품 List 가져오기
        List<String> productList = adminService.getProductList();

        // 제품이름으로 정보 가져오기
        ProductDto product = adminService.getProductByProductName(productName);

        model.addAttribute("majorCategoryList", majorCategoryList);
        model.addAttribute("middleCategoryList", middleCategoryList);
        model.addAttribute("productList", productList);
        model.addAttribute("productName", productName);
        model.addAttribute("product", product);
    }

    // 제품 수정 Put
    @PutMapping("product")
    public ResponseEntity<String> putAddProduct(@RequestPart(value = "mainImage", required = false) MultipartFile mainImage,
                                                 @RequestPart(value = "subImage", required = false) MultipartFile subImage,
                                                 ProductDto productDto) throws IOException {
        // 제품 수정
        adminService.modifyProduct(mainImage, subImage, productDto);
        return ResponseEntity.ok("제품 수정이 완료 되었습니다.");
    }
}
