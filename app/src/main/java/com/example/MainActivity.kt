package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.ui.components.QuizDialog
import com.example.ui.components.TopHeaderBar
import com.example.ui.screens.*
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.TimeMasteryViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TimeMasteryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Ensure RTL for Arabic language interface
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    TimeMasteryApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun TimeMasteryApp(viewModel: TimeMasteryViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    val unitProgressList by viewModel.unitProgressList.collectAsState()
    val dailyQuote by viewModel.dailyQuote.collectAsState()
    val quizState by viewModel.quizState.collectAsState()
    val snackbarMsg by viewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_app_scaffold"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopHeaderBar(
                userStats = userStats,
                onNotificationClick = { viewModel.triggerNotification() },
                modifier = Modifier.statusBarsPadding()
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBar(
                    modifier = Modifier.testTag("bottom_navigation_bar"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    val navColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.primary
                    )

                    // 1. Curriculum
                    NavigationBarItem(
                        selected = selectedTab == AppTab.CURRICULUM,
                        onClick = { viewModel.selectTab(AppTab.CURRICULUM) },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == AppTab.CURRICULUM) Icons.Filled.School else Icons.Outlined.School,
                                contentDescription = "الوحدات والكويزات"
                            )
                        },
                        label = {
                            Text(
                                "الوحدات",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_curriculum"),
                        colors = navColors
                    )

                    // 2. Practical Tools
                    NavigationBarItem(
                        selected = selectedTab == AppTab.TOOLS,
                        onClick = { viewModel.selectTab(AppTab.TOOLS) },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == AppTab.TOOLS) Icons.Filled.Build else Icons.Outlined.Build,
                                contentDescription = "الأدوات والحيل"
                            )
                        },
                        label = {
                            Text(
                                "الأدوات",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_tools"),
                        colors = navColors
                    )

                    // 3. Reports
                    NavigationBarItem(
                        selected = selectedTab == AppTab.REPORTS,
                        onClick = { viewModel.selectTab(AppTab.REPORTS) },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == AppTab.REPORTS) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                                contentDescription = "تقارير الأداء"
                            )
                        },
                        label = {
                            Text(
                                "التقارير",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_reports"),
                        colors = navColors
                    )

                    // 4. Gamification
                    NavigationBarItem(
                        selected = selectedTab == AppTab.GAMIFICATION,
                        onClick = { viewModel.selectTab(AppTab.GAMIFICATION) },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == AppTab.GAMIFICATION) Icons.Filled.EmojiEvents else Icons.Outlined.EmojiEvents,
                                contentDescription = "الإنجازات والتلعيب"
                            )
                        },
                        label = {
                            Text(
                                "الإنجازات",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_gamification"),
                        colors = navColors
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                AppTab.CURRICULUM -> {
                    CurriculumScreen(
                        progressList = unitProgressList,
                        dailyQuote = dailyQuote,
                        onRefreshQuote = { viewModel.refreshQuote() },
                        onStartQuiz = { unit -> viewModel.startQuiz(unit) }
                    )
                }

                AppTab.TOOLS -> {
                    ToolsScreen(viewModel = viewModel)
                }

                AppTab.REPORTS -> {
                    ReportsScreen(viewModel = viewModel)
                }

                AppTab.GAMIFICATION -> {
                    GamificationScreen(
                        userStats = userStats,
                        progressList = unitProgressList
                    )
                }
            }

            // Interactive Quiz Sheet / Dialog Overlay
            quizState?.let { qState ->
                QuizDialog(
                    quizState = qState,
                    onOptionSelected = { idx -> viewModel.selectQuizOption(idx) },
                    onSubmitAnswer = { viewModel.submitAnswer() },
                    onNextQuestion = { viewModel.nextQuestion() },
                    onClose = { viewModel.closeQuiz() }
                )
            }
        }
    }
}
