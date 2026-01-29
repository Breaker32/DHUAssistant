package com.example.dhuassistant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

// --- 样式常量 ---
val baseSlotHeight = 72.dp * 0.9f
val timeAxisWidth = 45.dp // 略微加宽，防止四位数字挤在一起
val ThemeBlue = Color(0xFF2196F3)
val TimeBlue = Color(0xFF64B5F6) // 稍微浅一点的蓝色，更接近图二

// 课程颜色池（建议使用更柔和的颜色，模拟图二的质感）
val courseColors = listOf(
    Color(0xFFFFD54F), Color(0xFF64B5F6), Color(0xFF81C784),
    Color(0xFFBA68C8), Color(0xFFF06292), Color(0xFF4DB6AC), Color(0xFFFF8A65)
)

fun getCourseColor(courseName: String): Color {
    val index = Math.abs(courseName.hashCode()) % courseColors.size
    return courseColors[index]
}

@Composable
fun CourseTableView(
    week: Int,
    table: Map<Int, Map<Int, List<Course>>>,
    startDate: LocalDate
) {
    val mondayOfThisWeek = remember(week) {
        startDate.plusWeeks((week - 1).toLong())
    }

    // 全局背景设为白色
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // --- 标题区域 ---
        Text(
            text = "第 $week 周",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ThemeBlue,
            modifier = Modifier
                .padding(vertical = 12.dp)
                .align(Alignment.CenterHorizontally)
        )

        // --- 星期与日期行 ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFBFBFB)) // 浅灰色表头背景
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.width(timeAxisWidth), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("星期", fontSize = 10.sp, color = Color.Gray)
                Text("日期", fontSize = 10.sp, color = Color.Gray)
            }

            val days = listOf("一", "二", "三", "四", "五", "六", "日")
            days.forEachIndexed { index, dayName ->
                val dateOfColumn = mondayOfThisWeek.plusDays(index.toLong())
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = dayName, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(text = "${dateOfColumn.monthValue}/${dateOfColumn.dayOfMonth}", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }

        // --- 课程区域 ---
        Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row {
                // 左侧时间轴
                Column(modifier = Modifier.width(timeAxisWidth)) {
                    timeSlots.forEach { slot ->
                        TimeSlotGap(slot.index, baseSlotHeight)
                        Column(
                            modifier = Modifier.height(baseSlotHeight),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            // 【关键修改点】对齐到顶部，实现与课程块顶端对齐
                            verticalArrangement = Arrangement.Top
                        ) {
                            // 稍微加一点 top padding，让数字中心与卡片顶边缘视觉重合
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = slot.start,
                                fontSize = 10.sp,
                                color = Color(0xFF333333)
                            )
                            Text(
                                text = slot.end,
                                fontSize = 10.sp,
                                color = TimeBlue
                            )
                        }
                    }
                }

                // 右侧课程格子
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (day in 1..7) {
                        Column(modifier = Modifier.weight(1f)) {
                            val renderedSlots = mutableSetOf<Int>()

                            timeSlots.forEach { slot ->
                                if (slot.index in renderedSlots) return@forEach

                                TimeSlotGap(slot.index, baseSlotHeight)

                                val coursesInSlot = table[day]?.get(slot.index) ?: emptyList()

                                if (coursesInSlot.isNotEmpty()) {
                                    val firstCourse = coursesInSlot[0]
                                    val span = calculateSpan(firstCourse, table[day], slot.index)
                                    Box(
                                        modifier = Modifier
                                            .height(baseSlotHeight * span)
                                            .padding(1.dp) // 保持 1dp 间距
                                    ) {
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            coursesInSlot.forEach { course ->
                                                Box(modifier = Modifier.weight(1f).padding(vertical = 0.5.dp)) {
                                                    CourseCard(course, isCompact = coursesInSlot.size > 1)
                                                }
                                            }
                                        }
                                        for (i in 0 until span) renderedSlots.add(slot.index + i)
                                    }
                                } else {
                                    Box(modifier = Modifier.height(baseSlotHeight).fillMaxWidth())
                                    renderedSlots.add(slot.index)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CourseCard(course: Course, isCompact: Boolean = false) {
    // 【关键修改点】字号统一，显示一致
    val fontSize = if (isCompact) 10.sp else 11.sp

    Surface(
        color = getCourseColor(course.name),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = course.name,
                fontSize = fontSize,
                fontWeight = FontWeight.Normal, // 图二看起来字重比较轻
                lineHeight = if (isCompact) 11.sp else 13.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            if (!course.location.isNullOrBlank()) {
                // 【关键修改点】加上 @，字号与课程名一致
                Text(
                    text = "@${course.location}",
                    fontSize = fontSize,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    lineHeight = if (isCompact) 11.sp else 13.sp
                )
            }
        }
    }
}

fun calculateSpan(current: Course, dayMap: Map<Int, List<Course>>?, currentIndex: Int): Int {
    var span = 1
    while (dayMap?.get(currentIndex + span)?.any { it.name == current.name && it.weeks == current.weeks } == true) {
        span++
        if (span > 4) break
    }
    return span
}

@Composable
fun TimeSlotGap(index: Int, baseHeight: androidx.compose.ui.unit.Dp) {
    // 增加间隙高度，使其更有层次感
    when (index) {
        5, 10 -> Spacer(modifier = Modifier.height(baseHeight * 0.4f))
    }
}