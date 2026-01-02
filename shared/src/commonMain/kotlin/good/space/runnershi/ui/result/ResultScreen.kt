package good.space.runnershi.ui.result

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import good.space.runnershi.model.dto.running.DailyQuestInfo
import good.space.runnershi.model.dto.running.NewBadgeInfo
import good.space.runnershi.model.dto.running.UpdatedUserResponse
import good.space.runnershi.model.dto.user.AvatarInfo
import good.space.runnershi.model.dto.user.NewUnlockedAvatarInfo
import good.space.runnershi.model.type.BottomItem
import good.space.runnershi.model.type.HeadItem
import good.space.runnershi.model.type.ShoeItem
import good.space.runnershi.model.type.TopItem
import good.space.runnershi.ui.components.AchievementData
import good.space.runnershi.ui.components.AchievementDialog
import good.space.runnershi.ui.components.ButtonStyle
import good.space.runnershi.ui.components.MapCameraFocus
import good.space.runnershi.ui.components.ProfileCard
import good.space.runnershi.ui.components.QuestCard
import good.space.runnershi.ui.components.RunnersHiButton
import good.space.runnershi.ui.components.RunningMap
import good.space.runnershi.ui.components.RunningSummaryCard
import good.space.runnershi.ui.components.TrophyCard
import good.space.runnershi.ui.running.RunningResultToShow
import good.space.runnershi.ui.theme.RunnersHiTheme
import good.space.runnershi.util.TimeFormatter
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import runnershi.shared.generated.resources.Res
import runnershi.shared.generated.resources.quest
import runnershi.shared.generated.resources.shoes
import runnershi.shared.generated.resources.star
import kotlin.time.ExperimentalTime

@Composable
fun ResultScreen(
    userInfo: UpdatedUserResponse?,
    runResult: RunningResultToShow,
    onCloseClick: () -> Unit
) {
    var showAchievementDialog by remember(userInfo) {
        mutableStateOf(userInfo?.newBadges?.isNotEmpty() == true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RunnersHiTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            // RunningMap: 전체 경로를 표시
            RunningMap(
                focus = MapCameraFocus.FitPath(
                    path = runResult.pathSegments,
                    padding = 50
                ),
                pathSegments = runResult.pathSegments,
                modifier = Modifier.height(300.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                @OptIn(ExperimentalTime::class)
                RunningSummaryCard(
                    distanceKm = runResult.distance / 1000.0,
                    runningTime = TimeFormatter.formatSecondsToTime(runResult.runningDuration.inWholeSeconds),
                    runningPace = runResult.runningPace,
                    totalTime = TimeFormatter.formatSecondsToTime(runResult.totalDuration.inWholeSeconds),
                    totalPace = runResult.totalPace,
                    calories = runResult.calory,
                    earnedExp = userInfo?.let { calculateGainedExp(it).toInt() },
                    modifier = Modifier.fillMaxWidth()
                )

                runResult.pacePercentile?.let { percentile ->
                    if (percentile.toIntOrNull()?.let { it <= 50 } == true) {
                        TrophyCard(
                            title = "상위 ${percentile}% 페이스",
                            description = when {
                                percentile.toIntOrNull()?.let { it <= 1 } == true -> "이보다 잘 할 수 없어요! 🏆🏆🏆"
                                percentile.toIntOrNull()?.let { it <= 10 } == true -> "최상위 러너입니다! 🏆"
                                percentile.toIntOrNull()?.let { it <= 30 } == true -> "평균보다 훨씬 빨라요! ⚡"
                                percentile.toIntOrNull()?.let { it <= 50 } == true -> "평균보다 빨라요! 👍"
                                else -> "좋은 페이스예요! 💪"
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } ?: run {
                    // 퍼센타일 정보가 없는 경우
                    TrophyCard(
                        title = "페이스 분석",
                        description = "퍼센타일 정보를 불러올 수 없습니다.",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                userInfo?.let { info ->
                    ProfileCard(
                        appearance = info.avatar.toCharacterAppearance(),
                        level = info.level.toLong(),
                        currentExp = info.userExp,
                        maxExp = calculateMaxExp(info.level), // TODO: maxExp 생기면 변경
                        gainedExp = calculateGainedExp(info),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (info.newBadges.isNotEmpty()) {
                        AchievementSection(info.newBadges)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (info.completedQuests.isNotEmpty()) {
                        QuestSection(info.completedQuests)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (info.unlockedAvatars.isNotEmpty()) {
                        ItemSection(info.unlockedAvatars)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // 하단 고정 버튼: 스크롤에 관계없이 화면 하단에 항상 떠 있음
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            RunnersHiButton(
                text = "돌아가기",
                onClick = onCloseClick,
                style = ButtonStyle.FILLED,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (showAchievementDialog && userInfo?.newBadges?.isNotEmpty() == true) {
            AchievementDialog(
                achievements = userInfo.newBadges.map { it.toDialogDto() },
                onDismissRequest = {
                    showAchievementDialog = false
                }
            )
        }
    }
}

@Composable
private fun AchievementSection(
    newBadges: List<NewBadgeInfo>
) {
    SectionTitle(
        icon = Res.drawable.star,
        title = "달성한 업적"
    )

    newBadges.forEach { badge ->
        QuestCard(
            title = badge.name,
            exp = badge.exp,
            isCleared = false,
            description = null,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun QuestSection(
    completedQuests: List<DailyQuestInfo>
) {
    SectionTitle(
        icon = Res.drawable.quest,
        title = "클리어한 퀘스트"
    )

    completedQuests.forEach { quest ->
        QuestCard(
            title = quest.title,
            exp = quest.exp,
            isCleared = quest.isComplete,
            description = null,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ItemSection(
    unlockedAvatars: List<NewUnlockedAvatarInfo>
) {
    SectionTitle(
        icon = Res.drawable.shoes,
        title = "획득한 아이템"
    )

    // 10. 아이템 목록이 있어야 하나, 컴포넌트가 완성 안돼서 미구현(임시로 텍스트 처리)
    unlockedAvatars.forEach { avatar ->
        Text(
            text = "${avatar.category}: ${avatar.itemName}",
            style = RunnersHiTheme.typography.bodyMedium,
            color = RunnersHiTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

@Composable
private fun SectionTitle(
    icon: org.jetbrains.compose.resources.DrawableResource,
    title: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = RunnersHiTheme.typography.titleLarge.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            ),
            color = RunnersHiTheme.colorScheme.onBackground
        )
    }
}

// 레벨에 따른 최대 경험치 계산 (임시 구현)
private fun calculateMaxExp(level: Int): Long {
    // TODO: 실제 레벨링 시스템에 맞게 수정
    return (level * 1000L).coerceAtLeast(1000L)
}

// 이번 러닝으로 획득한 경험치 계산 TODO: 서버 값으로 수정
private fun calculateGainedExp(userInfo: UpdatedUserResponse): Long {
    // newBadges와 completedQuests의 exp 합계
    val badgesExp = userInfo.newBadges.sumOf { it.exp }
    val questsExp = userInfo.completedQuests.sumOf { it.exp }
    return badgesExp + questsExp
}

private fun NewBadgeInfo.toDialogDto(): AchievementData {
    return AchievementData(
        title = "",
        description = "", // TODO: API 응답형태 변경 후 구현
        exp = exp
    )
}

@Preview
@Composable
private fun ResultScreenPreview() {
    RunnersHiTheme {
        ResultScreen(
            userInfo = sampleUserInfo,
            runResult = sampleRunResult,
            onCloseClick = {}
        )
    }
}

// 샘플 데이터
private val sampleUserInfo = UpdatedUserResponse(
    userId = 1L,
    userExp = 15000L,
    level = 13,
    totalRunningDays = 45L,
    badges = listOf("첫 러닝", "5km 달성", "10km 달성"),
    newBadges = listOf(
//        NewBadgeInfo("속도왕", 300L)
    ),
    dailyQuests = listOf(
        DailyQuestInfo("3km 달리기", 100L, false),
        DailyQuestInfo("15분 달리기", 150L, true),
        DailyQuestInfo("10km 달리기", 300L, false)
    ),
    avatar = AvatarInfo(
        head = HeadItem.RED_SUNGLASSES,
        top = TopItem.PINK_VEST,
        bottom = BottomItem.PINK_SHORTS,
        shoes = ShoeItem.ORANGE_SHOES
    ),
    unlockedAvatars = listOf(
        NewUnlockedAvatarInfo(
            category = "HEAD",
            itemName = "RED_SUNGLASSES"
        ),
        NewUnlockedAvatarInfo(
            category = "SHOES",
            itemName = "ORANGE_SHOES"
        )
    ),
    userExpProgressPercentage = 50,
    completedQuests = listOf(
        DailyQuestInfo("3km 달리기", 100L, true),
        DailyQuestInfo("15분 달리기", 150L, true)
    )
)

private val sampleRunResult = RunningResultToShow(
    distance = 5234.5, // 5.23km
    runningDurationMillis = (25 * 60 + 30) * 1000L, // 25분 30초 = 1530000ms
    totalDurationMillis = (28 * 60 + 15) * 1000L, // 28분 15초 = 1695000ms
    runningPace = "4'52''",
    totalPace = "5'24''",
    calory = 320,
    pacePercentile = "43", // 상위 43%
    pathSegments = emptyList() // 프리뷰에서는 빈 리스트
)
