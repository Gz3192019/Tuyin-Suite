package com.setgo.tank

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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

/** 幻影坦克二级页：表图（白底）+ 里图（黑底）合成一张带透明通道的 PNG。 */
@Composable
fun MirageTankScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var frontBmp by remember { mutableStateOf<Bitmap?>(null) }
    var backBmp by remember { mutableStateOf<Bitmap?>(null) }
    var resultBmp by remember { mutableStateOf<Bitmap?>(null) }
    var previewBmp by remember { mutableStateOf<Bitmap?>(null) }
    var previewWhite by remember { mutableStateOf(true) }
    var backMix by remember { mutableIntStateOf(50) }      // 里图混合权重 0-100
    var frontGain by remember { mutableIntStateOf(100) }   // 表图亮度 50-200 -> /100
    var frontDesat by remember { mutableIntStateOf(0) }    // 表图去色 0-100
    var backGain by remember { mutableIntStateOf(100) }    // 里图亮度 50-200 -> /100
    var backDesat by remember { mutableIntStateOf(0) }     // 里图去色 0-100
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
            } catch (e: Exception) { status = "读取里图失败：${e.message}" }
        }
    }

    fun generate() {
        val f = frontBmp; val b = backBmp
        if (f == null || b == null) { status = "请先选择表图和里图"; return }
        scope.launch {
            status = "正在合成…"
            try {
                val out = withContext(Dispatchers.Default) {
                    SuiteEngines.mirageTank(
                        f, b,
                        backMix / 100f, frontGain / 100f, frontDesat / 100f,
                        backGain / 100f, backDesat / 100f, outScale
                    )
                }
                resultBmp = out
                previewBmp = withContext(Dispatchers.Default) {
                    SuiteEngines.compositeOn(out, if (previewWhite) 0xFFFFFFFF.toInt() else 0xFF000000.toInt())
                }
                status = "合成完成，可用下方黑白背景预览效果"
            } catch (e: Exception) { status = "合成失败：${e.message}" }
        }
    }

    fun refreshPreview() {
        val r = resultBmp ?: return
        scope.launch {
            previewBmp = withContext(Dispatchers.Default) {
                SuiteEngines.compositeOn(r, if (previewWhite) 0xFFFFFFFF.toInt() else 0xFF000000.toInt())
            }
        }
    }

    // 参数变化实时预览（250ms 防抖，避免拖动滑块频繁重算）
    fun autoPreview() {
        val f = frontBmp; val b = backBmp
        if (f == null || b == null) return
        previewJob?.cancel()
        previewJob = scope.launch {
            delay(250)
            try {
                val out = withContext(Dispatchers.Default) {
                    SuiteEngines.mirageTank(
                        f, b,
                        backMix / 100f, frontGain / 100f, frontDesat / 100f,
                        backGain / 100f, backDesat / 100f, outScale
                    )
                }
                resultBmp = out
                previewBmp = withContext(Dispatchers.Default) {
                    SuiteEngines.compositeOn(out, if (previewWhite) 0xFFFFFFFF.toInt() else 0xFF000000.toInt())
                }
                status = "参数已更新（实时预览）"
            } catch (e: Exception) { status = "预览失败：${e.message}" }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(SuBg)) {
        SuiteFadingTopBar(
            title = t("幻影坦克", "幻影坦克", "Phantom Tank"),
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
                SuiteTileRow("原理", "同一张 PNG：白底显示表图，黑底显示里图（利用透明通道）") { }
            }
            SuitePickCard(
                title = "表图 · 白底显示",
                hint = "点击选择白色背景下显示的图片",
                color = Color(0xFF3482FF),
                bmp = frontBmp,
                onClick = { pickFront.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
            )
            SuitePickCard(
                title = "里图 · 黑底显示",
                hint = "点击选择黑色背景下显示的图片",
                color = Color(0xFF9C5BFF),
                bmp = backBmp,
                onClick = { pickBack.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
            )
            SuiteCard {
                SuiteTileRow("参数调节", "参考 Mirage_Colored：混合权重、亮度（色阶缩放）与去色程度，全部实时预览") { }
                Spacer(Modifier.height(6.dp))
                SuiteParamSlider("里图混合权重", backMix, 0, 100) { backMix = it; autoPreview() }
                SuiteParamSlider("表图亮度", frontGain, 50, 200) { frontGain = it; autoPreview() }
                SuiteParamSlider("表图去色", frontDesat, 0, 100) { frontDesat = it; autoPreview() }
                SuiteParamSlider("里图亮度", backGain, 50, 200) { backGain = it; autoPreview() }
                SuiteParamSlider("里图去色", backDesat, 0, 100) { backDesat = it; autoPreview() }
                Spacer(Modifier.height(4.dp))
                // 表里互换
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(1f)) {
                        SuiteGhostButton("交换表里图", SuBlue) {
                            val t = frontBmp; frontBmp = backBmp; backBmp = t
                            autoPreview()
                        }
                    }
                    Box(Modifier.weight(1f)) {
                        SuiteGhostButton("重置图片", Color(0xFFE5484D)) {
                            frontBmp = null; backBmp = null; resultBmp = null; previewBmp = null; status = ""
                        }
                    }
                }
            }
            SuiteCard {
                SuiteTileRow("输出尺寸", "缩小输出可减少体积，画布按最大边等比缩放") { }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuiteSegButton("原尺寸", outScale == 1f, { outScale = 1f }, Modifier.weight(1f))
                    SuiteSegButton("75%", outScale == 0.75f, { outScale = 0.75f }, Modifier.weight(1f))
                    SuiteSegButton("50%", outScale == 0.5f, { outScale = 0.5f }, Modifier.weight(1f))
                }
            }
            SuiteMainButton("生成幻影坦克", enabled = frontBmp != null && backBmp != null) { generate() }
            if (resultBmp != null) {
                SuiteCard {
                    SuiteTileRow("黑白背景预览", if (previewWhite) "当前：白色背景" else "当前：黑色背景") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SuSegBg)
                                .clickable { previewWhite = !previewWhite; refreshPreview() }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            BasicText(if (previewWhite) "切到黑底" else "切到白底", style = TextStyle(color = SuTitle, fontSize = 12.sp))
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    if (previewBmp != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (previewWhite) Color.White else Color.Black)
                                .clickable { previewWhite = !previewWhite; refreshPreview() },
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Image(
                                bitmap = previewBmp!!.asImageBitmap(),
                                contentDescription = null,
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(1f)) {
                        SuiteGhostButton("保存到相册", SuBlue) { requestSave(resultBmp, "mirage_tank.png") }
                    }
                    Box(Modifier.weight(1f)) {
                        SuiteGhostButton("重置", Color(0xFFE5484D)) {
                            resultBmp = null; previewBmp = null; frontBmp = null; backBmp = null; status = ""
                        }
                    }
                }
            }
            if (status.isNotEmpty()) {
                BasicText(status, style = TextStyle(color = SuSub, fontSize = 13.sp))
            }
            Spacer(Modifier.height(72.dp))
        }
    }
}
