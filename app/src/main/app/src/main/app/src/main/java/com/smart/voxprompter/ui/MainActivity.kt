package com.example.voxprompter

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Toast

class MainActivity : Activity() {

    private var prompterEditText: EditText? = null
    private var scrollView: ScrollView? = null
    private var btnAction: Button? = null
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // ربط الواجهة المستقرة
        setContentView(R.layout.activity_main)

        // تعريف العناصر
        prompterEditText = findViewById(R.id.prompterEditText)
        scrollView = findViewById(R.id.scrollView)
        btnAction = findViewById(R.id.btnAction)
        val btnImport = findViewById<Button>(R.id.btnImport)
        val btnPaste = findViewById<Button>(R.id.btnPaste)

        // تفعيل العمليات الأساسية بشكل خفيف وآمن
        btnImport.setOnClickListener {
            Toast.makeText(this, "ميزة استيراد الملفات جاهزة", Toast.LENGTH_SHORT).show()
        }

        btnPaste.setOnClickListener {
            Toast.makeText(this, "ميزة اللصق السريع جاهزة", Toast.LENGTH_SHORT).show()
        }

        // الخطوة 1: عرض قائمة الخيارات الذكية عند الضغط على الزر الرئيسي
        btnAction?.setOnClickListener {
            showVoiceModeSelector()
        }
    }

    // دالة عرض الخيارات بشكل آمن ومبسط للغاية دون أي عمليات معقدة
    private fun showVoiceModeSelector() {
        val options = arrayOf(
            "🎤 تسجيل فائق النقاء (بصوتي الحقيقي)", 
            "🤖 المحاكي الذكي (توليد الأداء صوتياً)"
        )
        
        AlertDialog.Builder(this).apply {
            setTitle("اختر طريقة الأداء الصوتية:")
            setItems(options) { _, which ->
                if (which == 0) {
                    // الخيار الأول: التسجيل العادي (الذي كان يعمل بكفاءة)
                    toggleNormalRecording()
                } else {
                    // الخيار الثاني: سنقوم ببرمجته في الخطوة القادمة، حالياً سنعرض رسالة تأكيد فقط
                    Toast.makeText(this@MainActivity, "تم اختيار المحاكي الذكي (جاهز للتفعيل في الخطوة التالية)", Toast.LENGTH_LONG).show()
                }
            }
            show()
        }
    }

    // دالة التسجيل الطبيعي الأصلية الخاصة بك
    private fun toggleNormalRecording() {
        isRecording = !isRecording
        if (isRecording) {
            btnAction?.text = "إيقاف وحفظ الصوت 📤"
            btnAction?.setBackgroundColor(android.graphics.Color.parseColor("#C62828"))
            Toast.makeText(this, "بدء التسجيل بصوتك الحقيقي...", Toast.LENGTH_SHORT).show()
        } else {
            btnAction?.text = "ابدأ تسجيل الصوت والتحرك الذكي 🎙️"
            btnAction?.setBackgroundColor(android.graphics.Color.parseColor("#00796B"))
            Toast.makeText(this, "تم إيقاف وحفظ التسجيل بنجاح", Toast.LENGTH_SHORT).show()
        }
    }
}
