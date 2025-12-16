package good.space.runnershi.global.running.controller

import good.space.runnershi.model.dto.running.RunCreateRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/running")
class RunningController (
    private val runningService: RunningService
){
    @PostMapping("/run-records")
    fun createRunning(
        @AuthenticationPrincipal userId: Long,
        @RequestBody runCreateRequest: RunCreateRequest
    ): ResponseEntity<Long> {
        val runId = runningService.createRunning(userId, runCreateRequest)

        return ResponseEntity.ok(runId)
    }
}
