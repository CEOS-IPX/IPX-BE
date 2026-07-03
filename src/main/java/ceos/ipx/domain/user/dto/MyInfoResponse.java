package ceos.ipx.domain.user.dto;

import ceos.ipx.domain.user.entity.User;

public record MyInfoResponse(
        Long userId,
        String email,
        String name,
        String company,
        String provider,
        boolean profileCompleted
) {
    public static MyInfoResponse from(User user) {
        return new MyInfoResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getCompany(),
                user.getProvider().name(),
                isProfileCompleted(user)
        );
    }

    private static boolean isProfileCompleted(User user) {
        return user.getName() != null && !user.getName().isBlank()
                && user.getCompany() != null && !user.getCompany().isBlank();
    }
}