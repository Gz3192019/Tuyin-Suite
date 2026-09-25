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
import androidx.compose.foundation.border
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 图片混淆二级页：Arnold 猫映射像素置乱 / 还原（混淆 tab + 还原 tab）。 */
@Composable
fun ArnoldScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var mode by remember { mutableStateOf(false) } // false=混淆, true=还原
    var srcBmp by remember { mutableStateOf<Bitmap?>(null) }
    var stegoBmp by remember { mutableStateOf<Bitmap?>(null) }
    var resultBmp by remember { mutableStateOf<Bitmap?>(null) }
    var algo by remember { mutableStateOf(SuiteEngines.ScrambleAlgo.Tomato) }
    var key by remember { mutableStateOf("114514") }
    var quality by remember { mutableIntStateOf(95) } // JPEG 输出质量，默认 0.95
    var status by remember { mutableStateOf("") }
    var previewBmp by remember { mutableStateOf<Bitmap?>(null) }

    var pendingSave by remember { mutableStateOf<(() -> Unit)?>(null) }
    val writePermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) pendingSave?.invoke()
    }
    fun requestSave(bmp: Bitmap?, name: String) {
        if (bmp == null) { status = t("还没有可保存的图片", "還沒有可保存的圖片", "Nothing to save yet"); return }
        if (Build.VERSION.SDK_INT <= 28 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            pendingSave = { status = if (TuyinImages.saveBitmapToGallery(context, bmp, name, "image/jpeg", quality)) t("已保存到相册", "已保存到相冊", "Saved to gallery") else t("保存失败", "保存失敗", "Save failed") }
            writePermLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }
        status = if (TuyinImages.saveBitmapToGallery(context, bmp, name, "image/jpeg", quality)) t("已保存到相册", "已保存到相冊", "Saved to gallery") else t("保存失败", "保存失敗", "Save failed")
    }

    val pickSrc = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) scope.launch {
            try {
                val data = withContext(Dispatchers.Default) { TuyinImages.decodeUri(context, uri, 2048) }
                srcBmp = TuyinImages.imageDataToBitmap(data)
            } catch (e: Exception) { status = "读取图片失败：${e.message}" }
        }
    }
    val pickStego = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) scope.launch {
            try {
                val data = withContext(Dispatchers.Default) { TuyinImages.decodeUri(context, uri, 2048) }
                stegoBmp = TuyinImages.imageDataToBitmap(data)
            } catch (e: Exception) { status = "读取混淆图失败：${e.message}" }
        }
    }

    fun runScramble() {
        val s = srcBmp
        if (s == null) { status = t("请先选择要混淆的图片", "請先選擇要混淆的圖片", "Pick an image first"); return }
        if (algo.needsKey && key.isBlank()) { status = t("请先输入密钥", "請先輸入密鑰", "Enter a key first"); return }
        scope.launch {
            status = t("正在混淆…", "正在混淆…", "Scrambling…")
            try {
                resultBmp = withContext(Dispatchers.Default) { SuiteEngines.obfuscate(s, algo, key, reverse = false) }
                status = t("混淆完成，用相同算法与密钥可还原", "混淆完成，用相同算法與密鑰可還原", "Done. Restore with same algo & key")
            } catch (e: Exception) { status = t("混淆失败：", "混淆失敗：", "Failed: ") + e.message }
        }
    }

    fun runRestore() {
        val s = stegoBmp
        if (s == null) { status = t("请先选择混淆后的图片", "請先選擇混淆後的圖片", "Pick the scrambled image first"); return }
        if (algo.needsKey && key.isBlank()) { status = t("请先输入密钥", "請先輸入密鑰", "Enter a key first"); return }
        scope.launch {
            status = t("正在还原…", "正在還原…", "Restoring…")
            try {
                resultBmp = withContext(Dispatchers.Default) { SuiteEngines.obfuscate(s, algo, key, reverse = true) }
                status = t("还原完成（需与混淆时算法、密钥一致）", "還原完成（需與混淆時算法、密鑰一致）", "Restored (same algo & key as scrambling)")
            } catch (e: Exception) { status = t("还原失败：", "還原失敗：", "Failed: ") + e.message }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(SuBg)) {
        SuiteFadingTopBar(
            title = t("图片混淆", "圖片混淆", "Image Scramble"),
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
            SuiteModeSwitch(
                mode = mode,
                onMode = { mode = it },
                leftLabel = t("混淆", "混淆", "Scramble"),
                rightLabel = t("还原", "還原", "Restore")
            )
            SuiteCard {
                SuiteTileRow("混", t("原理", "原理", "How it works"), t("参考 ObfuscationUtils：多种可逆混淆算法，用相同算法与密钥即可无损还原", "參考 ObfuscationUtils：多種可逆混淆算法，用相同算法與密鑰即可無損還原", "From ObfuscationUtils: reversible scrambles; restore with the same algo & key")) { }
            }
            if (!mode) {
                SuitePickCard(
                    title = t("选择原始图片", "選擇原始圖片", "Pick original image"),
                    hint = t("点击选择要被混淆的图片", "點擊選擇要被混淆的圖片", "Tap to pick the image to scramble"),
                    color = Color(0xFFFF7A00),
                    bmp = srcBmp,
                    onClick = { pickSrc.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                )
                SuiteCard {
                    SuiteTileRow("法", t("混淆算法", "混淆算法", "Algorithm"), t("算法与密钥需牢记，还原时保持一致", "算法與密鑰需牢記，還原時保持一致", "Remember the algo & key for restore")) { }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuiteSegButton("番茄", algo == SuiteEngines.ScrambleAlgo.Tomato, { algo = SuiteEngines.ScrambleAlgo.Tomato }, Modifier.weight(1f))
                        SuiteSegButton("分块", algo == SuiteEngines.ScrambleAlgo.Block, { algo = SuiteEngines.ScrambleAlgo.Block }, Modifier.weight(1f))
                        SuiteSegButton("行像素", algo == SuiteEngines.ScrambleAlgo.RowPixel, { algo = SuiteEngines.ScrambleAlgo.RowPixel }, Modifier.weight(1f))
                        SuiteSegButton("像素级", algo == SuiteEngines.ScrambleAlgo.PerPixel, { algo = SuiteEngines.ScrambleAlgo.PerPixel }, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuiteSegButton("行加密", algo == SuiteEngines.ScrambleAlgo.PicEncryptRow, { algo = SuiteEngines.ScrambleAlgo.PicEncryptRow }, Modifier.weight(1f))
                        SuiteSegButton("行列", algo == SuiteEngines.ScrambleAlgo.PicEncryptRowColumn, { algo = SuiteEngines.ScrambleAlgo.PicEncryptRowColumn }, Modifier.weight(1f))
                        SuiteSegButton("排序", algo == SuiteEngines.ScrambleAlgo.Sort, { algo = SuiteEngines.ScrambleAlgo.Sort }, Modifier.weight(1f))
                        SuiteSegButton("随机", algo == SuiteEngines.ScrambleAlgo.Random, { algo = SuiteEngines.ScrambleAlgo.Random }, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    BasicText(t("当前：", "當前：", "Now: ") + algo.label + " — " + t(algo.hint, algo.hint, algo.hint), style = TextStyle(color = SuSub, fontSize = 12.sp))
                }
                SuiteCard {
                    SuiteTileRow("钥", t("密钥", "密鑰", "Key"), if (algo.needsKey) t("混淆与还原必须使用相同密钥", "混淆與還原必須使用相同密鑰", "Same key needed for restore") else t("排序算法无需密钥", "排序算法無需密鑰", "No key needed for Sort")) { }
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SuSegBg)
                            .then(
                                if (algo.needsKey) Modifier.border(1.dp, SuBlue.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                else Modifier.border(1.dp, Color.Transparent, RoundedCornerShape(12.dp))
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        BasicTextField(
                            value = key,
                            onValueChange = { if (algo.needsKey || it.isBlank()) key = it },
                            textStyle = TextStyle(color = SuTitle, fontSize = 14.sp),
                            singleLine = true,
                            enabled = algo.needsKey,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (key.isEmpty() && algo.needsKey) {
                        BasicText(t("输入密钥（番茄=数字；行加密/行列=浮点；其余任意字符串）", "輸入密鑰（番茄=數字；行加密/行列=浮點；其餘任意字符串）", "Key: Tomato=number; Row/RowColumn=float; others any string"), style = TextStyle(color = SuSub, fontSize = 12.sp))
                    }
                }
                SuiteCard {
                    SuiteTileRow("质", t("输出质量", "輸出質量", "Output quality"), t("保存为 JPEG，默认 0.95（参考小番茄混淆）", "保存為 JPEG，預設 0.95（參考小番茄混淆）", "Saved as JPEG, default 0.95")) { }
                    Spacer(Modifier.height(6.dp))
                    SuiteParamSlider(t("质量", "質量", "Quality"), quality, 50, 100) { quality = it }
                }
                SuiteMainButton(t("开始混淆", "開始混淆", "Scramble"), enabled = srcBmp != null) { runScramble() }
            } else {
                SuitePickCard(
                    title = t("选择混淆后的图片", "選擇混淆後的圖片", "Pick scrambled image"),
                    hint = t("点击选择需要还原的噪声图", "點擊選擇需要還原的雜訊圖", "Tap to pick the noise image to restore"),
                    color = Color(0xFFFF7A00),
                    bmp = stegoBmp,
                    onClick = { pickStego.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                )
                SuiteCard {
                    SuiteTileRow("法", t("混淆算法", "混淆算法", "Algorithm"), t("必须与混淆时选择相同的算法", "必須與混淆時選擇相同的算法", "Must match the algorithm used to scramble")) { }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuiteSegButton("番茄", algo == SuiteEngines.ScrambleAlgo.Tomato, { algo = SuiteEngines.ScrambleAlgo.Tomato }, Modifier.weight(1f))
                        SuiteSegButton("分块", algo == SuiteEngines.ScrambleAlgo.Block, { algo = SuiteEngines.ScrambleAlgo.Block }, Modifier.weight(1f))
                        SuiteSegButton("行像素", algo == SuiteEngines.ScrambleAlgo.RowPixel, { algo = SuiteEngines.ScrambleAlgo.RowPixel }, Modifier.weight(1f))
                        SuiteSegButton("像素级", algo == SuiteEngines.ScrambleAlgo.PerPixel, { algo = SuiteEngines.ScrambleAlgo.PerPixel }, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuiteSegButton("行加密", algo == SuiteEngines.ScrambleAlgo.PicEncryptRow, { algo = SuiteEngines.ScrambleAlgo.PicEncryptRow }, Modifier.weight(1f))
                        SuiteSegButton("行列", algo == SuiteEngines.ScrambleAlgo.PicEncryptRowColumn, { algo = SuiteEngines.ScrambleAlgo.PicEncryptRowColumn }, Modifier.weight(1f))
                        SuiteSegButton("排序", algo == SuiteEngines.ScrambleAlgo.Sort, { algo = SuiteEngines.ScrambleAlgo.Sort }, Modifier.weight(1f))
                        SuiteSegButton("随机", algo == SuiteEngines.ScrambleAlgo.Random, { algo = SuiteEngines.ScrambleAlgo.Random }, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    BasicText(t("当前：", "當前：", "Now: ") + algo.label, style = TextStyle(color = SuSub, fontSize = 12.sp))
                }
                SuiteCard {
                    SuiteTileRow("钥", t("密钥", "密鑰", "Key"), t("必须与混淆时使用的密钥一致", "必須與混淆時使用的密鑰一致", "Must match the key used to scramble")) { }
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SuSegBg)
                            .then(
                                if (algo.needsKey) Modifier.border(1.dp, SuBlue.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                else Modifier.border(1.dp, Color.Transparent, RoundedCornerShape(12.dp))
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        BasicTextField(
                            value = key,
                            onValueChange = { if (algo.needsKey || it.isBlank()) key = it },
                            textStyle = TextStyle(color = SuTitle, fontSize = 14.sp),
                            singleLine = true,
                            enabled = algo.needsKey,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                SuiteMainButton(t("开始还原", "開始還原", "Restore"), enabled = stegoBmp != null) { runRestore() }
            }
            if (resultBmp != null) {
                SuiteCard {
                    SuiteTileRow("果", if (!mode) t("混淆结果", "混淆結果", "Scrambled") else t("还原结果", "還原結果", "Restored"), t("点击图片可全屏查看", "點擊圖片可全屏查看", "Tap image to view full screen")) { }
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
                        SuiteGhostButton(t("保存到相册", "保存到相冊", "Save"), SuBlue) { requestSave(resultBmp, "arnold.jpg") }
                    }
                    Box(Modifier.weight(1f)) {
                        SuiteGhostButton(t("重置", "重置", "Reset"), Color(0xFFE5484D)) {
                            resultBmp = null; srcBmp = null; stegoBmp = null; status = ""
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
