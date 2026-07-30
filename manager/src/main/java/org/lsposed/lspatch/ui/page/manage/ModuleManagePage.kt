package org.lsposed.lspatch.ui.page.manage

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.lsposed.lspatch.R
import org.lsposed.lspatch.ui.component.AnywhereDropdown
import org.lsposed.lspatch.ui.component.AppItem
import org.lsposed.lspatch.ui.viewmodel.manage.ModuleManageViewModel
import org.lsposed.lspatch.util.LSPPackageManager

@Composable
fun ModuleManageBody() {
    val context = LocalContext.current
    val viewModel = viewModel<ModuleManageViewModel>()
    if (viewModel.appList.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.Extension,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = if (LSPPackageManager.appList.isEmpty()) stringResource(R.string.manage_loading)
                    else stringResource(R.string.manage_no_modules),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (LSPPackageManager.appList.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.manage_no_modules_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    } else {
        var searchText by remember { mutableStateOf("") }
        val filteredModuleList = if (searchText.isBlank()) viewModel.appList else viewModel.appList.filter {
            it.first.label.contains(searchText, ignoreCase = true) ||
                it.first.app.packageName.contains(searchText, ignoreCase = true)
        }
        Column(Modifier.fillMaxSize()) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text(stringResource(R.string.manage_search_modules)) },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { searchText = "" }) {
                            Icon(Icons.Filled.Close, null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )
            LazyColumn(Modifier.fillMaxHeight()) {
                items(
                    items = filteredModuleList,
                key = { it.first.app.packageName }
            ) {
                var expanded by remember { mutableStateOf(false) }
                val settingsIntent = remember { LSPPackageManager.getSettingsIntent(it.first.app.packageName) }
                AnywhereDropdown(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    onClick = { settingsIntent?.let { context.startActivity(it) } },
                    onLongClick = { expanded = true },
                    surface = {
                        AppItem(
                            icon = LSPPackageManager.getIcon(it.first),
                            label = it.first.label,
                            packageName = it.first.app.packageName,
                            additionalContent = {
                                Text(
                                    text = it.second.description,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = buildAnnotatedString {
                                        append(AnnotatedString("API", SpanStyle(color = MaterialTheme.colorScheme.secondary)))
                                        append("  ")
                                        append(it.second.api.toString())
                                    },
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Serif,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        )
                    }
                ) {
                    DropdownMenuItem(
                        text = { Text(text = it.first.label, color = MaterialTheme.colorScheme.primary) },
                        onClick = {}, enabled = false
                    )
                    if (settingsIntent != null) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.manage_module_settings)) },
                            onClick = { context.startActivity(settingsIntent) }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.manage_app_info)) },
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", it.first.app.packageName, null)
                            )
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
        }
    }
}
