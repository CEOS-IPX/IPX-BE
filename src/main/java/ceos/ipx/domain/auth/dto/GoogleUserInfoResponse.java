package ceos.ipx.domain.auth.dto;

public record GoogleUserInfoResponse(
        String id,
        String email,
        String name,
        String picture
) {
}