package com.setgo.tank

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlin.math.roundToInt
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardColors
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight

private val Background get() = SuBg
private val TitleColor get() = SuTitle
private val SubtitleColor get() = SuSub
private val GroupColor get() = SuSub
private val ArrowColor get() = if (suiteDark) Color(0xFF55555A) else Color(0xFFC2C2C7)

/** 主页：功能区（HyperLight 风格：大标题 + 分组 + 列表项） */
@Composable
fun HomeScreen(onOpenFeature: (FeatureItem) -> Unit, onOpenSettings: () -> Unit) {
    val listState = rememberLazyListState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        SuiteFadingTopBar(
            title = "StegoTank",
            scrollOffset = listState.firstVisibleItemScrollOffset.toFloat(),
            trailing = {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 10.dp)
                        .size(42.dp)
                        .clickable(onClick = onOpenSettings),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = null,
                        tint = TitleColor,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                BasicText(
                    text = t("图片功能", "圖片功能", "Features"),
                    style = TextStyle(
                        color = GroupColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(start = 6.dp, top = 4.dp, bottom = 2.dp)
                )
            }
            items(features) { item ->
                FeatureCard(item = item, onClick = { onOpenFeature(item) })
            }
        }
    }
}

@Composable
private fun FeatureCard(item: FeatureItem, onClick: () -> Unit) {
    // 三语标题/副标题（按功能 id）
    val title = when (item.id) {
        "rac" -> t("RAC 图隐", "RAC 圖隱", "RAC Stegano")
        "phantom" -> t("幻影坦克", "幻影坦克", "Phantom Tank")
        "prism" -> t("光棱坦克", "光棱坦克", "Prism Tank")
        "arnold" -> t("图片混淆", "圖片混淆", "Image Scramble")
        else -> item.title
    }
    val subtitle = when (item.id) {
        "rac" -> t("把一张图，藏进另一张图", "把一張圖，藏進另一張圖", "Hide one image inside another")
        "phantom" -> t("同一张图随观察方式呈现不同画面", "同一張圖隨觀察方式呈現不同畫面", "One image, two views by brightness")
        "prism" -> t("棱镜级光学变换隐写", "棱鏡級光學變換隱寫", "Prism-grade optical steganography")
        "arnold" -> t("Arnold 置乱，图像像素级打乱", "Arnold 置亂，圖像像素級打亂", "Arnold cat-map pixel shuffling")
        else -> item.subtitle
    }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardColors(SuCardBg, SuTitle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(item.color, RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = item.avatarRes,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(13.dp))
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                BasicText(
                    text = title,
                    style = TextStyle(
                        color = TitleColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(Modifier.height(4.dp))
                BasicText(
                    text = subtitle,
                    style = TextStyle(
                        color = SubtitleColor,
                        fontSize = 13.sp
                    )
                )
            }
            Spacer(Modifier.width(10.dp))
            Icon(
                imageVector = MiuixIcons.Basic.ArrowRight,
                contentDescription = null,
                tint = ArrowColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** 关于页：Logo + 名称 + 标语 + 版本 + 开发者 + 图片功能开源出处 + 系统信息 + 适配 + 引用 */
@Composable
fun AboutScreen() {
    val scrollState = rememberScrollState()
    val openUrl = rememberOpenUrl()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .navigationBarsPadding()
                .padding(top = 96.dp)
        ) {
            // Logo + 名称 + 标语 + 版本
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher),
                    contentDescription = null,
                    modifier = Modifier.size(68.dp)
                )
                Spacer(Modifier.height(14.dp))
                BasicText(
                    text = "StegoTank",
                    style = TextStyle(
                        color = TitleColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(Modifier.height(5.dp))
                BasicText(
                    text = "把一张图，藏进另一张图",
                    style = TextStyle(
                        color = SubtitleColor,
                        fontSize = 13.sp
                    )
                )
                Spacer(Modifier.height(5.dp))
                BasicText(
                    text = "版本 ${BuildConfig.VERSION_NAME}",
                    style = TextStyle(
                        color = SubtitleColor,
                        fontSize = 12.sp
                    )
                )
            }

            Spacer(Modifier.height(20.dp))

            // 开发者：首次内置头像 → 首次打开有网时缓存覆盖一次 → 之后不再主动获取
            val ctx = LocalContext.current
            val scope = rememberCoroutineScope()
            var avatarCached by remember { mutableStateOf(AvatarCache.hasCached(ctx)) }
            LaunchedEffect(Unit) {
                if (!avatarCached) {
                    AvatarCache.ensureOnce(ctx, scope) { ok -> if (ok) avatarCached = true }
                }
            }
            AboutGroup("开发者") {
                AboutRow(
                    avatar = "G",
                    avatarColor = Color(0xFF3482FF),
                    title = "Gz3192019",
                    subtitle = "StegoTank 作者 · Compose 重构与交互工作台",
                    avatarModel = AvatarCache.cachedFile(ctx) ?: R.drawable.avatar_dev,
                    onClick = { openUrl("https://github.com/Gz3192019") }
                )
            }

            // 图片功能 · 开源出处（头像 = 原作者 GitHub 头像）
            AboutGroup("图片功能 · 开源出处") {
                AboutRow(
                    avatar = "R",
                    avatarColor = Color(0xFF1F1F1F),
                    title = "RAC-Hide",
                    subtitle = "DCT 鲁棒隐写 · 原作者 tuoPzf · rac-hide 仓库",
                    avatarModel = R.drawable.avatar_rac,
                    onClick = { openUrl("https://github.com/tuoPzf/rac-hide") }
                )
                AboutRow(
                    avatar = "幻",
                    avatarColor = Color(0xFF9C5BFF),
                    title = "幻影坦克",
                    subtitle = "wuyr/HideImageMaker · 黑白背景双显",
                    avatarModel = R.drawable.avatar_phantom,
                    onClick = { openUrl("https://github.com/wuyr/HideImageMaker") }
                )
                AboutRow(
                    avatar = "光",
                    avatarColor = Color(0xFF00B96B),
                    title = "光棱坦克",
                    subtitle = "Mirage_Decode · 亮度通道差分显影",
                    avatarModel = R.drawable.avatar_prism,
                    onClick = { openUrl("https://github.com/TankFactory/Mirage_Decode") }
                )
                AboutRow(
                    avatar = "混",
                    avatarColor = Color(0xFFFF7A00),
                    title = "图片混淆",
                    subtitle = "ObfuscationUtils · Arnold Cat Map 猫脸置乱",
                    avatarModel = R.drawable.avatar_arnold,
                    onClick = { openUrl("https://github.com/2195517546/ObfuscationUtils") }
                )
            }

            // 技术支持 · 开源（实际使用的库与框架；头像 = 组织 GitHub 头像）
            AboutGroup("技术支持 · 开源") {
                AboutRow(
                    avatar = "M",
                    avatarColor = Color(0xFF3482FF),
                    title = "miuix",
                    subtitle = "compose-miuix-ui/miuix · MIUI 风格 Compose 组件库",
                    avatarModel = R.drawable.avatar_miuix,
                    onClick = { openUrl("https://github.com/compose-miuix-ui/miuix") }
                )
                AboutRow(
                    avatar = "C",
                    avatarColor = Color(0xFF0E7490),
                    title = "Jetpack Compose",
                    subtitle = "Android 官方声明式 UI 框架",
                    avatarModel = R.drawable.avatar_compose,
                    onClick = { openUrl("https://developer.android.com/jetpack/compose") }
                )
                AboutRow(
                    avatar = "K",
                    avatarColor = Color(0xFF7F52FF),
                    title = "Kotlin",
                    subtitle = "JVM 现代编程语言",
                    avatarModel = R.drawable.avatar_kotlin,
                    onClick = { openUrl("https://kotlinlang.org") }
                )
                AboutRow(
                    avatar = "i",
                    avatarColor = Color(0xFF1F1F1F),
                    title = "Coil",
                    subtitle = "coil-kt/coil · Kotlin 图片加载库",
                    avatarModel = R.drawable.avatar_coil,
                    onClick = { openUrl("https://github.com/coil-kt/coil") }
                )
            }

            // 关于本软件
            AboutGroup("关于本软件") {
                BasicText(
                    text = "图隐套件（StegoTank）是一个完全本地化的图片隐写工具箱：" +
                        "RAC 图隐把一张图藏进另一张图（DCT 鲁棒隐写，可提取还原）；" +
                        "幻影坦克让同一张图在亮/暗背景下呈现两幅画面；" +
                        "光棱坦克通过亮度通道差分显影实现棱镜级光学变换隐写；" +
                        "图片混淆用 Arnold 猫脸置乱把图像像素级打乱。\n\n" +
                        "所有图片处理均在设备本地完成，不上传任何内容。",
                    style = TextStyle(
                        color = SubtitleColor,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                )
            }

            Spacer(Modifier.height(20.dp))
            BasicText(
                text = "GPL-3.0-or-later · 仅供研究与版权水印等合法用途",
                style = TextStyle(
                    color = SubtitleColor,
                    fontSize = 12.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )
            Spacer(Modifier.height(60.dp))
        }
        // 顶栏渐隐遮罩（overlay：滚动内容从顶部穿过时被渐隐覆盖）
        SuiteFadingTopBar(
            title = t("关于", "關於", "About"),
            scrollOffset = scrollState.value.toFloat()
        )
    }
}

@Composable
private fun AboutGroup(title: String, content: @Composable () -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        BasicText(
            text = title,
            style = TextStyle(
                color = GroupColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(start = 6.dp, top = 10.dp, bottom = 8.dp)
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun AboutRow(
    avatar: String,
    avatarColor: Color,
    title: String,
    subtitle: String,
    avatarModel: Any? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (avatarModel != null) {
            AsyncImage(
                model = avatarModel,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(avatarColor, RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                BasicText(
                    text = avatar,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            BasicText(
                text = title,
                style = TextStyle(
                    color = TitleColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(Modifier.height(2.dp))
            BasicText(
                text = subtitle,
                style = TextStyle(
                    color = SubtitleColor,
                    fontSize = 13.sp
                )
            )
        }
        if (onClick != null) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = MiuixIcons.Basic.ArrowRight,
                contentDescription = null,
                tint = ArrowColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/** 用系统浏览器打开外部链接 */
@Composable
private fun rememberOpenUrl(): (String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { url ->
            try {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(url))
                )
            } catch (_: Exception) {
            }
        }
    }
}

/** 二级页：按功能分发（已实现的功能走对应页面） */
@Composable
fun FeatureScreen(item: FeatureItem, onBack: () -> Unit) {
    when (item.id) {
        "rac" -> RacScreen(onBack = onBack)
        "phantom" -> MirageTankScreen(onBack = onBack)
        "prism" -> PrismTankScreen(onBack = onBack)
        "arnold" -> ArnoldScreen(onBack = onBack)
        else -> FeaturePlaceholder(item = item, onBack = onBack)
    }
}

@Composable
private fun FeaturePlaceholder(item: FeatureItem, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 8.dp, end = 20.dp, top = 10.dp, bottom = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    BasicText(
                        text = "‹",
                        style = TextStyle(fontSize = 30.sp, color = TitleColor),
                        modifier = Modifier.padding(start = 6.dp, top = 2.dp)
                    )
                }
                Spacer(Modifier.width(4.dp))
                BasicText(
                    text = item.title,
                    style = TextStyle(
                        color = TitleColor,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(item.color, RoundedCornerShape(26.dp)),
                contentAlignment = Alignment.Center
            ) {
                BasicText(
                    text = item.tag,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            Spacer(Modifier.height(20.dp))
            BasicText(
                text = item.title,
                style = TextStyle(
                    color = TitleColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(Modifier.height(10.dp))
            BasicText(
                text = "功能正在迁移中，敬请期待",
                style = TextStyle(
                    color = SubtitleColor,
                    fontSize = 14.sp
                )
            )
        }
    }
}
