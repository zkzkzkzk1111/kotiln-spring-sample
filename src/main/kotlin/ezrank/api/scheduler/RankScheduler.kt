package ezrank.api.scheduler

import ezrank.api.service.RankService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class RankScheduler(
    private val rankService: RankService
) {

    @Scheduled(cron = "0 0 08 * * ?", zone = "Asia/Seoul")
    fun scheduledRankProcessing() {
        val startTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        
        println("🔍 [RankScheduler] 스케줄링 시작: ${startTime.format(formatter)}")
        
        try {
            val result = kotlinx.coroutines.runBlocking {
                rankService.processAllRankDataInBatches()
            }
            
            result.fold(
                onSuccess = { message ->
                    val endTime = LocalDateTime.now()
                    println("🔍 [RankScheduler] 스케줄링 성공 완료: ${endTime.format(formatter)}")
                    println("🔍 [RankScheduler] 처리 결과: $message")
                    println("🔍 [RankScheduler] 소요 시간: ${java.time.Duration.between(startTime, endTime).toMinutes()}분")
                },
                onFailure = { error ->
                    val endTime = LocalDateTime.now()
                    println("🔍 [RankScheduler] 스케줄링 실패: ${endTime.format(formatter)}")
                    println("🔍 [RankScheduler] 오류 내용: ${error.message}")
                    error.printStackTrace()
                }
            )
            
        } catch (e: Exception) {
            val endTime = LocalDateTime.now()
            println("🔍 [RankScheduler] 스케줄링 예외 발생: ${endTime.format(formatter)}")
            println("🔍 [RankScheduler] 예외 내용: ${e.message}")
            e.printStackTrace()
        }
    }
}