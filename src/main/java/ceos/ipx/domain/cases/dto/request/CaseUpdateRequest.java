package ceos.ipx.domain.cases.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CaseUpdateRequest {

    @Size(max = 500, message = "사건명은 500자 이하여야 합니다.")
    private String title;

    @Size(max = 200, message = "사명은 200자 이하여야 합니다.")
    private String applicantName;

    @Size(max = 200, message = "의뢰인은 200자 이하여야 합니다.")
    private String inventorName;

    @JsonIgnore
    private boolean titlePresent;

    @JsonIgnore
    private boolean applicantNamePresent;

    @JsonIgnore
    private boolean inventorNamePresent;

    @JsonSetter("title")
    public void setTitle(String title) {
        this.title = title;
        this.titlePresent = true;
    }

    @JsonSetter("applicantName")
    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
        this.applicantNamePresent = true;
    }

    @JsonSetter("inventorName")
    public void setInventorName(String inventorName) {
        this.inventorName = inventorName;
        this.inventorNamePresent = true;
    }

    @JsonIgnore
    @AssertTrue(message = "수정할 필드를 하나 이상 입력해야 하며, 사건명은 비어 있을 수 없습니다.")
    public boolean isValidUpdateRequest() {
        boolean hasAnyField =
                titlePresent || applicantNamePresent || inventorNamePresent;

        if (!hasAnyField) {
            return false;
        }

        return !titlePresent || (title != null && !title.isBlank());
    }
}