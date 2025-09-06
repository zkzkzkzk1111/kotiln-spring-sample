package ezrank.api.dto

import io.swagger.v3.oas.annotations.media.Schema

object AuthDto {

    @Schema(description = "로그인 요청 DTO")
    data class LoginRequest(
        @Schema(description = "사용자 ID", example = "user123")
        val username: String,
        @Schema(description = "비밀번호", example = "password123")
        val password: String
    )

    @Schema(description = "회원가입 요청 DTO")
    data class SignupRequest(
        @Schema(description = "사용자 ID", example = "user123")
        val id: String,
        @Schema(description = "비밀번호", example = "password123")
        val password: String,
        @Schema(description = "이메일", example = "user@example.com")
        val email: String,
        @Schema(description = "사용자명", example = "홍길동")
        val name: String,
        @Schema(description = "약관 동의 여부", example = "true")
        val is_agree: Boolean = true,
        @Schema(description = "마케팅 동의 여부", example = "false")
        val marketing_agree: Boolean = false
    )

    @Schema(description = "사용자 정보 응답 DTO")
    data class UserResponse(
        @Schema(description = "사용자 ID", example = "user123")
        val user_id:String,
        @Schema(description = "사용자 인덱스", example = "1")
        val user_idx: Int,
        @Schema(description = "사용자명", example = "user123")
        val username: String,
        @Schema(description = "이메일", example = "user@example.com")
        val email: String,
        @Schema(description = "이름", example = "홍길동")
        val name: String
    )

    @Schema(description = "로그인/회원가입 성공 응답", example = """
    {
        "message": "로그인 성공",
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMTIzIiwiaWF0IjoxNjM5NTU1NTU1fQ.abc123",
        "user": {
            "user_id": "user123",
            "user_idx": 1,
            "username": "user123",
            "email": "user@example.com",
            "name": "홍길동"
        }
    }
    """)
    data class AuthSuccessResponse(
        @Schema(description = "응답 메시지", example = "로그인 성공")
        val message: String,
        @Schema(description = "JWT 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        val token: String,
        @Schema(description = "사용자 정보")
        val user: UserResponse
    )

    data class VerifyResponse(
        val message: String,
        val user: UserResponse
    )

    @Schema(description = "로그아웃 응답", example = """
    {
        "message": "로그아웃 되었습니다."
    }
    """)
    data class LogoutResponse(
        @Schema(description = "응답 메시지", example = "로그아웃 되었습니다.")
        val message: String
    )

    @Schema(description = "에러 응답", example = """
    {
        "status": 400,
        "message": "fail",
        "data": "요청 처리에 실패하였습니다."
    }
    """)
    data class ErrorResponse(
        @Schema(description = "HTTP 상태 코드", example = "400")
        val status: Int,
        @Schema(description = "에러 메시지", example = "fail")
        val message: String,
        @Schema(description = "에러 상세 내용", example = "요청 처리에 실패하였습니다.")
        val data: String
    ) {
        constructor(status: Int, message: String) : this(status, "fail", message)
        constructor(message: String) : this(400, "fail", message)
    }


    @Schema(description = "비밀번호 변경 요청 DTO")
    data class ChangePWRequest(
        @Schema(description = "사용자 ID", example = "user123")
        val user_id: String,
        @Schema(description = "사용자 인덱스", example = "1")
        val user_idx: Int,
        @Schema(description = "새 비밀번호", example = "newPassword123")
        val password: String
    )

    @Schema(description = "사용자 ID 찾기 요청 DTO")
    data class IDSearchRequest(
        @Schema(description = "사용자명", example = "홍길동")
        val user_name : String,
        @Schema(description = "이메일", example = "user@example.com")
        val user_email : String,
    )

    @Schema(description = "비밀번호 찾기 요청 DTO")
    data class PWSearchRequest(
        @Schema(description = "사용자 ID", example = "user123")
        val user_id : String,
        @Schema(description = "이메일", example = "user@example.com")
        val user_email : String,
    )


    data class SuccessResponse(
        val status: Int,
        val message: String,
        val data: Any
    )

    data class ChangePWResponse(
        val status: Int,
        val message: String,
        val data: ChangePWData
    )

    data class ChangePWData(
        val message: String
    )

    @Schema(description = "사용자 정보 변경 요청 DTO")
    data class ChangeUserInfoRequest(
        @Schema(description = "사용자 ID", example = "user123")
        val user_id : String,
        @Schema(description = "사용자 인덱스", example = "1")
        val user_idx : Int,
        @Schema(description = "변경할 사용자명", example = "김길동")
        val user_name : String?,
        @Schema(description = "변경할 이메일", example = "newemail@example.com")
        val user_email : String?,
    )
    
    @Schema(description = "아이디 중복검사")
    data class validateIDRequest(
        @Schema(description = "사용자 ID 중복검사")
        val user_id : String,
    )

    @Schema(description = "이메일 중복검사")
    data class validateEmailRequest(
        @Schema(description = "사용자 이메일 중복검사")
        val user_email : String,
    )

    @Schema(description = "회원탈퇴")
    data class LeaveUserRequest(
        @Schema(description = "회원탈퇴")
        val user_id : String,
        val user_idx : Long,
    )
}