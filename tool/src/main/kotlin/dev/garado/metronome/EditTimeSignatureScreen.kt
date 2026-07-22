package dev.garado.metronome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.lightClickable

private enum class TimeSignatureField { NUMERATOR, DENOMINATOR }

class EditTimeSignatureScreen(sealedActivity: SealedLightActivity) : SimpleLightScreen<Unit>(sealedActivity) {

    @Composable
    override fun Content() {
        val current by MetronomeState.timeSignature.collectAsState()
        val themeColors by LightThemeController.colors.collectAsState()

        var numerator by remember { mutableIntStateOf(current.numerator) }
        var denominator by remember { mutableIntStateOf(current.denominator) }
        var activeField by remember { mutableStateOf(TimeSignatureField.NUMERATOR) }

        fun submit() {
            MetronomeState.setTimeSignature(TimeSignature(numerator, denominator))
            goBack()
        }

        fun onDigit(digit: Int) {
            when (activeField) {
                TimeSignatureField.NUMERATOR -> {
                    numerator = digit
                    activeField = TimeSignatureField.DENOMINATOR
                }

                TimeSignatureField.DENOMINATOR -> {
                    denominator = digit
                    activeField = TimeSignatureField.NUMERATOR
                }
            }
        }

        LightTheme(colors = themeColors) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightThemeTokens.colors.background),
            ) {
                LightTopBar(center = LightTopBarCenter.Text("Time Signature"))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 8.dp),
                ) {
                    LightText(
                        text = numerator.toString(),
                        variant = LightTextVariant.Title,
                        underline = activeField == TimeSignatureField.NUMERATOR,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .lightClickable { activeField = TimeSignatureField.NUMERATOR },
                    )
                    LightText(text = "/", variant = LightTextVariant.Title)
                    LightText(
                        text = denominator.toString(),
                        variant = LightTextVariant.Title,
                        underline = activeField == TimeSignatureField.DENOMINATOR,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .lightClickable { activeField = TimeSignatureField.DENOMINATOR },
                    )
                }

                NumericKeypad(
                    onDigit = { digit -> onDigit(digit.toInt()) },
                    onBackspace = {},
                    onSubmit = { submit() },
                )
            }
        }
    }
}
