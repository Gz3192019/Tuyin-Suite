package com.setgo.tank

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.setgo.tank.core.LsbHistory
import com.setgo.tank.core.LsbTank
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 嵌入历史页：查看 LSB 嵌入记录，点击展开详情，可清空。 */
@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var entries by remember { mutableStateOf(LsbHistory.list(context)) }
    var expandedIndex by remember { mutableIntStateOf(-1) }
    var status by remember { mutableStateOf("") }

    fun formatSize(n: Int): String = when {
        n >= 1024 * 1024 -> String.format("%.1f MB", n / 1048576f)
        n >= 1024 -> String.format("%.1f KB", n / 1024f)
        else -> "$n B"
    }
    val fmt = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize().background(SuBg)) {
        SuiteFadingTopBar(
            title = t("嵌入历史", "嵌入歷史", "History"),
            scrollOffset = scrollState.value.toFloat(),
            onBack = onBack,
            trailing = {
                if (entries.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 14.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                LsbHistory.clear(context)
                                entries = emptyList()
                                expandedIndex = -1
                                status = t("已清空历史", "已清空歷史", "History cleared")
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BasicText(t("清空", "清空", "Clear"), style = TextStyle(color = Color(0xFFE5484D), fontSize = 13.sp))
                    }
                }
            }
        )
        if (entries.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                BasicText(
                    t("暂无嵌入记录", "暫無嵌入記錄", "No embed records yet"),
                    style = TextStyle(color = SuSub, fontSize = 14.sp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 72.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(entries) { index, e ->
                    val expanded = expandedIndex == index
                    val isText = e.type == LsbTank.TYPE_TEXT
                    SuiteCard {
                        Column(Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isText) Color(0xFF3482FF).copy(alpha = 0.14f) else Color(0xFF00A3C4).copy(alpha = 0.14f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    BasicText(
                                        if (isText) t("文本", "文本", "Text") else t("文件", "檔案", "File"),
                                        style = TextStyle(
                                            color = if (isText) Color(0xFF3482FF) else Color(0xFF00A3C4),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                                Spacer(Modifier.height(0.dp))
                                Spacer(Modifier.padding(4.dp))
                                BasicText(
                                    fmt.format(Date(e.ts)),
                                    style = TextStyle(color = SuSub, fontSize = 12.sp),
                                    modifier = Modifier.weight(1f)
                                )
                                if (e.encrypted) {
                                    BasicText(t("已加密", "已加密", "Encrypted"), style = TextStyle(color = Color(0xFF9C5BFF), fontSize = 11.sp))
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { expandedIndex = if (expanded) -1 else index }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Column {
                                    BasicText(
                                        text = if (isText) (if (expanded) e.summary else e.summary.take(40)) else e.summary,
                                        style = TextStyle(color = SuTitle, fontSize = 13.sp),
                                        maxLines = if (expanded) Int.MAX_VALUE else 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (isText && e.summary.length > 40) {
                                        Spacer(Modifier.height(4.dp))
                                        BasicText(
                                            t(if (expanded) "点击收起" else "点击查看全文", if (expanded) "點擊收起" else "點擊查看全文", if (expanded) "Tap to collapse" else "Tap to expand"),
                                            style = TextStyle(color = SuSub, fontSize = 11.sp)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            BasicText(
                                t("大小：", "大小：", "Size: ") + formatSize(e.size),
                                style = TextStyle(color = SuSub, fontSize = 12.sp)
                            )
                        }
                    }
                }
            }
        }
        if (status.isNotEmpty()) {
            Box(Modifier.fillMaxWidth().padding(16.dp)) {
                BasicText(status, style = TextStyle(color = SuSub, fontSize = 13.sp))
            }
        }
    }
}
