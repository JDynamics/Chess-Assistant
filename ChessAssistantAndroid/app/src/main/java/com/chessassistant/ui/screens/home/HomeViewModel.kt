package com.chessassistant.ui.screens.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for the Home screen.
 * Currently minimal as home screen is mostly navigation.
 */
@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    // Home screen is primarily navigation, minimal state needed
}
