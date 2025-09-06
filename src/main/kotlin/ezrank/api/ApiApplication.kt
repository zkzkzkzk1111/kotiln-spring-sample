package ezrank.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class ApiApplication

fun main(args: Array<String>) {
	println("🔍 [ApiApplication] 애플리케이션 시작")
	runApplication<ApiApplication>(*args)
	println("🔍 [ApiApplication] 애플리케이션 시작 완료")
}
