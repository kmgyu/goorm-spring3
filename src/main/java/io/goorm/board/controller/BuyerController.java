package io.goorm.board.controller;

import io.goorm.board.entity.User;
import io.goorm.board.service.OrderService;
import io.goorm.board.service.UserService;
import io.goorm.board.dto.order.OrderSearchDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.transaction.annotation.Transactional;

/**
 * 바이어 전용 컨트롤러
 */
@Controller
@RequestMapping("/buyer")
@RequiredArgsConstructor
public class BuyerController {

    private final OrderService orderService;
    private final UserService userService;

    @GetMapping("/dashboard")
    @Transactional(readOnly = true)
    public String dashboard(Model model, @AuthenticationPrincipal User user) {
        System.out.println("=== BuyerController.dashboard 디버깅 ===");
        System.out.println("User ID: " + user.getUserSeq());
        System.out.println("User Email: " + user.getEmail());
        System.out.println("User Nickname: " + user.getNickname());

        try {
            // 일단 기본 사용자 정보 먼저 전달
            model.addAttribute("pageTitle", "바이어 대시보드");
            model.addAttribute("user", user);

            // 회사 정보를 포함한 사용자 정보 다시 조회 (LAZY 로딩 해결)
            User userWithCompany = userService.findByIdWithCompany(user.getUserSeq());
            System.out.println("UserWithCompany 조회 성공");
            System.out.println("Company: " + userWithCompany.getCompany());
            System.out.println("CompanySeq: " + userWithCompany.getCompanySeq());

            // 회사 정보가 있는 사용자로 교체
            if (userWithCompany != null) {
                model.addAttribute("user", userWithCompany);
            }

            // 최근 발주 목록 (최대 5개)
            if (userWithCompany != null && userWithCompany.getCompanySeq() != null) {
                System.out.println("회사 정보 있음, 발주 목록 조회 시작");
                OrderSearchDto searchDto = new OrderSearchDto();
                model.addAttribute("recentOrders", orderService.findByCompany(userWithCompany.getCompanySeq(), searchDto));
                System.out.println("발주 목록 조회 완료");
            } else {
                System.out.println("회사 정보 없음");
            }
        } catch (Exception e) {
            System.out.println("에러 발생: " + e.getMessage());
            e.printStackTrace();
            // 에러 발생시 기본 사용자 정보라도 전달
            model.addAttribute("pageTitle", "바이어 대시보드");
            model.addAttribute("user", user);
        }

        return "buyer/dashboard";
    }

}