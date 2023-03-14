package com.example.lab1

import android.opengl.GLES20
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

// number of coordinates per vertex in this array
const val COORDS_PER_VERTEX = 3
val triangleCoords = floatArrayOf(     // in counterclockwise order:
    0.0f, 0.622008459f, 0.0f, // top

    -0.5f, -0.311004243f, 0.0f,    // bottom left

    0.5f, -0.311004243f, 0.0f,      // bottom right
)

val triangleColors = floatArrayOf(     // in counterclockwise order:
    1.0f, 0.0f, 0.0f, 1.0f, // top

    0.0f, 1.0f, 0.0f, 1.0f, // bottom left

    0.0f, 0.0f, 1.0f, 1.0f // bottom right
)

class Triangle {

    private val vertexShaderCode =
        """#version 300 es
            in vec4 vPosition;
            in vec4 color;
            out vec4 vColor;
            void main() {
                vColor = color;
                gl_Position = vPosition;
            }""".trimIndent()

    private val fragmentShaderCode =
        """#version 300 es
           precision mediump float;
           in vec4 vColor;
           out vec4 fragColor;
           void main() {
                fragColor = vColor;
           }""".trimIndent()

    fun loadShader(type: Int, shaderCode: String): Int {
        val id = GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }

        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(id, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            Log.e("[Shaders]", "Shader compilation error: " + GLES20.glGetShaderInfoLog(id))
//                Log.e("Shader Source : ", GLES30.glGetShaderSource(shader))
        }

        return id
    }

    private var mProgram: Int

    init {

        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
    }

    private var vertexBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(triangleCoords.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(triangleCoords)
                position(0)
            }
        }

    private var colorBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(triangleColors.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(triangleColors)
                position(0)
            }
        }

    private var positionHandle: Int = 0
    private var mColorHandle: Int = 0

    private val vertexCount: Int = triangleCoords.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    fun draw() {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram)

        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition").also {
            GLES20.glEnableVertexAttribArray(it)
            GLES20.glVertexAttribPointer(
                it,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                vertexBuffer
            )
        }

        mColorHandle = GLES20.glGetAttribLocation(mProgram, "color").also {
            GLES20.glEnableVertexAttribArray(it)
            GLES20.glVertexAttribPointer(
                it,
                4,
                GLES20.GL_FLOAT,
                false,
                4*4,
                colorBuffer
            )
        }
        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle)

    }
}