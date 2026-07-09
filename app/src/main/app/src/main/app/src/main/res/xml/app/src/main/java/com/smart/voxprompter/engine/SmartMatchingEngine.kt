package com.smart.voxprompter.engine

import kotlin.math.min

class SmartMatchingEngine(originalText: String) {

    // تقسيم النص الأصلي إلى مصفوفة من الكلمات وتصفية الفراغات
    private val originalWords = originalText.split(Regex("\\s+")).filter { it.isNotBlank() }
    
    // الاحتفاظ بآخر مؤشر لكلمة تم مطابقتها بنجاح لمنع القفز للخلف
    private var lastMatchedIndex = 0

    /**
     * تستقبل هذه الدالة الكلمات المنطوقة جزئياً (Partial Speech) 
     * وتعيد المؤشر (Index) الأنسب للكلمة الحالية في النص الأصلي.
     */
    fun findBestMatchingIndex(partialSpeech: String): Int {
        val speechWords = partialSpeech.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (speechWords.isEmpty() || originalWords.isEmpty()) return lastMatchedIndex

        // نأخذ آخر 3 كلمات نطقها المستخدم للبحث عن مطابقة سياقية
        val lookbackCount = min(3, speechWords.size)
        val recentSpeechTokens = speechWords.takeLast(lookbackCount)
        val speechString = recentSpeechTokens.joinToString(" ")

        var bestIndex = lastMatchedIndex
        var minDistance = Int.MAX_VALUE

        // البحث في نطاق 15 كلمة قادمة فقط لضمان السرعة ومنع التشتت وقفل الشاشة
        val searchEnd = min(originalWords.size, lastMatchedIndex + 15)
        
        for (i in lastMatchedIndex until searchEnd) {
            val windowSize = min(lookbackCount, originalWords.size - i)
            if (windowSize <= 0) break
            
            val originalWindow = originalWords.subList(i, i + windowSize)
            val originalString = originalWindow.joinToString(" ")

            // حساب نسبة الاختلاف بين المنطوق والمكتوب
            val distance = calculateLevenshteinDistance(speechString, originalString)

            if (distance < minDistance) {
                minDistance = distance
                bestIndex = i + windowSize - 1
            }
        }

        // الحارس الذكي: يمنع تماماً تعديل المؤشر للخلف إذا كرر المستخدم كلمة سابقة
        if (bestIndex > lastMatchedIndex) {
            lastMatchedIndex = bestIndex
        }
        
        return lastMatchedIndex
    }

    /**
     * خوارزمية قياس مسافة التعديل لمقارنة النصوص وتحمل أخطاء النطق البسيطة
     */
    private fun calculateLevenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = min(
                    min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[s1.length][s2.length]
    }
    
    /**
     * حساب نسبة التقدم الإجمالية في القراءة لتقديمها للمستخدم في الواجهة
     */
    fun getProgressPercentage(): Int {
        if (originalWords.isEmpty()) return 0
        return ((lastMatchedIndex.toFloat() / originalWords.size.toFloat()) * 100).toInt()
    }
}

