package hu.nexio.hud.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import hu.nexio.hud.ui.theme.rememberNexioFont

/** A mod menü színei (Brawl Stars-szerű kék felület, 3D-s gombok). */
object ModColors {
    val BgTop = Color(0xFF071840)
    val BgBottom = Color(0xFF0B2A66)
    val Frame = Color(0xFF0E3FA8)
    val Header = Color(0xFF2E8DF2)
    val Body = Color(0xFF1B5FE0)
    val Face = Color(0xFF2E72EA)
    val FaceEdge = Color(0xFF1546B4)
    val Outline = Color(0xFF0B2F86)
    val Green = Color(0xFF2CD13C)
    val GreenEdge = Color(0xFF14861F)
    val Red = Color(0xFFE22626)
    val RedEdge = Color(0xFF8E1010)
    val OnFace = Color(0xFF22A84C)
    val OnEdge = Color(0xFF106B2C)
}

/** Fekete körvonalas szöveg (mint a játék gombfeliratai); rgb = true esetén animált RGB kitöltéssel. */
@Composable
fun OutlineText(
    text: String,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    rgb: Boolean = false,
) {
    val strokeW = with(LocalDensity.current) { fontSize.toPx() * 0.2f }
    val base = TextStyle(
        fontSize = fontSize,
        fontFamily = rememberNexioFont(),
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center,
    )
    val brush = if (rgb) rgbBrush() else null
    Box(modifier) {
        Text(
            text,
            style = base.copy(color = Color.Black, drawStyle = Stroke(width = strokeW, join = StrokeJoin.Round)),
            maxLines = 3,
        )
        Text(
            text,
            style = if (brush != null) base.copy(brush = brush) else base.copy(color = color),
            maxLines = 3,
        )
    }
}

/**
 * 3D-s mod menü gomb. A magasságot a hívó adja a modifierrel.
 * progress != null esetén a gomb háttere animáltan kitöltődik (töltés).
 */
@Composable
fun ModButton(
    modifier: Modifier,
    face: Color = ModColors.Face,
    edge: Color = ModColors.FaceEdge,
    enabled: Boolean = true,
    progress: Float? = null,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    val p by animateFloatAsState(progress ?: 0f, label = "modProgress")
    Box(modifier.alpha(if (enabled) 1f else 0.45f)) {
        Box(Modifier.matchParentSize().padding(top = 4.dp).background(edge, shape))
        Box(
            Modifier
                .matchParentSize()
                .padding(bottom = 4.dp)
                .clip(shape)
                .background(face)
                .border(1.dp, ModColors.Outline, shape)
                .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (progress != null) {
                Box(
                    Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .fillMaxWidth(p.coerceIn(0f, 1f))
                        .background(Color.White.copy(alpha = 0.28f)),
                )
            }
            content()
        }
    }
}
