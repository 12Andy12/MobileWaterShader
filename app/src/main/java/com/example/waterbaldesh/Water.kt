package com.example.waterbaldesh

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import android.os.SystemClock
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.cos
import kotlin.math.sin

class Water (private val context: Context) {
    private var programId = 0

    private val stride = 2 * Float.SIZE_BYTES

    private val mModelMatrix = FloatArray(16)

    private var positionLocation: Int = 0
    private var textureLocation: Int = 0
    private var colorLocation: Int = 0
    private var textureUnitLocation: Int = 0
    private var timeLocation: Int = 0

    private lateinit var indexBuffer: ShortBuffer
    private lateinit var textureBuffer: FloatBuffer
    private lateinit var vertexBuffer: FloatBuffer

    private val color = floatArrayOf(
        0.0f, 0.0f, 0.0f, 1.0f
    )

    private lateinit var vertexDataSphere: FloatBuffer

    private val mMatrix = FloatArray(16)

    private var backgroundTexture = 0

    private val TIME: Long = 10000

    private var sphereVertexCount = 0

    init {
        createProgram()
        GLES20.glUseProgram(programId)
        getLocations()
        prepareTextures()
        prepareData()
    }

    private fun createProgram(){
        val vertexShaderId =
            ShaderUtils.createShader(context, GLES20.GL_VERTEX_SHADER, R.raw.vertex_shader)
        val fragmentShaderId =
            ShaderUtils.createShader(context, GLES20.GL_FRAGMENT_SHADER, R.raw.fragment_shader)
        programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId)
    }
    private fun getLocations() {
        positionLocation = GLES20.glGetAttribLocation(programId, "aPosition")
        textureLocation = GLES20.glGetAttribLocation(programId, "aTexCoord")
        colorLocation = GLES20.glGetUniformLocation(programId, "vColor")
        textureUnitLocation = GLES20.glGetUniformLocation(programId, "texSampler")
        timeLocation = GLES20.glGetUniformLocation(programId, "sysTime")
    }

    private fun prepareTextures()
    {
        backgroundTexture = TextureUtils.loadTexture(context, R.drawable.background)
    }


    private fun prepareData()
    {
        var x = -6f
        var y = -6f
        var width = 12f
        var height = 12f

        var drawOrder = shortArrayOf(0, 1, 2, 2, 3, 0)

        val coords = floatArrayOf(
            x, y,
            x + width, y,
            x + width, y + height,
            x, y + height
        )

        val texCoords = floatArrayOf(
            0f, 0f,
            1f, 0f,
            1f, 1f,
            0f, 1f
        )

        vertexBuffer = ByteBuffer.allocateDirect(coords.size * Float.SIZE_BYTES).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(coords)
                position(0)
            }
        }

        textureBuffer = ByteBuffer.allocateDirect(texCoords.size * Float.SIZE_BYTES).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(texCoords)
                position(0)
            }
        }

        indexBuffer = ByteBuffer.allocateDirect(drawOrder.size * Short.SIZE_BYTES).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(drawOrder)
                position(0)
            }
        }

    }

    private fun bindData()
    {
        GLES20.glVertexAttribPointer(
            positionLocation,
            2,
            GLES20.GL_FLOAT,
            false,
            stride,
            vertexBuffer
        )

        GLES20.glVertexAttribPointer(
            textureLocation,
            2,
            GLES20.GL_FLOAT,
            false,
            stride,
            textureBuffer
        )

        GLES20.glEnableVertexAttribArray(positionLocation)
        GLES20.glEnableVertexAttribArray(textureLocation)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, backgroundTexture)

        GLES20.glUniform1i(textureUnitLocation, 0)

        GLES20.glUniform4fv(colorLocation, 1, color, 0)

        val time = (SystemClock.uptimeMillis() / 50) % 1000

        GLES20.glUniform1f(timeLocation, time.toFloat())
    }

    public fun bindMatrix(
        mViewMatrix: FloatArray,
        mProjectionMatrix: FloatArray
    ) {
        Matrix.multiplyMM(mMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMatrix, 0, mProjectionMatrix, 0, mMatrix, 0);
//        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, mMatrix, 0)
        GLES20.glGetUniformLocation(programId, "uMVPMatrix").also {
            GLES20.glUniformMatrix4fv(it, 1, false, mMatrix, 0)
        }
    }

    public fun draw(
        mViewMatrix: FloatArray,
        mProjectionMatrix: FloatArray)
    {
        GLES20.glUseProgram(programId)
        Matrix.setIdentityM(mModelMatrix, 0);
        bindData()
        bindMatrix(mViewMatrix, mProjectionMatrix)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, indexBuffer)
    }
}