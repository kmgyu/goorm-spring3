package io.goorm.board.controller;

import io.goorm.board.dto.product.ProductCreateDto;
import io.goorm.board.dto.product.ProductDto;
import io.goorm.board.dto.product.ProductSearchDto;
import io.goorm.board.dto.product.ProductUpdateDto;
import io.goorm.board.enums.ProductStatus;
import io.goorm.board.service.CategoryService;
import io.goorm.board.service.ExcelExportService;
import io.goorm.board.service.ProductService;
import io.goorm.board.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 상품 관리 컨트롤러
 */
@Slf4j
@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final SupplierService supplierService;
    private final ExcelExportService excelExportService;
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    /**
     * 상품 목록 조회
     */
    @GetMapping
    public String list(
            @ModelAttribute ProductSearchDto searchDto,
            Model model
    ) {
        log.debug("Product list request with search: {}", searchDto);

        Page<ProductDto> products = productService.findAll(searchDto);

        model.addAttribute("products", products);
        model.addAttribute("search", searchDto);
        model.addAttribute("totalElements", products.getTotalElements());
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("currentPage", products.getNumber() + 1);

        // 검색 필터용 데이터
        model.addAttribute("categories", categoryService.findAllActive());
        model.addAttribute("productStatuses", ProductStatus.values());

        return "products/list";
    }

    /**
     * 상품 등록 폼
     */
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("product", new ProductCreateDto());
        model.addAttribute("categories", categoryService.findAllActive());
        model.addAttribute("suppliers", supplierService.findAllActive());
        return "products/form";
    }

    /**
     * 상품 등록 처리
     */
    @PostMapping
    public String create(
            @Valid @ModelAttribute("product") ProductCreateDto createDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model,
            HttpServletRequest request
    ) {
        log.debug("상품 등록 요청: {}", createDto);

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAllActive());
            model.addAttribute("suppliers", supplierService.findAllActive());
            return "products/form";
        }

        try {
            ProductDto savedProduct = productService.create(createDto);
            String message = messageSource.getMessage("product.message.create.success", null, localeResolver.resolveLocale(request));
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/products/" + savedProduct.getProductSeq();
        } catch (Exception e) {
            log.error("상품 등록 실패", e);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", categoryService.findAllActive());
            model.addAttribute("suppliers", supplierService.findAllActive());
            return "products/form";
        }
    }

    /**
     * 상품 수정 처리
     */
    @PostMapping("/{productSeq}")
    public String update(
            @PathVariable Long productSeq,
            @Valid @ModelAttribute("product") ProductUpdateDto updateDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model,
            HttpServletRequest request
    ) {
        log.debug("상품 수정 요청 seq: {}", productSeq);

        updateDto.setProductSeq(productSeq);

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAllActiveOrSelected(updateDto.getCategorySeq()));
            model.addAttribute("suppliers", supplierService.findAllActiveOrSelected(updateDto.getSupplierSeq()));
            return "products/form";
        }

        try {
            ProductDto updatedProduct = productService.update(updateDto);
            String message = messageSource.getMessage("product.message.update.success", null, localeResolver.resolveLocale(request));
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/products/" + updatedProduct.getProductSeq();
        } catch (Exception e) {
            log.error("상품 수정 실패", e);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", categoryService.findAllActiveOrSelected(updateDto.getCategorySeq()));
            model.addAttribute("suppliers", supplierService.findAllActiveOrSelected(updateDto.getSupplierSeq()));
            return "products/form";
        }
    }

    /**
     * 상품 상세 조회
     */
    @GetMapping("/{productSeq}")
    public String detail(
            @PathVariable Long productSeq,
            Model model
    ) {
        log.debug("Product detail request for seq: {}", productSeq);

        try {
            ProductDto product = productService.findById(productSeq);
            model.addAttribute("product", product);
            return "products/show";
        } catch (Exception e) {
            log.error("Failed to get product detail for seq: {}", productSeq, e);
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/products";
        }
    }

    /**
     * 상품 수정 폼
     */
    @GetMapping("/{productSeq}/edit")
    public String editForm(
            @PathVariable Long productSeq,
            Model model
    ) {
        log.debug("Product edit form request for seq: {}", productSeq);

        try {
            ProductDto product = productService.findById(productSeq);

            ProductUpdateDto updateDto = ProductUpdateDto.builder()
                    .productSeq(product.getProductSeq())
                    .code(product.getCode())
                    .name(product.getName())
                    .description(product.getDescription())
                    .categorySeq(product.getCategorySeq())
                    .supplierSeq(product.getSupplierSeq())
                    .unitPrice(product.getUnitPrice())
                    .unitCost(product.getUnitCost())
                    .unit(product.getUnit())
                    .sku(product.getSku())
                    .barcode(product.getBarcode())
                    .weight(product.getWeight())
                    .dimensions(product.getDimensions())
                    .status(product.getStatus())
                    .currentImageUrl(product.getImageUrl())
                    .build();

            model.addAttribute("product", updateDto);
            model.addAttribute("categories", categoryService.findAllActiveOrSelected(product.getCategorySeq()));
            model.addAttribute("suppliers", supplierService.findAllActiveOrSelected(product.getSupplierSeq()));
            return "products/form";
        } catch (Exception e) {
            log.error("Failed to get product for edit form, seq: {}", productSeq, e);
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/products";
        }
    }


    /**
     * 상품 삭제
     */
    @PostMapping("/{productSeq}/delete")
    public String delete(
            @PathVariable Long productSeq,
            RedirectAttributes redirectAttributes
    ) {
        log.debug("Deleting product seq: {}", productSeq);

        try {
            productService.delete(productSeq);
            redirectAttributes.addFlashAttribute("successMessage", "product.message.delete.success");
        } catch (Exception e) {
            log.error("Failed to delete product", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/products";
    }

    /**
     * 코드 중복 확인 API
     */
    @GetMapping("/check-code")
    @ResponseBody
    public ResponseEntity<Boolean> checkCodeDuplicate(
            @RequestParam String code,
            @RequestParam(required = false) Long excludeSeq
    ) {
        boolean isDuplicate;
        if (excludeSeq != null) {
            isDuplicate = productService.isCodeDuplicate(code, excludeSeq);
        } else {
            isDuplicate = productService.isCodeDuplicate(code);
        }
        return ResponseEntity.ok(isDuplicate);
    }

    /**
     * 상태별 상품 목록 API
     */
    @GetMapping("/by-status/{status}")
    @ResponseBody
    public ResponseEntity<Object> getProductsByStatus(@PathVariable String status) {
        try {
            return ResponseEntity.ok(productService.findByStatus(status));
        } catch (Exception e) {
            log.error("Failed to get products by status: {}", status, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 최근 상품 목록 API
     */
    @GetMapping("/recent")
    @ResponseBody
    public ResponseEntity<Object> getRecentProducts(
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            return ResponseEntity.ok(productService.findRecentProducts(limit));
        } catch (Exception e) {
            log.error("Failed to get recent products", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 상품 목록 Excel 다운로드
     */
    @GetMapping("/excel")
    public ResponseEntity<byte[]> downloadExcel(@ModelAttribute ProductSearchDto searchDto, HttpServletRequest request) {
        try {
            log.debug("Excel download request with search: {}", searchDto);

            // 서비스에서 Excel 데이터 생성
            byte[] excelData = productService.exportToExcel(searchDto);

            // 파일명 생성 (한글 지원)
            String fileName = excelExportService.generateFileName("상품목록", localeResolver.resolveLocale(request));

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);

        } catch (Exception e) {
            log.error("Failed to download Excel", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}