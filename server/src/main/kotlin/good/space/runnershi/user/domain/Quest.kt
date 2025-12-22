package good.space.runnershi.user.domain

import good.space.runnershi.global.running.entity.Running
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

enum class Quest(
    val title: String,
    val level: Long,
    val available: (Running) -> Boolean,
    val exp: Long,
    val questId: Long
) {
    DISTANCE_LV1(
        "2Km 달리기",
        1,
        { running -> running.distanceMeters >= 2_000 },
        100,
        101
    ),

    DURATION_LV1(
        "20분간 땀 흘리기",
        1,
        { running -> running.duration.inWholeMinutes >= 20 },
        100,
        102
    ),

    MORNING_LV1(
        "상쾌한 아침의 시작",
        1,
        { running -> val hour = running.startedAt.toLocalDateTime(TimeZone.currentSystemDefault()).hour
            hour in 6..9
        },
        100,
        103
    ),

    NONSTOP_LV1(
        "멈추지 않는 심장",
        1,
        { running -> running.longestNonStopDistance >= 1_000},
        100,
        104
    ),

    SPEED_LV1(
        "바람을 가르는 속도",
        1,
        { running ->
            val paceSeconds = running.duration.inWholeSeconds / (running.distanceMeters / 1000.0)
            running.distanceMeters < 1_000 && paceSeconds <= 420
        },
        100,
        105
    ),

    DISTANCE_LV2(
        "5km 챌린지",
        2,
        { run -> run.distanceMeters >= 5_000 },
        300,
        201
    ),

    // 2. 시간: 40분 이상 러닝 (유산소 효과 극대화)
    DURATION_LV2(
        "40분의 끈기",
        2,
        { run -> run.duration.inWholeMinutes >= 40 },
        300,
        202
    ),

    // 3. 아침: 오전 5시~9시 사이에 '3km' 이상 러닝 (LV1은 거리제한 없었음 -> 거리 조건 추가)
    MORNING_LV2(
        "아침을 깨우는 3km",
        2,
        { run ->
            val hour = run.startedAt.toLocalDateTime(TimeZone.currentSystemDefault()).hour
            run.distanceMeters >= 3_000 && hour in 5..8
        },
        350,
        203
    ),

    // 4. 휴식 최소화: 5km 이상 뛰면서
    NONSTOP_LV2(
        "멈추지 않는 5km",
        2,
        { run -> run.longestNonStopDistance >= 5_000 },
        400,
        204
    ),

    // 5. 속도: 3km 이상, 평균 페이스 6분 00초/km 이하 (조깅 수준 탈피)
    SPEED_LV2(
        "6분 페이스 돌파",
        2,
        { run ->
            val paceSeconds = run.duration.inWholeSeconds / (run.distanceMeters / 1000.0)
           run.distanceMeters >= 3_000 && paceSeconds <= 360
        },
        400,
        205
    ),


    // ==========================================
    // [LV.3] 고급 러너: 한계를 시험하는 단계
    // ==========================================

    // 1. 거리: 10km 마라톤 연습
    DISTANCE_LV3(
        "10km 완주",
        3,
        { run -> run.distanceMeters >= 10_000 },
        600,
        301
    ),

    // 2. 시간: 1시간(60분) 이상 러닝
    DURATION_LV3(
        "1시간의 몰입",
        3,
        { run -> run.duration.inWholeMinutes >= 60 },
        600,
        302
    ),

    // 3. 아침: 오전 4시~8시 사이에 '5km' 이상 러닝 (미라클 모닝)
    MORNING_LV3(
        "새벽을 여는 5km",
        3,
        { run ->
            val hour = run.startedAt.toLocalDateTime(TimeZone.currentSystemDefault()).hour
            // 거리 5km 이상 && 시간 04:00 ~ 07:59
            run.distanceMeters >= 5_000 && hour in 4..7
        },
        700,
        303
    ),

    // 4. 휴식 최소화: 10km 이상 뛰면서, 휴식 1분 미만 (거의 논스톱)
    NONSTOP_LV3(
         "강철 심장 (10km 논스톱)",
        3,
        { run -> run.longestNonStopDistance >= 10_000 },
        800,
        304
    ),

    // 5. 속도: 5km 이상, 평균 페이스 5분 00초/km 이하 (상급자 코스)
    SPEED_LV3(
        "5분 페이스의 벽",
        3,
        { run ->
            val paceSeconds = run.duration.inWholeSeconds / (run.distanceMeters / 1000.0)
            // 5분 00초 = 300초
            paceSeconds <= 300 && run.distanceMeters >= 5_000
        },
        700,
        305
    );

    companion object {
        private val questsByLevel = entries.groupBy { it.level }

        fun getRandomQuestByLevel(level: Long): Quest {
            val quests = questsByLevel[level]
                ?: throw IllegalStateException("Level $level 에 해당하는 퀘스트가 없습니다.")

            return quests.random()
        }
    }
}
