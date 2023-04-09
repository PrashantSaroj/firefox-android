package org.mozilla.fenix.library.pagesummary

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * view model for driving page summary ui.
 */
class PageSummaryViewModel : ViewModel() {
    private val summaryRepo = PageSummaryRepository()

    /**
     * track loading state of page summary.
     */
    val summaryState = mutableStateOf(LoadState.Init)

    /**
     * track text state of page summary.
     */
    val summaryText = mutableStateOf("")

    suspend fun startLoading(summaryUrl: String) {
        withContext(Dispatchers.IO) {
            try {
                summaryState.value = LoadState.Loading
                summaryRepo.getPageSummary(summaryUrl)?.let {
                    summaryText.value = it
                }
                summaryState.value = LoadState.Success
            } catch (e: Exception) {
                summaryState.value = LoadState.Error
            }
        }
    }
}

enum class LoadState {
    Init,
    Loading,
    Success,
    Error
}
