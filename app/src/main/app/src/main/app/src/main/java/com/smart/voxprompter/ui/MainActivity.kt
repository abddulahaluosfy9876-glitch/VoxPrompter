package com.example.voxprompter

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast

class MainActivity : Activity() {

    private var prompterInput: EditText? = null
    private var textScrollView: ScrollView? = null
    private var actionButton: Button? = null
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // فرض الخلفية السوداء مباشرة
        window.setBackgroundDrawable(ColorDrawable(Color.BLACK))

        // بناء الواجهة برمجياً بشكل مبسط وخفيف جداً
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
            setPadding(30, 30, 30, 30)
        }

        val topButtonsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 2f
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 130).apply { bottomMargin = 15 }
        }

        val importBtn = Button(this).apply {
            text = "استيراد ملف TXT 📂"
            setBackgroundColor(Color.parseColor("#00695C"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply { rightMargin = 10 }
            setOnClickListener { Toast.makeText(this@MainActivity, "ميزة الاستيراد جاهزة للتفعيل", Toast.LENGTH_SHORT).show() }
        }
        topButtonsLayout.addView(importBtn)

        val pasteBtn = Button(this).apply {
            text = "لصق النص 📋"
            setBackgroundColor(Color.parseColor("#37474F"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            setOnClickListener { Toast.makeText(this@MainActivity, "ميزة اللصق جاهزة للتفعيل", Toast.LENGTH_SHORT).show() }
        }
        topButtonsLayout.addView(pasteBtn)
        mainLayout.addView(topButtonsLayout)

        textScrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f).apply { bottomMargin = 20 }
            setBackgroundColor(Color.parseColor("#121212"))
        }

        prompterInput = EditText(this).apply {
            hint = "مرحباً بك في VoxPrompter المستقر! اكتب أو الصق نصك هنا... 🤖✨"
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            textSize = 22f
            gravity = Gravity.TOP or Gravity.START
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(25, 25, 25, 100)
        }
        textScrollView?.addView(prompterInput)
        mainLayout.addView(textScrollView)

        val bottomButtonsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 4f
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 140)
        }

        val resetBtn = Button(this).apply {
            text = "البداية ↩️"
            setBackgroundColor(Color.parseColor("#455A64"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply { rightMargin = 10 }
            setOnClickListener {
                textScrollView?.smoothScrollTo(0, 0)
                Toast.makeText(this@MainActivity, "تم إعادة النص للأعلى", Toast.LENGTH_SHORT).show()
            }
        }
        bottomButtonsLayout.addView(resetBtn)

        actionButton = Button(this).apply {
            text = "ابدأ تسجيل الصوت والتحرك الذكي 🎙️"
            setBackgroundColor(Color.parseColor("#00776B"))
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 3f)
            setOnClickListener {
                isRecording = !isRecording
                if (isRecording) {
                    text = "إيقاف وحفظ ومشاركة الصوت 📤"
                    setBackgroundColor(Color.parseColor("#C62828"))
                } else {
                    text = "ابدأ تسجيل الصوت والتحرك الذكي 🎙️"
                    setBackgroundColor(Color.parseColor("#00776B"))
                }
            }
        }
        bottomButtonsLayout.addView(actionButton)
        mainLayout.addView(bottomButtonsLayout)

        setContentView(mainLayout)
    }
}
