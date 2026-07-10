package com.example.voxprompter

import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // إنشاء واجهة برمجية صافية ومباشرة بدون الاعتماد على أي ثيم أو مكتبة خارجية
        val rootLayout = FrameLayout(this).apply {
            setBackgroundColor(0xFFFFFFFF.toInt()) // خلفية بيضاء صريحة
        }
        
        val textView = TextView(this).apply {
            text = "Welcome to VoxPrompter!\nApplication Started Successfully."
            textSize = 22f
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
