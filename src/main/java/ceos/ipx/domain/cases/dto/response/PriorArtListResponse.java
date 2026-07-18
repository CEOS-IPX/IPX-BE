package ceos.ipx.domain.cases.dto.response;

import java.util.List;

public record PriorArtListResponse(
        int totalCount,
        List<PriorArtResponse> priorArts
) {

    public static PriorArtListResponse of(List<PriorArtResponse> priorArts) {
        return new PriorArtListResponse(priorArts.size(), priorArts);
    }
}