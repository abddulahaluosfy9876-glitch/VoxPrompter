package com.smart.voxprompter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ربط الكود بملف التصميمactivity_main.xml ليعرض الشاشة للمستخدم
        setContentView(R.layout.activity_main)
    }
}
