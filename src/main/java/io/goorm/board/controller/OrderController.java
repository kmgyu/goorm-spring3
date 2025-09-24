package io.goorm.board.controller;

import io.goorm.board.dto.order.OrderCreateDto;
import io.goorm.board.dto.order.OrderDto;
import io.goorm.board.dto.order.OrderSearchDto;
import io.goorm.board.entity.User;
import io.goorm.board.service.OrderService;
import io.goorm.board.service.ProductService;
import io.goorm.board.service.DiscountService;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/buyer/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ProductService productService;
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
        return "buyer/orders/detail";
    }

}