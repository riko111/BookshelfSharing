package com.isoffice.bookshelfsharing

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.navigation.NavHostController
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
import com.isoffice.bookshelfsharing.ui.viewModel.MainViewModel
import timber.log.Timber

class MainActivity : ComponentActivity() {
    private val auth: FirebaseAuth = Firebase.auth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var authResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var navController: NavHostController
    private val viewModel: MainViewModel by viewModels()

    companion object {
        private var database = DBAccess().database
        private val bookDao = BookDao(database)
    }
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
                navController = rememberNavController()
                NavHost(navController = navController, startDestination = "auth" ) {
                    composable("auth") { //google認証画面
                        AuthScreen(
                            currentUser = viewModel.currentUser,
                            navController =  navController,
                        ) { signIn() }
                    }
                    composable("main"){ //メイン画面（本棚の書籍一覧）
                        MainScreen(
                            bookDao = bookDao,
                            onNavigateToBarcode ={ navController.navigate("barcode")},
                            onNavigateToDetail = {navController.navigate("bookDetail/${it}")}
                        )
                    }
                    composable("barcode"){ //ISBNバーコード読み取り画面
                        BarcodeScanScreen(navController = navController)
                    }
                    composable("book/{barcode}"){ //バーコードで読み取った書籍の表示画面
                        it.arguments?.getString("barcode")?.let { it1 ->
                            BookContentScreen(
                                barcode = it1,
                                user = viewModel.currentUser!!, bookDao = bookDao
                            )
                        }
                    }
                    composable("bookDetail/{key}"){ //本棚一覧からタップした本の詳細画面
                        it.arguments?.getString("key")?.let{ it2 ->
                            BookDetailScreen(key = it2, bookDao = bookDao,
                                    onNavigateToMain = { navController.navigate("main") })
                        }
                    }
                }
            }
            BackHandler(enabled = true) {
                if(navController.currentDestination?.route.toString() != "main"){
                    navController.navigate("main")
                } else {
                    this.finish()
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
                    navController.navigate("main")
                } else {
                    // If sign in fails, display a message to the user.
                    Timber.w(task.exception, "signInWithCredential:failure")
                }
            }
    }

}
