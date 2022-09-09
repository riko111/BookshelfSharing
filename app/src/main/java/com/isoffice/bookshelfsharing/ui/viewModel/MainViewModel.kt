package com.isoffice.bookshelfsharing.ui.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseUser
import com.isoffice.bookshelfsharing.model.Book

class MainViewModel:ViewModel() {
    var currentUser by mutableStateOf<FirebaseUser?>(null)
    var navController by mutableStateOf<NavHostController?>(null)

}