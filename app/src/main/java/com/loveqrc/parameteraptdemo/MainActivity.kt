package com.loveqrc.parameteraptdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.loveqrc.annotation.Parameter



class MainActivity : AppCompatActivity() {
    @Parameter
    var user: String? = null

    @Parameter
    var name: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}