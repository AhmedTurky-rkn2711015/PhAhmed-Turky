package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.entities.AbcdeTaskEntity
import com.example.data.database.entities.SalamiProjectEntity
import com.example.data.database.entities.SalamiSliceEntity
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.viewmodel.TimeMasteryViewModel
import com.example.ui.viewmodel.TimerState
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

enum class ToolTab(val title: String, val icon: @Composable () -> Unit) {
    ABCDE("مصفوفة ABCDE", { Icon(Icons.Default.FormatListNumbered, null, modifier = Modifier.size(18.dp)) }),
    FLOW_TIMER("مؤقت التدفق", { Icon(Icons.Default.HourglassBottom, null, modifier = Modifier.size(18.dp)) }),
    TRIF("نظام TRIF للمكتب", { Icon(Icons.Default.CleaningServices, null, modifier = Modifier.size(18.dp)) }),
    SALAMI("تقطيع السلامي", { Icon(Icons.Default.ContentCut, null, modifier = Modifier.size(18.dp)) }),
    CAR_UNIVERSITY("جامعة على عجلات", { Icon(Icons.Default.DirectionsCar, null, modifier = Modifier.size(18.dp)) }),
    LOMBARDI("وقت لومباردي", { Icon(Icons.Default.Timer, null, modifier = Modifier.size(18.dp)) }),
    LIFE_ALIGNMENT("بوصلة الـ 6 أشهر", { Icon(Icons.Default.Favorite, null, modifier = Modifier.size(18.dp)) })
}

@Composable
fun ToolsScreen(
    viewModel: TimeMasteryViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTool by remember { mutableStateOf(ToolTab.ABCDE) }
    val tasks by viewModel.tasks.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    val salamiProjects by viewModel.salamiProjects.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("tools_screen")
    ) {
        // Horizontal Tool Tab Selector
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ToolTab.values()) { tool ->
                val isSelected = selectedTool == tool
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTool = tool },
                    label = {
                        Text(
                            text = tool.title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                            )
                        )
                    },
                    leadingIcon = { tool.icon() },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(percent = 50),
                    modifier = Modifier.testTag("tool_chip_${tool.name}")
                )
            }
        }

        // Active Tool Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            when (selectedTool) {
                ToolTab.ABCDE -> AbcdeMatrixTool(tasks = tasks, viewModel = viewModel)
                ToolTab.FLOW_TIMER -> FlowTimerTool(timerState = timerState, viewModel = viewModel)
                ToolTab.TRIF -> TrifDeskTool()
                ToolTab.SALAMI -> SalamiTool(projects = salamiProjects, viewModel = viewModel)
                ToolTab.CAR_UNIVERSITY -> CarUniversityTool(viewModel = viewModel)
                ToolTab.LOMBARDI -> LombardiTimeTool()
                ToolTab.LIFE_ALIGNMENT -> LifeAlignmentTool()
            }
        }
    }
}

// -------------------------------------------------------------
// 1. ABCDE Prioritization Tool
// -------------------------------------------------------------
@Composable
fun AbcdeMatrixTool(
    tasks: List<AbcdeTaskEntity>,
    viewModel: TimeMasteryViewModel
) {
    var selectedCategory by remember { mutableStateOf("الكل") }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    val categories = listOf("الكل", "A", "B", "C", "D", "E")
    val filteredTasks = if (selectedCategory == "الكل") tasks else tasks.filter { it.category == selectedCategory }

    val hasPendingA = tasks.any { it.category == "A" && !it.isCompleted }

    Column(modifier = Modifier.fillMaxSize()) {
        // Golden Rule Callout
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("abcde_golden_rule_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
            border = BorderStroke(1.dp, Color(0xFFFDE68A))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "⚠️", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "القاعدة الذهبية لنظام ABCDE:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF92400E)
                    )
                    Text(
                        text = "إياك أن تبدأ بأي مهمة فئة B طالما توجد مهمة فئة A غير مكتملة!",
                        fontSize = 12.sp,
                        color = Color(0xFF78350F),
                        fontWeight = if (hasPendingA) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Category Filter & Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    val badgeColor = when (cat) {
                        "A" -> Color(0xFFDC2626)
                        "B" -> AmberSecondary
                        "C" -> EmeraldTertiary
                        "D" -> Color(0xFF2563EB)
                        "E" -> Color(0xFF6B7280)
                        else -> MaterialTheme.colorScheme.primary
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = badgeColor,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            IconButton(
                onClick = { showAddTaskDialog = true },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .testTag("add_task_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "إضافة مهمة",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tasks List
        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Checklist,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "لا توجد مهام في هذه الفئة حالياً",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showAddTaskDialog = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("إضافة مهمة جديدة")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskItemCard(
                        task = task,
                        onToggle = { viewModel.toggleTask(task) },
                        onDelete = { viewModel.deleteTask(task.id) }
                    )
                }
            }
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAdd = { title, cat, rank, note ->
                viewModel.addTask(title, cat, rank, note)
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun TaskItemCard(
    task: AbcdeTaskEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = when (task.category) {
        "A" -> Color(0xFFDC2626) // Red (Must do)
        "B" -> AmberSecondary   // Amber (Should do)
        "C" -> EmeraldTertiary  // Green (Nice to do)
        "D" -> Color(0xFF2563EB) // Blue (Delegate)
        else -> Color(0xFF6B7280) // Gray (Eliminate)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) Color(0xFFF1F5F9) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, if (task.isCompleted) Color(0xFFCBD5E1) else Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Badge (e.g. A1, B2)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = categoryColor.copy(alpha = if (task.isCompleted) 0.4f else 1f)
            ) {
                Text(
                    text = "${task.category}${task.priorityRank}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                if (task.note.isNotBlank()) {
                    Text(
                        text = task.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }

            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                modifier = Modifier.testTag("task_checkbox_${task.id}")
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp).testTag("delete_task_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "حذف المهمة",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Int, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("A") }
    var rank by remember { mutableStateOf("1") }
    var note by remember { mutableStateOf("") }

    val categoryDescriptions = mapOf(
        "A" to "يجب فعلها - عواقب وخيمة لو أهملتها (الـ 20% الحيوية)",
        "B" to "ينبغي فعلها - عواقب طفيفة (لا تلمسها قبل مهام A)",
        "C" to "لطيف لو فُعلت - بدون أي عواقب لو تركتها (المكتب/الترتيب)",
        "D" to "فوضها لغيرك - استعن بأدوات أو زملاء لتوفير وقتك",
        "E" to "احذفها تماماً - عادات قديمة لم يعد لها أي قيمة"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "إضافة مهمة إلى مصفوفة ABCDE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان المهمة") },
                    modifier = Modifier.fillMaxWidth().testTag("add_task_title_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                // Category Selector
                Text(
                    text = "فئة المهمة:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("A", "B", "C", "D", "E").forEach { cat ->
                        FilterChip(
                            selected = selectedCat == cat,
                            onClick = { selectedCat = cat },
                            label = { Text(cat, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Text(
                    text = categoryDescriptions[selectedCat] ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = AmberSecondary,
                    fontSize = 11.sp
                )

                OutlinedTextField(
                    value = rank,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) rank = it.take(2) },
                    label = { Text("الرقم الترتيبي (مثل: 1 لـ A1)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_task_rank_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("ملاحظة أو تذكير بالعواقب (اختياري)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_task_note_input"),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = rank.toIntOrNull() ?: 1
                    onAdd(title, selectedCat, r, note)
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("إضافة المهمة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

// -------------------------------------------------------------
// 2. Deep Work Flow Timer (Aircraft Engine Analogy)
// -------------------------------------------------------------
@Composable
fun FlowTimerTool(
    timerState: TimerState,
    viewModel: TimeMasteryViewModel
) {
    val totalSeconds = timerState.targetMinutes * 60
    val progress = if (totalSeconds > 0) timerState.secondsRemaining.toFloat() / totalSeconds else 0f
    val minutes = timerState.secondsRemaining / 60
    val seconds = timerState.secondsRemaining % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "✈️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تشبيه محرك الطائرة لحالة التدفق (Flow State)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "عقلك يحتاج 15 إلى 20 دقيقة ليسخن ويصل لسرعة الطيران القصوى. كل إشعار أو رد على رسالة يطفئ المحرك ويعيدك للصفر! ساعة تركيز عميق على الطائرة تعادل 3 ساعات في المكتب المشتت.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Preset Duration Selectors
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    25 to "25 د (شريحة سريعة)",
                    60 to "60 د (كتلة تركيز)",
                    90 to "90 د (تدفق كامل)"
                ).forEach { (mins, label) ->
                    val isSelected = timerState.targetMinutes == mins
                    OutlinedButton(
                        onClick = { viewModel.setTimerDuration(mins) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) AmberSecondary else Color.Transparent,
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, if (isSelected) AmberSecondary else MaterialTheme.colorScheme.outline)
                    ) {
                        Text(text = label, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        // Circular Timer Display
        item {
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(6.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(220.dp),
                    color = AmberSecondary,
                    strokeWidth = 10.dp,
                    trackColor = Color.Transparent
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeFormatted,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (timerState.isRunning) "🚀 وضع الطيران نشط" else "جاهز للانطلاق",
                        fontSize = 12.sp,
                        color = if (timerState.isRunning) EmeraldTertiary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Controls
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!timerState.isRunning) {
                    Button(
                        onClick = { viewModel.startTimer() },
                        modifier = Modifier
                            .height(52.dp)
                            .width(160.dp)
                            .testTag("start_timer_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldTertiary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("بدء التدفق", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                } else {
                    Button(
                        onClick = { viewModel.pauseTimer() },
                        modifier = Modifier
                            .height(52.dp)
                            .width(160.dp)
                            .testTag("pause_timer_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Pause, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إيقاف مؤقت", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                OutlinedButton(
                    onClick = { viewModel.resetTimer() },
                    modifier = Modifier.height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إعادة ضبط")
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. TRIF Desk Organizer & The Golden Question
// -------------------------------------------------------------
@Composable
fun TrifDeskTool() {
    var goldenQuestionInput by remember { mutableStateOf("") }
    var goldenQuestionResult by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "نظام TRIF لتطهير المكتب من العبء المعرفي",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "كل ورقة على مكتبك تمثل تبويباً مفتوحاً في عقلك يسحب من ذاكرتك العاملة.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        // T - Toss
        item {
            TrifStepCard(
                letter = "T",
                title = "Toss (تخلص منها في السلة)",
                subtitle = "سلة المهملات هي أعز أصدقاء إدارة الوقت",
                description = "80% من الأوراق التي نحتفظ بها بدافع الخوف لا نرجع إليها أبداً. تخلص من الفوضى لتسترد تركيزك.",
                color = Color(0xFFDC2626)
            )
        }

        // R - Refer
        item {
            TrifStepCard(
                letter = "R",
                title = "Refer (فوضها أو أحِلها فوراً)",
                subtitle = "لا تحتفظ بعمل غيرك على مكتبك",
                description = "إذا لم تكن المهمة من صميم اختصاصك المباشر، أرسلها فوراً لمن يجيدها ولا تدعها تستهلك طاقتك الذهنية.",
                color = Color(0xFF2563EB)
            )
        }

        // A - Action
        item {
            TrifStepCard(
                letter = "A",
                title = "Action (اتخذ إجراء وضعه في ملف)",
                subtitle = "الملف الأحمر للمهام النشطة بعيداً عن النظر",
                description = "ضع الأوراق التي تخص عملك الفعلي داخل ملف مخصص وضعه بعيداً عن مساحة رؤيتك المباشرة حتى يحين وقت العمل عليها.",
                color = AmberSecondary
            )
        }

        // F - File
        item {
            TrifStepCard(
                letter = "F",
                title = "File (أرشفها بالأرشيف)",
                subtitle = "احفظ فقط ما لا يمكن استرجاعه",
                description = "قبل فتح أي درج للحفظ، اسأل نفسك السؤال الذهبي: ما أسوأ شيء ممكن أن يحدث لو ضاعت هذه الورقة؟",
                color = EmeraldTertiary
            )
        }

        // Golden Question Simulator
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚖️ حاسبة السؤال الذهبي لبرايان تريسي:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "اكتب اسم الورقة أو الشيء الذي تتردد في رميه:",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = goldenQuestionInput,
                        onValueChange = {
                            goldenQuestionInput = it
                            goldenQuestionResult = null
                        },
                        placeholder = { Text("مثال: كتالوج قديم، فاتورة قديمة، ورقة ملاحظات...") },
                        modifier = Modifier.fillMaxWidth().testTag("golden_question_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            goldenQuestionResult = if (goldenQuestionInput.contains("فاتورة") || goldenQuestionInput.contains("عقد") || goldenQuestionInput.contains("رسمي")) {
                                "📄 نصيحة برايان تريسي: قم بأرشفة هذه الورقة (حرف F) أو تصويرها رقمياً على السحابة، ثم تخلص من النسخة الورقية لتفريغ مكتبك."
                            } else {
                                "🗑️ نصيحة برايان تريسي: عواقب فقدان هذا الشيء تكاد تكون صفراً أو يمكنك الحصول على بديل عبر الإنترنت. طبّق حرف T وألقهِ فوراً في سلة المهملات!"
                            }
                        },
                        enabled = goldenQuestionInput.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("check_golden_question_button")
                    ) {
                        Text("ما الإجراء المناسب؟")
                    }

                    if (goldenQuestionResult != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, AmberSecondary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = goldenQuestionResult ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrifStepCard(
    letter: String,
    title: String,
    subtitle: String,
    description: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 4. Salami Slicing Micro-Task Tool
// -------------------------------------------------------------
@Composable
fun SalamiTool(
    projects: List<SalamiProjectEntity>,
    viewModel: TimeMasteryViewModel
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5)),
            border = BorderStroke(1.dp, Color(0xFFA7F3D0))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🍕", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "تقنية تقطيع السلامي لقهر التسويف:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF065F46)
                    )
                    Text(
                        text = "كيف تأكل جبلاً؟ شريحة تلو شريحة! فكك أي مهمة مرعبة إلى خطوة تافهة تستغرق دقيقتين فقط ليكسر عقلك حاجز الجمود.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF064E3B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "مشاريع السلامي المفككة:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("add_salami_project_button")
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تفكيك مهمة جديدة")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (projects.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("لا توجد مهام مفككة حالياً. أضف مهمة تخشى البدء فيها!")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(projects, key = { it.id }) { project ->
                    SalamiProjectCard(project = project, viewModel = viewModel)
                }
            }
        }
    }

    if (showAddDialog) {
        AddSalamiDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, desc, slices ->
                viewModel.addSalamiProject(title, desc, slices)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SalamiProjectCard(
    project: SalamiProjectEntity,
    viewModel: TimeMasteryViewModel
) {
    val slicesFlow = remember(project.id) { viewModel.getSlicesForProject(project.id) }
    val slices by slicesFlow.collectAsState(initial = emptyList())
    val progress = if (project.totalSlices > 0) project.completedSlices.toFloat() / project.totalSlices else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (project.description.isNotBlank()) {
                        Text(
                            text = project.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.deleteSalamiProject(project.id) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, "حذف", tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = EmeraldTertiary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            slices.forEach { slice ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleSalamiSlice(slice, project) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = slice.isDone,
                        onCheckedChange = { viewModel.toggleSalamiSlice(slice, project) }
                    )
                    Text(
                        text = slice.title,
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = if (slice.isDone) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (slice.isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun AddSalamiDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var slice1 by remember { mutableStateOf("") }
    var slice2 by remember { mutableStateOf("") }
    var slice3 by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تفكيك مهمة كبيرة (شرائح سلامي)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("المهمة الكبيرة المخيفة") },
                    placeholder = { Text("مثال: كتابة خطة استراتيجية") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "قطعها لشرائح تافهة جداً لا يمكن رفضها:",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldTertiary
                )
                OutlinedTextField(
                    value = slice1,
                    onValueChange = { slice1 = it },
                    label = { Text("شريحة 1 (دقيقتين فقط)") },
                    placeholder = { Text("فتح الملف وكتابة العنوان") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = slice2,
                    onValueChange = { slice2 = it },
                    label = { Text("شريحة 2") },
                    placeholder = { Text("كتابة 3 نقاط رئيسية") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = slice3,
                    onValueChange = { slice3 = it },
                    label = { Text("شريحة 3") },
                    placeholder = { Text("صياغة الفقرة الأولى") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val slices = listOf(slice1, slice2, slice3).filter { it.isNotBlank() }
                    onAdd(title, "تفكيك السلامي لقهر التسويف", slices)
                },
                enabled = title.isNotBlank() && slice1.isNotBlank()
            ) {
                Text("تفكيك وبدء")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

// -------------------------------------------------------------
// 5. Car University & Dead Time Calculator
// -------------------------------------------------------------
@Composable
fun CarUniversityTool(viewModel: TimeMasteryViewModel) {
    var commuteDailyMinutes by remember { mutableStateOf(60f) }
    var coffeeDailyMinutes by remember { mutableStateOf(30f) }

    val yearlyCommuteHours = (commuteDailyMinutes * 250 / 60).toInt()
    val yearlyCoffeeHours = (coffeeDailyMinutes * 250 / 60).toInt()
    val totalYearlyDeadHours = yearlyCommuteHours + yearlyCoffeeHours

    val equivalentWorkWeeks = totalYearlyDeadHours / 40
    val collegeSemesters = (yearlyCommuteHours / 500f) * 2 // 500 hrs = 2 semesters
    val potentialAudiobooks = totalYearlyDeadHours / 6 // avg 6 hours per audiobook

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎓", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "جامعة على عجلات (University on Wheels)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "الشخص العادي يقضي 500 إلى 1000 ساعة سنوياً في المواصلات. هذا يعادل فصليين دراسيين جامعيين! استثمار هذا الوقت في الكتب الصوتية يجعلك من أندر الخبراء في مجالك.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Sliders
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "وقت المواصلات والسواقة اليومي: ${commuteDailyMinutes.toInt()} دقيقة",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = commuteDailyMinutes,
                        onValueChange = { commuteDailyMinutes = it },
                        valueRange = 15f..180f,
                        steps = 10
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "استراحات القهوة والانتظار يومياً: ${coffeeDailyMinutes.toInt()} دقيقة",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = coffeeDailyMinutes,
                        onValueChange = { coffeeDailyMinutes = it },
                        valueRange = 10f..90f,
                        steps = 7
                    )
                }
            }
        }

        // Live Calculated Impact Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ImpactMetricCard(
                    title = "ساعات سنوياً",
                    value = "$totalYearlyDeadHours ساعة",
                    subtitle = "كنز وقت مهدور",
                    color = Color(0xFFDC2626),
                    modifier = Modifier.weight(1f)
                )

                ImpactMetricCard(
                    title = "فصول جامعية",
                    value = "%.1f ترم".format(collegeSemesters),
                    subtitle = "تعليم أكاديمي كامل",
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
                ImpactMetricCard(
                    title = "كتب صوتية",
                    value = "$potentialAudiobooks كتاب",
                    subtitle = "ثروة معرفية سنوية",
                    color = EmeraldTertiary,
                    modifier = Modifier.weight(1f)
                )

                ImpactMetricCard(
                    title = "أسابيع عمل",
                    value = "$equivalentWorkWeeks أسابيع",
                    subtitle = "دوام كامل مجاني",
                    color = Color(0xFF2563EB),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Fast Action to Log dead time
        item {
            Button(
                onClick = {
                    viewModel.logDeadTime("مواصلات", 30, "استماع لكتاب صوتي في الطريق")
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AmberSecondary)
            ) {
                Icon(Icons.Default.Headphones, null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("تسجيل جلسة كتاب صوتي بالطريق (+30 دقيقة)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ImpactMetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        }
    }
}

// -------------------------------------------------------------
// 6. Lombardi Punctuality Calculator
// -------------------------------------------------------------
@Composable
fun LombardiTimeTool() {
    var meetingHour by remember { mutableStateOf("10") }
    var meetingMinute by remember { mutableStateOf("00") }

    val h = meetingHour.toIntOrNull() ?: 10
    val m = meetingMinute.toIntOrNull() ?: 0

    // Lombardi time: -15 mins
    var lombardiH = h
    var lombardiM = m - 15
    if (lombardiM < 0) {
        lombardiM += 60
        lombardiH = if (lombardiH == 0) 23 else lombardiH - 1
    }

    val formattedLombardi = "%02d:%02d".format(lombardiH, lombardiM)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                border = BorderStroke(1.dp, Color(0xFFFDE68A))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⏱️ قاعدة لومباردي: 'إذا لم تكن مبكراً، فأنت متأخر'",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "أقل من 5% من الناس يلتزمون بالمواعيد بدقة. التأخير إهانة للشخص الآخر ويعكس عدم كفاءة. وصولك مبكراً 15 دقيقة يمنحك هيبة احترافية كاسحة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF78350F)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "أدخل وقت الموعد أو الاجتماع الرسمي:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = meetingHour,
                            onValueChange = { if (it.length <= 2) meetingHour = it },
                            label = { Text("الساعة (0-23)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = meetingMinute,
                            onValueChange = { if (it.length <= 2) meetingMinute = it },
                            label = { Text("الدقيقة (0-59)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "موعد وصولك الإلزامي (وقت لومباردي):",
                                style = MaterialTheme.typography.labelMedium,
                                color = AmberSecondary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formattedLombardi,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "(قبل الموعد بـ 15 دقيقة للتنفس، مراجعة الأوراق، والهدوء)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 7. Life Alignment & 6-Month Reset Tool
// -------------------------------------------------------------
@Composable
fun LifeAlignmentTool() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🧭 زر إعادة الضبط (The 6-Month Reset Question)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ماذا ستفعل لو علمت أنه تبقى في عمرك 6 أشهر فقط؟ هذه ليست دعوة للكآبة بل بوصلة حاسمة تكشف لك قيمك الحقيقية التي تغطيها زحمة اليوميات. لن تقول أبداً 'سأقضي وقتاً أطول في المكتب'!",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "سؤال المدير: 'لماذا أنا على قائمة الرواتب؟'",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AmberSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "80-90% من الموظفين لا يعرفون بالضبط ما ينتظره المدير. اكتب مهامك واجلس مع مديرك واسأله: 'إذا كان عليّ إنجاز مهمة واحدة فقط اليوم فما هي؟ وما رقم 2؟ وما رقم 3؟' هذا يضمن أن مجهودك يصب في المكان الذي يمنحك الترقية والاحترام.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "بروتوكول حماية الحدود وكلمة 'لأ':",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldTertiary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. عندما يسألك زميل: 'عندك دقيقة؟' أجب بلطف: 'ليس الآن، هل يمكن بعد الظهر؟' (90% سينسون).\n2. إذا دخل عليك زائر بلا موعد: قف فوراً وامشِ نحو الباب ولا تمنحه بيئة استرخاء.\n3. عند الرد على الهاتف ابدأ بـ: 'أهلاً بك، كيف يمكنني مساعدتك اليوم؟' للسيطرة على الأجندة وقطع الرغي.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp)
                    )
                }
            }
        }
    }
}
