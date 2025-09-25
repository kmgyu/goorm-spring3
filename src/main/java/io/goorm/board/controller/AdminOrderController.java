package io.goorm.board.controller;

import io.goorm.board.dto.order.OrderDto;
import io.goorm.board.dto.order.OrderSearchDto;
import io.goorm.board.entity.Company;
import io.goorm.board.entity.User;
import io.goorm.board.enums.DeliveryStatus;
import io.goorm.board.enums.PaymentStatus;
import io.goorm.board.exception.DeliveryCompleteException;
import io.goorm.board.exception.PaymentCompleteException;
import io.goorm.board.service.OrderService;
import io.goorm.board.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final MessageSource messageSource;

    @GetMapping
    public String list(@ModelAttribute OrderSearchDto searchDto, Model model) {
        // 관리자는 모든 발주 보기 + 회사 검색 조건
        List<OrderDto> orders = orderService.findAll(searchDto);
        List<Company> companies = userService.findAllCompanies();

        model.addAttribute("orders", orders);
        model.addAttribute("companies", companies);
        model.addAttribute("searchDto", searchDto);
        return "buyer/orders/list"; // 같은 템플릿 사용
    }

    @GetMapping("/{orderSeq}")
    public String detail(@PathVariable Long orderSeq, Model model) {
        OrderDto order = orderService.findById(orderSeq);
        model.addAttribute("order", order);

        // 배송 완료된 경우에만 결제 관련 정보 추가 (OrderController와 동일)
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

        return "buyer/orders/detail"; // 같은 템플릿 사용
    }

    /**
     * 배송 완료 처리
     */
    @PostMapping("/{orderSeq}/complete-delivery")
    @PreAuthorize("hasRole('ADMIN')")
    public String completeDelivery(@PathVariable Long orderSeq,
                                   @AuthenticationPrincipal User user,
                                   RedirectAttributes redirectAttributes) {
        try {
            OrderDto order = orderService.completeDelivery(orderSeq, user);
            String message = messageSource.getMessage("order.delivery.complete.success", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("message", message);
            log.info("배송 완료 처리됨 - 주문: {}, 관리자: {}", orderSeq, user.getEmail());
        } catch (DeliveryCompleteException e) {
            String errorMessage = messageSource.getMessage(e.getMessageKey(), e.getArgs(), LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("error", errorMessage);
            log.warn("배송 완료 처리 실패 - 주문: {}, 오류: {}", orderSeq, e.getMessageKey());
        } catch (Exception e) {
            String errorMessage = messageSource.getMessage("order.delivery.complete.error", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("error", errorMessage);
            log.error("배송 완료 처리 중 예외 발생 - 주문: {}", orderSeq, e);
        }

        return "redirect:/admin/orders";
    }

    /**
     * 배송 시작 처리
     */
    @PostMapping("/{orderSeq}/start-delivery")
    @PreAuthorize("hasRole('ADMIN')")
    public String startDelivery(@PathVariable Long orderSeq,
                                @AuthenticationPrincipal User user,
                                RedirectAttributes redirectAttributes) {
        try {
            OrderDto order = orderService.startDelivery(orderSeq, user);
            redirectAttributes.addFlashAttribute("message", "배송이 시작되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "배송 시작 처리 중 오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/admin/orders";
    }

    /**
     * 입금 완료 처리
     */
    @PostMapping("/{orderSeq}/complete-payment")
    @PreAuthorize("hasRole('ADMIN')")
    public String completePayment(@PathVariable Long orderSeq,
                                 @AuthenticationPrincipal User user,
                                 RedirectAttributes redirectAttributes) {
        try {
            OrderDto order = orderService.completePayment(orderSeq, user);
            String message = messageSource.getMessage("order.payment.complete.success", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("message", message);
            log.info("입금 완료 처리됨 - 주문: {}, 관리자: {}", orderSeq, user.getEmail());
        } catch (PaymentCompleteException e) {
            String errorMessage = messageSource.getMessage(e.getMessageKey(), e.getArgs(), LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("error", errorMessage);
            log.warn("입금 완료 처리 실패 - 주문: {}, 오류: {}", orderSeq, e.getMessageKey());
        } catch (Exception e) {
            String errorMessage = messageSource.getMessage("order.payment.complete.error", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("error", errorMessage);
            log.error("입금 완료 처리 중 예외 발생 - 주문: {}", orderSeq, e);
        }

        return "redirect:/admin/orders";
    }
}