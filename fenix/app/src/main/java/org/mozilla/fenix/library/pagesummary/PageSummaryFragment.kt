/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.library.pagesummary

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.launch
import org.mozilla.fenix.R
import org.mozilla.fenix.components.FenixSnackbar
import org.mozilla.fenix.compose.Favicon
import org.mozilla.fenix.theme.FirefoxTheme

/**
 * fragment for displaying page summary.
 */
class PageSummaryFragment : Fragment() {
    private val args by navArgs<PageSummaryFragmentArgs>()
    private val viewModel: PageSummaryViewModel by viewModels()

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val navController by lazy { findNavController() }
        return ComposeView(requireContext()).apply {
            setContent {
                val scope = rememberCoroutineScope()
                val loadSummary: () -> Unit = {
                    scope.launch { viewModel.startLoading(args.url) }
                }
                FirefoxTheme {
                    Scaffold(
                        topBar = {
                            PageSummaryTopBar(
                                summaryUrl = args.url,
                                loadSummary = loadSummary,
                                navUpAction = { navController.navigateUp() },
                            )
                        },
                        content = {
                            PageSummaryUi(
                                rootView = this,
                                loadSummary = loadSummary,
                                summaryText = viewModel.summaryText,
                                summaryState = viewModel.summaryState,
                            )
                        },
                        backgroundColor = FirefoxTheme.colors.layer2,
                    )
                }
            }
        }
    }
}

@Composable
private fun PageSummaryUi(
    rootView: View,
    loadSummary: () -> Unit,
    summaryText: State<String>,
    summaryState: State<LoadState>,
) {
    LaunchedEffect(key1 = Unit) { loadSummary() }
    when (summaryState.value) {
        LoadState.Loading -> FenixLinearProgressIndicator()
        LoadState.Error -> FenixSnackbar.make(
            view = rootView,
            duration = FenixSnackbar.LENGTH_INDEFINITE,
            isDisplayedWithBrowserToolbar = false,
        )
            .setText(stringResource(id = R.string.something_went_wrong))
            .show()
        else -> Summary(textSummary = summaryText)
    }
}

@Composable
private fun FenixLinearProgressIndicator() {
    LinearProgressIndicator(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.Transparent,
        color = FirefoxTheme.colors.gradientStart,
    )
}

@Composable
private fun Summary(textSummary: State<String>) {
    LazyColumn(
        modifier = Modifier
            .padding(dimensionResource(id = R.dimen.mozac_browser_toolbar_menu_padding)),
    ) {
        item {
            Text(
                text = textSummary.value,
                color = FirefoxTheme.colors.textPrimary,
                style = FirefoxTheme.typography.body1,
            )
        }
    }

}

@Composable
private fun PageSummaryTopBar(
    summaryUrl: String,
    navUpAction: () -> Unit,
    loadSummary: () -> Unit,
) {
    TopAppBar(
        backgroundColor = FirefoxTheme.colors.layer1,
        elevation = dimensionResource(id = R.dimen.mozac_browser_toolbar_elevation),
    ) {
        IconButton(onClick = navUpAction) {
            Icon(
                painter = painterResource(id = R.drawable.mozac_ic_back),
                contentDescription = stringResource(id = R.string.browser_menu_back),
                tint = FirefoxTheme.colors.textPrimary,
            )
        }
        Favicon(
            url = summaryUrl,
            size = 24.dp,
            modifier = Modifier.align(Alignment.CenterVertically),
        )
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.8f)
                .padding(horizontal = dimensionResource(id = R.dimen.mozac_browser_toolbar_menu_padding)),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(id = R.string.page_summary),
                style = FirefoxTheme.typography.headline7,
                color = FirefoxTheme.colors.textPrimary,
            )
            Text(
                text = summaryUrl,
                style = FirefoxTheme.typography.caption,
                color = FirefoxTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = loadSummary) {
            Icon(
                painter = painterResource(id = R.drawable.mozac_ic_refresh),
                contentDescription = stringResource(id = R.string.browser_menu_refresh),
                tint = FirefoxTheme.colors.textPrimary,
            )
        }
    }
}
