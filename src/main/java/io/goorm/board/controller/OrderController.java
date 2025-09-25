package io.goorm.board.controller;

import io.goorm.board.dto.order.OrderCreateDto;
import io.goorm.board.dto.order.OrderDto;
import io.goorm.board.dto.order.OrderSearchDto;
import io.goorm.board.entity.User;
import io.goorm.board.enums.DeliveryStatus;
import io.goorm.board.enums.PaymentStatus;
import io.goorm.board.service.OrderService;
import io.goorm.board.service.ProductService;
import io.goorm.board.service.DiscountService;
import io.goorm.board.service.InvoiceService;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/buyer/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ProductService productService;
    private final InvoiceService invoiceService;
    private final DiscountService discountService;

    @GetMapping
    public String list(@AuthenticationPrincipal User user,
                      @ModelAttribute OrderSearchDto searchDto,
                      Model model) {
        // 바이어는 본인 회사 발주만 보기
        List<OrderDto> orders = orderService.findByCompany(user.getCompanySeq(), searchDto);
        model.addAttribute("orders", orders);
        model.addAttribute("searchDto", searchDto);
        return "buyer/orders/list";
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('BUYER') and authentication.principal.companySeq != null")
    public String createForm(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("orderCreateDto", new OrderCreateDto());
        model.addAttribute("products", productService.findSellableProducts());

        // 할인율 정보 추가
        var discountRate = discountService.calculateDiscountRate(user.getCompanySeq());
        model.addAttribute("discountRate", discountRate != null ? discountRate : 0);
        model.addAttribute("companyName", user.getCompany() != null ? user.getCompany().getCompanyName() : "N/A");

        return "buyer/orders/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('BUYER') and authentication.principal.companySeq != null")
    public String create(@Valid @ModelAttribute OrderCreateDto orderCreateDto,
                        BindingResult bindingResult,
                        @AuthenticationPrincipal User user,
                        RedirectAttributes redirectAttributes,
                        Model model) {

        log.info("=== ORDER CREATE START ===");
        log.info("User: {}, Company: {}", user.getEmail(), user.getCompanySeq());
        log.info("OrderCreateDto: {}", orderCreateDto);

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("products", productService.findSellableProducts());
            model.addAttribute("discountRate", discountService.calculateDiscountRate(user.getCompanySeq()));
            model.addAttribute("companyName", user.getCompany() != null ? user.getCompany().getCompanyName() : "N/A");
            model.addAttribute("errorMessage", "발주할 상품을 선택해주세요.");
            return "buyer/orders/form";
        }

        log.info("Validation passed, calling orderService.createOrder()");
        OrderDto order = orderService.createOrder(orderCreateDto, user);
        log.info("Order created successfully: {}", order.getOrderSeq());

        redirectAttributes.addFlashAttribute("successMessage", "발주가 성공적으로 등록되었습니다.");
        return "redirect:/buyer/orders/" + order.getOrderSeq();
    }

    @GetMapping("/{orderSeq}")
    public String detail(@PathVariable Long orderSeq, Model model) {
        OrderDto order = orderService.findById(orderSeq);
        model.addAttribute("order", order);

        // 배송 완료된 경우에만 결제 관련 정보 추가
        if (order.getDeliveryStatus() == DeliveryStatus.DELIVERY_COMPLETED) {
            // 결제 정보 표시 플래그
            model.addAttribute("showPaymentInfo", true);

            // 연체 여부 계산
            boolean isOverdue = order.getPaymentDueDate() != null &&
                order.getPaymentDueDate().isBefore(LocalDate.now()) &&
                order.getPaymentStatus() == PaymentStatus.PENDING;
            model.addAttribute("isOverdue", isOverdue);

            // 인보이스 다운로드 버튼 표시
            model.addAttribute("showInvoiceDownload", true);

            // 결제 상태 한글명
            String paymentStatusDisplay = order.getPaymentStatus() == PaymentStatus.PENDING ?
                "입금대기" : "입금완료";
            model.addAttribute("paymentStatusDisplay", paymentStatusDisplay);
        }

        return "buyer/orders/detail";
    }

    @GetMapping("/{orderSeq}/invoice")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long orderSeq,
                                                 @AuthenticationPrincipal User user) {
        try {
            log.info("인보이스 다운로드 요청 - OrderSeq: {}, User: {}", orderSeq, user.getEmail());

            // 주문 정보 확인 (본인 회사 주문인지 검증)
            OrderDto order = orderService.findById(orderSeq);
            if (!order.getCompanySeq().equals(user.getCompanySeq())) {
                log.warn("권한 없는 인보이스 다운로드 시도 - OrderSeq: {}, UserCompany: {}, OrderCompany: {}",
                        orderSeq, user.getCompanySeq(), order.getCompanySeq());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // PDF 생성
            byte[] pdfBytes = invoiceService.generateInvoicePdf(orderSeq, user);

            // 파일명 생성
            String fileName = invoiceService.generateFileName(order);

            // 응답 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(pdfBytes.length);

            log.info("인보이스 다운로드 성공 - OrderSeq: {}, FileName: {}", orderSeq, fileName);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("인보이스 다운로드 실패 - OrderSeq: {}, Error: {}", orderSeq, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}