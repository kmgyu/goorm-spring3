package io.goorm.board.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import io.goorm.board.dto.order.OrderDto;
import io.goorm.board.entity.InvoiceHistory;
import io.goorm.board.entity.User;
import io.goorm.board.mapper.InvoiceHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 인보이스 생성 및 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceService {

    private final TemplateEngine templateEngine;
    private final OrderService orderService;
    private final InvoiceHistoryMapper invoiceHistoryMapper;

    /**
     * 인보이스 PDF 생성 및 다운로드
     */
    @Transactional
    public byte[] generateInvoicePdf(Long orderSeq, User user) {
        try {
            // 주문 정보 조회
            OrderDto order = orderService.findById(orderSeq);

            // 인보이스 ID 생성
            String invoiceId = generateInvoiceId(order);

            // QR 코드 생성
            String qrCodeBase64 = generateQRCode(invoiceId, order);

            // 템플릿 데이터 준비
            Map<String, Object> templateData = prepareTemplateData(order, invoiceId, qrCodeBase64);

            // HTML 렌더링
            String htmlContent = renderHtmlTemplate(templateData);

            // PDF 생성
            byte[] pdfBytes = convertToPdf(htmlContent);

            // 출력 이력 저장
            saveInvoiceHistory(orderSeq, invoiceId, user);

            log.info("인보이스 PDF 생성 완료 - Order: {}, InvoiceId: {}, User: {}",
                    orderSeq, invoiceId, user.getEmail());

            return pdfBytes;

        } catch (Exception e) {
            log.error("인보이스 PDF 생성 실패 - Order: {}, Error: {}", orderSeq, e.getMessage(), e);
            throw new RuntimeException("인보이스 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 인보이스 ID 생성 (INV-YYYYMMDD-SEQ)
     */
    private String generateInvoiceId(OrderDto order) {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("INV-%s-%03d", datePrefix, order.getOrderSeq() % 1000);
    }

    /**
     * QR 코드 생성 (Base64)
     */
    private String generateQRCode(String invoiceId, OrderDto order) {
        try {
            // QR 코드에 포함될 검증 데이터
            String qrData = String.format(
                "{\"invoiceId\":\"%s\",\"orderSeq\":%d,\"companySeq\":%d,\"finalAmount\":%s,\"issueDate\":\"%s\"}",
                invoiceId,
                order.getOrderSeq(),
                order.getCompanySeq(),
                order.getFinalAmount().toString(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, 100, 100);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());

        } catch (Exception e) {
            log.warn("QR 코드 생성 실패 - InvoiceId: {}, Error: {}", invoiceId, e.getMessage());
            return ""; // QR 코드 생성 실패해도 인보이스는 생성
        }
    }

    /**
     * 템플릿 데이터 준비
     */
    private Map<String, Object> prepareTemplateData(OrderDto order, String invoiceId, String qrCodeBase64) {
        Map<String, Object> data = new HashMap<>();

        data.put("order", order);
        data.put("invoiceId", invoiceId);
        data.put("invoiceGeneratedAt", order.getInvoiceGeneratedAt());
        data.put("paymentDueDate", order.getPaymentDueDate());
        data.put("qrCodeBase64", qrCodeBase64);
        data.put("currentDateTime", LocalDateTime.now());

        return data;
    }

    /**
     * HTML 템플릿 렌더링
     */
    private String renderHtmlTemplate(Map<String, Object> data) {
        Context context = new Context();
        data.forEach(context::setVariable);

        return templateEngine.process("invoice/invoice-template", context);
    }

    /**
     * HTML을 PDF로 변환
     */
    private byte[] convertToPdf(String htmlContent) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();

            // HTML 내용 설정
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();

            // PDF 생성
            renderer.createPDF(outputStream);
            renderer.finishPDF();

            return outputStream.toByteArray();
        }
    }

    /**
     * 인보이스 출력 이력 저장
     */
    private void saveInvoiceHistory(Long orderSeq, String invoiceId, User user) {
        InvoiceHistory history = InvoiceHistory.builder()
                .orderSeq(orderSeq)
                .invoiceId(invoiceId)
                .printedAt(LocalDateTime.now())
                .printedBySeq(user.getUserSeq())
                .printedBy(user.getEmail())
                .printCount(1)
                .build();

        invoiceHistoryMapper.insert(history);
        log.debug("인보이스 출력 이력 저장 완료 - InvoiceId: {}", invoiceId);
    }

    /**
     * 파일명 생성 (invoice_20251224_143052_orderNumber.pdf)
     */
    public String generateFileName(OrderDto order) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("invoice_%s_%s.pdf", timestamp, order.getOrderNumber());
    }
}