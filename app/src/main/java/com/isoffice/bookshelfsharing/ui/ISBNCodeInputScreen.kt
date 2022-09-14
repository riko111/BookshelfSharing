package com.isoffice.bookshelfsharing.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun ISBNCodeInputScreen(
    onNavigateToContent:(code:String) -> Unit
){
    Column(
        modifier = Modifier.padding(3.dp)
    ) {
        var code by remember { mutableStateOf("") }
        Text(
            fontSize = 26.sp,
            text = "ISBNコード入力",
            modifier = Modifier
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))
        Row() {
            TextField(
                onValueChange = {code = it},
                placeholder = { Text(text = "ISBNコード入力") },
                value = code,
                keyboardOptions = KeyboardOptions.Default.copy(
                    capitalization = KeyboardCapitalization.Words,
                    autoCorrect = false,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { onNavigateToContent(code) }
                ),
            )
            TextButton(onClick = { onNavigateToContent(code) }) {
                Text(text = "検索")
            }
        }
    }
}