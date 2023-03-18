package com.isoffice.bookshelfsharing

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.isoffice.bookshelfsharing.dao.BookDao
import com.isoffice.bookshelfsharing.dao.FireStoreAccess
import com.isoffice.bookshelfsharing.ui.*
import com.isoffice.bookshelfsharing.ui.theme.BookshelfSharingTheme
import com.isoffice.bookshelfsharing.ui.viewModel.*
import timber.log.Timber

class MainActivity : ComponentActivity() {
    private val auth: FirebaseAuth = Firebase.auth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var authResultLauncher: ActivityResultLauncher<Intent>
    private val viewModel: MainViewModel by viewModels()

    private var database = FireStoreAccess().db
    private val bookDao = BookDao(database)
    private val booksViewModel = BooksViewModel(bookDao)
    private val bookViewModel = BookViewModel(bookDao)
    private val scrollViewModel = ScrollViewModel()
    private val tagsViewModel = TagsViewModel(bookDao)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureTimber()

        auth.addAuthStateListener { auth ->
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
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Timber.w(e, "Google sign in failed")
            }
        }



        setContent {
            BookshelfSharingTheme {
                viewModel.navController = rememberNavController()
                val navController = viewModel.navController!!

                NavHost(navController = navController, startDestination = "auth" ) {
                    composable("auth") { //google認証画面
                        AuthScreen(
                            currentUser = viewModel.currentUser,
                            navController =  navController,
                        ) { signIn() }
                    }
                    composable("main"){ //メイン画面（本棚の書籍一覧）
                        MainScreen(
                            navController,
                            user = viewModel.currentUser!!.email.toString(),
                            bookDao,
                            booksViewModel,
                            scrollViewModel
                        )
                    }
                    composable("titleSearch/{title}") { // タイトル検索結果
                        TitleSearchResultScreen(
                            navController,
                            booksViewModel,
                            scrollViewModel,
                            it.arguments?.getString("title")!!
                        )
                    }
                    composable("tagSearch/{tag}"){ //tag検索結果
                        TagSearchResultScreen(
                            navController,
                            booksViewModel,
                            scrollViewModel,
                            it.arguments?.getString("tag")!!
                        )
                    }
                    composable("barcode"){ //ISBNバーコード読み取り画面
                        BarcodeScanScreen(navController = navController)
                    }
                    composable("book/{barcode}"){ //バーコードで読み取った書籍の表示画面
                        it.arguments?.getString("barcode")?.let { it1 ->
                            BookContentScreen(
                                navController,
                                it1,
                                viewModel.currentUser!!,
                                bookViewModel
                            )
                        }
                    }
                    composable("bookDetail/{key}"){ //本棚一覧からタップした本の詳細画面
                        it.arguments?.getString("key")?.let{ key ->
                            BookDetailScreen(key, bookDao, bookViewModel, viewModel,)
                        }
                    }
                    composable("bookInfoEdit/{key}"){ //本の情報編集画面
                        it.arguments?.getString("key")?.let{ key ->
                            BookInfoUpdateScreen(
                                navController,
                                key, bookDao, bookViewModel,)
                        }
                    }
                    composable("inputISBN"){    //ISBN手入力画面
                        ISBNCodeInputScreen { navController.navigate("book/$it") }
                    }
                    composable("inputBook/{isbn}"){    // 手動登録(ISBN見つからなかったとき)
                        BookInfoInputScreen(
                            navController,viewModel.currentUser!!,
                            it.arguments?.getString("isbn")!!,
                            bookViewModel)
                    }
                    composable("inputBook"){    // 手動登録
                        BookInfoInputScreen(
                            navController,viewModel.currentUser!!,
                            "",
                            bookViewModel)
                    }
                    composable("filter"){// 詳細検索画面
                        FilterScreen(navController, booksViewModel)
                    }
                    composable("detailedSearch/{str}"){// 詳細検索結果画面
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
                if(viewModel.navController!!.currentDestination?.route.toString() == "main") {
                    this.finish()
                } else if(viewModel.navController!!.currentDestination?.route.toString().startsWith("bookDetail")){
                    viewModel.navController!!.popBackStack()
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
                    Timber.d("signInWithCredential:success")
                    viewModel.navController!!.navigate("main")
                } else {
                    Timber.w(task.exception, "signInWithCredential:failure")
                }
            }
    }

}
