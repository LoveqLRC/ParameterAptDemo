package com.loveqrc.parameteraptdemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.loveqrc.annotation.Parameter


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button).setOnClickListener {

            val intent = Intent(this, TestActivity::class.java)
            intent.putExtra("user","Rc在努力")
            startActivity(intent)
        }
    }
}