package com.example.labb2

import android.opengl.GLES20
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

// number of coordinates per vertex in this array
const val COORDS_PER_VERTEX = 3
val cubeCoords = floatArrayOf(
     -0.5f, -0.5f, 0.5f ,  -0.5f, 0.5f, 0.5f ,  0.5f, 0.5f, 0.5f  ,
     0.5f, 0.5f, 0.5f , 0.5f, -0.5f, 0.5f  ,  -0.5f, -0.5f, 0.5f ,
     -0.5f, -0.5f, -0.5f ,  0.5f, 0.5f, -0.5f ,  -0.5f, 0.5f, -0.5f ,
     0.5f, 0.5f, -0.5f , -0.5f, -0.5f, -0.5f ,   0.5f, -0.5f, -0.5f ,

     -0.5f, +0.5f, -0.5f  ,  -0.5f, +0.5f, +0.5f  ,  +0.5f, +0.5f, +0.5f  ,
     +0.5f, +0.5f, +0.5f  ,  +0.5f, +0.5f, -0.5f  ,  -0.5f, +0.5f, -0.5f  ,
     -0.5f, -0.5f, -0.5f  ,  +0.5f, -0.5f, +0.5f  ,  -0.5f, -0.5f, +0.5f  ,
     +0.5f, -0.5f, +0.5f  ,  -0.5f, -0.5f, -0.5f  ,  +0.5f, -0.5f, -0.5f  ,

     +0.5f, -0.5f, -0.5f  ,  +0.5f, -0.5f, +0.5f  ,  +0.5f, +0.5f, +0.5f  ,
     +0.5f, +0.5f, +0.5f  ,  +0.5f, +0.5f, -0.5f  ,  +0.5f, -0.5f, -0.5f  ,
     -0.5f, -0.5f, -0.5f  ,  -0.5f, +0.5f, +0.5f  ,  -0.5f, -0.5f, +0.5f  ,
     -0.5f, +0.5f, +0.5f  ,  -0.5f, -0.5f, -0.5f  ,  -0.5f, +0.5f, -0.5f  ,   // bottom right
)

class Cube {

    private val vertexShaderCode =
        """#version 300 es
            in vec3 vvPosition;
            uniform mat4 uMVPMatrix;
            out vec3 vPosition;
            void main() {
                vPosition = vvPosition;
                // Захардкодим углы поворота
                float x_angle = 1.0;
                float y_angle = 1.0;
        
                // Поворачиваем вершину
                vec3 position = vvPosition * mat3(
                    1.0, 0.0, 0.0,
                    0.0, cos(x_angle), -sin(x_angle),
                    0.0, sin(x_angle), cos(x_angle)
                ) * mat3(
                    cos(y_angle), 0.0, sin(y_angle),
                    0.0, 1.0, 0.0,
                    -sin(y_angle), 0.0, cos(y_angle)
                );
                gl_Position = vec4(position, 1.0);
                gl_Position = uMVPMatrix * gl_Position;
            }""".trimIndent()

    private val fragmentShaderCode =
        """#version 300 es
           precision mediump float;
           in vec3 vPosition;
           out vec4 color;
           void main() {
                color = vec4(0.1, 0.5, 0.7, 1);
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
        ByteBuffer.allocateDirect(cubeCoords.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(cubeCoords)
                position(0)
            }
        }

    private var positionHandle: Int = 0
    private var vPMatrixHandle: Int = 0

    private val vertexCount: Int = cubeCoords.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    fun draw(mvpMatrix: FloatArray) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram)

        positionHandle = GLES20.glGetAttribLocation(mProgram, "vvPosition").also {
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

        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix").also { matrixHandle ->
            GLES20.glUniformMatrix4fv(matrixHandle, 1, false, mvpMatrix, 0)
        }

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle)

    }
}