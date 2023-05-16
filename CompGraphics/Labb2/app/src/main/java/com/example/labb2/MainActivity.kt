package com.example.labb2

import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    private lateinit var gLView: GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        gLView = MyGLSurfaceView(this)
        setContentView(gLView)
    }
}