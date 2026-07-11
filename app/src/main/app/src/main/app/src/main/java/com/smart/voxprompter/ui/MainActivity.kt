package com.example.voxprompter

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // المتغيرات البرمجية للتحكم في النص والسرعة
    private var fontSize = 28f
    private var scrollSpeed = 5 // القيمة الافتراضية للسرعة
    private var isScrolling = false

    private lateinit var scrollView: ScrollView
    private lateinit var prompterTextView: TextView
    private lateinit var btnPlayPause: Button
    private lateinit var tvSpeedValue: TextView
    private lateinit var tvSizeValue: TextView

    private val scrollHandler = Handler(Looper.getMainLooper())
    private val scrollRunnable = object : Runnable {
        override fun run() {
            if (isScrolling) {
                // تمرير الشاشة لأسفل بناءً على السرعة المحددة
                scrollView.smoothScrollBy(0, scrollSpeed)
                // إعادة التشغيل بشكل دوري للحصول على تمرير ناعم
                scrollHandler.postDelayed(this, 30)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. الحاوية الرئيسية للتطبيق (ترتيب رأسي - ثيم غامق)
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121212")) // أسود مريح للعين
            weightSum = 10f
        }

        // 2. حاوية النص القابلة للتمرير (ScrollView)
        scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                8.5f // تأخذ 85% من مساحة الشاشة
            )
            isVerticalScrollBarEnabled = false
        }

        // 3. نص الـ Teleprompter الترحيبي (قابل للتغيير لاحقاً)
        prompterTextView = TextView(this).apply {
            layoutParams = ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(40, 100, 40, 500) // هوامش علوية وسفلية مريحة للقراءة
            }
            text = "مرحباً بك في VoxPrompter!\n\nهذا النص مخصص للتمرير التلقائي. يمكنك الآن اختبار ميزة التحكم في حجم الخط وسرعة التمرير عبر لوحة التحكم السفلية.\n\nالخط مصمم ليكون واضحاً ومريحاً للعين أثناء الإلقاء أمام الكاميرا. يمكنك الضغط على زر التشغيل لبدء التمرير التلقائي، واستخدام أزرار (+) و (-) لضبط الأبعاد بما يناسبك تماماً أثناء التحدث الفعلي."
            textSize = fontSize
            setTextColor(Color.parseColor("#E0E0E0")) // لون نص أبيض خفيف
            gravity = Gravity.CENTER_HORIZONTAL
            typeface = Typeface.DEFAULT_BOLD
            lineSpacingMultiplier = 1.3f // مسافة مريحة بين السطور
        }
        scrollView.addView(prompterTextView)
        mainLayout.addView(scrollView)

        // 4. لوحة التحكم السفلية (Control Panel)
        val controlPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1F1F1F")) // رمادي داكن للوحة
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.5f // تأخذ 15% من مساحة الشاشة
            )
            gravity = Gravity.CENTER
            setPadding(20, 10, 20, 10)
        }

        // صف التحكم بالخط والسرعة
        val controlsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // أزرار التحكم في الخط
        val btnSizeMinus = createControlButton("-A") { modifyFontSize(-4f) }
        tvSizeValue = createStatusTextView("خط: ${fontSize.toInt()}")
        val btnSizePlus = createControlButton("+A") { modifyFontSize(4f) }

        // زر التشغيل والإيقاف الرئيسي في المنتصف
        btnPlayPause = Button(this).apply {
            text = "▶ تشغيل"
            setBackgroundColor(Color.parseColor("#00E676")) // لون أخضر جذاب
            setTextColor(Color.BLACK)
            typeface = Typeface.DEFAULT_BOLD
            setOnClickListener { toggleScroll() }
            layoutParams = LinearLayout.LayoutParams(240, 120).apply {
                setMargins(40, 0, 40, 0)
            }
        }

        // أزرار التحكم في السرعة
        val btnSpeedMinus = createControlButton("-S") { modifySpeed(-1) }
        tvSpeedValue = createStatusTextView("سرعة: $scrollSpeed")
        val btnSpeedPlus = createControlButton("+S") { modifySpeed(1) }

        // تجميع العناصر داخل صف التحكم
        controlsRow.addView(btnSizeMinus)
        controlsRow.addView(tvSizeValue)
        controlsRow.addView(btnSizePlus)
        controlsRow.addView(btnPlayPause)
        controlsRow.addView(btnSpeedMinus)
        controlsRow.addView(tvSpeedValue)
        controlsRow.addView(btnSpeedPlus)

        controlPanel.addView(controlsRow)
        mainLayout.addView(controlPanel)

        setContentView(mainLayout)
    }

    // دالة تشغيل وإيقاف التمرير التلقائي
    private fun toggleScroll() {
        isScrolling = !isScrolling
        if (isScrolling) {
            btnPlayPause.text = "⏸ إيقاف"
            btnPlayPause.setBackgroundColor(Color.parseColor("#FF1744")) // أحمر عند الإيقاف
            btnPlayPause.setTextColor(Color.WHITE)
            scrollHandler.post(scrollRunnable)
        } else {
            btnPlayPause.text = "▶ تشغيل"
            btnPlayPause.setBackgroundColor(Color.parseColor("#00E676")) // أخضر عند التشغيل
            btnPlayPause.setTextColor(Color.BLACK)
            scrollHandler.removeCallbacks(scrollRunnable)
        }
    }

    // دالة تعديل حجم الخط
    private fun modifyFontSize(delta: Float) {
        fontSize = (fontSize + delta).coerceIn(16f, 60f)
        prompterTextView.textSize = fontSize
        tvSizeValue.text = "خط: ${fontSize.toInt()}"
    }

    // دالة تعديل السرعة
    private fun modifySpeed(delta: Int) {
        scrollSpeed = (scrollSpeed + delta).coerceIn(1, 20)
        tvSpeedValue.text = "سرعة: $scrollSpeed"
    }

    // دالة مساعدة لإنشاء أزرار التحكم الصغيرة
    private fun createControlButton(label: String, action: () -> Unit): Button {
        return Button(this).apply {
            text = label
            textSize = 14f
            setBackgroundColor(Color.parseColor("#333333"))
            setTextColor(Color.WHITE)
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(110, 100)
        }
    }

    // دالة مساعدة لإنشاء نصوص الحالة الرقمية
    private fun createStatusTextView(label: String): TextView {
        return TextView(this).apply {
            text = label
            textSize = 12f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(110, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scrollHandler.removeCallbacks(scrollRunnable) // منع تسريب الذاكرة عند إغلاق التطبيق
    }
}
