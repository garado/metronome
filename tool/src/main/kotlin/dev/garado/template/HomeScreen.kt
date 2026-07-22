package dev.garado.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thelightphone.lp3Keyboard.ui.KeyboardOptions
import com.thelightphone.sdk.InitialScreen
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightBottomBar
import com.thelightphone.sdk.ui.LightIcon
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextField
import com.thelightphone.sdk.ui.LightTextInputEditor
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.lightClickable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class HomeTab { HELLO, WORLD, SETTINGS }

data class SettingsOption(val label: String, val enabled: Boolean)

class HomeScreenViewModel : LightViewModel<Unit>() {
    private val _selectedTab = MutableStateFlow(HomeTab.HELLO)
    val selectedTab: StateFlow<HomeTab> = _selectedTab.asStateFlow()

    private val _settingsOptions = MutableStateFlow(
        listOf(
            SettingsOption("Invert Colors", enabled = false),
        )
    )
    val settingsOptions: StateFlow<List<SettingsOption>> = _settingsOptions.asStateFlow()

    private val _displayName = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    private val _isEditingName = MutableStateFlow(false)
    val isEditingName: StateFlow<Boolean> = _isEditingName.asStateFlow()

    // LightTextInputEditor caches its embedded keyboard's ViewModel by editorKey;
    // bump this each time editing starts so a stale keyboard/TextFieldState pairing
    // from a previous session isnt reused
    private val _editSessionId = MutableStateFlow(0)
    val editSessionId: StateFlow<Int> = _editSessionId.asStateFlow()

    fun selectTab(tab: HomeTab) {
        _selectedTab.value = tab
    }

    fun toggleSetting(label: String) {
        _settingsOptions.value = _settingsOptions.value.map {
            if (it.label == label) it.copy(enabled = !it.enabled) else it
        }
    }

    fun startEditingName() {
        _editSessionId.value += 1
        _isEditingName.value = true
    }

    fun submitName(value: CharSequence) {
        _displayName.value = value.toString()
        _isEditingName.value = false
    }

    fun cancelEditingName() {
        _isEditingName.value = false
    }
}

@InitialScreen
class HomeScreen(sealedActivity: SealedLightActivity) : LightScreen<Unit, HomeScreenViewModel>(sealedActivity) {

    override val viewModelClass: Class<HomeScreenViewModel>
        get() = HomeScreenViewModel::class.java

    override fun createViewModel(): HomeScreenViewModel {
        return HomeScreenViewModel()
    }

    @Composable
    override fun Content() {
        val selectedTab by viewModel.selectedTab.collectAsState()
        val settingsOptions by viewModel.settingsOptions.collectAsState()
        val displayName by viewModel.displayName.collectAsState()
        val isEditingName by viewModel.isEditingName.collectAsState()
        val editSessionId by viewModel.editSessionId.collectAsState()
        val themeColors by LightThemeController.colors.collectAsState()

        LightTheme(colors = themeColors) {
            if (isEditingName) {
                val nameFieldState = rememberTextFieldState(displayName)
                val keyboardOptionsFlow = remember {
                    MutableStateFlow(KeyboardOptions(emptyList(), true, false, true))
                }

                LightTextInputEditor(
                    title = "Display Name",
                    state = nameFieldState,
                    onSubmit = { viewModel.submitName(it) },
                    onBack = { viewModel.cancelEditingName() },
                    keyboardOptionsFlow = keyboardOptionsFlow,
                    singleLine = true,
                    editorKey = editSessionId,
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LightThemeTokens.colors.background),
                ) {
                    if (selectedTab == HomeTab.SETTINGS) {
                        LightTopBar(center = LightTopBarCenter.Text("Settings"))
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 32.dp)
                            .padding(
                                top = if (selectedTab == HomeTab.SETTINGS) 0.dp else 16.dp,
                                bottom = 16.dp,
                            ),
                    ) {
                        when (selectedTab) {
                            HomeTab.HELLO -> HelloTabContent()
                            HomeTab.WORLD -> WorldTabContent()
                            HomeTab.SETTINGS -> SettingsTabContent(
                                options = settingsOptions,
                                displayName = displayName,
                                onToggle = viewModel::toggleSetting,
                                onAboutClick = { navigateTo(::AboutScreen) },
                                onEditName = { viewModel.startEditingName() },
                            )
                        }
                    }

                    HomeBottomBar(onSelectTab = viewModel::selectTab)
                }
            }
        }
    }
}

@Composable
private fun HelloTabContent() {
    LightText(text = "Hello", variant = LightTextVariant.Heading)
}

@Composable
private fun WorldTabContent() {
    LightText(text = "World", variant = LightTextVariant.Heading)
}

@Composable
private fun SettingsTabContent(
    options: List<SettingsOption>,
    displayName: String,
    onToggle: (String) -> Unit,
    onAboutClick: () -> Unit,
    onEditName: () -> Unit,
) {
    LazyColumn {
        item {
            LightTextField(
                label = "Display Name",
                value = displayName,
                placeholder = "Enter your name",
                onClick = onEditName,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }

        items(options) { option ->
            SettingsToggleRow(
                option = option,
                onClick = { onToggle(option.label) },
            )
        }

        item {
            SettingsNavigationRow(
                label = "About",
                onClick = onAboutClick,
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(option: SettingsOption, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .lightClickable(onClick = onClick)
            .padding(vertical = 12.dp),
    ) {
        LightIcon(
            icon = if (option.enabled) LightIcons.TOGGLE_STATE_ON else LightIcons.TOGGLE_STATE_OFF,
            modifier = Modifier.padding(end = 16.dp),
        )
        LightText(text = option.label, variant = LightTextVariant.Copy)
    }
}

@Composable
private fun SettingsNavigationRow(label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .lightClickable(onClick = onClick)
            .padding(vertical = 12.dp),
    ) {
        LightText(text = label, variant = LightTextVariant.Copy, modifier = Modifier.weight(1f))
        LightIcon(icon = LightIcons.ARROW_RIGHT)
    }
}

@Composable
private fun HomeBottomBar(onSelectTab: (HomeTab) -> Unit) {
    LightBottomBar(
        items = listOf(
            LightBarButton.LightIcon(
                icon = LightIcons.COMPOSE_MESSAGE,
                contentDescription = "Hello",
                onClick = { onSelectTab(HomeTab.HELLO) },
            ),
            LightBarButton.LightIcon(
                icon = LightIcons.MAP,
                contentDescription = "World",
                onClick = { onSelectTab(HomeTab.WORLD) },
            ),
            LightBarButton.LightIcon(
                icon = LightIcons.SETTINGS,
                contentDescription = "Settings",
                onClick = { onSelectTab(HomeTab.SETTINGS) },
            ),
        ),
    )
}
