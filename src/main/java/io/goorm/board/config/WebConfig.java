package io.goorm.board.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 웹 설정
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.web-root}")
    private String uploadRoot;

    /**
     * 정적 리소스 핸들러 설정
     * 업로드된 파일들을 웹에서 접근할 수 있도록 경로 매핑
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 업로드 파일 경로 매핑
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadRoot + "/")
                .setCachePeriod(3600); // 1시간 캐시

        // 기본 정적 리소스들 (필요시)
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);

        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCachePeriod(3600);

        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCachePeriod(3600);

        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(3600);
    }
}