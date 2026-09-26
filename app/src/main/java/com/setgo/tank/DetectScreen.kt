package com.setgo.tank

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.setgo.tank.core.StegDetect
import com.setgo.tank.core.TuyinImages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 隐写检测二级页：只作"判断提示"，不保证精确。 */
@Composable
fun DetectScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var imgBmp by remember { mutableStateOf<Bitmap?>(null) }
    var analyzing by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<StegDetect.Result?>(null) }
    var status by remember { mutableStateOf("") }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) scope.launch {
            try {
                val bmp = withContext(Dispatchers.Default) { TuyinImages.decodeKeepAlpha(context, uri, 2048) }
                imgBmp = bmp
                result = null
            } catch (e: Exception) { status = t("读取图片失败：", "讀取圖片失敗：", "Failed to read image: ") + e.message }
        }
    }

    fun formatSize(n: Int): String = when {
        n >= 1024 * 1024 -> String.format("%.1f MB", n / 1048576f)
        n >= 1024 -> String.format("%.1f KB", n / 1024f)
        else -> "$n B"
    }

    fun runDetect() {
        val bmp = imgBmp ?: return
        scope.launch {
            analyzing = true
            status = t("正在分析…", "正在分析…", "Analyzing…")
            try {
                val r = withContext(Dispatchers.Default) { StegDetect.analyze(bmp) }
                result = r
                status = ""
            } catch (e: Exception) {
                status = t("分析失败：", "分析失敗：", "Failed: ") + e.message
            } finally {
                analyzing = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(SuBg)) {
        SuiteFadingTopBar(
            title = t("隐写检测", "隱寫檢測", "Stego Detect"),
            scrollOffset = scrollState.value.toFloat(),
            onBack = onBack
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SuiteCard {
                SuiteTileRow(
                    t("仅作判断", "僅作判斷", "Heuristic only"),
                    t("本工具只给出「疑似藏入数据」的判断提示，不保证 100% 准确，误报/漏报均有可能", "本工具只給出「疑似藏入資料」的判斷提示，不保證 100% 準確，誤報/漏報均有可能", "This tool only gives a \"possibly hidden\" hint; not guaranteed accurate"))
                { }
            }
            SuitePickCard(
                title = t("选择要检测的图片", "選擇要檢測的圖片", "Pick an image to analyze"),
                hint = t("点击选择图片", "點擊選擇圖片", "Tap to pick an image"),
                color = Color(0xFFFF7A00),
                bmp = imgBmp,
                onClick = { pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
            )
            SuiteMainButton(
                t("开始检测", "開始檢測", "Analyze"),
                enabled = imgBmp != null && !analyzing
            ) { runDetect() }

            val r = result
            if (r != null) {
                SuiteCard {
                    SuiteTileRow(detectTitle(r), detectSubtitle(r)) { }
                    Spacer(Modifier.height(10.dp))
                    // 分级色块
                    val (tag, color) = detectBadge(r)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(color.copy(alpha = 0.12f))
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BasicText(tag, style = TextStyle(color = color, fontSize = 15.sp, fontWeight = FontWeight.SemiBold))
                    }
                    Spacer(Modifier.height(8.dp))
                    BasicText(
                        t("LSB bit0 相同率：", "LSB bit0 相同率：", "LSB bit0 match: ") + String.format("%.1f%%", r.lsbMatchRatio * 100f) +
                                t("　bit1：", "　bit1：", "  bit1: ") + String.format("%.1f%%", r.lsbMatchRatio1 * 100f),
                        style = TextStyle(color = SuSub, fontSize = 12.sp)
                    )
                    Spacer(Modifier.height(4.dp))
                    BasicText(
                        t("bit0 分通道 R/G/B：", "bit0 分通道 R/G/B：", "bit0 per channel R/G/B: ") +
                                String.format("%.0f%%", r.chRatioR * 100f) + " / " +
                                String.format("%.0f%%", r.chRatioG * 100f) + " / " +
                                String.format("%.0f%%", r.chRatioB * 100f),
                        style = TextStyle(color = SuSub, fontSize = 12.sp)
                    )
                    Spacer(Modifier.height(4.dp))
                    BasicText(
                        t("alpha 半透明率：", "alpha 半透明率：", "Alpha translucent: ") + String.format("%.1f%%", r.alphaTransRatio * 100f) +
                                t("　灰度像素：", "　灰階像素：", "  Gray: ") + String.format("%.1f%%", r.grayRatio * 100f),
                        style = TextStyle(color = SuSub, fontSize = 12.sp)
                    )

                    val hints = ArrayList<String>()
                    if (r.blocky) hints.add(t("分块痕迹（疑似 RAC 分块调制）", "分塊痕跡（疑似 RAC 分塊調製）", "Blocking artifact (possible RAC)"))
                    if (r.highContrast) hints.add(t("高对比直方图（疑似光棱双显）", "高對比直方圖（疑似光棱雙顯）", "High-contrast histogram (possible Prism)"))
                    if (hints.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        BasicText(
                            t("可疑线索：", "可疑線索：", "Hints: ") + hints.joinToString(t("、", "、", ", ")),
                            style = TextStyle(color = Color(0xFFFF7A00), fontSize = 12.sp)
                        )
                    }
                    if (r.hasMagic && r.payloadBytes != null) {
                        Spacer(Modifier.height(4.dp))
                        BasicText(
                            t("载荷长度：", "載荷長度：", "Payload: ") + formatSize(r.payloadBytes) +
                                    if (r.encrypted == true) t("（已加密）", "（已加密）", " (encrypted)") else "",
                            style = TextStyle(color = SuSub, fontSize = 12.sp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    SuiteGhostButton(t("重新检测", "重新檢測", "Re-analyze"), SuBlue) { runDetect() }
                }
            }
            if (status.isNotEmpty()) {
                BasicText(status, style = TextStyle(color = SuSub, fontSize = 13.sp))
            }
            Spacer(Modifier.height(72.dp))
        }
    }
}

private fun detectLevel(r: StegDetect.Result): Int = when {
    r.hasMagic -> 1
    r.alphaTransRatio > 0.15f && r.grayRatio > 0.8f -> 2
    r.lsbMatchRatio < 0.42f || r.lsbMatchRatio1 < 0.42f -> 3
    r.alphaTransRatio > 0.25f -> 4
    else -> 0
}

@Composable
private fun detectTitle(r: StegDetect.Result): String = when (detectLevel(r)) {
    1 -> t("检测到 LSB 隐写", "檢測到 LSB 隱寫", "LSB steganography detected")
    2 -> t("检测到幻影通道调制", "檢測到幻影通道調製", "Phantom alpha modulation detected")
    3 -> t("疑似藏入数据", "疑似藏入資料", "Possibly hidden data")
    4 -> t("疑似 alpha 通道异常", "疑似 alpha 通道異常", "Suspected alpha anomaly")
    else -> t("未检测到明显隐写", "未檢測到明顯隱寫", "No obvious steganography")
}

@Composable
private fun detectSubtitle(r: StegDetect.Result): String = when (detectLevel(r)) {
    1 -> t("图片位流含图隐套件魔数，确认是 LSB 藏图", "圖片位流含圖隱套件魔數，確認是 LSB 藏圖", "Bit stream carries the suite magic; confirmed LSB image")
    2 -> t("大量半透明像素且整体为灰度，符合幻影坦克双显特征", "大量半透明像素且整體為灰階，符合幻影坦克雙顯特徵", "Many translucent pixels and full grayscale; matches Phantom dual-image")
    3 -> t("相邻像素最低位（bit0/bit1）相同率偏低，位流疑似被随机化改写", "相鄰像素最低位（bit0/bit1）相同率偏低，位流疑似被隨機化改寫", "Low LSB neighbor match (bit0/bit1); bit stream looks randomized")
    4 -> t("半透明像素占比偏高，alpha 通道疑似被人为调制", "半透明像素佔比偏高，alpha 通道疑似被人為調製", "High translucent ratio; alpha may be modulated")
    else -> t("最低位分布接近自然图，未发现明显嵌入痕迹", "最低位分佈接近自然圖，未發現明顯嵌入痕跡", "LSB distribution looks natural; no obvious traces")
}

@Composable
private fun detectBadge(r: StegDetect.Result): Pair<String, Color> = when (detectLevel(r)) {
    1, 2 -> Pair(t("确定", "確定", "Confirmed"), Color(0xFFE5484D))
    3, 4 -> Pair(t("疑似", "疑似", "Suspected"), Color(0xFFFF7A00))
    else -> Pair(t("未发现", "未發現", "None"), Color(0xFF00A3C4))
}
