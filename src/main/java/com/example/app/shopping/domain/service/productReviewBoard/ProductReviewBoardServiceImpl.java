package com.example.app.shopping.domain.service.productReviewBoard;

import com.example.app.shopping.domain.dto.ProductReviewBoardDto;
import com.example.app.shopping.domain.dto.common.Criteria;
import com.example.app.shopping.domain.dto.common.PageDto;
import com.example.app.shopping.domain.mapper.OrderItemMapper;
import com.example.app.shopping.domain.mapper.PaymentMapper;
import com.example.app.shopping.domain.mapper.ProductReviewBoardMapper;
import com.example.app.shopping.properties.FileUploadPathProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ProductReviewBoardServiceImpl implements ProductReviewBoardService {

    @Autowired
    private ProductReviewBoardMapper mapper;
    @Autowired
    private PaymentMapper paymentMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;

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

    @Override
    public Map<String, Object> checkAddReview(Long oId, Long pId, String uId) throws Exception {
        Map<String, Object> result = new HashMap<>();

        // oId 를 사용하여 Payment 를 조회, uid 가 일치하는지 체크한다.
        Map<String, Object> paymentInfo = paymentMapper.selectPaymentByoId(oId);

        if( !uId.equals(paymentInfo.get("userid")) ) {
            // 로그인 한 사용자의 주문내역이 아닌 경우
            throw new Exception("본인이 구매한 상품만 리뷰를 작성할 수 있습니다.");
        }

        // oId, pId 를 사용하여 order_item 을 조회, 해당하는 데이터가 있는지 체크한다.
        Map<String, Object> orderItemInfo = orderItemMapper.selectOrderItemByoIdAndpId(oId, pId);

        if( orderItemInfo.isEmpty() ) {
            // 주문 내역에 해당하는 상품 구매 내역이 없다면
            throw new Exception("구매 내역이 있는 상품에 대해서만 리뷰를 작성할 수 있습니다.");
        }

        // 조회한 order_item 의 리뷰 작성 여부를 체크한다.
        if( "Y".equals( orderItemInfo.get("review_status") ) ) {
            throw new Exception("리뷰는 한 번만 작성할 수 있습니다.");
        }

        result.put("success", true);

        return result;
    }

    @Override
    @Transactional
    public void insertProductReviewServ(ProductReviewBoardDto boardDto, MultipartFile image, Long oId) throws Exception {
        if (image != null && !image.isEmpty()) {
            String relativeDir = "productReview/" + LocalDate.now();
            String uploadDir = FileUploadPathProperties.getUploadDir() + "/" + relativeDir;
            File uploadDirFile = new File(uploadDir);
            if (!uploadDirFile.exists()) {
                uploadDirFile.mkdirs(); // 디렉터리가 존재하지 않으면 생성
            }

            String originalFileName = image.getOriginalFilename();
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            String filePath = Paths.get(uploadDir, uniqueFileName).toString();

            File destinationFile = new File(filePath);
            try {
                image.transferTo(destinationFile);
            } catch (IOException e) {
                throw new Exception("파일 업로드에 실패했습니다.");
            }

            // boardDto에 파일 경로와 파일 이름 설정 (상대 경로 사용)
            boardDto.setImgPath("/uploads/" + relativeDir);
            boardDto.setImgName(uniqueFileName);
        }

        boardDto.setOid(oId);

        int insertReturnVal = mapper.insertProductReview(boardDto);

        // order_item 의 리뷰 상태를 "Y" 로 변경한다.
        // 변경이 필요하다 ( oId, pId 값을 받아와서 조건문에 사용할 수 있도록 넘겨줘야한다 )
        Long pId = boardDto.getPid();
        int updateReturnVal = orderItemMapper.updateReviewStatus("Y", pId, oId);

        if (insertReturnVal < 1 || updateReturnVal < 1) {
            throw new Exception("리뷰 등록에 실패하였습니다.");
        }
    }

    @Override
    @Transactional
    public Map<String, Object> putProductReviewServ(ProductReviewBoardDto boardDto, MultipartFile file, boolean deleteImage) throws Exception {
        Map<String, Object> result = new HashMap<>();

        ProductReviewBoardDto originalBoardDto = mapper.selectProductReviewBoardById(boardDto.getId());

        if (originalBoardDto == null) {
            throw new Exception("해당 ID의 리뷰를 찾을 수 없습니다.");
        }

        if (!originalBoardDto.getUid().equals(boardDto.getUid())) {
            throw new Exception("본인의 게시글만 수정할 수 있습니다.");
        }

        // 업데이트할 데이터 복사
        originalBoardDto.setTitle(boardDto.getTitle());
        originalBoardDto.setContent(boardDto.getContent());
        originalBoardDto.setRating(boardDto.getRating());

        if (deleteImage) {
            if (originalBoardDto.getImgPath() != null && originalBoardDto.getImgName() != null) {
                String fullPath = FileUploadPathProperties.getUploadDir() + originalBoardDto.getImgPath().replaceFirst("/uploads", "") + "/" + originalBoardDto.getImgName();
                System.out.println(fullPath);
                File imageFile = new File(fullPath);
                if (imageFile.exists() && imageFile.isFile()) {
                    if (!imageFile.delete()) {
                        throw new Exception("기존 이미지를 삭제하는 데 실패했습니다.");
                    }
                }
            }
            originalBoardDto.setImgPath(null);
            originalBoardDto.setImgName(null);
        }

        if (file != null && !file.isEmpty()) {

            if (originalBoardDto.getImgPath() != null && originalBoardDto.getImgName() != null) {
                String fullPath = FileUploadPathProperties.getUploadDir() + originalBoardDto.getImgPath().replaceFirst("/uploads", "") + "/" + originalBoardDto.getImgName();
                System.out.println(fullPath);
                File imageFile = new File(fullPath);
                if (imageFile.exists() && imageFile.isFile()) {
                    if (!imageFile.delete()) {
                        throw new Exception("기존 이미지를 삭제하는 데 실패했습니다.");
                    }
                }
            }
            originalBoardDto.setImgPath(null);
            originalBoardDto.setImgName(null);

            String relativeDir = "productReview/" + LocalDate.now();
            String uploadDir = FileUploadPathProperties.getUploadDir() + "/" + relativeDir;
            File uploadDirFile = new File(uploadDir);
            if (!uploadDirFile.exists()) {
                uploadDirFile.mkdirs(); // 디렉터리가 존재하지 않으면 생성
            }

            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            String filePath = Paths.get(uploadDir, uniqueFileName).toString();

            File destinationFile = new File(filePath);
            try {
                file.transferTo(destinationFile);
            } catch (IOException e) {
                throw new Exception("파일 업로드에 실패했습니다.");
            }

            // boardDto에 파일 경로와 파일 이름 설정 (상대 경로 사용)
            originalBoardDto.setImgPath("/uploads/" + relativeDir);
            originalBoardDto.setImgName(uniqueFileName);
        }

        originalBoardDto.setUpdateDate(LocalDate.now());

        // 데이터베이스 업데이트
        int updateReturnVal = mapper.updateProductReview(originalBoardDto);

        if (updateReturnVal < 1) {
            throw new Exception("리뷰 수정에 실패하였습니다.");
        } else {
            result.put("success", true);
            result.put("msg", "리뷰 수정에 성공하였습니다.");
        }

        return result;
    }



    @Override
    @Transactional
    public Map<String, Object> getReviewDetail(Long id) throws Exception {
        return mapper.findReviewById(id);
    }

    @Override
    @Transactional
    public Map<String, Object> deleteReview(Long id) throws Exception {
        Map<String, Object> result = new HashMap<>();

        // 리뷰 정보 가져오기
        ProductReviewBoardDto reviewDto = mapper.selectReviewById(id);

        if (reviewDto == null) {
            throw new Exception("해당 ID의 리뷰를 찾을 수 없습니다.");
        }

        // 이미지 파일 삭제 (리뷰에 이미지가 있는 경우)
        if (reviewDto.getImgPath() != null && reviewDto.getImgName() != null) {
            String filePath = FileUploadPathProperties.getUploadDir() + reviewDto.getImgPath().replaceFirst("/uploads", "") + "/" + reviewDto.getImgName();
            File file = new File(filePath);
            if (file.exists()) {
                boolean isDeleted = file.delete();
                if (!isDeleted) {
                    throw new Exception("이미지 파일 삭제에 실패했습니다.");
                } else {
                    System.out.println("파일 삭제 성공: " + filePath);
                }
            } else {
                System.out.println("삭제할 파일이 존재하지 않습니다: " + filePath);
            }
        }

        // 리뷰 삭제
        int deleteReturnVal = mapper.deleteReview(id);

        if (deleteReturnVal < 1) {
            throw new Exception("리뷰 삭제에 실패하였습니다.");
        } else {

            // orderItem 의 review_status 변경해야한다
            Long oId = reviewDto.getOid();
            Long pId = reviewDto.getPid();

            Integer updateReturnVal = orderItemMapper.updateReviewStatus("N", pId, oId);

            if(updateReturnVal < 1) throw new Exception("리뷰 삭제에 실패하였습니다.");

            result.put("success", true);
            result.put("msg", "리뷰 삭제에 성공하였습니다.");
        }

        return result;
    }
}
