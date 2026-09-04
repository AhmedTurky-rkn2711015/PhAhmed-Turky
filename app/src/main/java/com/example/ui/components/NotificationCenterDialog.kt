package com.example.ui.components

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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.InAppNotification
import com.example.data.model.NotificationCategory
import com.example.data.model.NotificationFrequency
import com.example.data.model.NotificationScheduleSettings
import com.example.ui.viewmodel.AppTab

enum class NotificationCenterTab(val label: String) {
    INBOX("التنبيهات الواردة"),
    SETTINGS("تخصيص المواعيد")
}

@Composable
fun NotificationCenterDialog(
    notifications: List<InAppNotification>,
    settings: NotificationScheduleSettings,
    onDismissRequest: () -> Unit,
    onUpdateSettings: (NotificationScheduleSettings) -> Unit,
    onNotificationActionClick: (InAppNotification) -> Unit,
    onDeleteNotification: (String) -> Unit,
    onTriggerTestNotification: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(NotificationCenterTab.INBOX) }
    var currentSettings by remember { mutableStateOf(settings) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("notification_center_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "مركز الإشعارات والتذكيرات",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "حافز يومي وفق منهج برايان تريسي",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Segmented Tab Switcher (Inbox vs Settings)
                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        NotificationCenterTab.values().forEach { tab ->
                            val isSelected = selectedTab == tab
                            Surface(
                                shape = RoundedCornerShape(percent = 50),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedTab = tab }
                            ) {
                                Text(
                                    text = if (tab == NotificationCenterTab.INBOX) "${tab.label} (${notifications.size})" else tab.label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                                    ),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab Content
                when (selectedTab) {
                    NotificationCenterTab.INBOX -> {
                        InboxTabContent(
                            notifications = notifications,
                            onNotificationActionClick = { notif ->
                                onNotificationActionClick(notif)
                                onDismissRequest()
                            },
                            onDeleteNotification = onDeleteNotification,
                            onTriggerTest = onTriggerTestNotification
                        )
                    }
                    NotificationCenterTab.SETTINGS -> {
                        SettingsTabContent(
                            settings = currentSettings,
                            onSettingsChange = { newSettings ->
                                currentSettings = newSettings
                                onUpdateSettings(newSettings)
                            },
                            onTriggerTest = onTriggerTestNotification
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InboxTabContent(
    notifications: List<InAppNotification>,
    onNotificationActionClick: (InAppNotification) -> Unit,
    onDeleteNotification: (String) -> Unit,
    onTriggerTest: () -> Unit
) {
    if (notifications.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🔔", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "صندوق الإشعارات فارغ حالياً",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ستصلك هنا حكم تحفيزية دورية وتذكيرات بالمهام والوحدات التعليمية.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onTriggerTest,
                    shape = RoundedCornerShape(percent = 50)
                ) {
                    Text(text = "إرسال إشعار تجريبي الآن ⚡", fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(notifications, key = { it.id }) { item ->
                NotificationItemCard(
                    item = item,
                    onActionClick = { onNotificationActionClick(item) },
                    onDeleteClick = { onDeleteNotification(item.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onTriggerTest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(percent = 50)
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إرسال إشعار تحفيزي جديد فوراً", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun NotificationItemCard(
    item: InAppNotification,
    onActionClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ) {
                        Text(
                            text = "${item.category.emoji} ${item.category.title}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "حذف",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.body,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action button based on target tab
            item.targetTab?.let { target ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilledTonalButton(
                        onClick = onActionClick,
                        shape = RoundedCornerShape(percent = 50),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = when (target) {
                                AppTab.CURRICULUM -> "ابدأ بالتعلم 📖"
                                AppTab.TOOLS -> "تطبيق الأداة 🛠️"
                                AppTab.GAMIFICATION -> "عرض الصدارة 🏆"
                                AppTab.REPORTS -> "عرض التقرير 📊"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsTabContent(
    settings: NotificationScheduleSettings,
    onSettingsChange: (NotificationScheduleSettings) -> Unit,
    onTriggerTest: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Master Toggle Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (settings.isEnabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تفعيل نظام التذكيرات الدورية",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "إرسال إشعارات ذكية تحفزك على الالتزام بخطة تنظيم الوقت",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.isEnabled,
                        onCheckedChange = { onSettingsChange(settings.copy(isEnabled = it)) }
                    )
                }
            }
        }

        // Frequency Selection
        item {
            Text(
                text = "وتيرة وتكرار الإشعارات",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NotificationFrequency.values().forEach { freq ->
                    val isSelected = settings.frequency == freq
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSettingsChange(settings.copy(frequency = freq))
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSettingsChange(settings.copy(frequency = freq)) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = freq.label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = freq.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Customizable Timing Hours
        item {
            Text(
                text = "تخصيص أوقات التذكيرات اليومية",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TimeSlotPickerRow(
                        title = "تذكير الصباح (الضفدع والتخطيط)",
                        icon = "🌅",
                        timeValue = settings.morningTime,
                        onTimeSelected = { onSettingsChange(settings.copy(morningTime = it)) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                    TimeSlotPickerRow(
                        title = "تذكير الظهيرة (كتلة التركيز العميق)",
                        icon = "☀️",
                        timeValue = settings.afternoonTime,
                        onTimeSelected = { onSettingsChange(settings.copy(afternoonTime = it)) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                    TimeSlotPickerRow(
                        title = "تذكير المساء (تجهيز خطة الغد)",
                        icon = "🌙",
                        timeValue = settings.eveningTime,
                        onTimeSelected = { onSettingsChange(settings.copy(eveningTime = it)) }
                    )
                }
            }
        }

        // Notification Content Preferences
        item {
            Text(
                text = "المحتوى المفضل للتنبيهات",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    CheckboxRow(
                        title = "حكم وعبارات برايان تريسي التحفيزية 💡",
                        checked = settings.remindQuotes,
                        onCheckedChange = { onSettingsChange(settings.copy(remindQuotes = it)) }
                    )
                    CheckboxRow(
                        title = "تذكيرات إكمال الوحدات والكويزات 📚",
                        checked = settings.remindCurriculum,
                        onCheckedChange = { onSettingsChange(settings.copy(remindCurriculum = it)) }
                    )
                    CheckboxRow(
                        title = "تذكيرات مهام مصفوفة ABCDE والضفدع 🐸",
                        checked = settings.remindTasks,
                        onCheckedChange = { onSettingsChange(settings.copy(remindTasks = it)) }
                    )
                }
            }
        }

        item {
            Button(
                onClick = onTriggerTest,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(percent = 50)
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تجربة إرسال إشعار فوري الآن", fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun TimeSlotPickerRow(
    title: String,
    icon: String,
    timeValue: String,
    onTimeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("06:00", "07:00", "08:00", "09:00", "12:00", "14:00", "16:00", "18:00", "20:00", "21:30")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Box {
            Surface(
                shape = RoundedCornerShape(percent = 50),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.clickable { expanded = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeValue,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt, fontWeight = FontWeight.Bold) },
                        onClick = {
                            onTimeSelected(opt)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckboxRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
