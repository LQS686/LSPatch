package org.lsposed.lspatch.ui.page

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import org.lsposed.lspatch.R
import org.lsposed.lspatch.ui.component.CenterTopBar
import org.lsposed.lspatch.ui.component.EmptyHint
import org.lsposed.lspatch.ui.component.LocalModuleItem
import org.lsposed.lspatch.ui.component.SectionHeader
import org.lsposed.lspatch.ui.component.SourceItem
import org.lsposed.lspatch.ui.viewmodel.RepoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun RepoScreen() {
    val viewModel: RepoViewModel = viewModel()

    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val localModules by viewModel.localModules.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (localModules.isEmpty()) viewModel.refresh()
    }

    Scaffold(
        topBar = { CenterTopBar(stringResource(BottomBarDestination.Repo.label)) }
    ) { innerPadding ->
        PullToRefreshBox(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                item {
                    SectionHeader(
                        icon = Icons.Outlined.Apps,
                        title = stringResource(R.string.repo_local_modules)
                    )
                }
                if (localModules.isEmpty()) {
                    item {
                        EmptyHint(text = stringResource(R.string.repo_no_local_modules))
                    }
                } else {
                    items(
                        items = localModules,
                        key = { it.app.packageName }
                    ) { module ->
                        LocalModuleItem(module)
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }

                item {
                    SectionHeader(
                        icon = Icons.Outlined.Download,
                        title = stringResource(R.string.repo_sources),
                        subtitle = stringResource(R.string.repo_sources_desc)
                    )
                }
                items(
                    items = viewModel.sources,
                    key = { it.url }
                ) { source ->
                    SourceItem(source = source, onClick = { viewModel.openLink(source.url) })
                }

                item {
                    androidx.compose.foundation.layout.Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    )
                }
            }
        }
    }
}
