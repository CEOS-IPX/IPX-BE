package ceos.ipx.global.python.dto.response.inventivestep;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Python /analyze/inventive-step/select-secondary
 */
public record PythonSelectSecondaryResponse(

        @JsonProperty("d2_application_number")
        String d2ApplicationNumber
) {}