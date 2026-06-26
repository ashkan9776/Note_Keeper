package com.ahoura.notekeeper.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ahoura.notekeeper.presentation.archive.ArchiveScreen
import com.ahoura.notekeeper.presentation.editor.EditorScreen
import com.ahoura.notekeeper.presentation.home.HomeScreen
import com.ahoura.notekeeper.presentation.search.SearchScreen
import com.ahoura.notekeeper.presentation.settings.SettingsScreen
import com.ahoura.notekeeper.presentation.trash.TrashScreen

private const val TRANSITION_MS = 300

/**
 * Root navigation graph wiring every screen together with tailored transitions.
 *
 * [deepLinkNoteId] is set when the app is launched by tapping a reminder notification; it opens the
 * corresponding note in the editor once, on first composition.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    deepLinkNoteId: Long? = null
) {
    LaunchedEffect(deepLinkNoteId) {
        if (deepLinkNoteId != null) {
            navController.navigate(Screen.Editor.createRoute(deepLinkNoteId))
        }
    }
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = { fadeIn(tween(TRANSITION_MS)) },
        exitTransition = { fadeOut(tween(TRANSITION_MS)) },
        popEnterTransition = { fadeIn(tween(TRANSITION_MS)) },
        popExitTransition = { fadeOut(tween(TRANSITION_MS)) }
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNoteClick = { id -> navController.navigate(Screen.Editor.createRoute(id)) },
                onCreateNote = { navController.navigate(Screen.Editor.createRoute()) },
                onSearchClick = { navController.navigate(Screen.Search.route) },
                onArchiveClick = { navController.navigate(Screen.Archive.route) },
                onTrashClick = { navController.navigate(Screen.Trash.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            route = Screen.Editor.route,
            arguments = listOf(
                navArgument(Screen.Editor.ARG_NOTE_ID) {
                    type = NavType.LongType
                    defaultValue = Screen.Editor.NEW_NOTE_ID
                }
            ),
            // Editor rises like a sheet from the bottom and slides back down on exit.
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    tween(TRANSITION_MS)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    tween(TRANSITION_MS)
                )
            }
        ) {
            EditorScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.Search.route,
            enterTransition = { fadeIn(tween(TRANSITION_MS)) },
            exitTransition = { fadeOut(tween(TRANSITION_MS)) }
        ) {
            SearchScreen(
                onNoteClick = { id -> navController.navigate(Screen.Editor.createRoute(id)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Archive.route,
            enterTransition = {
                slideInVertically(tween(TRANSITION_MS)) { it / 4 } + fadeIn(tween(TRANSITION_MS))
            },
            popExitTransition = {
                slideOutVertically(tween(TRANSITION_MS)) { it / 4 } + fadeOut(tween(TRANSITION_MS))
            }
        ) {
            ArchiveScreen(
                onNoteClick = { id -> navController.navigate(Screen.Editor.createRoute(id)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Trash.route,
            enterTransition = {
                slideInVertically(tween(TRANSITION_MS)) { it / 4 } + fadeIn(tween(TRANSITION_MS))
            },
            popExitTransition = {
                slideOutVertically(tween(TRANSITION_MS)) { it / 4 } + fadeOut(tween(TRANSITION_MS))
            }
        ) {
            TrashScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.Settings.route,
            enterTransition = {
                slideInVertically(tween(TRANSITION_MS)) { it / 4 } + fadeIn(tween(TRANSITION_MS))
            },
            popExitTransition = {
                slideOutVertically(tween(TRANSITION_MS)) { it / 4 } + fadeOut(tween(TRANSITION_MS))
            }
        ) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
