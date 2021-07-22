/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.showdetails.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.tivi.common.compose.ExpandingText
import app.tivi.common.compose.Layout
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.LogCompositions
import app.tivi.common.compose.bodyMarginSpacer
import app.tivi.common.compose.gutterSpacer
import app.tivi.common.compose.rememberFlowWithLifecycle
import app.tivi.common.compose.ui.AutoSizedCircularProgressIndicator
import app.tivi.common.compose.ui.Carousel
import app.tivi.common.compose.ui.ExpandableFloatingActionButton
import app.tivi.common.compose.ui.PosterCard
import app.tivi.common.compose.ui.SwipeDismissSnackbar
import app.tivi.common.compose.ui.drawForegroundGradientScrim
import app.tivi.common.compose.ui.foregroundColor
import app.tivi.common.imageloading.TrimTransparentEdgesTransformation
import app.tivi.data.entities.Episode
import app.tivi.data.entities.Genre
import app.tivi.data.entities.ImageType
import app.tivi.data.entities.Season
import app.tivi.data.entities.ShowStatus
import app.tivi.data.entities.ShowTmdbImage
import app.tivi.data.entities.TiviShow
import app.tivi.data.entities.TmdbImageEntity
import app.tivi.data.resultentities.EpisodeWithSeason
import app.tivi.data.resultentities.EpisodeWithWatches
import app.tivi.data.resultentities.RelatedShowEntryWithShow
import app.tivi.data.resultentities.SeasonWithEpisodesAndWatches
import app.tivi.data.resultentities.nextToAir
import app.tivi.data.resultentities.numberAired
import app.tivi.data.resultentities.numberAiredToWatch
import app.tivi.data.resultentities.numberToAir
import app.tivi.data.resultentities.numberWatched
import app.tivi.data.views.FollowedShowsWatchStats
import coil.compose.rememberImagePainter
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.LocalScaffoldPadding
import com.google.accompanist.insets.ui.TopAppBar
import kotlinx.coroutines.flow.collect
import org.threeten.bp.OffsetDateTime

@Composable
fun ShowDetails(
    navigateUp: () -> Unit,
    openShowDetails: (showId: Long) -> Unit,
    openEpisodeDetails: (episodeId: Long) -> Unit,
) {
    ShowDetails(
        viewModel = hiltViewModel(),
        navigateUp = navigateUp,
        openShowDetails = openShowDetails,
        openEpisodeDetails = openEpisodeDetails,
    )
}

@Composable
internal fun ShowDetails(
    viewModel: ShowDetailsViewModel,
    navigateUp: () -> Unit,
    openShowDetails: (showId: Long) -> Unit,
    openEpisodeDetails: (episodeId: Long) -> Unit,
) {
    val viewState by rememberFlowWithLifecycle(viewModel.state)
        .collectAsState(initial = ShowDetailsViewState.Empty)

    ShowDetails(viewState = viewState) { action ->
        when (action) {
            ShowDetailsAction.NavigateUp -> navigateUp()
            else -> viewModel.submitAction(action)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is OpenShowUiEffect -> openShowDetails(effect.showId)
                is OpenEpisodeUiEffect -> openEpisodeDetails(effect.episodeId)
                else -> Unit // TODO: any remaining ui effects need to be passed down to the UI
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun ShowDetails(
    viewState: ShowDetailsViewState,
    actioner: (ShowDetailsAction) -> Unit,
) = Box(modifier = Modifier.fillMaxSize()) {
    LogCompositions("ShowDetails")

    val listState = rememberLazyListState()

    Surface(Modifier.fillMaxSize()) {
        ShowDetailsScrollingContent(
            show = viewState.show,
            posterImage = viewState.posterImage,
            backdropImage = viewState.backdropImage,
            relatedShows = viewState.relatedShows,
            nextEpisodeToWatch = viewState.nextEpisodeToWatch,
            seasons = viewState.seasons,
            expandedSeasonIds = viewState.expandedSeasonIds,
            watchStats = viewState.watchStats,
            listState = listState,
            actioner = actioner,
            modifier = Modifier.fillMaxSize()
        )
    }

    var appBarHeight by remember { mutableStateOf(0) }
    val showAppBarBackground by remember {
        derivedStateOf {
            val visibleItemsInfo = listState.layoutInfo.visibleItemsInfo
            when {
                visibleItemsInfo.isEmpty() -> false
                appBarHeight <= 0 -> false
                else -> {
                    val firstVisibleItem = visibleItemsInfo[0]
                    when {
                        // If the first visible item is > 0, we want to show the app bar background
                        firstVisibleItem.index > 0 -> true
                        // If the first item is visible, only show the app bar background once the only
                        // remaining part of the item is <= the app bar
                        else -> firstVisibleItem.size + firstVisibleItem.offset <= appBarHeight
                    }
                }
            }
        }
    }

    ShowDetailsAppBar(
        title = viewState.show.title,
        isRefreshing = viewState.refreshing,
        showAppBarBackground = showAppBarBackground,
        actioner = actioner,
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { appBarHeight = it.height }
    )

    val snackbarHostState = remember { SnackbarHostState() }

    Column(
        Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
            snackbar = {
                SwipeDismissSnackbar(
                    data = it,
                    onDismiss = { actioner(ShowDetailsAction.ClearError) }
                )
            },
            modifier = Modifier
                .padding(horizontal = Layout.bodyMargin)
                .fillMaxWidth()
        )

        val expanded by remember {
            derivedStateOf { listState.firstVisibleItemIndex > 0 }
        }

        ToggleShowFollowFloatingActionButton(
            isFollowed = viewState.isFollowed,
            expanded = { expanded },
            onClick = { actioner(ShowDetailsAction.FollowShowToggleAction) },
            modifier = Modifier
                .align(Alignment.End)
                .padding(Layout.bodyMargin)
                .navigationBarsPadding(bottom = false)
                .padding(LocalScaffoldPadding.current)
        )
    }

    LaunchedEffect(viewState.refreshError) {
        viewState.refreshError?.let { error ->
            snackbarHostState.showSnackbar(error.message)
        }
    }
}

@Composable
private fun ShowDetailsScrollingContent(
    show: TiviShow,
    posterImage: TmdbImageEntity?,
    backdropImage: TmdbImageEntity?,
    relatedShows: List<RelatedShowEntryWithShow>,
    nextEpisodeToWatch: EpisodeWithSeason?,
    seasons: List<SeasonWithEpisodesAndWatches>,
    expandedSeasonIds: Set<Long>,
    watchStats: FollowedShowsWatchStats?,
    listState: LazyListState,
    actioner: (ShowDetailsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    LogCompositions("ShowDetailsScrollingContent")

    LazyColumn(
        state = listState,
        modifier = modifier
    ) {
        item {
            BackdropImage(
                backdropImage = backdropImage,
                showTitle = show.title ?: "",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10)
                    .clipToBounds()
                    .offset {
                        IntOffset(
                            x = 0,
                            y = if (listState.firstVisibleItemIndex == 0) {
                                listState.firstVisibleItemScrollOffset / 2
                            } else 0
                        )
                    }
            )
        }

        bodyMarginSpacer()

        item {
            PosterInfoRow(
                show = show,
                posterImage = posterImage,
                modifier = Modifier.fillMaxWidth()
            )
        }

        gutterSpacer()

        item {
            Header(stringResource(R.string.details_about))
        }

        if (show.summary != null) {
            item {
                ExpandingText(
                    text = show.summary!!,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Layout.bodyMargin, vertical = Layout.gutter)
                )
            }
        }

        if (show.genres.isNotEmpty()) {
            item {
                Genres(show.genres)
            }
        }

        if (nextEpisodeToWatch?.episode != null && nextEpisodeToWatch.season != null) {
            gutterSpacer()

            item {
                Header(stringResource(id = R.string.details_next_episode_to_watch))
            }
            item {
                NextEpisodeToWatch(
                    season = nextEpisodeToWatch.season!!,
                    episode = nextEpisodeToWatch.episode!!,
                    onClick = {
                        actioner(ShowDetailsAction.OpenEpisodeDetails(nextEpisodeToWatch.episode!!.id))
                    }
                )
            }
        }

        if (relatedShows.isNotEmpty()) {
            gutterSpacer()

            item {
                Header(stringResource(R.string.details_related))
            }
            item {
                RelatedShows(
                    related = relatedShows,
                    actioner = actioner,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp)
                )
            }
        }

        if (watchStats != null) {
            gutterSpacer()

            item {
                Header(stringResource(R.string.details_view_stats))
            }
            item {
                WatchStats(watchStats.watchedEpisodeCount, watchStats.episodeCount)
            }
        }

        if (seasons.isNotEmpty()) {
            gutterSpacer()

            item {
                Header(stringResource(R.string.show_details_seasons))
            }

            items(seasons) { season ->
                SeasonWithEpisodesRow(
                    season = season.season,
                    episodes = season.episodes,
                    expanded = season.season.id in expandedSeasonIds,
                    actioner = actioner,
                    modifier = Modifier.fillParentMaxWidth(),
                )
            }
        }

        // Spacer to push up content from under the FloatingActionButton
        item {
            val height = LocalScaffoldPadding.current.calculateBottomPadding() + 56.dp + 32.dp
            Spacer(Modifier.height(height))
        }
    }
}

@Composable
private fun PosterInfoRow(
    show: TiviShow,
    posterImage: TmdbImageEntity?,
    modifier: Modifier = Modifier,
) {
    Row(modifier.padding(horizontal = Layout.bodyMargin)) {
        Image(
            painter = rememberImagePainter(posterImage) {
                crossfade(true)
            },
            contentDescription = stringResource(R.string.cd_show_poster, show.title ?: ""),
            modifier = Modifier
                .weight(1f)
                .aspectRatio(2 / 3f)
                .clip(MaterialTheme.shapes.medium),
            alignment = Alignment.TopStart,
        )

        InfoPanels(
            show = show,
            modifier = Modifier
                .weight(1f)
                .padding(start = Layout.gutter)
        )
    }
}

@Composable
private fun BackdropImage(
    backdropImage: TmdbImageEntity?,
    showTitle: String,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier) {
        Box {
            if (backdropImage != null) {
                Image(
                    painter = rememberImagePainter(backdropImage) {
                        crossfade(true)
                    },
                    contentDescription = stringResource(R.string.cd_show_poster),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .drawForegroundGradientScrim(Color.Black.copy(alpha = 0.7f)),
                )
            }

            val originalTextStyle = MaterialTheme.typography.h4

            val shadowSize = with(LocalDensity.current) {
                originalTextStyle.fontSize.toPx() / 16
            }

            Text(
                text = showTitle,
                style = originalTextStyle.copy(
                    color = Color.White,
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(shadowSize, shadowSize),
                        blurRadius = 0.1f,
                    )
                ),
                fontWeight = FontWeight.Thin,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(Layout.bodyMargin)
            )
        }
        // TODO show a placeholder if null
    }
}

@Composable
private fun NetworkInfoPanel(
    networkName: String,
    modifier: Modifier = Modifier,
    networkLogoPath: String? = null,
) {
    Column(modifier) {
        Text(
            text = stringResource(R.string.network_title),
            style = MaterialTheme.typography.subtitle2
        )

        Spacer(Modifier.height(4.dp))

        if (networkLogoPath != null) {
            val tmdbImage = remember(networkLogoPath) {
                ShowTmdbImage(path = networkLogoPath, type = ImageType.LOGO, showId = 0)
            }

            Image(
                painter = rememberImagePainter(tmdbImage) {
                    crossfade(true)
                    transformations(TrimTransparentEdgesTransformation)
                },
                contentDescription = stringResource(R.string.cd_network_logo),
                modifier = Modifier.sizeIn(maxWidth = 72.dp, maxHeight = 32.dp),
                alignment = Alignment.TopStart,
                contentScale = ContentScale.Fit,
                colorFilter = when {
                    isSystemInDarkTheme() -> ColorFilter.tint(foregroundColor())
                    else -> null
                },
            )
        } else {
            Text(
                text = networkName,
                style = MaterialTheme.typography.body2
            )
        }
    }
}

@Composable
private fun RuntimeInfoPanel(
    runtime: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            text = stringResource(R.string.runtime_title),
            style = MaterialTheme.typography.subtitle2
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.minutes_format, runtime),
            style = MaterialTheme.typography.body2
        )
    }
}

@Composable
private fun ShowStatusPanel(
    showStatus: ShowStatus,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            text = stringResource(R.string.status_title),
            style = MaterialTheme.typography.subtitle2
        )

        Spacer(Modifier.height(4.dp))

        val textCreator = LocalTiviTextCreator.current
        Text(
            text = textCreator.showStatusText(showStatus).toString(),
            style = MaterialTheme.typography.body2
        )
    }
}

@Composable
private fun AirsInfoPanel(
    show: TiviShow,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            text = stringResource(R.string.airs_title),
            style = MaterialTheme.typography.subtitle2
        )

        Spacer(Modifier.height(4.dp))

        val textCreator = LocalTiviTextCreator.current
        Text(
            text = textCreator.airsText(show).toString(),
            style = MaterialTheme.typography.body2
        )
    }
}

@Composable
private fun CertificateInfoPanel(
    certification: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            text = stringResource(R.string.certificate_title),
            style = MaterialTheme.typography.subtitle2
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = certification,
            style = MaterialTheme.typography.body2,
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colors.onSurface,
                    shape = RoundedCornerShape(2.dp)
                )
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun TraktRatingInfoPanel(
    rating: Float,
    votes: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            text = stringResource(R.string.trakt_rating_title),
            style = MaterialTheme.typography.subtitle2
        )

        Spacer(Modifier.height(4.dp))

        Row {
            Image(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(MaterialTheme.colors.secondaryVariant),
                modifier = Modifier.size(32.dp),
            )

            Spacer(Modifier.width(4.dp))

            Column {
                Text(
                    text = stringResource(
                        R.string.trakt_rating_text,
                        rating * 10f
                    ),
                    style = MaterialTheme.typography.body2
                )

                Text(
                    text = stringResource(
                        R.string.trakt_rating_votes,
                        votes / 1000f
                    ),
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
}

@Composable
private fun Header(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Layout.bodyMargin, vertical = Layout.gutter)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.subtitle1
        )
    }
}

@Composable
private fun Genres(genres: List<Genre>) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Layout.bodyMargin, vertical = Layout.gutter)
    ) {
        val textCreator = LocalTiviTextCreator.current
        Text(
            textCreator.genreString(genres).toString(),
            style = MaterialTheme.typography.body2
        )
    }
}

@Composable
private fun RelatedShows(
    related: List<RelatedShowEntryWithShow>,
    actioner: (ShowDetailsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    LogCompositions("RelatedShows")

    Carousel(
        items = related,
        contentPadding = PaddingValues(horizontal = Layout.bodyMargin, vertical = Layout.gutter),
        itemSpacing = 4.dp,
        modifier = modifier
    ) { item, padding ->
        PosterCard(
            show = item.show,
            poster = item.poster,
            onClick = { actioner(ShowDetailsAction.OpenShowDetails(item.show.id)) },
            modifier = Modifier
                .padding(padding)
                .fillParentMaxHeight()
                .aspectRatio(2 / 3f)
        )
    }
}

@Composable
private fun NextEpisodeToWatch(
    season: Season,
    episode: Episode,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .wrapContentHeight()
            .clickable(onClick = onClick)
            .padding(horizontal = Layout.bodyMargin, vertical = Layout.gutter)
    ) {
        val textCreator = LocalTiviTextCreator.current

        Text(
            textCreator.seasonEpisodeTitleText(season, episode),
            style = MaterialTheme.typography.caption
        )

        Spacer(Modifier.height(4.dp))

        Text(
            episode.title ?: stringResource(R.string.episode_title_fallback, episode.number!!),
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
private fun InfoPanels(
    show: TiviShow,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        mainAxisSpacing = Layout.gutter,
        crossAxisSpacing = Layout.gutter,
        modifier = modifier,
    ) {
        if (show.traktRating != null) {
            TraktRatingInfoPanel(show.traktRating!!, show.traktVotes ?: 0)
        }
        if (show.network != null) {
            NetworkInfoPanel(networkName = show.network!!, networkLogoPath = show.networkLogoPath)
        }
        if (show.status != null) {
            ShowStatusPanel(show.status!!)
        }
        if (show.certification != null) {
            CertificateInfoPanel(show.certification!!)
        }
        if (show.runtime != null) {
            RuntimeInfoPanel(show.runtime!!)
        }
        if (show.airsDay != null && show.airsTime != null && show.airsTimeZone != null) {
            AirsInfoPanel(show)
        }
    }
}

@Composable
private fun WatchStats(
    watchedEpisodeCount: Int,
    episodeCount: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Layout.bodyMargin, vertical = Layout.gutter)
    ) {
        LinearProgressIndicator(
            progress = when {
                episodeCount > 0 -> watchedEpisodeCount / episodeCount.toFloat()
                else -> 0f
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(Layout.gutter))

        val textCreator = LocalTiviTextCreator.current

        // TODO: Do something better with CharSequences containing markup/spans
        Text(
            text = "${textCreator.followedShowEpisodeWatchStatus(watchedEpisodeCount, episodeCount)}",
            style = MaterialTheme.typography.body2
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun SeasonWithEpisodesRow(
    season: Season,
    episodes: List<EpisodeWithWatches>,
    expanded: Boolean,
    actioner: (ShowDetailsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val elevation by animateDpAsState(if (expanded) 2.dp else 0.dp)
    Surface(
        elevation = elevation,
        modifier = modifier
    ) {
        Column(Modifier.fillMaxWidth()) {
            SeasonRow(
                season = season,
                episodesAired = episodes.numberAired,
                episodesWatched = episodes.numberWatched,
                episodesToWatch = episodes.numberAiredToWatch,
                episodesToAir = episodes.numberToAir,
                nextToAirDate = episodes.nextToAir?.firstAired,
                actioner = actioner,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !season.ignored) {
                        actioner(ShowDetailsAction.ChangeSeasonExpandedAction(season.id, !expanded))
                    }
            )

            // Ideally each EpisodeWithWatchesRow would be in a different item {}, but there
            // are currently 2 issues for that:
            // #1: AnimatedVisibility currently crashes in Lazy*: b/170287733
            // #2: Can't use a Surface across different items: b/170472398
            // So instead we bundle the items in an inner Column, within a single item.
            episodes.forEach { episodeEntry ->
                AnimatedVisibility(visible = expanded) {
                    EpisodeWithWatchesRow(
                        episode = episodeEntry.episode,
                        isWatched = episodeEntry.isWatched,
                        hasPending = episodeEntry.hasPending,
                        onlyPendingDeletes = episodeEntry.onlyPendingDeletes,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                actioner(ShowDetailsAction.OpenEpisodeDetails(episodeEntry.episode.id))
                            }
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Divider()
            }
        }
    }
}

@Composable
private fun SeasonRow(
    season: Season,
    episodesAired: Int,
    episodesWatched: Int,
    episodesToWatch: Int,
    episodesToAir: Int,
    actioner: (ShowDetailsAction) -> Unit,
    modifier: Modifier = Modifier,
    nextToAirDate: OffsetDateTime? = null,
) {
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .wrapContentHeight(Alignment.CenterVertically)
            .padding(horizontal = Layout.bodyMargin, vertical = Layout.gutter)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            val textCreator = LocalTiviTextCreator.current

            val contentAlpha = when {
                season.ignored -> ContentAlpha.disabled
                else -> ContentAlpha.high
            }
            CompositionLocalProvider(LocalContentAlpha provides contentAlpha) {
                Text(
                    text = season.title
                        ?: stringResource(R.string.season_title_fallback, season.number!!),
                    style = MaterialTheme.typography.body1
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = textCreator.seasonSummaryText(
                        episodesWatched,
                        episodesToWatch,
                        episodesToAir,
                        nextToAirDate
                    ).toString(),
                    style = MaterialTheme.typography.caption
                )
            }

            if (!season.ignored && episodesAired > 0) {
                Spacer(Modifier.height(4.dp))

                LinearProgressIndicator(
                    episodesWatched / episodesAired.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        var showMenu by remember { mutableStateOf(false) }

        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    Icons.Default.MoreVert,
                    stringResource(R.string.cd_open_overflow)
                )
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            if (season.ignored) {
                DropdownMenuItem(
                    onClick = { actioner(ShowDetailsAction.ChangeSeasonFollowedAction(season.id, true)) }
                ) {
                    Text(text = stringResource(id = R.string.popup_season_follow))
                }
            } else {
                DropdownMenuItem(
                    onClick = { actioner(ShowDetailsAction.ChangeSeasonFollowedAction(season.id, false)) }
                ) {
                    Text(text = stringResource(id = R.string.popup_season_ignore))
                }
            }

            // Season number starts from 1, rather than 0
            if (season.number ?: -100 >= 2) {
                DropdownMenuItem(
                    onClick = { actioner(ShowDetailsAction.UnfollowPreviousSeasonsFollowedAction(season.id)) }
                ) {
                    Text(text = stringResource(id = R.string.popup_season_ignore_previous))
                }
            }

            if (episodesWatched > 0) {
                DropdownMenuItem(
                    onClick = { actioner(ShowDetailsAction.MarkSeasonUnwatchedAction(season.id)) }
                ) {
                    Text(text = stringResource(id = R.string.popup_season_mark_all_unwatched))
                }
            }

            if (episodesWatched < episodesAired) {
                if (episodesToAir == 0) {
                    DropdownMenuItem(
                        onClick = { actioner(ShowDetailsAction.MarkSeasonWatchedAction(season.id)) }
                    ) {
                        Text(text = stringResource(id = R.string.popup_season_mark_watched_all))
                    }
                } else {
                    DropdownMenuItem(
                        onClick = { actioner(ShowDetailsAction.MarkSeasonWatchedAction(season.id, onlyAired = true)) }
                    ) {
                        Text(text = stringResource(id = R.string.popup_season_mark_watched_aired))
                    }
                }
            }
        }
    }
}

@Composable
private fun EpisodeWithWatchesRow(
    episode: Episode,
    isWatched: Boolean,
    hasPending: Boolean,
    onlyPendingDeletes: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .wrapContentHeight(Alignment.CenterVertically)
            .padding(horizontal = Layout.bodyMargin, vertical = Layout.gutter)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val textCreator = LocalTiviTextCreator.current

            Text(
                text = textCreator.episodeNumberText(episode).toString(),
                style = MaterialTheme.typography.caption
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = episode.title
                    ?: stringResource(R.string.episode_title_fallback, episode.number!!),
                style = MaterialTheme.typography.body2
            )
        }

        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            var needSpacer = false
            if (hasPending) {
                Icon(
                    painter = painterResource(R.drawable.ic_cloud_upload),
                    contentDescription = stringResource(R.string.cd_episode_syncing),
                    modifier = Modifier.align(Alignment.CenterVertically),
                )
                needSpacer = true
            }
            if (isWatched) {
                if (needSpacer) Spacer(Modifier.width(4.dp))

                Icon(
                    painter = painterResource(
                        when {
                            onlyPendingDeletes -> R.drawable.ic_visibility_off
                            else -> R.drawable.ic_visibility
                        }
                    ),
                    contentDescription = when {
                        onlyPendingDeletes -> stringResource(R.string.cd_episode_deleted)
                        else -> stringResource(R.string.cd_episode_watched)
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
private fun ShowDetailsAppBar(
    title: String?,
    isRefreshing: Boolean,
    showAppBarBackground: Boolean,
    actioner: (ShowDetailsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    LogCompositions("ShowDetailsAppBar")

    val backgroundColor by animateColorAsState(
        targetValue = when {
            showAppBarBackground -> MaterialTheme.colors.surface
            else -> Color.Transparent
        },
        animationSpec = spring(),
    )

    val elevation by animateDpAsState(
        targetValue = when {
            showAppBarBackground -> 4.dp
            else -> 0.dp
        },
        animationSpec = spring(),
    )

    TopAppBar(
        title = {
            Crossfade(showAppBarBackground && title != null) { show ->
                if (show) Text(text = title!!)
            }
        },
        contentPadding = rememberInsetsPaddingValues(
            LocalWindowInsets.current.systemBars,
            applyBottom = false
        ),
        navigationIcon = {
            IconButton(onClick = { actioner(ShowDetailsAction.NavigateUp) }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.cd_navigate_up)
                )
            }
        },
        actions = {
            if (isRefreshing) {
                AutoSizedCircularProgressIndicator(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxHeight()
                        .padding(14.dp)
                )
            } else {
                IconButton(onClick = { actioner(ShowDetailsAction.RefreshAction) }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.cd_refresh)
                    )
                }
            }
        },
        elevation = elevation,
        backgroundColor = backgroundColor,
        modifier = modifier
    )
}

@Composable
private fun ToggleShowFollowFloatingActionButton(
    isFollowed: Boolean,
    onClick: () -> Unit,
    expanded: () -> Boolean,
    modifier: Modifier = Modifier,
) {
    LogCompositions("ToggleShowFollowFloatingActionButton")

    ExpandableFloatingActionButton(
        onClick = onClick,
        icon = {
            Icon(
                imageVector = when {
                    isFollowed -> Icons.Default.FavoriteBorder
                    else -> Icons.Default.Favorite
                },
                contentDescription = when {
                    isFollowed -> stringResource(R.string.cd_follow_show_remove)
                    else -> stringResource(R.string.cd_follow_show_add)
                }
            )
        },
        text = {
            Text(
                when {
                    isFollowed -> stringResource(R.string.follow_show_remove)
                    else -> stringResource(R.string.follow_show_add)
                }
            )
        },
        backgroundColor = when {
            isFollowed -> MaterialTheme.colors.surface
            else -> MaterialTheme.colors.primary
        },
        expanded = expanded(),
        modifier = modifier
    )
}

private val previewShow = TiviShow(title = "Detective Penny")

@Preview
@Composable
private fun PreviewTopAppBar() {
    ShowDetailsAppBar(
        title = previewShow.title ?: "",
        showAppBarBackground = true,
        isRefreshing = true,
        actioner = {}
    )
}
