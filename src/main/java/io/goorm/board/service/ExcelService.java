package io.goorm.board.service;

import io.goorm.board.dto.excel.StockReceivingDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 엑셀 파일 처리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelService {

    /**
     * 입고처리용 엑셀 템플릿 생성 (테스트 데이터 포함)
     */
    public byte[] generateStockReceivingTemplate() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("입고처리");

            // 헤더 생성
            Row headerRow = sheet.createRow(0);
            String[] headers = {"상품명", "카테고리명", "입고수량", "입고단가", "비고"};

            CellStyle headerStyle = createHeaderStyle(workbook);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 완성된 테스트 데이터 추가
            Object[][] testData = {
                {"노트북 컴퓨터", "전자제품", 20, 1500000, "고성능 업무용 노트북"},
                {"무선 마우스", "전자제품", 50, 25000, "블루투스 무선 마우스"},
                {"키보드", "전자제품", 30, 85000, "기계식 키보드"},
                {"모니터", "전자제품", 15, 350000, "27인치 4K 모니터"},
                {"책상", "가구", 10, 180000, "높이조절 책상"},
                {"의자", "가구", 12, 220000, "인체공학적 의자"},
                {"필기구 세트", "사무용품", 100, 15000, "볼펜, 연필, 지우개 세트"},
                {"복사용지", "사무용품", 200, 8000, "A4 80g 500매"},
                {"테이블", "가구", 8, 150000, "회의용 테이블"},
                {"프린터", "전자제품", 5, 180000, "레이저 프린터"}
            };

            for (int i = 0; i < testData.length; i++) {
                Row dataRow = sheet.createRow(i + 1);
                Object[] rowData = testData[i];

                dataRow.createCell(0).setCellValue((String) rowData[0]);     // 상품명
                dataRow.createCell(1).setCellValue((String) rowData[1]);     // 카테고리명
                dataRow.createCell(2).setCellValue((Integer) rowData[2]);    // 입고수량
                dataRow.createCell(3).setCellValue((Integer) rowData[3]);    // 입고단가
                dataRow.createCell(4).setCellValue((String) rowData[4]);     // 비고
            }

            // 컬럼 너비 자동 조정
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 2000);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("엑셀 템플릿 생성 실패", e);
            throw new RuntimeException("엑셀 템플릿 생성 중 오류가 발생했습니다.");
        }
    }

    /**
     * 빈 엑셀 템플릿 생성 (헤더만)
     */
    public byte[] generateEmptyTemplate() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("입고처리");

            // 헤더 생성
            Row headerRow = sheet.createRow(0);
            String[] headers = {"상품명", "카테고리명", "입고수량", "입고단가", "비고"};

            CellStyle headerStyle = createHeaderStyle(workbook);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 컬럼 너비 설정
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 2000);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("빈 엑셀 템플릿 생성 실패", e);
            throw new RuntimeException("빈 엑셀 템플릿 생성 중 오류가 발생했습니다.");
        }
    }

    /**
     * 엑셀 파일에서 입고 데이터 파싱
     */
    public List<StockReceivingDto> parseStockReceivingExcel(MultipartFile file) {
        List<StockReceivingDto> stockList = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // 헤더 행 건너뛰기
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) {
                    continue;
                }

                try {
                    StockReceivingDto dto = parseRowToDto(row, i + 1);
                    if (dto != null) {
                        stockList.add(dto);
                    }
                } catch (Exception e) {
                    log.warn("엑셀 {}행 파싱 실패: {}", i + 1, e.getMessage());
                    // 개별 행 오류는 로그만 남기고 계속 진행
                }
            }

            log.info("엑셀 파싱 완료 - 총 {}개 행 처리", stockList.size());
            return stockList;

        } catch (IOException e) {
            log.error("엑셀 파일 읽기 실패", e);
            throw new RuntimeException("엑셀 파일을 읽을 수 없습니다.");
        }
    }

    /**
     * 행을 DTO로 변환
     */
    private StockReceivingDto parseRowToDto(Row row, int rowNumber) {
        StockReceivingDto dto = new StockReceivingDto();
        dto.setRowNumber(rowNumber);

        // 상품명 (필수)
        String productName = getCellStringValue(row.getCell(0));
        if (productName == null || productName.trim().isEmpty()) {
            throw new RuntimeException("상품명이 비어있습니다.");
        }
        dto.setProductName(productName.trim());

        // 카테고리명 (필수)
        String categoryName = getCellStringValue(row.getCell(1));
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new RuntimeException("카테고리명이 비어있습니다.");
        }
        dto.setCategoryName(categoryName.trim());

        // 입고수량 (필수)
        Double quantity = getCellNumericValue(row.getCell(2));
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("입고수량이 올바르지 않습니다.");
        }
        dto.setQuantity(quantity.intValue());

        // 입고단가 (필수)
        Double unitPrice = getCellNumericValue(row.getCell(3));
        if (unitPrice == null || unitPrice < 0) {
            throw new RuntimeException("입고단가가 올바르지 않습니다.");
        }
        dto.setUnitPrice(BigDecimal.valueOf(unitPrice));

        // 비고 (선택)
        String note = getCellStringValue(row.getCell(4));
        dto.setNote(note != null ? note.trim() : "");

        return dto;
    }

    /**
     * 셀 문자열 값 가져오기
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    /**
     * 셀 숫자 값 가져오기
     */
    private Double getCellNumericValue(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }

    /**
     * 빈 행 체크
     */
    private boolean isEmptyRow(Row row) {
        for (int i = 0; i < 5; i++) { // 5개 컬럼 체크
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellStringValue(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 헤더 스타일 생성
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        return style;
    }
}