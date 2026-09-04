package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.InAppNotification
import com.example.data.model.NotificationCategory
import com.example.ui.viewmodel.AppTab

object NotificationHelper {
    private const val CHANNEL_ID = "brian_tracy_reminders"
    private const val CHANNEL_NAME = "تذكيرات برايان تريسي لإدارة الوقت"

    val quotes = listOf(
        "إذا لم تكن مبكراً، فأنت متأخر! وقت لومباردي هو 15 دقيقة قبل الموعد.",
        "سلة المهملات هي أعز أصدقائك في إدارة الوقت. ألقِ ما لا تحتاجه الآن.",
        "قاعدتك الذهبية اليوم: إياك أن تبدأ بمهمة B طالما لديك مهمة A غير مكتملة!",
        "محرك عقلك يحتاج 15 إلى 20 دقيقة ليسخن؛ لا تقطعه بالرد على رسائل تافهة.",
        "ساعة واحدة من التركيز العميق تعادل 3 ساعات من العمل المكتبي المشتت.",
        "حول سيارتك إلى جامعة متنقلة! 1000 ساعة في المواصلات تعادل فصلين دراسيين.",
        "ماذا ستفعل لو علمت أنه تبقى في عمرك ستة أشهر فقط؟ ركز على ما يهم حقاً.",
        "أسوأ ما في العالم هو إنجاز عمل رائع لشيء لا يجب القيام به على الإطلاق.",
        "إذا دخل عليك زائر دون موعد، قف فوراً وامشِ باتجاه الباب لحماية وقتك.",
        "القوة تذهب لمن يملك أفضل الملاحظات المكتوبة؛ لا ترفع سماعة الهاتف دون قلم.",
        "افعلها الآن! كررها في عقلك واكسر التسويف بشريحة سلامي صغيرة لمدة دقيقتين.",
        "لماذا أنت على قائمة الرواتب؟ حدد مجالات نتائجك الرئيسية (KRAs) بوضوح."
    )

    val curriculumReminders = listOf(
        "تحدي اليوم: أكمل كويز الوحدة 2 لترسيخ التخطيط الليلي وكسب 100 نقطة XP!",
        "هل تعرف نظام TRIF لتنظيم المكتب؟ راجع مفاهيم الوحدة 1 لرفع إنتاجيتك بنسبة 40%.",
        "تذكر قانون 80/20: 20% من أنشطتك تحقق 80% من نتائجك. هل راجعت وحدتك التعليمية اليوم؟"
    )

    val priorityPlanReminders = listOf(
        "التهم هذا الضفدع! حدد أهم مهمة A1 اليوم وابدأ بها قبل تصفح البريد أو الأخبار.",
        "تخطيط الغد: خصص 10 دقائق الليلة لكتابة قائمة الغد. توفر لك ساعتين من التخبط غداً!",
        "قاعدة وقت لومباردي: هل لديك اجتماع اليوم؟ اضبط وصولك قبل الموعد بـ 15 دقيقة."
    )

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "إشعارات تحفيزية دورية وتذكيرات بالمهام اليومية وفق منهج برايان تريسي"
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendMotivationalNotification(
        context: Context,
        customMessage: String? = null,
        title: String = "إتقان الوقت - حكمة برايان تريسي"
    ) {
        createNotificationChannel(context)
        val text = customMessage ?: quotes.random()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
        } catch (_: Exception) {
            // Notification dispatch handled safely
        }
    }

    fun generateInitialInAppNotifications(): List<InAppNotification> {
        return listOf(
            InAppNotification(
                id = "notif_1",
                title = "حكمة برايان تريسي اليومية 💡",
                body = "إذا لم تكن مبكراً، فأنت متأخر! وقت لومباردي هو 15 دقيقة قبل الموعد.",
                category = NotificationCategory.MOTIVATION,
                timestamp = "منذ 10 دقائق",
                targetTab = AppTab.CURRICULUM
            ),
            InAppNotification(
                id = "notif_2",
                title = "تذكير بخطة تنظيم الوقت 🐸",
                body = "التهم هذا الضفدع: حدد مهمة A1 في مصفوفة ABCDE ولا تبدأ بـ B حتى تفرغ منها تماماً.",
                category = NotificationCategory.PRIORITY_TASK,
                timestamp = "منذ 45 دقيقة",
                targetTab = AppTab.TOOLS
            ),
            InAppNotification(
                id = "notif_3",
                title = "تحدي المنهج التعليمي 📚",
                body = "اجتز كويز الوحدة الحالية واكسب حتى 100 XP لترتقي في لوحة صدارة المتدربين!",
                category = NotificationCategory.CURRICULUM,
                timestamp = "منذ ساعتين",
                targetTab = AppTab.CURRICULUM
            ),
            InAppNotification(
                id = "notif_4",
                title = "سلسلة الانضباط اليومي 🔥",
                body = "أنت في مسار رائع! حافظ على سلسلة نشاطك اليومي لفتح وسام سيد الانضباط.",
                category = NotificationCategory.STREAK,
                timestamp = "صباح اليوم",
                targetTab = AppTab.GAMIFICATION
            )
        )
    }
}
