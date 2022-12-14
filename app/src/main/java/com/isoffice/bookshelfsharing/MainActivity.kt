package com.isoffice.bookshelfsharing

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.isoffice.bookshelfsharing.dao.BookDao
import com.isoffice.bookshelfsharing.dao.DBAccess
import com.isoffice.bookshelfsharing.ui.*
import com.isoffice.bookshelfsharing.ui.theme.BookshelfSharingTheme
import com.isoffice.bookshelfsharing.ui.viewModel.BookViewModel
import com.isoffice.bookshelfsharing.ui.viewModel.BooksViewModel
import com.isoffice.bookshelfsharing.ui.viewModel.MainViewModel
import com.isoffice.bookshelfsharing.ui.viewModel.ScrollViewModel
import timber.log.Timber

class MainActivity : ComponentActivity() {
    private val auth: FirebaseAuth = Firebase.auth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var authResultLauncher: ActivityResultLauncher<Intent>
    private val viewModel: MainViewModel by viewModels()

    private var database = DBAccess().db
    private val bookDao = BookDao(database)
    private val booksViewModel = BooksViewModel(bookDao)
    private val bookViewModel = BookViewModel(bookDao)
    private val scrollViewModel = ScrollViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureTimber()

        auth.addAuthStateListener { auth ->
            Timber.d("addAuthStateListener: ${auth.currentUser}")
            viewModel.currentUser = auth.currentUser
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.gcp_id))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        authResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Timber.d("firebaseAuthWithGoogle:%s", account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Timber.w(e, "Google sign in failed")
            }
        }
        setContent {
            BookshelfSharingTheme {
                viewModel.navController = rememberNavController()
                val navController = viewModel.navController!!
                NavHost(navController = navController, startDestination = "auth" ) {
                    composable("auth") { //google????????????
                        AuthScreen(
                            currentUser = viewModel.currentUser,
                            navController =  navController,
                        ) { signIn() }
                    }
                    composable("main"){ //??????????????????????????????????????????
                        MainScreen(
                            navController,
                            booksViewModel,
                            scrollViewModel
                        )
                    }
                    composable("titleSearch/{title}") { // ????????????????????????
                        TitleSearchResultScreen(
                            navController,
                            booksViewModel,
                            scrollViewModel,
                            it.arguments?.getString("title")!!
                        )
                    }
                    composable("tagSearch/{tag}"){ //??????????????????tag???????????????
                        TagSearchResultScreen(
                            navController,
                            booksViewModel,
                            scrollViewModel,
                            it.arguments?.getString("tag")!!
                        )
                    }
                    composable("barcode"){ //ISBN?????????????????????????????????
                        BarcodeScanScreen(navController = navController)
                    }
                    composable("book/{barcode}"){ //??????????????????????????????????????????????????????
                        it.arguments?.getString("barcode")?.let { it1 ->
                            BookContentScreen(
                                barcode = it1,
                                user = viewModel.currentUser!!,
                                bookViewModel
                            )
                        }
                    }
                    composable("bookDetail/{key}"){ //???????????????????????????????????????????????????

                        it.arguments?.getString("key")?.let{ key ->
                            BookDetailScreen(key = key, bookDao = bookDao,
                                viewModel = viewModel)
                        }
                    }
                    composable("inputISBN"){    //ISBN???????????????
                        ISBNCodeInputScreen { navController.navigate("book/$it") }
                    }
                    composable("inputBook"){    // ????????????
                        BookInfoInputScreen(navController,viewModel.currentUser!!,bookViewModel)
                    }
                }
            }
            BackHandler(enabled = true) {
                if(viewModel.navController!!.currentDestination?.route.toString() == "main"){
                    this.finish()
                } else {
                    viewModel.navController!!.navigate("main")
                }
            }
        }

    }

    private fun configureTimber() {
        if(BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        // startActivityForResult(signInIntent, RC_SIGN_IN)

        authResultLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Timber.d("signInWithCredential:success")
                    viewModel.navController!!.navigate("main")
                } else {
                    // If sign in fails, display a message to the user.
                    Timber.w(task.exception, "signInWithCredential:failure")
                }
            }
    }

}
