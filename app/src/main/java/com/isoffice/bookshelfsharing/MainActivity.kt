package com.isoffice.bookshelfsharing

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.view.WindowCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.security.ProviderInstaller
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.isoffice.bookshelfsharing.dao.BookDao
import com.isoffice.bookshelfsharing.dao.FireStoreAccess
import com.isoffice.bookshelfsharing.ui.*
import com.isoffice.bookshelfsharing.ui.theme.BookshelfSharingTheme
import com.isoffice.bookshelfsharing.ui.viewModel.BookViewModel
import com.isoffice.bookshelfsharing.ui.viewModel.BooksViewModel
import com.isoffice.bookshelfsharing.ui.viewModel.MainViewModel
import com.isoffice.bookshelfsharing.ui.viewModel.ScrollViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.security.SecureRandom
import java.util.Base64

class MainActivity : ComponentActivity() {

    private val auth: FirebaseAuth = Firebase.auth
    private val viewModel: MainViewModel by viewModels()

    private val database = FireStoreAccess().db
    private val bookDao = BookDao(database)
    private val booksViewModel = BooksViewModel(bookDao)
    private val bookViewModel = BookViewModel(bookDao)
    private val scrollViewModel = ScrollViewModel()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        try {
            ProviderInstaller.installIfNeeded(this)
            Timber.i("ProviderInstaller installed successfully")
        } catch (e: Exception) {
            Timber.e(e, "ProviderInstaller failed to install")
        }

        val webClientId = getString(R.string.gcp_id)

        auth.addAuthStateListener { firebaseAuth ->
            viewModel.currentUser = firebaseAuth.currentUser
            Timber.d("AuthState changed. currentUser=${firebaseAuth.currentUser?.email}")
        }

        setContent {
            BookshelfSharingTheme {
                val navController = rememberNavController()
                viewModel.navController = navController

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = if (viewModel.currentUser != null) "main" else "auth"
                    ) {
                        composable("auth") {
                            AuthScreen(
                                currentUser = viewModel.currentUser,
                                navController = navController,
                            ) {
                                SignInButton(
                                    webClientId = webClientId,
                                    onSignInSuccess = {
                                        if (navController.currentDestination?.route != "main") {
                                            navController.navigate("main") {
                                                popUpTo("auth") { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        composable("main") {
                            val currentUserEmail = viewModel.currentUser?.email.orEmpty()
                            MainScreen(
                                navController,
                                user = currentUserEmail,
                                booksViewModel,
                                scrollViewModel
                            )
                        }

                        composable("titleSearch/{title}") {
                            TitleSearchResultScreen(
                                navController,
                                booksViewModel,
                                scrollViewModel,
                                it.arguments?.getString("title")!!
                            )
                        }

                        composable("tagSearch/{tag}") {
                            TagSearchResultScreen(
                                navController,
                                booksViewModel,
                                scrollViewModel,
                                it.arguments?.getString("tag")!!
                            )
                        }

                        composable("barcode") {
                            BarcodeScanScreen(navController = navController)
                        }

                        composable("book/{barcode}") {
                            it.arguments?.getString("barcode")?.let { barcode ->
                                val user = viewModel.currentUser
                                if (user != null) {
                                    BookContentScreen(
                                        navController,
                                        barcode,
                                        user,
                                        bookViewModel
                                    )
                                } else {
                                    navController.navigate("auth") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                }
                            }
                        }

                        composable("bookDetail/{key}") {
                            it.arguments?.getString("key")?.let { key ->
                                BookDetailScreen(key, bookDao, bookViewModel, viewModel)
                                Timber.d("DESTINATION key=$key")
                            }
                        }

                        composable("bookInfoEdit/{key}") {
                            it.arguments?.getString("key")?.let { key ->
                                BookInfoUpdateScreen(
                                    navController,
                                    key,
                                    bookDao,
                                    bookViewModel,
                                )
                            }
                        }

                        composable("inputISBN") {
                            ISBNCodeInputScreen { navController.navigate("book/$it") }
                        }

                        composable("inputBook/{isbn}") {
                            val user = viewModel.currentUser
                            if (user != null) {
                                BookInfoInputScreen(
                                    navController,
                                    user,
                                    it.arguments?.getString("isbn")!!,
                                    bookViewModel
                                )
                            } else {
                                navController.navigate("auth")
                            }
                        }

                        composable("inputBook") {
                            val user = viewModel.currentUser
                            if (user != null) {
                                BookInfoInputScreen(
                                    navController,
                                    user,
                                    "",
                                    bookViewModel
                                )
                            } else {
                                navController.navigate("auth")
                            }
                        }

                        composable("filter") {
                            FilterScreen(navController, booksViewModel)
                        }

                        composable("detailedSearch/{str}") {
                            DetailSearchResultScreen(
                                navController,
                                booksViewModel,
                                scrollViewModel,
                                it.arguments?.getString("str")!!
                            )
                        }
                    }
                }

                BackHandler(enabled = true) {
                    when {
                        navController.currentDestination?.route == "main" -> finish()
                        navController.currentDestination?.route?.startsWith("bookDetail") == true ->
                            navController.popBackStack()
                        else -> navController.navigate("main") {
                            launchSingleTop = true
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SignInButton(
        webClientId: String,
        onSignInSuccess: () -> Unit,
    ) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        var signingIn by remember { mutableStateOf(false) }

        val onClick: () -> Unit = click@{
            if (signingIn) {
                Timber.d("Sign-in ignored because a request is already running")
                return@click
            }

            signingIn = true

            coroutineScope.launch {
                try {
                    val idToken = signInAndGetIdToken(
                        activity = this@MainActivity,
                        webClientId = webClientId
                    )

                    if (idToken == null) {
                        Toast.makeText(context, "Sign in failed!", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val success = firebaseAuthWithGoogle(idToken)
                    if (success) {
                        Toast.makeText(context, "Sign in successful!", Toast.LENGTH_SHORT).show()
                        onSignInSuccess()
                    } else {
                        Toast.makeText(context, "Firebase sign in failed!", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    signingIn = false
                }
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.siwg_button),
                    contentDescription = "Sign in with Google",
                    modifier = Modifier.clickable(
                        enabled = !signingIn,
                        onClick = onClick
                    )
                )

                if (signingIn) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    private suspend fun signInAndGetIdToken(
        activity: ComponentActivity,
        webClientId: String,
    ): String? {
        val credentialManager = CredentialManager.create(activity)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .setNonce(generateSecureRandomNonce())
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            Timber.d("Starting CredentialManager.getCredential()")

            val result = credentialManager.getCredential(
                request = request,
                context = activity,
            )

            val credential = result.credential
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken

            Timber.d("CredentialManager success. idToken acquired")
            idToken

        } catch (e: GetCredentialCancellationException) {
            Timber.w(e, "Sign-in was cancelled")
            null
        } catch (e: NoCredentialException) {
            Timber.w(e, "No credentials found")
            null
        } catch (e: GetCredentialCustomException) {
            Timber.e(e, "Custom credential error")
            null
        } catch (e: GetCredentialException) {
            Timber.e(e, "CredentialManager failed")
            null
        } catch (e: GoogleIdTokenParsingException) {
            Timber.e(e, "Failed to parse Google ID token")
            null
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error during sign-in")
            null
        }
    }

    private suspend fun firebaseAuthWithGoogle(idToken: String): Boolean {
        return try {
            Timber.d("Firebase auth started")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).awaitCompat()
            Timber.d("Firebase auth success: ${result.user?.email}")
            true
        } catch (e: Exception) {
            Timber.e(e, "Firebase auth failed")
            false
        }
    }
}

suspend fun com.google.android.gms.tasks.Task<com.google.firebase.auth.AuthResult>.awaitCompat():
        com.google.firebase.auth.AuthResult =
    kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val result = task.result
                if (result != null) {
                    cont.resume(result) { cause, _, _ -> }
                } else {
                    cont.resumeWith(Result.failure(IllegalStateException("AuthResult is null")))
                }
            } else {
                cont.resumeWith(Result.failure(task.exception ?: Exception("Unknown auth error")))
            }
        }
    }

fun generateSecureRandomNonce(byteLength: Int = 32): String {
    val randomBytes = ByteArray(byteLength)
    SecureRandom.getInstanceStrong().nextBytes(randomBytes)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
}