package org.lsposed.lspatch.ui.page

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Ballot
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.lsposed.lspatch.R
import org.lsposed.lspatch.config.Configs
import org.lsposed.lspatch.config.MyKeyStore
import org.lsposed.lspatch.share.LSPConfig
import org.lsposed.lspatch.ui.component.AnywhereDropdown
import org.lsposed.lspatch.ui.component.CenterTopBar
import org.lsposed.lspatch.ui.component.SettingsCategoryHeader
import org.lsposed.lspatch.ui.component.settings.SettingsItem
import org.lsposed.lspatch.ui.component.settings.SettingsSwitch
import org.lsposed.lspatch.ui.util.HtmlText
import org.lsposed.lspatch.ui.util.LocalSnackbarHost
import java.io.File
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun SettingsScreen() {
    val snackbarHost = LocalSnackbarHost.current
    Scaffold(
        topBar = { CenterTopBar(stringResource(BottomBarDestination.Settings.label)) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            AppearanceSection()
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            KeyStore()
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            DetailPatchLogs()
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            DataSection()
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            AboutSection()
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ============ Appearance ============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppearanceSection() {
    SettingsCategoryHeader(stringResource(R.string.settings_appearance))

    val themeLabel = when (Configs.themeMode) {
        "light" -> stringResource(R.string.settings_theme_light)
        "dark" -> stringResource(R.string.settings_theme_dark)
        else -> stringResource(R.string.settings_theme_system)
    }
    var expanded by remember { mutableStateOf(false) }
    AnywhereDropdown(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        onClick = { expanded = true },
        surface = {
            SettingsItem(
                icon = Icons.Outlined.Brightness6,
                title = stringResource(R.string.settings_theme_mode),
                desc = themeLabel
            )
        }
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.settings_theme_system)) },
            onClick = { Configs.themeMode = "system"; expanded = false }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.settings_theme_light)) },
            onClick = { Configs.themeMode = "light"; expanded = false }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.settings_theme_dark)) },
            onClick = { Configs.themeMode = "dark"; expanded = false }
        )
    }

    val dynamicSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    SettingsSwitch(
        modifier = Modifier.clickable {
            if (dynamicSupported) Configs.dynamicColor = !Configs.dynamicColor
        },
        enabled = dynamicSupported,
        checked = Configs.dynamicColor && dynamicSupported,
        icon = Icons.Outlined.Palette,
        title = stringResource(R.string.settings_dynamic_color),
        desc = stringResource(
            if (dynamicSupported) R.string.settings_dynamic_color_desc
            else R.string.settings_dynamic_color_unsupported
        )
    )
}

// ============ Keystore ============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KeyStore() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    SettingsCategoryHeader(stringResource(R.string.settings_keystore))

    AnywhereDropdown(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        onClick = { expanded = true },
        surface = {
            SettingsItem(
                icon = Icons.Outlined.Ballot,
                title = stringResource(R.string.settings_keystore),
                desc = stringResource(if (MyKeyStore.useDefault) R.string.settings_keystore_default else R.string.settings_keystore_custom)
            )
        }
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.settings_keystore_default)) },
            onClick = {
                scope.launch { MyKeyStore.reset() }
                expanded = false
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.settings_keystore_custom)) },
            onClick = {
                expanded = false
                showDialog = true
            }
        )
    }

    if (showDialog) {
        var wrongKeystore by rememberSaveable { mutableStateOf(false) }
        var wrongPassword by rememberSaveable { mutableStateOf(false) }
        var wrongAliasName by rememberSaveable { mutableStateOf(false) }
        var wrongAliasPassword by rememberSaveable { mutableStateOf(false) }

        var path by rememberSaveable { mutableStateOf("") }
        var password by rememberSaveable { mutableStateOf("") }
        var alias by rememberSaveable { mutableStateOf("") }
        var aliasPassword by rememberSaveable { mutableStateOf("") }

        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            context.contentResolver.openInputStream(uri).use { input ->
                MyKeyStore.tmpFile.outputStream().use { output ->
                    input?.copyTo(output)
                }
            }
            path = uri.path ?: ""
        }

        AlertDialog(
            onDismissRequest = { expanded = false; showDialog = false },
            confirmButton = {
                TextButton(
                    content = { Text(stringResource(android.R.string.ok)) },
                    onClick = {
                        wrongKeystore = false
                        wrongPassword = false
                        wrongAliasName = false
                        wrongAliasPassword = false

                        if (path.isEmpty()) {
                            wrongKeystore = true
                            return@TextButton
                        }
                        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
                        try {
                            MyKeyStore.tmpFile.inputStream().use { input ->
                                keyStore.load(input, password.toCharArray())
                            }
                        } catch (e: IOException) {
                            wrongKeystore = true
                            if (e.message == "KeyStore integrity check failed.") {
                                wrongPassword = true
                            }
                            return@TextButton
                        }
                        if (!keyStore.containsAlias(alias)) {
                            wrongAliasName = true
                            return@TextButton
                        }
                        try {
                            keyStore.getKey(alias, aliasPassword.toCharArray())
                        } catch (e: GeneralSecurityException) {
                            wrongAliasPassword = true
                            return@TextButton
                        }

                        scope.launch { MyKeyStore.setCustom(password, alias, aliasPassword) }
                        expanded = false
                        showDialog = false
                    })
            },
            dismissButton = {
                TextButton(
                    content = { Text(stringResource(android.R.string.cancel)) },
                    onClick = { expanded = false; showDialog = false }
                )
            },
            title = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.settings_keystore_dialog_title),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val interactionSource = remember { MutableInteractionSource() }
                    LaunchedEffect(interactionSource) {
                        interactionSource.interactions.collect { interaction ->
                            if (interaction is PressInteraction.Release) {
                                launcher.launch("*/*")
                            }
                        }
                    }

                    val wrongText = when {
                        wrongAliasPassword -> stringResource(R.string.settings_keystore_wrong_alias_password)
                        wrongAliasName -> stringResource(R.string.settings_keystore_wrong_alias)
                        wrongPassword -> stringResource(R.string.settings_keystore_wrong_password)
                        wrongKeystore -> stringResource(R.string.settings_keystore_wrong_keystore)
                        else -> null
                    }
                    Text(
                        modifier = Modifier.padding(bottom = 8.dp),
                        text = wrongText ?: stringResource(R.string.settings_keystore_desc),
                        color = if (wrongText != null) MaterialTheme.colorScheme.error else Color.Unspecified
                    )

                    OutlinedTextField(
                        value = path,
                        onValueChange = { path = it },
                        readOnly = true,
                        label = { Text(stringResource(R.string.settings_keystore_file)) },
                        placeholder = { Text(stringResource(R.string.settings_keystore_file)) },
                        singleLine = true,
                        isError = wrongKeystore,
                        interactionSource = interactionSource
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.settings_keystore_password)) },
                        singleLine = true,
                        isError = wrongPassword
                    )
                    OutlinedTextField(
                        value = alias,
                        onValueChange = { alias = it },
                        label = { Text(stringResource(R.string.settings_keystore_alias)) },
                        singleLine = true,
                        isError = wrongAliasName
                    )
                    OutlinedTextField(
                        value = aliasPassword,
                        onValueChange = { aliasPassword = it },
                        label = { Text(stringResource(R.string.settings_keystore_alias_password)) },
                        singleLine = true,
                        isError = wrongAliasPassword
                    )
                }
            }
        )
    }
}

// ============ Patch Logs ============

@Composable
private fun DetailPatchLogs() {
    SettingsCategoryHeader(stringResource(R.string.settings_patch))
    SettingsSwitch(
        modifier = Modifier.clickable { Configs.detailPatchLogs = !Configs.detailPatchLogs },
        checked = Configs.detailPatchLogs,
        icon = Icons.Outlined.BugReport,
        title = stringResource(R.string.settings_detail_patch_logs)
    )
}

// ============ Data ============

@Composable
private fun DataSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHost = LocalSnackbarHost.current

    SettingsCategoryHeader(stringResource(R.string.settings_data))

    // Storage directory
    val storageLabel = Configs.storageDirectory?.let {
        Uri.fromFile(File(it)).toString().takeLast(40).let { short ->
            if (short.length < Uri.fromFile(File(it)).toString().length) "…$short" else it
        }
    } ?: stringResource(R.string.settings_storage_default)

    val dirLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        runCatching {
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
        }
        Configs.storageDirectory = uri.toString()
    }

    SettingsItem(
        modifier = Modifier.clickable {
            runCatching {
                dirLauncher.launch(null)
            }.onFailure {
                Toast.makeText(context, it.message ?: "error", Toast.LENGTH_SHORT).show()
            }
        },
        icon = Icons.Outlined.Folder,
        title = stringResource(R.string.settings_storage_dir),
        desc = storageLabel
    )

    // Cache size + clear
    var cacheSize by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) {
        cacheSize = withContext(Dispatchers.IO) {
            formatSize(calcDirSize(context.cacheDir))
        }
    }
    SettingsItem(
        modifier = Modifier.clickable {
            scope.launch {
                val deleted = withContext(Dispatchers.IO) {
                    deleteRecursively(context.cacheDir)
                }
                cacheSize = formatSize(0)
                val msg = if (deleted) stringResource(R.string.settings_cache_cleared)
                else stringResource(R.string.settings_cache_clear_failed)
                snackbarHost.showSnackbar(msg)
            }
        },
        icon = Icons.Outlined.DeleteSweep,
        title = stringResource(R.string.settings_clear_cache),
        desc = cacheSize.ifEmpty { stringResource(R.string.settings_cache_calculating) }
    )
}

// ============ About ============

@Composable
private fun AboutSection() {
    val context = LocalContext.current

    SettingsCategoryHeader(stringResource(R.string.settings_about))

    SettingsItem(
        icon = Icons.Outlined.Info,
        title = stringResource(R.string.settings_version),
        desc = stringResource(
            R.string.settings_version_desc,
            LSPConfig.instance.VERSION_NAME,
            LSPConfig.instance.VERSION_CODE.toString(),
            LSPConfig.instance.CORE_VERSION_NAME,
            "API ${LSPConfig.instance.API_CODE}"
        )
    )

    val sourceUrl = "https://github.com/LQS686/LSPatch"
    SettingsItem(
        modifier = Modifier.clickable { openUrl(context, sourceUrl) },
        icon = Icons.Outlined.Info,
        title = stringResource(R.string.settings_source),
        desc = sourceUrl
    )

    val channelUrl = "https://t.me/LSPosed"
    val sourceHtml = stringResource(
        R.string.settings_support_html,
        "<b><a href=\"$sourceUrl\">GitHub</a></b>",
        "<b><a href=\"$channelUrl\">Telegram</a></b>"
    )
    Text(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        text = stringResource(R.string.home_description_ds)
    )
    HtmlText(
        modifier = Modifier.padding(horizontal = 16.dp),
        text = sourceHtml
    )
}

// ============ Helpers ============

private fun openUrl(context: Context, url: String) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }.getOrElse {
        if (it is ActivityNotFoundException) {
            Toast.makeText(context, url, Toast.LENGTH_LONG).show()
        }
    }
}

private fun calcDirSize(dir: File): Long {
    if (!dir.exists()) return 0L
    if (dir.isFile) return dir.length()
    var total = 0L
    dir.listFiles()?.forEach { total += calcDirSize(it) }
    return total
}

private fun deleteRecursively(file: File): Boolean {
    if (file.isDirectory) {
        file.listFiles()?.forEach { deleteRecursively(it) }
    }
    return file.delete() || !file.exists()
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${(bytes / 1024.0 * 100).roundToLong() / 100.0} KB"
        bytes < 1024 * 1024 * 1024 -> "${(bytes / 1024.0 / 1024.0 * 100).roundToLong() / 100.0} MB"
        else -> "${(bytes / 1024.0 / 1024.0 / 1024.0 * 100).roundToLong() / 100.0} GB"
    }
}
