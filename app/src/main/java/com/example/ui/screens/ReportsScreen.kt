package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.data.database.entities.AbcdeTaskEntity
import com.example.data.database.entities.UnitProgressEntity
import com.example.data.database.entities.UserStatsEntity
import com.example.data.model.LearningCurriculum
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.viewmodel.TimeMasteryViewModel

@Composable
fun ReportsScreen(
    viewModel: TimeMasteryViewModel,
    modifier: Modifier = Modifier
) {
    val progressList by viewModel.unitProgressList.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val totalFocusMins by viewModel.totalFocusMinutes.collectAsState()
    val totalDeadTimeMins by viewModel.totalDeadTimeMinutes.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    val quote by viewModel.dailyQuote.collectAsState()

    val completedUnitsCount = progressList.count { it.isCompleted }
    val totalUnits = LearningCurriculum.units.size
    val averageQuizScore = if (progressList.isNotEmpty()) {
        progressList.map { it.bestScore }.average().toInt()
    } else 0

    val tasksCompleted = tasks.count { it.isCompleted }
    val tasksTotal = tasks.size
    val taskCompletionRatio = if (tasksTotal > 0) (tasksCompleted.toFloat() / tasksTotal * 100).toInt() else 0

    // Productivity Mastery Index (0 - 100)
    val masteryIndex = ((completedUnitsCount.toFloat() / totalUnits * 40) +
            (averageQuizScore * 0.3f) +
            ((tasksCompleted.coerceAtMost(10) / 10f) * 20) +
            ((totalFocusMins.coerceAtMost(180) / 180f) * 10)).toInt().coerceIn(10, 100)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Executive Mastery Health Card in Bold Typography style
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mastery_index_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE6FFFA)),
                border = BorderStroke(1.dp, Color(0xFFB2F5EA)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تقرير الأداء والكفاءة 📈",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = Color(0xFF2C7A7B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "مؤشر إتقان إدارة الوقت",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black
                            ),
                            color = Color(0xFF1A1C1E)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                masteryIndex >= 85 -> "مستوى استثنائي: تحكم فائق في الأولويات 🏆"
                                masteryIndex >= 60 -> "مستوى متقدم: وتيرة إنجاز وانضباط متميزة ⚡"
                                else -> "مستوى واعد: استمر في تطبيق أدوات برايان تريسي 🚀"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF2C7A7B)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, Color(0xFFB2F5EA), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$masteryIndex%",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = Color(0xFF2C7A7B)
                            )
                            Text(
                                text = "الكفاءة",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF2C7A7B).copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Quick Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReportStatCard(
                    title = "الوحدات المكتملة",
                    value = "$completedUnitsCount / $totalUnits",
                    subtitle = "كويزات مجتازة",
                    icon = Icons.Default.CheckCircle,
                    color = EmeraldTertiary,
                    modifier = Modifier.weight(1f)
                )

                ReportStatCard(
                    title = "دقائق التركيز",
                    value = "$totalFocusMins دقيقة",
                    subtitle = "وضع الطيران والتدفق",
                    icon = Icons.Default.HourglassTop,
                    color = AmberSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReportStatCard(
                    title = "الفتات المستثمر",
                    value = "$totalDeadTimeMins دقيقة",
                    subtitle = "جامعة على عجلات",
                    icon = Icons.Default.DirectionsCar,
                    color = Color(0xFF2563EB),
                    modifier = Modifier.weight(1f)
                )

                ReportStatCard(
                    title = "مهام ABCDE",
                    value = "$tasksCompleted / $tasksTotal",
                    subtitle = "$taskCompletionRatio% نسبة الإنجاز",
                    icon = Icons.Default.TaskAlt,
                    color = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Periodic Reminders & Notifications Settings
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notifications_report_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = AmberSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "الإشعارات والتذكيرات الدورية",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { viewModel.triggerNotification() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("trigger_notification_button")
                        ) {
                            Icon(Icons.Default.Send, null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إرسال تذكير الآن", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "يتم إرسال تذكيرات دورية صباحية ومسائية بحكم برايان تريسي وقوائم المهام لتنشيط العقل الباطن (تأثير زيجارنيك).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lightbulb, null, tint = AmberSecondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "\"$quote\"",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Detailed Units Progress Breakdown
        item {
            Text(
                text = "تفصيل نتائج الكويزات واستيعاب الوحدات:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(LearningCurriculum.units.size) { idx ->
            val unit = LearningCurriculum.units[idx]
            val progress = progressList.find { it.unitId == unit.id }
            val isCompleted = progress?.isCompleted == true
            val bestScore = progress?.bestScore ?: 0

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isCompleted) Color(0xFFD1FAE5) else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${unit.id}",
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) EmeraldTertiary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = unit.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = unit.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCompleted) Color(0xFFD1FAE5) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (isCompleted) "$bestScore%" else "غير مجتاز",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) Color(0xFF065F46) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(14.dp),
                color = color.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
        }
    }
}
