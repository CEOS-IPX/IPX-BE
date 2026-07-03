package ceos.ipx.domain.auth.controller;

import ceos.ipx.domain.auth.dto.EmailVerificationSendRequest;
import ceos.ipx.domain.auth.dto.EmailVerificationSendResponse;
import ceos.ipx.domain.auth.service.EmailVerificationSendService;
import ceos.ipx.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/email")
public class EmailVerificationController {

    private final EmailVerificationSendService emailVerificationSendService;

    @PostMapping("/send")
    public ApiResponse<EmailVerificationSendResponse> sendEmailVerificationCode(
            @Valid @RequestBody EmailVerificationSendRequest request
    ) {
        return ApiResponse.ok(emailVerificationSendService.sendEmailVerificationCode(request));
    }
}