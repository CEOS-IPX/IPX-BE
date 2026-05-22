package ceos.ipx.domain.swagger.controller;

import ceos.ipx.domain.swagger.dto.SwaggerTestRequest;
import ceos.ipx.domain.swagger.dto.SwaggerTestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Swagger Test", description = "Swagger UI 동작 확인용 임시 테스트 API")
@RestController
@RequestMapping("/api/swagger-test")
public class SwaggerTestController {

    @Operation(
        summary = "Swagger health check",
        description = "Swagger UI에서 GET API 호출을 확인하기 위한 테스트 API입니다."
    )
    @GetMapping("/health")
    public ResponseEntity<SwaggerTestResponse> health() {
        return ResponseEntity.ok(SwaggerTestResponse.health());
    }

    @Operation(
        summary = "Swagger mock login",
        description = "Swagger UI에서 Request Body가 있는 POST API 호출을 확인하기 위한 테스트 API입니다."
    )
    @PostMapping("/login")
    public ResponseEntity<SwaggerTestResponse> login(@Valid @RequestBody SwaggerTestRequest request) {
        return ResponseEntity.ok(SwaggerTestResponse.login(request.email()));
    }
}
