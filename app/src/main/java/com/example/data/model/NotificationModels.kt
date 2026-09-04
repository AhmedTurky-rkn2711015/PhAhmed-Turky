package com.example.data.model

import com.example.ui.viewmodel.AppTab

enum class NotificationCategory(val title: String, val emoji: String) {
    MOTIVATION("حكمة تحفيزية", "💡"),
    CURRICULUM("وحدات تعليمية", "📚"),
    PRIORITY_TASK("مهام وأولويات", "🐸"),
    STREAK("سلسلة الانضباط", "🔥")
}

enum class NotificationFrequency(val label: String, val description: String) {
    EVERY_2_HOURS("كل ساعتين", "تذكيرات متقاربة للحفاظ على أقصى درجات التركيز واليقظة"),
    EVERY_4_HOURS("كل 4 ساعات", "تذكير مع كل دورة عمل رئيسية في اليوم"),
    TWICE_DAILY("مرتين يومياً", "تذكير صباحي لانطلاقة اليوم وتذكير مسائي للمراجعة"),
    THREE_TIMES_DAILY("3 مرات يومياً", "صباحاً (الضفدع)، ظهراً (التدفق)، مساءً (التخطيط)")
}

data class NotificationScheduleSettings(
    val isEnabled: Boolean = true,
    val frequency: NotificationFrequency = NotificationFrequency.THREE_TIMES_DAILY,
    val morningTime: String = "08:00",
    val afternoonTime: String = "14:00",
    val eveningTime: String = "20:00",
    val remindQuotes: Boolean = true,
    val remindCurriculum: Boolean = true,
    val remindTasks: Boolean = true
)

data class InAppNotification(
    val id: String,
    val title: String,
    val body: String,
    val category: NotificationCategory,
    val timestamp: String,
    val targetTab: AppTab? = null,
    val isRead: Boolean = false
)
