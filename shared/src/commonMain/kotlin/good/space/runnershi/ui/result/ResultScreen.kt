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
import good.space.runnershi.model.dto.user.BadgeInfo
import good.space.runnershi.model.dto.user.DailyQuestInfo
import good.space.runnershi.model.dto.user.UpdatedUserResponse
import good.space.runnershi.model.dto.user.AvatarInfo
import good.space.runnershi.model.dto.user.UnlockedItem
import good.space.runnershi.model.type.item.BottomItem
import good.space.runnershi.model.type.item.HeadItem
import good.space.runnershi.model.type.item.ShoeItem
import good.space.runnershi.model.type.item.TopItem
import good.space.runnershi.ui.components.AchievementData
import good.space.runnershi.ui.components.AchievementDialog
import good.space.runnershi.ui.components.ButtonStyle
import good.space.runnershi.ui.components.ItemCard
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
import org.jetbrains.compose.resources.DrawableResource
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
                    earnedExp = userInfo?.runningExp,
                    modifier = Modifier.fillMaxWidth()
                )

                runResult.pacePercentile?.let { percentile ->
                    if (percentile.toIntOrNull()?.let { it <= 50 } == true) {
                        TrophyCard(
                            title = "상위 ${percentile}% 페이스",
                            description = when {
                                percentile.toIntOrNull()
                                    ?.let { it <= 1 } == true -> "이보다 잘 할 수 없어요! 🏆🏆🏆"

                                percentile.toIntOrNull()?.let { it <= 10 } == true -> "최상위 러너입니다! 🏆"
                                percentile.toIntOrNull()
                                    ?.let { it <= 30 } == true -> "평균보다 훨씬 빨라요! ⚡"

                                percentile.toIntOrNull()?.let { it <= 50 } == true -> "평균보다 빨라요! 👍"
                                else -> "좋은 페이스예요! 💪"
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                userInfo?.let { info ->
                    ProfileCard(
                        appearance = info.avatar.toCharacterAppearance(),
                        level = info.level.toLong(),
                        currentExp = info.userExp,
                        maxExp = info.requiredExpForLevel,
                        gainedExp = info.totalExp,
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
                .padding(bottom = 50.dp)
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
    newBadges: List<BadgeInfo>
) {
    SectionTitle(
        icon = Res.drawable.star,
        title = "달성한 업적"
    )

    newBadges.forEach { badge ->
        QuestCard(
            title = badge.title,
            exp = badge.exp,
            isCleared = false,
            description = badge.description,
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
            isCleared = false,
            description = null,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ItemSection(
    unlockedAvatars: List<UnlockedItem> // UnlockedItem은 AvatarItem을 포함하는 래퍼 클래스라고 가정
) {
    SectionTitle(
        icon = Res.drawable.shoes,
        title = "획득한 아이템"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp), // 제목과의 간격
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val rows = unlockedAvatars.chunked(4)

        // 4열 그리드
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { avatar ->
                    ItemCard(
                        item = avatar.item,
                        modifier = Modifier.weight(1f)
                    )
                }

                val emptySlots = 4 - rowItems.size
                repeat(emptySlots) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(
    icon: DrawableResource,
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

private fun BadgeInfo.toDialogDto(): AchievementData {
    return AchievementData(
        title = title,
        description = description,
        exp = exp
    )
}

private val UpdatedUserResponse.totalExp: Long
    get() {
        return runningExp + newBadges.sumOf { it.exp } + completedQuests.sumOf { it.exp }
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
    newBadges = listOf(
        BadgeInfo("멋지다!", "업적설명", 5000)
    ),
    avatar = AvatarInfo(
        head = HeadItem.PinkSunglasses,
        top = TopItem.PinkVest,
        bottom = BottomItem.PinkShorts,
        shoes = ShoeItem.OrangeShoes
    ),
    unlockedAvatars = listOf(
        UnlockedItem(HeadItem.RedSunglasses),
        UnlockedItem(ShoeItem.BlueShoes)
    ),
    userExpProgressPercentage = 50,
    completedQuests = listOf(
        DailyQuestInfo("3km 달리기", 100L, true),
        DailyQuestInfo("15분 달리기", 150L, true)
    ),
    requiredExpForLevel = 30_000,
    runningExp = 250
)

private val sampleRunResult = RunningResultToShow(
    distance = 5234.5,
    runningDurationMillis = (25 * 60 + 30) * 1000L, // 25분 30초
    totalDurationMillis = (28 * 60 + 15) * 1000L, // 28분 15초
    runningPace = "4'52''",
    totalPace = "5'24''",
    calory = 320,
    pacePercentile = "43", // 상위 43%
    pathSegments = emptyList() // 프리뷰에서는 빈 리스트
)
