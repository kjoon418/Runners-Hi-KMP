package good.space.runnershi.quest.controller

import good.space.runnershi.model.dto.user.QuestResponse
import good.space.runnershi.quest.service.QuestService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/quest")
class QuestController (
    private val questService: QuestService
    ){
    @Operation(summary = "일일 퀘스트 조회", description = "오늘의 퀘스트 목록을 조회합니다.")
    @GetMapping("/quest")
    fun getQuestData(
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<List<QuestResponse>> {

        val response = questService.getDailyQuests(userId)

        return ResponseEntity.ok(response)
    }
}
