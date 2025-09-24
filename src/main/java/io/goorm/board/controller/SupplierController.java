package io.goorm.board.controller;

import io.goorm.board.dto.supplier.SupplierCreateDto;
import io.goorm.board.dto.supplier.SupplierDto;
import io.goorm.board.dto.supplier.SupplierSearchDto;
import io.goorm.board.dto.supplier.SupplierUpdateDto;
import io.goorm.board.entity.User;
import io.goorm.board.enums.SupplierStatus;
import io.goorm.board.service.ExcelExportService;
import io.goorm.board.service.SupplierService;
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
 * 공급업체 관리 컨트롤러
 */
@Slf4j
@Controller
@RequestMapping("/admin/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;
    private final ExcelExportService excelExportService;
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    /**
     * 공급업체 목록 조회
     */
    @GetMapping
    public String list(
            @ModelAttribute SupplierSearchDto searchDto,
            Model model
    ) {
        log.debug("Supplier list request with search: {}", searchDto);

        Page<SupplierDto> suppliers = supplierService.searchSuppliers(searchDto);

        model.addAttribute("suppliers", suppliers.getContent());
        model.addAttribute("currentPage", suppliers.getNumber() + 1);
        model.addAttribute("totalPages", suppliers.getTotalPages());
        model.addAttribute("totalElements", suppliers.getTotalElements());
        model.addAttribute("size", suppliers.getSize());
        model.addAttribute("search", searchDto);
        model.addAttribute("statuses", SupplierStatus.values());
        model.addAttribute("pageTitle", "공급업체 관리");

        return "suppliers/list";
    }

    /**
     * 공급업체 상세보기
     */
    @GetMapping("/{seq}")
    public String show(
            @PathVariable Long seq,
            Model model
    ) {
        log.debug("Supplier detail request for seq: {}", seq);

        SupplierDto supplier = supplierService.findBySeq(seq);
        model.addAttribute("supplier", supplier);
        return "suppliers/show";
    }

    /**
     * 공급업체 등록 폼
     */
    @GetMapping("/new")
    public String createForm(Model model) {
        log.debug("Supplier create form request");

        model.addAttribute("supplier", new SupplierCreateDto());
        return "suppliers/form";
    }

    /**
     * 공급업체 등록 처리
     */
    @PostMapping
    public String create(
            @Valid @ModelAttribute("supplier") SupplierCreateDto createDto,
            BindingResult bindingResult,
            @AuthenticationPrincipal User currentUser,
            RedirectAttributes redirectAttributes,
            Model model,
            HttpServletRequest request
    ) {
        log.debug("공급업체 등록 요청: {}", createDto.getName());

        if (bindingResult.hasErrors()) {
            return "suppliers/form";
        }

        // 사용자 정보 설정
        createDto.setCreatedSeq(currentUser.getUserSeq());
        createDto.setUpdatedSeq(currentUser.getUserSeq());

        Long supplierSeq = supplierService.create(createDto);
        String message = messageSource.getMessage("supplier.message.create.success", null, localeResolver.resolveLocale(request));
        redirectAttributes.addFlashAttribute("successMessage", message);
        return "redirect:/suppliers/" + supplierSeq;
    }

    /**
     * 공급업체 수정 폼
     */
    @GetMapping("/{seq}/edit")
    public String editForm(
            @PathVariable Long seq,
            Model model
    ) {
        log.debug("Supplier edit form request for seq: {}", seq);

        SupplierDto supplier = supplierService.findBySeq(seq);

        SupplierUpdateDto updateDto = SupplierUpdateDto.builder()
                .supplierSeq(supplier.getSupplierSeq())
                .name(supplier.getName())
                .contactPerson(supplier.getContactPerson())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .description(supplier.getDescription())
                .status(supplier.getStatus())
                .isActive(supplier.getStatus() == SupplierStatus.ACTIVE)
                .build();

        model.addAttribute("supplier", updateDto);
        return "suppliers/form";
    }

    /**
     * 공급업체 수정 처리
     */
    @PostMapping("/{seq}")
    public String update(
            @PathVariable Long seq,
            @Valid @ModelAttribute("supplier") SupplierUpdateDto updateDto,
            BindingResult bindingResult,
            @AuthenticationPrincipal User currentUser,
            RedirectAttributes redirectAttributes,
            Model model,
            HttpServletRequest request
    ) {
        log.debug("공급업체 수정 요청 seq: {}", seq);

        updateDto.setSupplierSeq(seq);

        if (bindingResult.hasErrors()) {
            return "suppliers/form";
        }

        // 사용자 정보 설정
        updateDto.setUpdatedSeq(currentUser.getUserSeq());

        supplierService.update(seq, updateDto);
        String message = messageSource.getMessage("supplier.message.update.success", null, localeResolver.resolveLocale(request));
        redirectAttributes.addFlashAttribute("successMessage", message);
        return "redirect:/suppliers/" + seq;
    }

    /**
     * 공급업체 활성화
     */
    @PostMapping("/{seq}/activate")
    public String activate(@PathVariable Long seq, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        log.debug("Supplier activate request for seq: {}", seq);

        supplierService.activate(seq);
        String message = messageSource.getMessage("supplier.message.activate.success", null, localeResolver.resolveLocale(request));
        redirectAttributes.addFlashAttribute("successMessage", message);

        return "redirect:/suppliers/" + seq;
    }

    /**
     * 공급업체 비활성화
     */
    @PostMapping("/{seq}/deactivate")
    public String deactivate(@PathVariable Long seq, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        log.debug("Supplier deactivate request for seq: {}", seq);

        supplierService.deactivate(seq);
        String message = messageSource.getMessage("supplier.message.deactivate.success", null, localeResolver.resolveLocale(request));
        redirectAttributes.addFlashAttribute("successMessage", message);

        return "redirect:/suppliers/" + seq;
    }

    /**
     * Excel 다운로드
     */
    @GetMapping("/excel")
    @ResponseBody
    public ResponseEntity<byte[]> downloadExcel(
            @ModelAttribute SupplierSearchDto searchDto,
            HttpServletRequest request
    ) {
        log.debug("Supplier Excel download request with search: {}", searchDto);

        byte[] excelData = supplierService.exportToExcel(searchDto);

        // 파일명 생성 (한글 지원)
        String fileName = excelExportService.generateFileName("공급업체목록", localeResolver.resolveLocale(request));

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }
}