package io.goorm.board.controller;

import io.goorm.board.dto.category.CategoryCreateDto;
import io.goorm.board.dto.category.CategoryDto;
import io.goorm.board.dto.category.CategorySearchDto;
import io.goorm.board.dto.category.CategoryUpdateDto;
import io.goorm.board.entity.User;
import io.goorm.board.enums.CategoryStatus;
import io.goorm.board.service.CategoryService;
import io.goorm.board.service.ExcelExportService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 카테고리 관리 컨트롤러
 */
@Slf4j
@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final ExcelExportService excelExportService;
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    /**
     * 카테고리 목록 조회
     */
    @GetMapping
    public String list(
            @ModelAttribute CategorySearchDto searchDto,
            Model model
    ) {
        log.debug("Category list request with search: {}", searchDto);

        Page<CategoryDto> categories = categoryService.findAll(searchDto);

        model.addAttribute("categories", categories);
        model.addAttribute("search", searchDto);
        model.addAttribute("totalElements", categories.getTotalElements());
        model.addAttribute("totalPages", categories.getTotalPages());
        model.addAttribute("currentPage", categories.getNumber() + 1);

        // 검색 필터용 데이터
        model.addAttribute("categoryStatuses", CategoryStatus.values());

        return "categories/list";
    }

    /**
     * 카테고리 등록 폼
     */
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("category", new CategoryCreateDto());
        return "categories/form";
    }

    /**
     * 카테고리 등록 처리
     */
    @PostMapping
    public String create(
            @Valid @ModelAttribute("category") CategoryCreateDto createDto,
            BindingResult bindingResult,
            @AuthenticationPrincipal User currentUser,
            RedirectAttributes redirectAttributes,
            Model model,
            HttpServletRequest request
    ) {
        log.debug("카테고리 등록 요청: {}", createDto);

        if (bindingResult.hasErrors()) {
            return "categories/form";
        }

        // 사용자 정보 설정
        createDto.setCreatedSeq(currentUser.getUserSeq());
        createDto.setUpdatedSeq(currentUser.getUserSeq());

        try {
            CategoryDto savedCategory = categoryService.create(createDto);
            String message = messageSource.getMessage("category.message.create.success", null, localeResolver.resolveLocale(request));
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/categories/" + savedCategory.getCategorySeq();
        } catch (Exception e) {
            log.error("카테고리 등록 실패", e);
            model.addAttribute("errorMessage", e.getMessage());
            return "categories/form";
        }
    }

    /**
     * 카테고리 수정 처리
     */
    @PostMapping("/{categorySeq}")
    public String update(
            @PathVariable Long categorySeq,
            @Valid @ModelAttribute("category") CategoryUpdateDto updateDto,
            BindingResult bindingResult,
            @AuthenticationPrincipal User currentUser,
            RedirectAttributes redirectAttributes,
            Model model,
            HttpServletRequest request
    ) {
        log.debug("카테고리 수정 요청 seq: {}", categorySeq);

        updateDto.setCategorySeq(categorySeq);

        if (bindingResult.hasErrors()) {
            return "categories/form";
        }

        // 사용자 정보 설정
        updateDto.setUpdatedSeq(currentUser.getUserSeq());

        try {
            CategoryDto updatedCategory = categoryService.update(updateDto);
            String message = messageSource.getMessage("category.message.update.success", null, localeResolver.resolveLocale(request));
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/categories/" + updatedCategory.getCategorySeq();
        } catch (Exception e) {
            log.error("카테고리 수정 실패", e);
            model.addAttribute("errorMessage", e.getMessage());
            return "categories/form";
        }
    }

    /**
     * 카테고리 상세 조회
     */
    @GetMapping("/{categorySeq}")
    public String detail(
            @PathVariable Long categorySeq,
            Model model
    ) {
        log.debug("Category detail request for seq: {}", categorySeq);

        try {
            CategoryDto category = categoryService.findById(categorySeq);
            model.addAttribute("category", category);
            return "categories/show";
        } catch (Exception e) {
            log.error("Failed to get category detail for seq: {}", categorySeq, e);
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/categories";
        }
    }

    /**
     * 카테고리 수정 폼
     */
    @GetMapping("/{categorySeq}/edit")
    public String editForm(
            @PathVariable Long categorySeq,
            Model model
    ) {
        log.debug("Category edit form request for seq: {}", categorySeq);

        try {
            CategoryDto category = categoryService.findById(categorySeq);

            CategoryUpdateDto updateDto = CategoryUpdateDto.builder()
                    .categorySeq(category.getCategorySeq())
                    .name(category.getName())
                    .description(category.getDescription())
                    .build();

            model.addAttribute("category", updateDto);
            return "categories/form";
        } catch (Exception e) {
            log.error("Failed to get category for edit form, seq: {}", categorySeq, e);
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/categories";
        }
    }

    /**
     * 카테고리 활성화
     */
    @PostMapping("/{categorySeq}/activate")
    public String activate(
            @PathVariable Long categorySeq,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        log.debug("Activating category seq: {}", categorySeq);

        try {
            categoryService.activate(categorySeq);
            String message = messageSource.getMessage("category.message.activate.success", null, localeResolver.resolveLocale(request));
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            log.error("Failed to activate category", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/categories";
    }

    /**
     * 카테고리 비활성화
     */
    @PostMapping("/{categorySeq}/deactivate")
    public String deactivate(
            @PathVariable Long categorySeq,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        log.debug("Deactivating category seq: {}", categorySeq);

        try {
            categoryService.deactivate(categorySeq);
            String message = messageSource.getMessage("category.message.deactivate.success", null, localeResolver.resolveLocale(request));
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            log.error("Failed to deactivate category", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/categories";
    }


    /**
     * 활성 카테고리 목록 API
     */
    @GetMapping("/active")
    @ResponseBody
    public ResponseEntity<Object> getActiveCategories() {
        try {
            return ResponseEntity.ok(categoryService.findAllActive());
        } catch (Exception e) {
            log.error("Failed to get active categories", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 카테고리 목록 Excel 다운로드
     */
    @GetMapping("/excel")
    public ResponseEntity<byte[]> downloadExcel(@ModelAttribute CategorySearchDto searchDto, HttpServletRequest request) {
        try {
            log.debug("Excel download request with search: {}", searchDto);

            // 서비스에서 Excel 데이터 생성
            byte[] excelData = categoryService.exportToExcel(searchDto);

            // 파일명 생성 (한글 지원)
            String fileName = excelExportService.generateFileName("카테고리목록", localeResolver.resolveLocale(request));

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