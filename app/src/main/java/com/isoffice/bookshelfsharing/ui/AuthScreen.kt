package com.isoffice.bookshelfsharing.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseUser

/*
 google認証画面
 認証できてたらメイン画面へ
 */
@Composable
fun AuthScreen(currentUser: FirebaseUser?, navController:NavHostController, onSignIn: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        if (currentUser == null) {
            Button(onClick = { onSignIn() }) {
                Text(text = "Sign in with Google")
            }
        } else {
            LaunchedEffect(Unit) {
                navController.navigate("main")
            }
        }
    }
}

