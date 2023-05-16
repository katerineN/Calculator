package com.example.labb2

import android.opengl.GLES20
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

const val COORDS_PER_VERTEX_SQUARE = 2
// number of coordinates per vertex in this array
var squareCoords = floatArrayOf(
    -0.5f,  0.5f,     // top left
    -0.5f, -0.5f,     // bottom left
    0.5f, -0.5f,      // bottom right
    0.5f,  0.5f,     // top right
)


class Square {

    private val vertexShaderCode = """
        #version 300 es
        in vec2 vvPosition;
        uniform mat4 uMVPMatrix;
        out vec2 vPosition;
        void main(){
            vPosition = vvPosition;
            gl_Position = vec4(vvPosition, 0.0, 1.0);
            gl_Position = uMVPMatrix * gl_Position;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        #version 300 es
        precision mediump float;
        in vec2 vPosition;
        out vec4 color;
        void main(){
            color = vec4(1.0, 0.0, 0.0, 1.0);
            float k = 20.0;
            int sum = int((vPosition.x-1.0)*k);
            if (sum % 2 == 0)
                color = vec4(1.0, 1.0, 1.0, 1.0);
            else
                color = vec4(0.0, 0.0, 1.0, 1.0);
        }
    """.trimIndent()

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

    // initialize vertex byte buffer for shape coordinates
    private var vertexBuffer: FloatBuffer =
        // (# of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(squareCoords.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(squareCoords)
                position(0)
            }
        }

    private var positionHandle: Int = 0
    // Use to access and set the view transformation
    private var vPMatrixHandle: Int = 0


    private val vertexStride: Int = COORDS_PER_VERTEX_SQUARE * 4 // 4 bytes per vertex

    fun draw(mvpMatrix: FloatArray) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram)

        positionHandle = GLES20.glGetAttribLocation(mProgram, "vvPosition").also {
            GLES20.glEnableVertexAttribArray(it)
            GLES20.glVertexAttribPointer(
                it,
                2,
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
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle)

    }
}