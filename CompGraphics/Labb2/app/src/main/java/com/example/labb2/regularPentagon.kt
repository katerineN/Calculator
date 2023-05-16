package com.example.labb2

import android.opengl.GLES20
import android.util.Log
import java.lang.Math.cos
import java.lang.Math.sin
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

const val COORDS_PER_VERTEX_PENT = 2
// number of coordinates per vertex in this array
var pentagonCoords = floatArrayOf(
    0.0f,  0.0f,     // top left
    0.0f,  0.0f,     // bottom left
    0.0f,  0.0f,      // bottom right
    0.0f,  0.0f,
    0.0f,  0.0f,
)

class regularPentagon {
    private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3) // order to draw vertices

    private val vertexShaderCode = """
        #version 300 es
        uniform mat4 uMVPMatrix;
        in vec2 vvPosition;
        void main(){
            gl_Position = vec4(vvPosition, 0.0, 1.0);
            gl_Position = uMVPMatrix * gl_Position;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        #version 300 es
        precision mediump float;
        out vec4 color;
        void main(){
            color = vec4(0.0, 1.0, 0.0, 1.0);
        }
    """.trimIndent()

    fun initVertex(){
        var angleIncrement = 360.0f / 5f
        angleIncrement *= (3.14f / 180.0f)
        var angle = 0.0f
        val radius = 1.0f
        for (k in 0..9) {
            if (k % 2 == 0)
                pentagonCoords[k] = radius * kotlin.math.cos(angle)
            else pentagonCoords[k] = radius * kotlin.math.sin(angle)
            println(pentagonCoords[k])
            angle += angleIncrement
        }
    }

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
        ByteBuffer.allocateDirect(pentagonCoords.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(pentagonCoords)
                position(0)
            }
        }

    // initialize byte buffer for the draw list
    private val drawListBuffer: ShortBuffer =
        // (# of coordinate values * 2 bytes per short)
        ByteBuffer.allocateDirect(drawOrder.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(drawOrder)
                position(0)
            }
        }

    private var positionHandle: Int = 0
    // Use to access and set the view transformation
    private var vPMatrixHandle: Int = 0


    private val vertexStride: Int = COORDS_PER_VERTEX_PENT * 4 // 4 bytes per vertex

    fun draw(mvpMatrix: FloatArray) {
        initVertex()
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
        GLES20.glDrawArrays(GLES20.GL_POLYGON_OFFSET_FILL, 0, 5)

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle)

    }
}