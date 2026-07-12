package ceos.ipx.domain.cases.service;

import ceos.ipx.domain.cases.dto.request.ComponentExtractRequest;
import ceos.ipx.domain.cases.dto.response.ComponentExtractResponse;
import ceos.ipx.global.python.PythonComponentClient;
import ceos.ipx.global.python.dto.request.PythonComponentExtractRequest;
import ceos.ipx.global.python.dto.response.PythonComponentExtractResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 구성요소 자동 추출 서비스
 *
 * Python 호출만 하고 DB 저장은 하지 않음:
 *   - 자동 추출 결과는 사용자가 편집
 *   - 저장은 검색 실행 시 POST /api/searches에서 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComponentService {

    private final PythonComponentClient pythonComponentClient;

    /**
     * 발명 정보를 바탕으로 구성요소 자동 추출
     * Python LLM 호출
     * label은 Spring에서 배열 순서대로 부여 (A, B, C, ...)
     */
    public ComponentExtractResponse extract(ComponentExtractRequest request) {
        PythonComponentExtractRequest pyRequest = PythonComponentExtractRequest.builder()
                .title(request.title())
                .description(request.description())
                .technicalField(request.technicalField())
                .build();

        PythonComponentExtractResponse response = pythonComponentClient.extract(pyRequest);

        return buildResponse(response);
    }

    private ComponentExtractResponse buildResponse(PythonComponentExtractResponse response) {
        List<PythonComponentExtractResponse.Component> pyComponents =
                response != null && response.components() != null
                        ? response.components()
                        : Collections.emptyList();

        List<ComponentExtractResponse.ComponentDto> dtos = pyComponents.stream()
                .map(c -> new ComponentExtractResponse.ComponentDto(
                        toLabel(pyComponents.indexOf(c)),
                        c.name(),
                        c.description()
                ))
                .toList();

        return new ComponentExtractResponse(dtos);
    }

    /**
     * 0 → "A", 1 → "B", 2 → "C", ...
     * 26 이상은 실무적으로 없을 것
     */
    private String toLabel(int index) {
        return String.valueOf((char) ('A' + index));
    }
}