package good.space.runnershi.user.domain

enum class AchievementCategory {
    ATTENDANCE,        // 📅 출석 (일수)
    CUMULATIVE,        // 🏃‍♂️ 누적 거리 (미터)
    MAX_DISTANCE,      // 🗺️ 한 번에 뛴 최대 거리 (미터)
    PACE,               // ⚡ 평균 페이스 (초/km, 낮을수록 좋음)
}

enum class Achievement(
    val category: AchievementCategory,
    val title: String,
    val description: String,
    val available: (User) -> Boolean
) {
    // 1. 📅 출석 (단위: 일수)
    ATTENDANCE_LV1(
        AchievementCategory.ATTENDANCE,
        "작심삼일 브레이커",
        "3일이면 포기한다고요? 저는 아닌데요!",
        { user -> user.totalRunningDays >= 3 }
    ),
    ATTENDANCE_LV2(
        AchievementCategory.ATTENDANCE,
        "운동화와 썸타는 중",
        "이제 운동화 끈 묶는 게 제법 설레기 시작했어요.",
        { user -> user.totalRunningDays >= 10 }
    ),
    ATTENDANCE_LV3(
        AchievementCategory.ATTENDANCE,
        "습관의 형성",
        "러닝이 양치질만큼 자연스러워진 당신!",
        { user -> user.totalRunningDays >= 30 }
    ),
    ATTENDANCE_LV4(
        AchievementCategory.ATTENDANCE,
        "곰도 사람이 된 시간",
        "쑥과 마늘 대신 러닝으로 100일을 버텨낸 끈기!",
        { user -> user.totalRunningDays >= 100 }
    ),
    ATTENDANCE_LV5(
        AchievementCategory.ATTENDANCE,
        "비가 오나 눈이 오나",
        "1년 365일, 당신이 가는 길이 곧 러닝 코스입니다.",
        { user -> user.totalRunningDays >= 365 }
    ),

    // 2. 🏃‍♂️ 누적 거리 (단위: 미터) - 요청하신 5단계 적용
    CUMULATIVE_LV1(
        AchievementCategory.CUMULATIVE,
        "러닝 새내기",
        "첫 1km의 짜릿함! 위대한 여정의 시작입니다.",
        { user -> user.totalDistanceMeters >= 1_000 }
    ),
    CUMULATIVE_LV2(
        AchievementCategory.CUMULATIVE,
        "동네 마라토너",
        "10km 돌파! 이제 어디 가서 '저 좀 뜁니다' 말할 수 있어요.",
        { user -> user.totalDistanceMeters >= 10_000 }
    ),
    CUMULATIVE_LV3(
        AchievementCategory.CUMULATIVE,
        "국토횡단러",
        "서울에서 강릉까지 거리를 내 발로 뛰었습니다. (약 300km)",
        { user -> user.totalDistanceMeters >= 300_000 }
    ),
    CUMULATIVE_LV4(
        AchievementCategory.CUMULATIVE,
        "국토종단러",
        "서울에서 부산까지 달렸습니다. 끝이 안 보이네요!",
        { user -> user.totalDistanceMeters >= 500_000 }
    ),
    CUMULATIVE_LV5(
        AchievementCategory.CUMULATIVE,
        "전설의 알바트로스",
        "한 번 날면 쉬지 않고 지구 반 바퀴. 당신은 더 이상 사람이 아닙니다.",
        { user -> user.totalDistanceMeters >= 1_000_000 }
    ),

    // 3. 🗺️ 최대 거리 (단위: 미터) - 한 번 러닝 기준
    MAX_DIST_LV1(
        AchievementCategory.MAX_DISTANCE,
        "동네 보안관",
        "우리 동네 골목골목은 내가 다 꿰고 있지! (3km)",
        { user -> user.longestDistanceMeters >= 3_000 }
    ),
    MAX_DIST_LV2(
        AchievementCategory.MAX_DISTANCE,
        "옆 동네 마실러",
        "가볍게 뛰다 보니 어느새 옆 동네까지? (5km)",
        { user -> user.longestDistanceMeters >= 5_000 }
    ),
    MAX_DIST_LV3(
        AchievementCategory.MAX_DISTANCE,
        "도시 탐험가",
        "이 정도면 차보다 내 두 다리가 더 믿음직스러워요. (10km)",
        { user -> user.longestDistanceMeters >= 10_000 }
    ),
    MAX_DIST_LV4(
        AchievementCategory.MAX_DISTANCE,
        "멈추지 않는 심장",
        "하프 마라톤 완주 거리! 강철 체력 인정합니다. (21.1km)",
        { user -> user.longestDistanceMeters >= 21_000 }
    ),
    MAX_DIST_LV5(
        AchievementCategory.MAX_DISTANCE,
        "인간 기관차",
        "마라톤 풀코스 거리 정복. 달리기 위해 태어난 사람! (42.195km)",
        { user -> user.longestDistanceMeters >= 42_195 }
    ),

    // ==========================================
    // 4. ⚡ 페이스 (단위: 초/km) - 낮을수록 달성하기 어려움
    // ==========================================
    PACE_LV1(
        AchievementCategory.PACE,
        "여유로운 거북이",
        "빠르지 않아도 괜찮아, 완주가 목표니까요. (9'00\"/km)",
        { user -> user.bestPace <= 540 }
    ),
    PACE_LV2(
        AchievementCategory.PACE,
        "총총 걸음",
        "산책보다는 빠르고 달리기라기엔 우아한 속도. (7'00\"/km)",
        { user -> user.bestPace <=  420}
    ),
    PACE_LV3(
        AchievementCategory.PACE,
        "바람의 라이더",
        "귓가를 스치는 바람 소리가 기분 좋게 들려요. (6'00\"/km)",
        { user -> user.bestPace <=  360}
    ),
    PACE_LV4(
        AchievementCategory.PACE,
        "로드 러너",
        "누구보다 빠르게 도로를 질주합니다. (5'00\"/km)",
        { user -> user.bestPace <=  300}
    ),
    PACE_LV5(
        AchievementCategory.PACE,
        "우사인 볼트",
        "이 속도 실화? 땅 위를 날아다니는 수준입니다. (4'00\"/km)",
        { user -> user.bestPace <=  240}
    );
}
