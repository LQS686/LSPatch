@file:OptIn(ExperimentalMaterial3Api::class)

package org.lsposed.lspatch.ui.page

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import org.lsposed.lspatch.R
import org.lsposed.lspatch.ui.component.EmptyHint
import org.lsposed.lspatch.ui.component.LogLineItem
import org.lsposed.lspatch.ui.viewmodel.LogsViewModel

@Destination<RootGraph>
@Composable
fun LogsScreen() {
    val viewModel: LogsViewModel = viewModel()
    val context = LocalContext.current

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val levelFilter by viewModel.levelFilter.collectAsStateWithLifecycle()
    val filteredLogs = viewModel.filteredLogs

    var showFilterMenu by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (logs.isEmpty()) viewModel.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(BottomBarDestination.Logs.label)) },
                actions = {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Text(
                            text = if (levelFilter == "All") "ALL" else levelFilter,
                            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        viewModel.availableLevels().forEach { level ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = when (level) {
                                            "All" -> stringResource(R.string.logs_level_all)
                                            "V" -> "Verbose"
                                            "D" -> "Debug"
                                            "I" -> "Info"
                                            "W" -> "Warning"
                                            "E" -> "Error"
                                            else -> level
                                        }
                                    )
                                },
                                onClick = {
                                    viewModel.updateLevelFilter(level)
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            Icons.Outlined.Refresh,
                            contentDescription = stringResource(R.string.logs_refresh)
                        )
                    }
                    IconButton(
                        enabled = logs.isNotEmpty(),
                        onClick = {
                            context.startActivity(
                                Intent.createChooser(viewModel.buildShareIntent(), null)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    ) {
                        Icon(
                            Icons.Outlined.Share,
                            contentDescription = stringResource(R.string.logs_share)
                        )
                    }
                    IconButton(
                        enabled = logs.isNotEmpty(),
                        onClick = { viewModel.clear() }
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.logs_clear)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.CircularProgressIndicator()
                        androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))
                        Text(
                            text = stringResource(R.string.logs_loading),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                filteredLogs.isEmpty() -> {
                    EmptyHint(
                        text = if (logs.isEmpty()) stringResource(R.string.logs_empty)
                        else stringResource(R.string.logs_no_match)
                    )
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(
                            items = filteredLogs,
                            key = { it.hashCode() }
                        ) { line ->
                            LogLineItem(line)
                        }
                        item {
                            androidx.compose.foundation.layout.Spacer(Modifier.padding(bottom = 24.dp))
                        }
                    }
                }
            }
        }
    }
}
