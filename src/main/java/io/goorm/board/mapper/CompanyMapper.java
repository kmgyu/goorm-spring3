package io.goorm.board.mapper;

import io.goorm.board.entity.Company;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 회사 관련 MyBatis Mapper 인터페이스
 */
@Mapper
public interface CompanyMapper {

    /**
     * 모든 회사 조회 (회사명 순)
     */
    List<Company> findAllOrderByCompanyName();

    /**
     * 회사 ID로 조회
     */
    Company findById(Long companySeq);
}