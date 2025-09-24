package io.goorm.board.controller;

import io.goorm.board.dto.order.OrderDto;
import io.goorm.board.dto.order.OrderSearchDto;
import io.goorm.board.entity.Company;
import io.goorm.board.service.OrderService;
import io.goorm.board.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;
    private final UserService userService;

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
        return "buyer/orders/detail"; // 같은 템플릿 사용
    }


}