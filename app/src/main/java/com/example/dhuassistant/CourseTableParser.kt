package com.example.dhuassistant

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

object CourseTableParser {
    fun parse(html: String): List<Course> {
        val result = mutableListOf<Course>()
        val doc = Jsoup.parse(html)
        val tableElement = doc.select("table").firstOrNull() ?: return emptyList()
        val rows = tableElement.select("tr")

        // 建立一个 20行 x 10列 的虚拟矩阵，存放解析出的原始 Element
        // 这样可以物理定位每一个格子，无视 rowspan 的干扰
        val grid = Array(30) { arrayOfNulls<org.jsoup.nodes.Element>(10) }

        // 第一步：将 HTML 映射到物理矩阵中
        for (r in rows.indices) {
            val cells = rows[r].select("td, th")
            var cOffset = 0
            for (cell in cells) {
                // 寻找矩阵中下一个空闲的列
                while (grid[r][cOffset] != null) {
                    cOffset++
                }

                val rowspan = cell.attr("rowspan").toIntOrNull() ?: 1
                val colspan = cell.attr("colspan").toIntOrNull() ?: 1

                // 将当前单元格填入矩阵（包括它占用的所有跨行格子）
                for (rs in 0 until rowspan) {
                    for (cs in 0 until colspan) {
                        if (r + rs < 30 && cOffset + cs < 10) {
                            grid[r + rs][cOffset + cs] = cell
                        }
                    }
                }
                cOffset += colspan
            }
        }

        // 第二步：遍历矩阵提取课程
        // 矩阵的每一行 r 代表 HTML 的一行
        // 矩阵的第 0 列通常是“节次”，第 1-7 列是周一到周日
        for (r in grid.indices) {
            val firstColumnCell = grid[r][0] ?: continue
            val rowHeaderText = firstColumnCell.text()

            // 关键：通过文字判断当前行对应的真实节次（如“一节”->1, “三节”->3）
            val lessonIndex = getLessonNumber(rowHeaderText)
            if (lessonIndex == -1) continue // 说明是标题行或休息行，跳过

            for (day in 1..7) {
                val cell = grid[r][day] ?: continue

                // 避免重复解析同一个跨行单元格
                // 如果当前格子和上一行格子是同一个 Element 对象，说明是 rowspan 连下来的，跳过
                if (r > 0 && grid[r - 1][day] == cell) continue

                val rowspan = cell.attr("rowspan").toIntOrNull() ?: 1

                // 移除无用标签（按钮等）
                val clonedCell = cell.clone()
                clonedCell.select("a, button, script, span[style*=none]").remove()

                // 按照换行符切割课程块
                val htmlContent = clonedCell.html().replace("&nbsp;", " ")
                val blocks = htmlContent.split(Regex("(?i)<br\\s*/?>|<p>|</div>"))

                for (block in blocks) {
                    val text = Jsoup.parse(block).text().trim()
                    // 过滤掉只有老师名字或只有节次文字的噪音
                    if (text.length > 5 && text.any { it.isDigit() }) {
                        parseSingleCourse(text, day, lessonIndex, rowspan)?.let {
                            result.add(it)
                        }
                    }
                }
            }
        }
        return result
    }

    // 根据行首文字精准定位节次
    private fun getLessonNumber(text: String): Int {
        return when {
            text.contains("一节") -> 1
            text.contains("二节") -> 2
            text.contains("三节") -> 3
            text.contains("四节") -> 4
            text.contains("五节") -> 5
            text.contains("六节") -> 6
            text.contains("七节") -> 7
            text.contains("八节") -> 8
            text.contains("九节") -> 9
            text.contains("十节") -> 10
            text.contains("十一") -> 11
            text.contains("十二") -> 12
            text.contains("十三") -> 13
            else -> -1
        }
    }

    private fun parseSingleCourse(text: String, day: Int, start: Int, duration: Int): Course? {
        // 使用更强力的正则。捕获组1:名称 组2:周数 组3:剩余部分
        val regex = Regex("""(.+?)\s*(\d+[\d\-\,]+周(?:\([单双]\))?)\s*(.*)""")
        val match = regex.find(text) ?: return null

        val name = match.groupValues[1].trim()
        val weeks = match.groupValues[2].trim()
        val rest = match.groupValues[3].trim()

        // 剩余部分：通常第一个空格前是老师，后面是地点
        val restParts = rest.split(Regex("\\s+"))
        val teacher = restParts.getOrNull(0) ?: ""
        val location = restParts.lastOrNull() ?: ""

        return Course(
            name = name,
            teacher = teacher,
            weeks = weeks,
            weekList = parseWeekList(weeks),
            day = day,
            startLesson = start,
            duration = duration,
            location = location
        )
    }

    private fun parseWeekList(str: String): List<Int> {
        val result = mutableListOf<Int>()
        val isEven = str.contains("(双)")
        val isOdd = str.contains("(单)")
        val clean = str.replace(Regex("[周(单双)]"), "")
        clean.split(",").forEach { seg ->
            if (seg.contains("-")) {
                val r = seg.split("-")
                for (i in r[0].toInt()..r[1].toInt()) {
                    if (isEven && i % 2 != 0) continue
                    if (isOdd && i % 2 == 0) continue
                    result.add(i)
                }
            } else if (seg.isNotBlank()) result.add(seg.toInt())
        }
        return result
    }
}