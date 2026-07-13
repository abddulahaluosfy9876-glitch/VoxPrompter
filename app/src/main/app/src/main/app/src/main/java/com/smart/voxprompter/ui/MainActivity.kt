package com.example.voxprompter

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // ربط الكود بملف الواجهة المضمون
        setContentView(R.layout.activity_main)

        // تفعيل زر التجربة للتأكد من استجابة التطبيق
        val testBtn = findViewById<Button>(R.id.testBtn)
        testBtn.setOnClickListener {
            Toast.makeText(this, "رائع! التطبيق يعمل ومستقر بنسبة 100% 🎉", Toast.LENGTH_LONG).show()
        }
    }
}
