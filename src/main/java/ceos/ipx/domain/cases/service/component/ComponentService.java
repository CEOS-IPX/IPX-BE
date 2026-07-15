package ceos.ipx.domain.cases.service.component;

import ceos.ipx.domain.cases.dto.request.ComponentExtractRequest;
import ceos.ipx.domain.cases.dto.request.ComponentSaveRequest;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 구성요소 관련 서비스
 *
 * - 발명 정보를 기반으로 AI 구성요소 자동 추출
 * - 특정 사건에 저장된 구성요소 목록 조회
 * - 특정 사건의 구성요소 전체 목록 저장 및 수정
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
        Case caseEntity = findCaseAndValidateOwner(userId, caseId);

        List<InventionComponent> components =
                inventionComponentRepository
                        .findByCaseEntityOrderByDisplayOrderAsc(caseEntity);

        return ComponentListResponse.of(caseId, components);
    }

    /**
     * 특정 사건의 구성요소 전체 목록 저장 및 수정
     *
     * - 요청 배열 순서대로 displayOrder를 1부터 부여
     * - 동일한 displayOrder의 기존 구성요소는 수정
     * - 요청에서 제외된 기존 구성요소는 삭제
     * - 기존에 없던 displayOrder는 새로 생성
     * - 빈 배열 요청 시 기존 구성요소 전체 삭제
     */
    @Transactional
    public ComponentListResponse saveComponents(
            Long userId,
            Long caseId,
            ComponentSaveRequest request
    ) {
        Case caseEntity = findCaseAndValidateOwner(userId, caseId);

        List<InventionComponent> existingComponents =
                inventionComponentRepository
                        .findByCaseEntityOrderByDisplayOrderAsc(caseEntity);

        Map<Short, InventionComponent> existingByDisplayOrder =
                new HashMap<>();

        for (InventionComponent existingComponent : existingComponents) {
            existingByDisplayOrder.put(
                    existingComponent.getDisplayOrder(),
                    existingComponent
            );
        }

        List<ComponentSaveRequest.ComponentDto> requestedComponents =
                request.components();

        List<InventionComponent> newComponents = new ArrayList<>();

        for (int index = 0; index < requestedComponents.size(); index++) {
            short displayOrder = (short) (index + 1);

            ComponentSaveRequest.ComponentDto requestedComponent =
                    requestedComponents.get(index);

            InventionComponent existingComponent =
                    existingByDisplayOrder.remove(displayOrder);

            if (existingComponent != null) {
                existingComponent.update(
                        requestedComponent.name(),
                        requestedComponent.description()
                );

                continue;
            }

            InventionComponent newComponent =
                    InventionComponent.builder()
                            .caseEntity(caseEntity)
                            .name(requestedComponent.name())
                            .description(requestedComponent.description())
                            .displayOrder(displayOrder)
                            .build();

            newComponents.add(newComponent);
        }

        /*
         * Map에 남은 구성요소는 요청 목록에 포함되지 않은 기존 구성요소다.
         *
         * 신규 INSERT 전에 DELETE SQL을 DB에 먼저 반영하여
         * UNIQUE(case_id, display_order) 충돌 가능성을 방지한다.
         */
        if (!existingByDisplayOrder.isEmpty()) {
            inventionComponentRepository.deleteAll(
                    existingByDisplayOrder.values()
            );
            inventionComponentRepository.flush();
        }

        if (!newComponents.isEmpty()) {
            inventionComponentRepository.saveAll(newComponents);
            inventionComponentRepository.flush();
        }

        List<InventionComponent> savedComponents =
                inventionComponentRepository
                        .findByCaseEntityOrderByDisplayOrderAsc(caseEntity);

        return ComponentListResponse.of(caseId, savedComponents);
    }

    /**
     * 사건 존재 여부와 소유자 권한 검증
     *
     * - 사건 미존재: CA001
     * - 사건 접근 권한 없음: CA002
     */
    private Case findCaseAndValidateOwner(Long userId, Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.CASE_NOT_FOUND)
                );

        if (!caseEntity.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.CASE_ACCESS_DENIED);
        }

        return caseEntity;
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