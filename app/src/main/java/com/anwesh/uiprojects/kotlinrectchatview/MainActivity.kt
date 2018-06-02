package com.anwesh.uiprojects.kotlinrectchatview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.rectchatbuttonview.RectChatView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RectChatView.create(this)
    }
}
