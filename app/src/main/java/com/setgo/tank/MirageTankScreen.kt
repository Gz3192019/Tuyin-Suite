package com.setgo.tank

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.setgo.tank.core.SuiteEngines
import com.setgo.tank.core.TuyinImages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 幻影坦克二级页：两张图合成“白天/黑夜双视图”的 PNG（对齐 Mirage_Colored）。 */
@Composable
fun MirageTankScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var frontBmp by remember { mutableStateOf<Bitmap?>(null) }
    var backBmp by remember { mutableStateOf<Bitmap?>(null) }
    var resultBmp by remember { mutableStateOf<Bitmap?>(null) }
    var outScale by remember { mutableFloatStateOf(1f) }
    var status by remember { mutableStateOf("") }
    var previewJob by remember { mutableStateOf<Job?>(null) }
    val scrollState = rememberScrollState()

    var pendingSave by remember { mutableStateOf<(() -> Unit)?>(null) }
    val writePermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) pendingSave?.invoke()
    }
    fun requestSave(bmp: Bitmap?, name: String) {
        if (bmp == null) { status = "还没有可保存的图片"; return }
        val doSave: () -> Unit = { scope.launch {
            val ok = withContext(Dispatchers.Default) { TuyinImages.saveBitmapToGallery(context, bmp, name, "image/png", 100) }
            status = if (ok) "已保存到相册" else "保存失败"
        } }
        if (Build.VERSION.SDK_INT <= 28 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            pendingSave = doSave
            writePermLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }
        doSave()
    }

    val pickFront = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) scope.launch {
            try {
                val data = withContext(Dispatchers.Default) { TuyinImages.decodeUri(context, uri, 2048) }
                frontBmp = TuyinImages.imageDataToBitmap(data)
            } catch (e: Exception) { status = "读取表图失败：${e.message}" }
        }
    }
    val pickBack = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) scope.launch {
            try {
                val data = withContext(Dispatchers.Default) { TuyinImages.decodeUri(context, uri, 2048) }
                backBmp = TuyinImages.imageDataToBitmap(data)
            } catch (e: Exception) { status = "读取底图失败：${e.message}" }
        }
    }

    fun generatePreview() {
        val f = frontBmp ?: return
        val b = backBmp ?: return
        previewJob?.cancel()
        previewJob = scope.launch {
            try {
                resultBmp = withContext(Dispatchers.Default) { SuiteEngines.mirage(f, b, outScale) }
                status = "预览已更新"
            } catch (e: Exception) {
                status = "生成失败：${e.message}"
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(SuBg)) {
        SuiteFadingTopBar(
            title = t("幻影坦克", "幻影坦克", "Mirage Tank"),
            scrollOffset = scrollState.value.toFloat(),
            onBack = onBack
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SuiteCard {
                SuiteTileRow("影", t("原理", "原理", "How it works"), t("参考 Mirage_Colored：同一张 PNG 在亮背景显示前景图，在暗背景显示另一张图", "參考 Mirage_Colored：同一張 PNG 在亮背景顯示前景圖，在暗背景顯示另一張圖", "From Mirage_Colored: one PNG shows front on light bg, another on dark bg")) { }
            }
            SuitePickCard(
                title = t("前景图（亮背景显示）", "前景圖（亮背景顯示）", "Front (light bg)"),
                hint = t("点击选择前景图", "點擊選擇前景圖", "Tap to pick front image"),
                color = Color(0xFF00A854),
                bmp = frontBmp,
                onClick = { pickFront.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
            )
            SuitePickCard(
                title = t("背景图（暗背景显示）", "背景圖（暗背景顯示）", "Back (dark bg)"),
                hint = t("点击选择背景图", "點擊選擇背景圖", "Tap to pick back image"),
                color = Color(0xFF0077E6),
                bmp = backBmp,
                onClick = { pickBack.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
            )
            SuiteCard {
                SuiteTileRow("出", t("输出尺寸", "輸出尺寸", "Output scale"), t("1.0 为原始尺寸", "1.0 為原始尺寸", "1.0 = original size")) { }
                Spacer(Modifier.height(6.dp))
                SuiteParamSlider(t("缩放", "縮放", "Scale"), (outScale * 100).toInt(), 10, 200) { v -> outScale = v / 100f; generatePreview() }
            }
            SuiteMainButton(t("生成幻影图", "生成幻影圖", "Generate"), enabled = frontBmp != null && backBmp != null) { generatePreview() }
            if (resultBmp != null) {
                SuiteCard {
                    SuiteTileRow("果", t("幻影图", "幻影圖", "Result"), t("点击图片可全屏查看", "點擊圖片可全屏查看", "Tap image to view full screen")) { }
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .clickable { previewBmp = resultBmp },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = resultBmp!!.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(1f)) {
                        SuiteGhostButton(t("保存到相册", "保存到相冊", "Save"), SuBlue) { requestSave(resultBmp, "mirage.png") }
                    }
                    Box(Modifier.weight(1f)) {
                        SuiteGhostButton(t("重置", "重置", "Reset"), Color(0xFFE5484D)) {
                            resultBmp = null; frontBmp = null; backBmp = null; status = ""
                        }
                    }
                }
            }
            if (status.isNotEmpty()) {
                BasicText(status, style = TextStyle(color = SuSub, fontSize = 13.sp))
            }
            Spacer(Modifier.height(8.dp))
        }
    }
    SuiteImageViewer(bmp = previewBmp, onDismiss = { previewBmp = null })
}
