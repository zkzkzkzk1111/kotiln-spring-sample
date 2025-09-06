package ezrank.api.controller

import ezrank.api.dto.AuthDto
import ezrank.api.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "인증 API", description = "사용자 인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = ["*"])
class AuthController(
    private val authService: AuthService
) {

    @Operation(summary = "회원가입", description = "회원가입")
    @PostMapping("/signup")
    fun signup(@RequestBody request: AuthDto.SignupRequest): ResponseEntity<Any> {
        return authService.register(request).fold(
            onSuccess = { response ->
                ResponseEntity.status(HttpStatus.CREATED).body(response)
            },
            onFailure = { error ->
                ResponseEntity.badRequest().body(AuthDto.ErrorResponse(error.message ?: "회원가입 실패"))
            }
        )
    }

    @Operation(summary = "로그인", description = "로그인")
    @PostMapping("/login")
    fun login(@RequestBody request: AuthDto.LoginRequest): ResponseEntity<Any> {
        return authService.login(request).fold(
            onSuccess = { response ->
                ResponseEntity.ok(response)
            },
            onFailure = { error ->
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthDto.ErrorResponse(401, error.message ?: "로그인 실패"))
            }
        )
    }

    @Operation(summary = "비밀번호 변경", description = "비밀번호 변경")
    @PostMapping("/change-password")
    fun pwChange(@RequestBody request: AuthDto.ChangePWRequest): ResponseEntity<Any> {
        return authService.pwChange(request).fold(
            onSuccess = { response ->
                ResponseEntity.ok(response)
            },
            onFailure = { error ->
                val errorResponse = AuthDto.ErrorResponse(
                    status = 400,
                    message = "fail",
                    data = error.message ?: "비밀번호 변경 실패"
                )
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
            }
        )
    }

    @Operation(summary = "토큰 검증", description = "JWT 토큰의 유효성 검증")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/verify")
    fun verifyToken(@Parameter(description = "Bearer 토큰") @RequestHeader("Authorization") token: String): ResponseEntity<Any> {
        val userId = authService.getCurrentUserId(token)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthDto.ErrorResponse(401, "유효하지 않은 토큰입니다."))

        return authService.verifyUser(userId).fold(
            onSuccess = { response ->
                ResponseEntity.ok(response)
            },
            onFailure = { error ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(AuthDto.ErrorResponse(404, error.message ?: "토큰 검증 실패"))
            }
        )
    }

    @Operation(summary = "로그아웃", description = "로그아웃")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/logout")
    fun logout(@Parameter(description = "Bearer 토큰") @RequestHeader("Authorization") token: String): ResponseEntity<AuthDto.LogoutResponse> {
        return ResponseEntity.ok(AuthDto.LogoutResponse(message = "로그아웃 되었습니다."))
    }

    @Operation(summary = "사용자 ID 찾기", description = "ID 찾기")
    @ApiResponse(responseCode = "200", description = "사용자 ID 찾기 성공",
        content = [Content(
            mediaType = "application/json",
            examples = [ExampleObject(value = """
            {
                "status": 200,
                "message": "success", 
                "data": {
                    "user_id": "user123"
                }
            }
            """)]
        )])
    @ApiResponse(responseCode = "404", description = "사용자 ID 찾기 실패",
        content = [Content(
            mediaType = "application/json",
            examples = [ExampleObject(value = """
            {
                "status": 404,
                "message": "fail",
                "data": "해당 이메일로 등록된 사용자를 찾을 수 없습니다."
            }
            """)]
        )])
    @PostMapping("/find-userid")
    fun findUserId(@RequestBody request: AuthDto.IDSearchRequest): ResponseEntity<Any> {
        return authService.findUserId(request).fold(
            onSuccess = { response ->
                ResponseEntity.ok(response)
            },
            onFailure = { error ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(AuthDto.ErrorResponse(404, error.message ?: "사용자 ID 찾기 실패"))
            }
        )
    }

    @Operation(summary = "비밀번호 찾기", description = " 비밀번호를 찾기")
    @PostMapping("/find-userpw")
    fun findUserPW(@RequestBody request: AuthDto.PWSearchRequest): ResponseEntity<Any> {
        return authService.findPW(request).fold(
            onSuccess = { response ->
                ResponseEntity.ok(response)
            },
            onFailure = { error ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(AuthDto.ErrorResponse(404, error.message ?: "비밀번호 찾기 실패"))
            }
        )
    }

    @Operation(summary = "사용자 정보 수정", description = "사용자 정보 수정")
    @PostMapping("/update-userinfo")
    fun updateUser(@RequestBody request:AuthDto.ChangeUserInfoRequest):ResponseEntity<Any>{
        return authService.ChangeUserInfo(request).fold(
            onSuccess = { response ->
                ResponseEntity.ok(response)
            },
            onFailure = { error ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(AuthDto.ErrorResponse(404, error.message ?: "사용자 찾기 실패"))
            }
        )
    }

    @Operation(summary = "ID 중복검사", description = "ID 중복검사")
    @PostMapping("/validate-ID")
    fun validateID(@RequestBody request:AuthDto.validateIDRequest):ResponseEntity<Any>{
        return authService.validateID(request).fold(
            onSuccess = { response ->
                ResponseEntity.ok(response)
            },
            onFailure = { error ->
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(AuthDto.ErrorResponse(500, error.message ?: "ID 중복검사 처리 중 오류가 발생했습니다."))
            }
        )
    }

    @Operation(summary = "이메일 중복검사", description = "이메일 중복검사")
    @PostMapping("/validate-email")
    fun validateEmail(@RequestBody request:AuthDto.validateEmailRequest):ResponseEntity<Any>{
        return authService.validateEmail(request).fold(
            onSuccess = { response ->
                ResponseEntity.ok(response)
            },
            onFailure = { error ->
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(AuthDto.ErrorResponse(500, error.message ?: "이메일 중복검사 처리 중 오류가 발생했습니다."))
            }
        )
    }

    @Operation(summary = "회원탈퇴", description = "회원탈퇴")
    @PostMapping("/leave-user")
    fun leaveUser(@RequestBody request:AuthDto.LeaveUserRequest):ResponseEntity<Any>{
        return authService.LeaveUser(request).fold(
            onSuccess = { response ->
                ResponseEntity.ok(response)
            },
            onFailure = { error ->
                val errorMsg = error.message ?: "회원탈퇴 처리 중 오류가 발생했습니다."
                if (errorMsg == "회원을 찾을 수 없습니다.") {
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body(AuthDto.ErrorResponse(404, errorMsg))
                } else {
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(AuthDto.ErrorResponse(500, errorMsg))
                }
            }
        )
    }
}
