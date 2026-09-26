package com.setgo.tank.core

import android.graphics.Bitmap

/**
 * 隐写检测启发式（简化版，只作"判断提示"，不保证精确）。
 *
 * 主判据（高置信→疑似）：
 *  1) LSB 魔数：位流前 4 字节 "ST" → 图隐套件 LSB 格式（确定）。
 *  2) 幻影通道：灰度模式 alpha 被调成半透明且 RGB 三通道相等；
 *     alphaTransRatio>0.15 且 grayRatio>0.8 → 幻影灰度（确定）；alphaTransRatio>0.25 → 疑似 alpha 异常。
 *  3) LSB 相邻相同率（bit0 或 bit1 <0.42）→ 疑似随机化改写。
 *     同时统计 R/G/B 三通道 bit0，指出数据可能藏在哪个通道。
 *
 * 弱线索（仅供参考，不改变主级别）：
 *  - highContrast：亮度两极聚集 → 疑似光棱双显混合。
 *  - blocky：8x8 分块边界不连续明显 → 疑似 RAC 分块调制。
 */
object StegDetect {

    data class Result(
        val hasMagic: Boolean,       // LSB 魔数
        val payloadBytes: Int?,      // 载荷长度
        val encrypted: Boolean?,     // 加密标志
        val lsbMatchRatio: Float,    // bit0 相邻相同率（全通道）
        val lsbMatchRatio1: Float,   // bit1 相邻相同率（全通道）
        val chRatioR: Float,         // bit0 R 通道相同率
        val chRatioG: Float,         // bit0 G 通道相同率
        val chRatioB: Float,         // bit0 B 通道相同率
        val alphaTransRatio: Float,  // 半透明像素占比
        val grayRatio: Float,        // RGB 相等像素占比
        val highContrast: Boolean,   // 光棱弱线索
        val blocky: Boolean          // RAC 分块弱线索
    )

    fun analyze(bitmap: Bitmap): Result {
        val w = bitmap.width
        val h = bitmap.height
        if (w < 4 || h < 4) return Result(false, null, null, 0.5f, 0.5f, 0f, 0f, 0f, 0f, 0f, false, false)

        val px = IntArray(w * h)
        bitmap.getPixels(px, 0, w, 0, 0, w, h)

        val magic = readBitInt(px, 0, 16)
        var payloadBytes: Int? = null
        var encrypted: Boolean? = null
        if (magic == 0x5354) { // 'S','T'
            val flags = readBitInt(px, 16, 8)
            val len = readBitInt(px, 24, 32)
            encrypted = (flags and 1) == 1
            payloadBytes = len
        }

        val bit0 = bitMatchRatio(px, w, h, 0)
        val bit1 = bitMatchRatio(px, w, h, 1)
        val (cr, cg, cb) = channelBit0(px, w, h)
        val (alphaRatio, grayRatio) = phantomStats(px)
        val (hc, blk) = weakSignals(px, w, h)

        return Result(
            hasMagic = magic == 0x5354,
            payloadBytes = payloadBytes,
            encrypted = encrypted,
            lsbMatchRatio = bit0,
            lsbMatchRatio1 = bit1,
            chRatioR = cr,
            chRatioG = cg,
            chRatioB = cb,
            alphaTransRatio = alphaRatio,
            grayRatio = grayRatio,
            highContrast = hc,
            blocky = blk
        )
    }

    /** 读位流第 from 位起共 n 位的整数（大端） */
    private fun readBitInt(px: IntArray, fromBit: Int, n: Int): Int {
        var v = 0
        repeat(n) { i ->
            val bitIdx = fromBit + i
            val unit = bitIdx / 2
            val pixelIdx = unit / 3
            val chan = CHAN[unit % 3]
            val pos = bitIdx % 2
            val cur = (px[pixelIdx] shr chan) and 0xFF
            val bit = if (pos == 0) cur and 1 else (cur shr 1) and 1
            v = (v shl 1) or bit
        }
        return v
    }

    /** 抽样相邻像素对在指定 bit 位深的相同率（RGB 全通道） */
    private fun bitMatchRatio(px: IntArray, w: Int, h: Int, bit: Int): Float {
        var match = 0
        var total = 0
        var sample = 0
        for (y in 0 until h step 3) {
            val rowBase = y * w
            for (x in 0 until w - 1 step 3) {
                val a = px[rowBase + x]
                val b = px[rowBase + x + 1]
                for (c in 0..2) {
                    val shift = CHAN[c]
                    val la = (a shr (shift + bit)) and 1
                    val lb = (b shr (shift + bit)) and 1
                    total++
                    if (la == lb) match++
                }
                sample++
                if (sample >= 12000) return match.toFloat() / total.toFloat()
            }
        }
        return if (total == 0) 0.5f else match.toFloat() / total.toFloat()
    }

    /** 抽样相邻像素对 bit0 的分通道相同率（R/G/B） */
    private fun channelBit0(px: IntArray, w: Int, h: Int): Triple<Float, Float, Float> {
        val m = IntArray(3)
        val c = IntArray(3)
        var sample = 0
        for (y in 0 until h step 3) {
            val rowBase = y * w
            for (x in 0 until w - 1 step 3) {
                val a = px[rowBase + x]
                val b = px[rowBase + x + 1]
                for (ch in 0..2) {
                    val shift = CHAN[ch]
                    val la = (a shr shift) and 1
                    val lb = (b shr shift) and 1
                    c[ch]++
                    if (la == lb) m[ch]++
                }
                sample++
                if (sample >= 12000) break
            }
            if (sample >= 12000) break
        }
        fun ratio(idx: Int): Float = if (c[idx] > 0) m[idx].toFloat() / c[idx] else 0.5f
        return Triple(ratio(0), ratio(1), ratio(2))
    }

    /** 抽样统计半透明占比与 RGB 相等（灰度）占比 */
    private fun phantomStats(px: IntArray): Pair<Float, Float> {
        var alphaTrans = 0
        var gray = 0
        var total = 0
        var i = 0
        while (i < px.size) {
            val p = px[i]
            val a = (p ushr 24) and 0xff
            val r = (p shr 16) and 0xff
            val g = (p shr 8) and 0xff
            val b = p and 0xff
            if (a in 1..254) alphaTrans++
            if (r == g && g == b) gray++
            total++
            i += 4
            if (total >= 6000) break
        }
        if (total == 0) return 0f to 0f
        return alphaTrans.toFloat() / total to gray.toFloat() / total
    }

    /** 弱线索：光棱亮度两极聚集 + RAC 分块痕迹 */
    private fun weakSignals(px: IntArray, w: Int, h: Int): Pair<Boolean, Boolean> {
        var dark = 0
        var bright = 0
        var total2 = 0
        var i = 0
        while (i < px.size) {
            val p = px[i]
            val L = 0.299 * (p shr 16 and 0xff) + 0.587 * (p shr 8 and 0xff) + 0.114 * (p and 0xff)
            if (L < 64f) dark++
            else if (L > 191f) bright++
            total2++
            i += 3
            if (total2 >= 4000) break
        }
        val darkRatio = if (total2 > 0) dark.toFloat() / total2 else 0f
        val brightRatio = if (total2 > 0) bright.toFloat() / total2 else 0f
        val highContrast = darkRatio > 0.28f && brightRatio > 0.28f

        val bs = 8
        var eSum = 0.0
        var eCnt = 0
        var iSum = 0.0
        var iCnt = 0
        var row = 0
        while (row < h && row + 1 < h && eCnt < 12000) {
            val base = row * w
            var col = 0
            while (col < w - 1) {
                val a = px[base + col]
                val b = px[base + col + 1]
                val d = Math.abs(lum(a) - lum(b))
                if ((col % bs) == bs - 1) { eSum += d; eCnt++ } else { iSum += d; iCnt++ }
                col++
            }
            row += 3
        }
        val eAvg = if (eCnt > 0) eSum / eCnt else 0.0
        val iAvg = if (iCnt > 0) iSum / iCnt else 0.0
        val blocky = eCnt > 60 && iAvg > 0.0 && eAvg > iAvg * 1.35

        return highContrast to blocky
    }

    private fun lum(p: Int): Double =
        0.299 * (p shr 16 and 0xff) + 0.587 * (p shr 8 and 0xff) + 0.114 * (p and 0xff)

    private val CHAN = intArrayOf(16, 8, 0)
}
