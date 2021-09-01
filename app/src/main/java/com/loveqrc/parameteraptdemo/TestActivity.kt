package com.loveqrc.parameteraptdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.loveqrc.annotation.Parameter

class TestActivity : AppCompatActivity() {

    @Parameter
    lateinit var user: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        TestActivityParameter().loadParameter(this)
        Log.e("TestActivity", "user:$user")
    }
}