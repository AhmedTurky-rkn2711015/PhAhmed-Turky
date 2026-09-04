package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.entities.UnitProgressEntity
import com.example.data.database.entities.UserStatsEntity
import com.example.data.model.LeaderboardPeriod
import com.example.data.model.LeaderboardUser
import com.example.data.model.LearningCurriculum
import com.example.ui.components.CurrentUserRankHighlight
import com.example.ui.components.LeaderboardPodium
import com.example.ui.components.LeaderboardUserRow

data class BadgeItem(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val isUnlocked: Boolean
)

enum class GamificationViewMode(val title: String, val icon: String) {
    LEADERBOARD("لوحة الصدارة", "🏆"),
    BADGES("الأوسمة والشهادة", "🎖️")
}

@Composable
fun GamificationScreen(
    userStats: UserStatsEntity?,
    progressList: List<UnitProgressEntity>,
    leaderboardUsers: List<LeaderboardUser>,
    selectedPeriod: LeaderboardPeriod,
    onPeriodSelected: (LeaderboardPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(GamificationViewMode.LEADERBOARD) }

    val xp = userStats?.totalXp ?: 100
    val streak = userStats?.currentStreak ?: 3

    // Level computation
    val levelInfo = when {
        xp >= 1200 -> Triple("المستوى 5", "خبير إدارة الحياة", 1.0f)
        xp >= 800 -> Triple("المستوى 4", "سيد الأولويات الاستراتيجية", (xp - 800) / 400f)
        xp >= 500 -> Triple("المستوى 3", "محترف التركيز والتدفق", (xp - 500) / 300f)
        xp >= 200 -> Triple("المستوى 2", "منضبط المواعيد وبيئة العمل", (xp - 200) / 300f)
        else -> Triple("المستوى 1", "مبتدئ منظم", xp / 200f)
    }

    val completedUnitsCount = progressList.count { it.isCompleted }
    val isAllCompleted = completedUnitsCount >= LearningCurriculum.units.size

    val badges = listOf(
        BadgeItem(
            id = "b1",
            title = "وسام سيد البيئة",
            description = "إكمال الوحدة 1 وكويز نظام TRIF",
            iconEmoji = "🧹",
            isUnlocked = progressList.any { it.unitId == 1 && it.isCompleted }
        ),
        BadgeItem(
            id = "b2",
            title = "المخطط الاستراتيجي",
            description = "إكمال الوحدة 2 والتخطيط الليلي",
            iconEmoji = "🧭",
            isUnlocked = progressList.any { it.unitId == 2 && it.isCompleted }
        ),
        BadgeItem(
            id = "b3",
            title = "حارس الأولويات (ABCDE)",
            description = "إكمال الوحدة 3 وقانون 80/20",
            iconEmoji = "🎯",
            isUnlocked = progressList.any { it.unitId == 3 && it.isCompleted }
        ),
        BadgeItem(
            id = "b4",
            title = "درع التدفق العميق",
            description = "إكمال الوحدة 4 وكتل التركيز",
            iconEmoji = "🛡️",
            isUnlocked = progressList.any { it.unitId == 4 && it.isCompleted }
        ),
        BadgeItem(
            id = "b5",
            title = "خبير إدارة الحياة",
            description = "إكمال الوحدة 5 ومجالات النتائج",
            iconEmoji = "🌟",
            isUnlocked = progressList.any { it.unitId == 5 && it.isCompleted }
        ),
        BadgeItem(
            id = "b6",
            title = "صائد الفتات الزمني",
            description = "استثمار المواصلات بجامعة على عجلات",
            iconEmoji = "🎧",
            isUnlocked = xp >= 150
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("gamification_screen")
    ) {
        // Mode Selector: Leaderboard vs Badges
        Surface(
            shape = RoundedCornerShape(percent = 50),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                GamificationViewMode.values().forEach { mode ->
                    val isSelected = viewMode == mode
                    Surface(
                        shape = RoundedCornerShape(percent = 50),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewMode = mode }
                            .testTag("gamification_tab_${mode.name}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = mode.icon, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = mode.title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                                ),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        when (viewMode) {
            GamificationViewMode.LEADERBOARD -> {
                LeaderboardViewContent(
                    leaderboardUsers = leaderboardUsers,
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = onPeriodSelected
                )
            }
            GamificationViewMode.BADGES -> {
                BadgesViewContent(
                    levelInfo = levelInfo,
                    xp = xp,
                    streak = streak,
                    badges = badges,
                    isAllCompleted = isAllCompleted,
                    completedUnitsCount = completedUnitsCount
                )
            }
        }
    }
}

@Composable
private fun LeaderboardViewContent(
    leaderboardUsers: List<LeaderboardUser>,
    selectedPeriod: LeaderboardPeriod,
    onPeriodSelected: (LeaderboardPeriod) -> Unit
) {
    val currentUser = leaderboardUsers.firstOrNull { it.isCurrentUser }
    val currentUserRank = currentUser?.rank ?: 0
    val competitorAhead = leaderboardUsers.firstOrNull { it.rank == currentUserRank - 1 }
    val topThree = leaderboardUsers.take(3)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Period Selector Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LeaderboardPeriod.values().forEach { period ->
                    val isSelected = selectedPeriod == period
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPeriodSelected(period) },
                        label = {
                            Text(
                                text = period.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(percent = 50)
                    )
                }
            }
        }

        // Podium of Top 3
        if (topThree.size >= 3) {
            item {
                LeaderboardPodium(topThree = topThree)
            }
        }

        // Current User Status Banner
        item {
            CurrentUserRankHighlight(
                user = currentUser,
                competitorAhead = competitorAhead
            )
        }

        // Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ترتيب المتدربين الأكثر تفاعلاً",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${leaderboardUsers.size} متنافس",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // List of all leaderboard users
        items(leaderboardUsers, key = { it.id }) { user ->
            LeaderboardUserRow(user = user)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BadgesViewContent(
    levelInfo: Triple<String, String, Float>,
    xp: Int,
    streak: Int,
    badges: List<BadgeItem>,
    isAllCompleted: Boolean,
    completedUnitsCount: Int
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Level & Status Hero Card in Bold Typography style
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("level_hero_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Surface(
                                shape = RoundedCornerShape(percent = 50),
                                color = Color(0xFFFEF3C7),
                                border = BorderStroke(1.dp, Color(0xFFFDE68A))
                            ) {
                                Text(
                                    text = levelInfo.first,
                                    color = Color(0xFF92400E),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = levelInfo.second,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Streak Flame Counter Pill
                        Surface(
                            shape = RoundedCornerShape(percent = 50),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🔥", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$streak أيام",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = Color(0xFFD97706)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // XP Progress bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "نقاط الخبرة: $xp XP",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "المستوى التالي",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { levelInfo.third.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(percent = 50)),
                        color = Color(0xFFFFB800),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        // Daily Challenge Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "⚡", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تحدي اليوم (+50 XP):",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "اكتب قائمة مهام الغد الليلة قبل النوم لتفعيل تأثير زيجارنيك وإراحة عقلك.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Official Certificate Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("certificate_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAllCompleted) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(0.dp),
                border = BorderStroke(
                    1.dp,
                    if (isAllCompleted) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outline
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isAllCompleted) "📜" else "🔒",
                        fontSize = 38.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "شهادة إتقان إدارة الوقت مع برايان تريسي",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = if (isAllCompleted) Color(0xFF92400E) else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isAllCompleted)
                            "مُنحت للمتدرب لاجتياز كافة الوحدات التعليمية والكويزات بنجاح والتمكن من أدوات الإنتاجية وضبط البيئة."
                        else
                            "أكمل جميع الوحدات التعليمية الخمس لفتح الشهادة الرسمية وتتويج رحلتك التعليمية (المكتمل: $completedUnitsCount من ${LearningCurriculum.units.size}).",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        textAlign = TextAlign.Center,
                        color = if (isAllCompleted) Color(0xFF78350F) else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isAllCompleted) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(percent = 50),
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "⭐ ختم الاعتماد الرسمي متوفر", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Section Title: Badges & Trophies
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🎖️", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "معرض الأوسمة والمكافآت الرمزية",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Badges Grid
        items(badges.chunked(2)) { rowBadges ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowBadges.forEach { badge ->
                    BadgeCard(
                        badge = badge,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowBadges.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun BadgeCard(
    badge: BadgeItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag("badge_card_${badge.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(
            1.dp,
            if (badge.isUnlocked) Color(0xFFFFB800) else MaterialTheme.colorScheme.outline
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (badge.isUnlocked) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, if (badge.isUnlocked) Color(0xFFFDE68A) else MaterialTheme.colorScheme.outline)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (badge.isUnlocked) badge.iconEmoji else "🔒",
                        fontSize = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = badge.title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Black
                ),
                color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = badge.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (badge.isUnlocked) 0.7f else 0.4f),
                textAlign = TextAlign.Center
            )
        }
    }
}
