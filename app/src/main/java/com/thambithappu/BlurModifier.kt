package com.thambithappu

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Blurs whatever this modifier is applied to. Real blur (RenderEffect) needs API 31+;
 * on older devices it falls back to a solid black cover instead - less pretty, but it
 * still fully hides the content, which is the actual requirement.
 */
fun Modifier.blurEffect(radiusPx: Float): Modifier =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.graphicsLayer {
            renderEffect = RenderEffect
                .createBlurEffect(radiusPx, radiusPx, Shader.TileMode.CLAMP)
                .asComposeRenderEffect()
        }
    } else {
        this.background(Color.Black)
    }
