package io.goorm.board.controller.axios;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/axios/auth")
@RequiredArgsConstructor
public class AxiosAuthController {

    @GetMapping("/login")
    public String login() {
        log.debug("Axios 로그인 페이지 요청");
        return "axios/auth/login";
    }
}