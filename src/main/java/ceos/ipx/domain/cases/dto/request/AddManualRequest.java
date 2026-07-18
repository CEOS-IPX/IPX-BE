package ceos.ipx.domain.cases.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 선행기술 수동 추가 요청
 *
 * 사용자가 출원번호로 특허를 직접 추가할 때 사용
 * Spring은 이 요청을 받아:
 *   1. 기존 prior_arts와 중복 확인/필터링
 *   2. Case에서 context 조립
 *   3. Python에 filtered application_numbers + context 전달
 *   4. 결과를 prior_arts에 INSERT
 */
@Schema(description = "선행기술 수동 추가 요청")
public record AddManualRequest(

        @Schema(
                description = "추가할 특허 출원번호 목록 (한 번에 최대 10개). 이미 추가된 특허는 자동으로 필터링됨.",
                example = "[\"1020200012345\", \"1020210056789\"]",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "출원번호 목록은 필수입니다.")
        @NotEmpty(message = "최소 1개 이상의 출원번호를 입력해주세요.")
        @Size(max = 10, message = "한 번에 최대 10개까지 추가 가능합니다.")
        List<
                @NotNull(message = "출원번호에 null이 포함될 수 없습니다.")
                @Pattern(
                        regexp = "^\\d{2}-?\\d{4}-?\\d{7}$",
                        message = "출원번호는 13자리 숫자(하이픈 포함/미포함 모두 가능)여야 합니다."
                )String> applicationNumbers
) {}