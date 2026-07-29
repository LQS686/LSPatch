package org.lsposed.lspatch.ui.viewmodel

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.lsposed.lspatch.lspApp
import org.lsposed.lspatch.util.LSPPackageManager

class RepoViewModel : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _localModules = MutableStateFlow<List<LSPPackageManager.AppInfo>>(emptyList())
    val localModules: StateFlow<List<LSPPackageManager.AppInfo>> = _localModules

    val sources: List<ModuleSource> = defaultSources

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            runCatching {
                if (LSPPackageManager.appList.isEmpty()) {
                    LSPPackageManager.fetchAppList()
                }
                _localModules.value = LSPPackageManager.appList.filter { it.isXposedModule }
            }
            _isRefreshing.value = false
        }
    }

    fun openLink(url: String) {
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            lspApp.startActivity(intent)
        }
    }
}

data class ModuleSource(
    val name: String,
    val description: String,
    val url: String
)

private val defaultSources = listOf(
    ModuleSource(
        name = "LSPosed Modules Repository",
        description = "Official LSPosed compatible modules index",
        url = "https://github.com/LSPosed/LSPosed/wiki/Modules"
    ),
    ModuleSource(
        name = "Xposed Module Repository (Androidacy)",
        description = "Community-curated Xposed / LSPosed modules",
        url = "https://www.androidacy.com/modules-repo/"
    ),
    ModuleSource(
        name = "GitHub Search: xposed-module topic",
        description = "Browse open-source Xposed modules on GitHub",
        url = "https://github.com/topics/xposed-module"
    )
)
