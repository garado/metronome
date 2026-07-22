package dev.garado.metronome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thelightphone.sdk.ui.LightIcon
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.lightClickable

private val KEYPAD_ROWS = listOf(
    listOf("1", "2", "3"),
    listOf("4", "5", "6"),
    listOf("7", "8", "9"),
)

@Composable
fun NumericKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onSubmit: () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)) {
        KEYPAD_ROWS.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
            ) {
                row.forEach { digit ->
                    LightText(
                        text = digit,
                        variant = LightTextVariant.Heading,
                        modifier = Modifier.lightClickable { onDigit(digit) },
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        ) {
            LightIcon(
                icon = LightIcons.CLOSE,
                modifier = Modifier.lightClickable { onBackspace() },
            )
            LightText(
                text = "0",
                variant = LightTextVariant.Heading,
                modifier = Modifier.lightClickable { onDigit("0") },
            )
            LightIcon(
                icon = LightIcons.ACCEPT,
                modifier = Modifier.lightClickable { onSubmit() },
            )
        }
    }
}
