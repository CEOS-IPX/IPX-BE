package ceos.ipx.domain.cases.service.component;

import ceos.ipx.domain.cases.dto.request.ComponentExtractRequest;
import ceos.ipx.domain.cases.dto.response.ComponentExtractResponse;
import ceos.ipx.domain.cases.dto.response.ComponentListResponse;
import ceos.ipx.domain.cases.entity.Case;
import ceos.ipx.domain.cases.entity.InventionComponent;
import ceos.ipx.domain.cases.repository.CaseRepository;
import ceos.ipx.domain.cases.repository.InventionComponentRepository;
import ceos.ipx.global.exception.BusinessException;
import ceos.ipx.global.exception.ErrorCode;
import ceos.ipx.global.python.PythonComponentClient;
import ceos.ipx.global.python.dto.request.search.PythonComponentExtractRequest;
import ceos.ipx.global.python.dto.response.search.PythonComponentExtractResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 구성요소 관련 서비스
 *
 * - 발명 정보를 기반으로 AI 구성요소 자동 추출
 * - 특정 사건에 저장된 구성요소 목록 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComponentService {

    private final PythonComponentClient pythonComponentClient;
    private final CaseRepository caseRepository;
    private final InventionComponentRepository inventionComponentRepository;

    /**
     * 특정 사건에 저장된 구성요소 목록 조회
     *
     * - 사건 미존재: CA001
     * - 사건 접근 권한 없음: CA002
     * - 구성요소 없음: 빈 목록 정상 반환
     * - displayOrder 오름차순 조회
     */
    @Transactional(readOnly = true)
    public ComponentListResponse getComponents(Long userId, Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.CASE_NOT_FOUND)
                );

        if (!caseEntity.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.CASE_ACCESS_DENIED);
        }

        List<InventionComponent> components =
                inventionComponentRepository
                        .findByCaseEntityOrderByDisplayOrderAsc(caseEntity);

        return ComponentListResponse.of(caseId, components);
    }

    /**
     * 발명 정보를 바탕으로 구성요소 자동 추출
     *
     * Python LLM을 호출하며 DB에는 저장하지 않는다.
     * 자동 추출 결과는 사용자가 편집한 뒤 검색 실행 시 저장한다.
     */
    public ComponentExtractResponse extract(ComponentExtractRequest request) {
        PythonComponentExtractRequest pyRequest =
                PythonComponentExtractRequest.builder()
                        .title(request.title())
                        .description(request.description())
                        .technicalField(request.technicalField())
                        .build();

        PythonComponentExtractResponse response =
                pythonComponentClient.extract(pyRequest);

        return buildResponse(response);
    }

    private ComponentExtractResponse buildResponse(
            PythonComponentExtractResponse response
    ) {
        List<PythonComponentExtractResponse.Component> pyComponents =
                response != null && response.components() != null
                        ? response.components()
                        : Collections.emptyList();

        List<ComponentExtractResponse.ComponentDto> dtos =
                pyComponents.stream()
                        .map(component ->
                                new ComponentExtractResponse.ComponentDto(
                                        toLabel(pyComponents.indexOf(component)),
                                        component.name(),
                                        component.description()
                                )
                        )
                        .toList();

        return new ComponentExtractResponse(dtos);
    }

    /**
     * 0 → A, 1 → B, 2 → C, ...
     */
    private String toLabel(int index) {
        return String.valueOf((char) ('A' + index));
    }
}