package good.space.runnershi.state

object LevelPolicy {
    const val MAX_LEVEL = 30

    private val LEVEL_TABLE = longArrayOf(
        0,
        1_000,
        2_500,
        4_500,
        7_000,
        10_000,
        14_000,
        19_000,
        25_000,
        32_000,

        40_000,
        50_000,
        62_000,
        76_000,
        92_000,
        110_000,
        130_000,
        152_000,
        176_000,
        202_000,

        230_000,
        260_000,
        295_000,
        335_000,
        380_000,
        430_000,
        485_000,
        545_000,
        610_000,
        700_000
    )

    fun calculateLevel(totalExp: Long): Int {
        for (level in MAX_LEVEL downTo 1) {
            if (totalExp >= getRequiredExpForLevel(level)) {
                return level
            }
        }
        return 1
    }

    fun getProgressPercentage(totalExp: Long): Int {
        val currentLv = calculateLevel(totalExp)
        if (currentLv == MAX_LEVEL) return 100

        val currentLvExp = getRequiredExpForLevel(currentLv)
        val nextLvExp = getRequiredExpForLevel(currentLv + 1)

        val expInThisLevel = totalExp - currentLvExp
        val expNeededForNext = nextLvExp - currentLvExp

        return ((expInThisLevel.toDouble() / expNeededForNext) * 100).toInt()
    }


    fun getRequiredExpForLevel(level: Int): Long {
        if (level > MAX_LEVEL) return LEVEL_TABLE.last()
        if (level < 1) return 0
        return LEVEL_TABLE[level]
    }
}
