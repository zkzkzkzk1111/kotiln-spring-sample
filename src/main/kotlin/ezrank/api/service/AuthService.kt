package ezrank.api.service

import ezrank.api.dto.AuthDto
import ezrank.api.repository.UserRepository
import ezrank.api.entity.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${jwt.secret:your-secret-key-for-testing-only}")
    private val jwtSecret: String
) {

    private val secretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())

    fun register(request: AuthDto.SignupRequest): Result<AuthDto.AuthSuccessResponse> {
        return try {

            val errors = validateUserInput(request)
            if (errors.isNotEmpty()) {
                return Result.failure(Exception(errors.joinToString(", ")))
            }

            if (userRepository.existsByUserId(request.id)) {
                return Result.failure(Exception("이미 존재하는 아이디입니다."))
            }
            if (userRepository.existsByUserEmail(request.email)) {
                return Result.failure(Exception("이미 존재하는 이메일입니다."))
            }

            val hashedPassword = passwordEncoder.encode(request.password)
            val user = User(
                userId = request.id,
                userPassword = hashedPassword,
                userEmail = request.email,
                userName = request.name,
                isAgree = request.is_agree,
                marketingAgree = request.marketing_agree
            )
            val savedUser = userRepository.save(user)

            val token = generateToken(savedUser.userId, savedUser.userIdx!!.toInt())

            val response = AuthDto.AuthSuccessResponse(
                message = "회원가입이 완료되었습니다.",
                token = token,
                user = AuthDto.UserResponse(
                    user_id = savedUser.userId,
                    user_idx = savedUser.userIdx!!.toInt(),
                    username = savedUser.userId,
                    email = savedUser.userEmail,
                    name = savedUser.userName
                )
            )

            Result.success(response)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun login(request: AuthDto.LoginRequest): Result<AuthDto.AuthSuccessResponse> {
        return try {
            if (request.username.isBlank() || request.password.isBlank()) {
                return Result.failure(Exception("사용자명과 비밀번호를 입력해주세요."))
            }

            val user = userRepository.findByUserId(request.username)
                ?: return Result.failure(Exception("아이디 또는 비밀번호가 틀렸습니다."))

            if (!passwordEncoder.matches(request.password, user.userPassword)) {
                return Result.failure(Exception("아이디 또는 비밀번호가 틀렸습니다."))
            }

            val token = generateToken(user.userId, user.userIdx!!.toInt())

            val response = AuthDto.AuthSuccessResponse(
                message = "로그인 성공",
                token = token,
                user = AuthDto.UserResponse(
                    user_id = user.userId,
                    user_idx = user.userIdx!!.toInt(),
                    username = user.userId,
                    email = user.userEmail,
                    name = user.userName
                )
            )

            Result.success(response)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun findUserId(request: AuthDto.IDSearchRequest): Result<AuthDto.SuccessResponse> {
        return try {
            if (request.user_name.isBlank()) {
                return Result.failure(Exception("사용자 이름이 없습니다."))
            }
            
            if (request.user_email.isBlank()) {
                return Result.failure(Exception("사용자 이메일이 없습니다."))
            }

            val user = userRepository.findByUserNameAndUserEmail(request.user_name, request.user_email)
                ?: return Result.failure(Exception("이름 또는 이메일이 틀렸습니다."))

            val successResponse = AuthDto.SuccessResponse(
                status = 200,
                message = "success",
                data = mapOf(
                    "user_id" to user.userId,
                    "user_idx" to user.userIdx
                )
            )
            Result.success(successResponse)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun findPW(request: AuthDto.PWSearchRequest): Result<AuthDto.SuccessResponse> {
        return try {
            if (request.user_id.isBlank()) {
                return Result.failure(Exception("사용자 아이디가 없습니다."))
            }

            if (request.user_email.isBlank()) {
                return Result.failure(Exception("사용자 이메일이 없습니다."))
            }

            val user = userRepository.findByUserIdAndUserEmail(request.user_id, request.user_email)
                ?: return Result.failure(Exception("아이디 또는 이메일이 틀렸습니다."))

            val successResponse = AuthDto.SuccessResponse(
                status = 200,
                message = "success",
                data = mapOf(
                    "user_id" to user.userId,
                    "user_idx" to user.userIdx
                )
            )
            Result.success(successResponse)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun ChangeUserInfo(request: AuthDto.ChangeUserInfoRequest): Result<AuthDto.SuccessResponse> {
        return try {

            val user = userRepository.findByUserIdAndUserIdx(request.user_id, request.user_idx.toLong())
                ?: return Result.failure(Exception("사용자를 찾을 수 없습니다."))

            request.user_name?.let { name ->
                if (name.isNotBlank()) {
                    user.userName = name
                }
            }
            
            request.user_email?.let { email ->
                if (email.isNotBlank()) {
                    user.userEmail = email
                }
            }
            
            userRepository.save(user)

            val response = AuthDto.SuccessResponse(
                status = 200,
                message = "success",
                data = mapOf("message" to "정보가 성공적으로 변경되었습니다.")
            )

            Result.success(response)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun LeaveUser(request:AuthDto.LeaveUserRequest) : Result<AuthDto.SuccessResponse>{
        return try {
            val existingUser = userRepository.findByUserIdAndUserIdx(request.user_id, request.user_idx)
                ?: return Result.failure(Exception("회원을 찾을 수 없습니다."))

            userRepository.delete(existingUser)

            val response = AuthDto.SuccessResponse(
                status = 200,
                message = "success",
                data = "탈퇴가 완료되었습니다."
            )

            Result.success(response)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun pwChange(request: AuthDto.ChangePWRequest): Result<AuthDto.SuccessResponse> {
        return try {
            if (request.user_id.isBlank()) {
                return Result.failure(Exception("사용자 ID는 필수입니다."))
            }
            
            if (request.password.isBlank()) {
                return Result.failure(Exception("새 비밀번호는 필수입니다."))
            }
            
            if (request.password.length < 6) {
                return Result.failure(Exception("비밀번호는 6자 이상이어야 합니다."))
            }

            val user = userRepository.findByUserIdAndUserIdx(request.user_id, request.user_idx.toLong())
                ?: return Result.failure(Exception("사용자를 찾을 수 없습니다."))

            val hashedPassword = passwordEncoder.encode(request.password)
            user.userPassword = hashedPassword
            userRepository.save(user)

            val response = AuthDto.SuccessResponse(
                status = 200,
                message = "success",
                data = mapOf("message" to "비밀번호가 성공적으로 변경되었습니다.")
            )

            Result.success(response)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun validateID(request: AuthDto.validateIDRequest): Result<AuthDto.SuccessResponse> {
        return try {
            val existingUser = userRepository.findByUserId(request.user_id)
            
            val message = if (existingUser != null) {
                "이미 사용중인 ID입니다."
            } else {
                "사용 가능한 ID입니다."
            }

            val response = AuthDto.SuccessResponse(
                status = 200,
                message = "success",
                data = message
            )

            Result.success(response)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun validateEmail(request: AuthDto.validateEmailRequest): Result<AuthDto.SuccessResponse> {
        return try {
            val existingUser = userRepository.findByUserEmail(request.user_email)
            
            val message = if (existingUser != null) {
                "이미 사용중인 이메일입니다."
            } else {
                "사용 가능한 이메일입니다."
            }

            val response = AuthDto.SuccessResponse(
                status = 200,
                message = "success",
                data = message
            )

            Result.success(response)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun verifyUser(userId: String): Result<AuthDto.VerifyResponse> {
        return try {
            val user = userRepository.findByUserId(userId)
                ?: return Result.failure(Exception("사용자를 찾을 수 없습니다."))

            val response = AuthDto.VerifyResponse(
                message = "토큰이 유효합니다.",
                user = AuthDto.UserResponse(
                    user_id = user.userId,
                    user_idx = user.userIdx!!.toInt(),
                    username = user.userId,
                    email = user.userEmail,
                    name = user.userName
                )
            )

            Result.success(response)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateToken(userId: String, userIdx: Int): String {
        val now = Date()
        val expiration = Date(now.time + 24 * 60 * 60 * 1000) // 24시간

        return Jwts.builder()
            .setSubject(userId)
            .claim("user_id", userId)
            .claim("user_idx", userIdx)
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
    }

    fun getCurrentUserIdx(token: String): Int? {
        return try {
            val cleanToken = if (token.startsWith("Bearer ")) token.substring(7) else token
            val claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(cleanToken)
                .body

            claims["user_idx"] as? Int
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentUserId(token: String): String? {
        return try {
            val cleanToken = if (token.startsWith("Bearer ")) token.substring(7) else token
            val claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(cleanToken)
                .body

            claims["user_id"] as? String
        } catch (e: Exception) {
            null
        }
    }

    private fun validateUserInput(userData: AuthDto.SignupRequest): List<String> {
        val errors = mutableListOf<String>()

        if (userData.id.isBlank()) {
            errors.add("아이디는 필수입니다.")
        } else if (userData.id.length < 4) {
            errors.add("아이디는 4자 이상이어야 합니다.")
        }

        if (userData.password.isBlank()) {
            errors.add("비밀번호는 필수입니다.")
        } else if (userData.password.length < 6) {
            errors.add("비밀번호는 6자 이상이어야 합니다.")
        }

        if (userData.email.isBlank()) {
            errors.add("이메일은 필수입니다.")
        } else if (!isValidEmail(userData.email)) {
            errors.add("올바른 이메일 형식을 입력해주세요.")
        }

        if (userData.name.isBlank()) {
            errors.add("이름은 필수입니다.")
        } else if (userData.name.length < 2) {
            errors.add("이름은 2자 이상이어야 합니다.")
        }

        return errors
    }

    private fun isValidEmail(email: String): Boolean {
        val pattern = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
        return pattern.matches(email)
    }
}