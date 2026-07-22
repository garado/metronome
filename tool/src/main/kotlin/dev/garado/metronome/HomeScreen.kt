package dev.garado.metronome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thelightphone.sdk.InitialScreen
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightBottomBar
import com.thelightphone.sdk.ui.LightIcon
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.lightClickable

private val TRANSPORT_ICON_SIZE = 40.dp
private val TRANSPORT_BAR_THICKNESS = 3.dp
private val TRANSPORT_BAR_LENGTH_FRACTION = 0.6f
private val SUBDIVISION_ICON_SIZE = 22.dp

@InitialScreen
class HomeScreen(sealedActivity: SealedLightActivity) : SimpleLightScreen<Unit>(sealedActivity) {

    @Composable
    override fun Content() {
        val bpm by MetronomeState.bpm.collectAsState()
        val isPlaying by MetronomeState.isPlaying.collectAsState()
        val subdivision by MetronomeState.subdivision.collectAsState()
        val timeSignature by MetronomeState.timeSignature.collectAsState()
        val themeColors by LightThemeController.colors.collectAsState()

        LightTheme(colors = themeColors) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightThemeTokens.colors.background),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    // BPM
                    LightText(
                        text = bpm.toString(),
                        variant = LightTextVariant.Title,
                        modifier = Modifier.lightClickable { navigateTo(::EditBpmScreen) },
                    )

                    // Play + pause
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(40.dp),
                        modifier = Modifier.padding(vertical = 24.dp),
                    ) {
                        MinusSymbol(
                            modifier = Modifier.lightClickable { MetronomeState.decrementBpm() },
                        )
                        LightIcon(
                            icon = if (isPlaying) LightIcons.PAUSE else LightIcons.PLAY,
                            modifier = Modifier
                                .size(30.dp)
                                .lightClickable { MetronomeState.togglePlaying() },
                        )
                        PlusSymbol(
                            modifier = Modifier.lightClickable { MetronomeState.incrementBpm() },
                        )
                    }


                    // Time division + note subdivision
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 16.dp, bottom = 36.dp),
                    ) {
                        LightText(
                            text = timeSignature.toString(),
                            variant = LightTextVariant.Copy,
                            modifier = Modifier.lightClickable { navigateTo(::EditTimeSignatureScreen) },
                        )
                        SubdivisionIndicator(
                            subdivision = subdivision,
                            modifier = Modifier
                                .padding(start = 20.dp)
                                .lightClickable { navigateTo(::SubdivisionsScreen) },
                        )
                    }
                }

                HomeBottomBar(
                    onSettingsClick = { navigateTo(::SettingsScreen) },
                )
            }
        }
    }
}

@Composable
private fun SubdivisionIndicator(subdivision: Subdivision, modifier: Modifier = Modifier) {
    val tint = LightThemeTokens.colors.content
    val (drawableId, superscript) = when (subdivision) {
        Subdivision.QUARTER -> R.drawable.ic_note_quarter to null
        Subdivision.EIGHTH -> R.drawable.ic_note to null
        Subdivision.SIXTEENTH -> R.drawable.ic_note_16th to null
        Subdivision.TRIPLET -> R.drawable.ic_note to "3"
        Subdivision.QUINTUPLET -> R.drawable.ic_note to "5"
        Subdivision.SEXTUPLET -> R.drawable.ic_note to "6"
    }

    Box(modifier = modifier) {
        Image(
            painter = tintedPainter(drawableId, tint),
            contentDescription = subdivision.label,
            modifier = Modifier.size(SUBDIVISION_ICON_SIZE),
        )
        if (superscript != null) {
            LightText(
                text = superscript,
                variant = LightTextVariant.Superfine,
                modifier = Modifier.align(Alignment.TopEnd).padding(start = 20.dp),
            )
        }
    }
}

@Composable
private fun MinusSymbol(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(TRANSPORT_ICON_SIZE),
        contentAlignment = Alignment.Center,
    ) {
        Bar(horizontal = true)
    }
}

@Composable
private fun PlusSymbol(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(TRANSPORT_ICON_SIZE),
        contentAlignment = Alignment.Center,
    ) {
        Bar(horizontal = true)
        Bar(horizontal = false)
    }
}

@Composable
private fun Bar(horizontal: Boolean) {
    Box(
        modifier = if (horizontal) {
            Modifier
                .fillMaxWidth(TRANSPORT_BAR_LENGTH_FRACTION)
                .height(TRANSPORT_BAR_THICKNESS)
        } else {
            Modifier
                .fillMaxHeight(TRANSPORT_BAR_LENGTH_FRACTION)
                .width(TRANSPORT_BAR_THICKNESS)
        }.background(LightThemeTokens.colors.content),
    )
}

@Composable
private fun HomeBottomBar(onSettingsClick: () -> Unit) {
    LightBottomBar(
        items = listOf(
            LightBarButton.Icon(
                painter = tintedPainter(R.drawable.ic_note, LightThemeTokens.colors.content),
                contentDescription = "Metronome",
                onClick = {},
            ),
            LightBarButton.LightIcon(
                icon = LightIcons.SETTINGS,
                contentDescription = "Settings",
                onClick = onSettingsClick,
            ),
        ),
    )
}
