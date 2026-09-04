package com.example.data.model

enum class LeaderboardPeriod(val title: String) {
    WEEKLY("هذا الأسبوع"),
    ALL_TIME("كافة الأوقات")
}

data class LeaderboardUser(
    val id: String,
    val name: String,
    val title: String,
    val avatarEmoji: String,
    val xp: Int,
    val streakDays: Int,
    val completedUnits: Int,
    val isCurrentUser: Boolean = false,
    val rank: Int = 0
)

object LeaderboardData {

    private val baseWeeklyCompetitors = listOf(
        LeaderboardUser(
            id = "user_1",
            name = "م. عمر فاروق",
            title = "سيد الأولويات الاستراتيجية",
            avatarEmoji = "🦁",
            xp = 720,
            streakDays = 14,
            completedUnits = 5
        ),
        LeaderboardUser(
            id = "user_2",
            name = "سارة القحطاني",
            title = "محترفة التركيز والتدفق",
            avatarEmoji = "⚡",
            xp = 580,
            streakDays = 9,
            completedUnits = 4
        ),
        LeaderboardUser(
            id = "user_3",
            name = "د. طارق المنصور",
            title = "منضبط بيئة TRIF",
            avatarEmoji = "🎯",
            xp = 420,
            streakDays = 7,
            completedUnits = 3
        ),
        LeaderboardUser(
            id = "user_4",
            name = "هند السالم",
            title = "طالبة جامعة على عجلات",
            avatarEmoji = "🎧",
            xp = 260,
            streakDays = 4,
            completedUnits = 2
        ),
        LeaderboardUser(
            id = "user_5",
            name = "فيصل الحربي",
            title = "مبتدئ منظم",
            avatarEmoji = "🚀",
            xp = 140,
            streakDays = 2,
            completedUnits = 1
        )
    )

    private val baseAllTimeCompetitors = listOf(
        LeaderboardUser(
            id = "user_all_1",
            name = "خالد البلوشي",
            title = "أسطورة إتقان الوقت",
            avatarEmoji = "👑",
            xp = 1450,
            streakDays = 42,
            completedUnits = 5
        ),
        LeaderboardUser(
            id = "user_all_2",
            name = "م. عمر فاروق",
            title = "سيد الأولويات الاستراتيجية",
            avatarEmoji = "🦁",
            xp = 1120,
            streakDays = 28,
            completedUnits = 5
        ),
        LeaderboardUser(
            id = "user_all_3",
            name = "نورة العتيبي",
            title = "حارسة مصفوفة ABCDE",
            avatarEmoji = "💎",
            xp = 980,
            streakDays = 21,
            completedUnits = 5
        ),
        LeaderboardUser(
            id = "user_all_4",
            name = "سارة القحطاني",
            title = "محترفة التركيز والتدفق",
            avatarEmoji = "⚡",
            xp = 840,
            streakDays = 19,
            completedUnits = 4
        ),
        LeaderboardUser(
            id = "user_all_5",
            name = "د. طارق المنصور",
            title = "منضبط بيئة TRIF",
            avatarEmoji = "🎯",
            xp = 610,
            streakDays = 12,
            completedUnits = 3
        )
    )

    fun getLeaderboard(
        period: LeaderboardPeriod,
        currentUserXp: Int,
        currentUserStreak: Int,
        currentUserCompletedUnits: Int
    ): List<LeaderboardUser> {
        val baseList = if (period == LeaderboardPeriod.WEEKLY) baseWeeklyCompetitors else baseAllTimeCompetitors

        val currentUser = LeaderboardUser(
            id = "current_user",
            name = "أنت (المتدرب)",
            title = when {
                currentUserXp >= 1200 -> "خبير إدارة الحياة 🏆"
                currentUserXp >= 800 -> "سيد الأولويات 🎯"
                currentUserXp >= 500 -> "محترف التدفق ⚡"
                currentUserXp >= 200 -> "منضبط المواعيد 🧭"
                else -> "مبتدئ طموح 🚀"
            },
            avatarEmoji = "⭐",
            xp = currentUserXp,
            streakDays = currentUserStreak,
            completedUnits = currentUserCompletedUnits,
            isCurrentUser = true
        )

        val combined = (baseList + currentUser)
            .sortedByDescending { it.xp }
            .mapIndexed { index, user ->
                user.copy(rank = index + 1)
            }

        return combined
    }
}
