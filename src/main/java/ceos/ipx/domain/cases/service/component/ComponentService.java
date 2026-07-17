package ceos.ipx.domain.cases.service.component;

import ceos.ipx.domain.cases.dto.request.ComponentExtractRequest;
import ceos.ipx.domain.cases.dto.response.ComponentExtractResponse;
import ceos.ipx.global.python.PythonComponentClient;
import ceos.ipx.global.python.dto.request.search.PythonComponentExtractRequest;
import ceos.ipx.global.python.dto.response.search.PythonComponentExtractResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 구성요소 관련 서비스
 *
 * - 발명 정보를 기반으로 AI 구성요소 자동 추출
 * - 자동 추출 결과는 DB에 저장하지 않음
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComponentService {

    private final PythonComponentClient pythonComponentClient;

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