package dev.garado.metronome

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.annotation.DrawableRes

// LightBarButton.Icon renders its painter via a plain Image with no tint param
// (unlike LightIcon, which tints internally via Material3's Icon). This wraps
// a vector resource so it still follows the current theme's content color.
@Composable
fun tintedPainter(@DrawableRes id: Int, tint: Color): Painter {
    val base = painterResource(id)
    return remember(base, tint) { TintedPainter(base, tint) }
}

private class TintedPainter(private val inner: Painter, private val tint: Color) : Painter() {
    override val intrinsicSize: Size get() = inner.intrinsicSize

    override fun DrawScope.onDraw() {
        with(inner) {
            draw(size = size, colorFilter = ColorFilter.tint(tint))
        }
    }
}
