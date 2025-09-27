package io.goorm.board.controller;

import io.goorm.board.dto.excel.ExcelStockDto;
import io.goorm.board.dto.inventory.InventoryTransactionSearchDto;
import io.goorm.board.dto.supplier.SupplierDto;
import io.goorm.board.entity.InventoryTransaction;
import io.goorm.board.entity.User;
import io.goorm.board.mapper.InventoryTransactionMapper;
import io.goorm.board.mapper.SupplierMapper;
import io.goorm.board.service.ExcelService;
import io.goorm.board.service.InventoryService;
import io.goorm.board.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

/**
 * 관리자 재고 관리 컨트롤러
 */
@Slf4j
@Controller
@RequestMapping("/admin/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminInventoryController {

    private final ExcelService excelService;
    private final InventoryService inventoryService;
    private final FileUploadUtil fileUploadUtil;
    private final InventoryTransactionMapper inventoryTransactionMapper;
    private final SupplierMapper supplierMapper;

    /**
     * 재고 관리 메인 페이지
     */
    @GetMapping
    public String inventory(Model model) {
        return "admin/inventory/main";
    }

    /**
     * 엑셀 입고 처리 페이지
     */
    @GetMapping("/receiving")
    public String receivingForm(Model model) {
        // 활성 공급업체 목록 조회
        List<SupplierDto> suppliers = supplierMapper.findAllActive();
        model.addAttribute("suppliers", suppliers);
        return "admin/inventory/receiving";
    }

    /**
     * 엑셀 템플릿 다운로드 (헤더만)
     */
    @GetMapping("/template/download")
    public ResponseEntity<byte[]> downloadTemplate(@RequestParam(defaultValue = "empty") String type) {
        try {
            log.info("엑셀 템플릿 다운로드 요청 - Type: {}", type);

            byte[] templateBytes;
            String fileName;

            if ("sample".equals(type)) {
                // 완성된 테스트 데이터가 포함된 템플릿
                templateBytes = excelService.generateStockReceivingTemplate();
                fileName = "stock_receiving_sample.xlsx";
                log.info("샘플 데이터 포함 템플릿 다운로드");
            } else {
                // 헤더만 있는 빈 템플릿
                templateBytes = excelService.generateEmptyTemplate();
                fileName = "stock_receiving_template.xlsx";
                log.info("빈 템플릿 다운로드");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(templateBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(templateBytes);

        } catch (Exception e) {
            log.error("엑셀 템플릿 다운로드 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 입출고 이력 조회
     */
    @GetMapping("/transactions")
    public String transactionHistory(Model model, InventoryTransactionSearchDto searchDto) {
        try {
            List<InventoryTransaction> transactions = inventoryTransactionMapper.findAll(searchDto);
            int totalCount = inventoryTransactionMapper.countAll(searchDto);

            model.addAttribute("transactions", transactions);
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("searchDto", searchDto);

            return "admin/inventory/transactions";
        } catch (Exception e) {
            log.error("입출고 이력 조회 실패", e);
            model.addAttribute("errorMessage", "입출고 이력 조회 중 오류가 발생했습니다.");
            return "admin/inventory/transactions";
        }
    }

    /**
     * 엑셀 파일 업로드 및 입고 처리
     */
    @PostMapping("/receiving/upload")
    public String uploadAndProcess(@RequestParam("file") MultipartFile file,
                                   @RequestParam("supplierSeq") Long supplierSeq,
                                   @AuthenticationPrincipal User user,
                                  RedirectAttributes redirectAttributes) {
        try {
            log.info("엑셀 입고 처리 시작 - User: {}, FileName: {}, SupplierSeq: {}",
                    user.getEmail(), file.getOriginalFilename(), supplierSeq);

            // 공급업체 선택 여부 확인 (필수)
            if (supplierSeq == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "공급업체를 선택해주세요.");
                return "redirect:/admin/inventory/receiving";
            }
            // 파일 검증 (FileUploadUtil을 통한 검증)
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "업로드할 파일을 선택해주세요.");
                return "redirect:/admin/inventory/receiving";
            }

            // 엑셀 파일 저장 (문서 저장소에 저장, 웹에서 직접 접근 불가)
            FileUploadUtil.FileUploadResult uploadResult;
            try {
                uploadResult = fileUploadUtil.uploadFile(file, FileUploadUtil.UploadType.EXCEL_DOCUMENT);
                log.info("엑셀 파일 저장됨: {}", uploadResult.getFullPath());
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "파일 업로드 실패: " + e.getMessage());
                return "redirect:/admin/inventory/receiving";
            }

            // 엑셀 파싱
            List<ExcelStockDto> stockList = excelService.parseStockReceivingExcel(file);

            if (stockList.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "처리할 데이터가 없습니다.");
                // 빈 파일이면 업로드된 파일 삭제
                try {
                    fileUploadUtil.deleteFileByPath(uploadResult.getFullPath());
                } catch (IOException ignored) {}
                return "redirect:/admin/inventory/receiving";
            }

            // 입고 처리 (파일 정보 및 공급업체 정보 포함)
            List<String> errors = inventoryService.processStockReceiving(
                    stockList, supplierSeq, user, uploadResult.getFilename(), uploadResult.getFullPath());

            // 결과 메시지 설정
            int successCount = stockList.size() - errors.size();
            int totalCount = stockList.size();

            if (errors.isEmpty()) {
                redirectAttributes.addFlashAttribute("successMessage",
                        String.format("입고 처리가 완료되었습니다. (성공: %d건) - 파일: %s",
                                successCount, uploadResult.getFilename()));
            } else {
                redirectAttributes.addFlashAttribute("warningMessage",
                        String.format("입고 처리가 완료되었습니다. (성공: %d건, 실패: %d건) - 파일: %s",
                                successCount, errors.size(), uploadResult.getFilename()));
                redirectAttributes.addFlashAttribute("errorDetails", errors);
            }

            log.info("엑셀 입고 처리 완료 - 전체: {}건, 성공: {}건, 실패: {}건, 저장파일: {}",
                    totalCount, successCount, errors.size(), uploadResult.getFullPath());

            return "redirect:/admin/inventory/receiving";

        } catch (Exception e) {
            log.error("엑셀 입고 처리 실패 - User: {}, Error: {}", user.getEmail(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "입고 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/admin/inventory/receiving";
        }
    }

}