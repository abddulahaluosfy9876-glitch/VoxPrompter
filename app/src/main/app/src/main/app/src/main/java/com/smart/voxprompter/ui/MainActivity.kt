package com.example.voxprompter

import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // إنشاء حاوية الواجهة
        val rootLayout = FrameLayout(this).apply {
            setBackgroundColor(0xFFFFFFFF.toInt()) // خلفية بيضاء صافية
        }
        
        // إنشاء نص الترحيب
        val textView = TextView(this).apply {
            text = "Welcome to VoxPrompter!\nApplication Started Successfully."
            textSize = 24f
            setTextColor(0xFF000000.toInt()) // نص أسود صريح
            gravity = Gravity.CENTER
        }
        
        rootLayout.addView(textView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
        
        setContentView(rootLayout)
    }
}
