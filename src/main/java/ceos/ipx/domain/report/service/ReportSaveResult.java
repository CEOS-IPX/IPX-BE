package ceos.ipx.domain.report.service;

import ceos.ipx.domain.report.dto.response.ReportCreateResponse;

public record ReportSaveResult(
        ReportCreateResponse response,
        boolean created
) {
}