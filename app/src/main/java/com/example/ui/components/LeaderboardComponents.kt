package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeaderboardPeriod
import com.example.data.model.LeaderboardUser

@Composable
fun LeaderboardPodium(
    topThree: List<LeaderboardUser>,
    modifier: Modifier = Modifier
) {
    if (topThree.size < 3) return

    val first = topThree.getOrNull(0) ?: return
    val second = topThree.getOrNull(1) ?: return
    val third = topThree.getOrNull(2) ?: return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("leaderboard_podium_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "منصة أبطال التنافس الأسبوعي 🏆",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = Color(0xFFD97706)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // #2 Silver
                PodiumColumn(
                    user = second,
                    rank = 2,
                    podiumHeight = 85.dp,
                    crownColor = Color(0xFF94A3B8),
                    medalEmoji = "🥈"
                )

                // #1 Gold
                PodiumColumn(
                    user = first,
                    rank = 1,
                    podiumHeight = 115.dp,
                    crownColor = Color(0xFFFFB800),
                    medalEmoji = "👑"
                )

                // #3 Bronze
                PodiumColumn(
                    user = third,
                    rank = 3,
                    podiumHeight = 65.dp,
                    crownColor = Color(0xFFD97706),
                    medalEmoji = "🥉"
                )
            }
        }
    }
}

@Composable
private fun PodiumColumn(
    user: LeaderboardUser,
    rank: Int,
    podiumHeight: androidx.compose.ui.unit.Dp,
    crownColor: Color,
    medalEmoji: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(92.dp)
    ) {
        Text(text = medalEmoji, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(2.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (user.isCurrentUser) MaterialTheme.colorScheme.primaryContainer else crownColor.copy(alpha = 0.15f))
                .border(2.dp, crownColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = user.avatarEmoji, fontSize = 22.sp)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = user.name,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (user.isCurrentUser) FontWeight.Black else FontWeight.Bold
            ),
            color = if (user.isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        Text(
            text = "${user.xp} XP",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
            color = Color(0xFFD97706)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Podium Pillar
        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color = crownColor.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, crownColor.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(podiumHeight)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "#$rank",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black
                    ),
                    color = crownColor
                )
            }
        }
    }
}

@Composable
fun CurrentUserRankHighlight(
    user: LeaderboardUser?,
    competitorAhead: LeaderboardUser?,
    modifier: Modifier = Modifier
) {
    if (user == null) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("current_user_rank_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "#${user.rank}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "موقعك في سباق الصدارة",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "⭐", fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (competitorAhead != null) {
                            val diff = competitorAhead.xp - user.xp
                            if (diff > 0) "يتبقى لك $diff XP لتجاوز ${competitorAhead.name} والصعود للمركز #${user.rank - 1}! 🚀"
                            else "أنت في صدارة المنافسين! حافظ على إنجازك 🔥"
                        } else {
                            "تهانينا! أنت تتربع على قمة لوحة الصدارة 👑"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(percent = 50),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text(
                    text = "${user.xp} XP",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                    color = Color(0xFFD97706),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun LeaderboardUserRow(
    user: LeaderboardUser,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("leaderboard_row_${user.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (user.isCurrentUser) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (user.isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Rank Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when (user.rank) {
                        1 -> Color(0xFFFEF3C7)
                        2 -> Color(0xFFF1F5F9)
                        3 -> Color(0xFFFFEDD5)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = when (user.rank) {
                                1 -> "🥇"
                                2 -> "🥈"
                                3 -> "🥉"
                                else -> "#${user.rank}"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = user.avatarEmoji, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (user.isCurrentUser) FontWeight.Black else FontWeight.Bold
                            ),
                            color = if (user.isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        if (user.isCurrentUser) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(percent = 50),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "أنت",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🔥 ${user.streakDays}ي",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFD97706)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${user.xp} XP",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = Color(0xFFD97706)
                )
                Text(
                    text = "${user.completedUnits} وحدات",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
