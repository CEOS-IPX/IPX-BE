package ceos.ipx.global.opensearch.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

/**
 * OpenSearch에서 조회한 특허 원본 문서.
 *
 * 출원번호를 OpenSearch 문서의 _id로 사용하며,
 * mget 응답의 _source와 매핑된다.
 */
public record PatentDocument(

        @JsonProperty("application_number")
        String applicationNumber,

        @JsonProperty("registration_number")
        String registrationNumber,

        @JsonProperty("open_number")
        String openNumber,

        @JsonProperty("application_date")
        LocalDate applicationDate,

        @JsonProperty("open_date")
        LocalDate openDate,

        @JsonProperty("registration_date")
        LocalDate registrationDate,

        String title,

        @JsonProperty("applicant_name")
        String applicantName,

        @JsonProperty("inventor_name")
        String inventorName,

        @JsonProperty("legal_status")
        String legalStatus,

        @JsonProperty("ipc_codes")
        List<String> ipcCodes,

        @JsonProperty("cpc_codes")
        List<String> cpcCodes,

        @JsonProperty("abstract_clean")
        String abstractClean,

        @JsonProperty("claims_independent")
        List<String> claimsIndependent

) {

        public String claimsIndependentAsString() {
                if (claimsIndependent == null || claimsIndependent.isEmpty()) {
                        return "";
                }

                return String.join("\n", claimsIndependent);
        }
}