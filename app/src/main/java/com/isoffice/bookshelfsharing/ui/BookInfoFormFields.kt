package com.isoffice.bookshelfsharing.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun BookInfoFormFields(
    title: String,
    onTitleChange: (String) -> Unit,
    furigana: String,
    onFuriganaChange: (String) -> Unit,
    subtitle: String,
    onSubtitleChange: (String) -> Unit,
    author: String,
    onAuthorChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    publisher: String,
    onPublisherChange: (String) -> Unit,
    publishedDate: String,
    onPublishedDateChange: (String) -> Unit,
    isbn: String,
    onIsbnChange: (String) -> Unit,
    furiganaLabel: String,
    submitLabel: String,
    onSubmit: () -> Unit,
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth().safeDrawingPadding()
            .padding(3.dp),
        label = { Text("タイトル*") },
        value = title,
        onValueChange = onTitleChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth().safeDrawingPadding()
            .padding(3.dp),
        label = { Text(furiganaLabel) },
        value = furigana,
        onValueChange = onFuriganaChange,
        placeholder = { Text(text = "フリガナ") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth().safeDrawingPadding()
            .padding(3.dp),
        label = { Text("サブタイトル") },
        value = subtitle,
        onValueChange = onSubtitleChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth().safeDrawingPadding()
            .padding(3.dp),
        label = { Text("著者*") },
        value = author,
        onValueChange = onAuthorChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth().safeDrawingPadding()
            .padding(3.dp),
        label = { Text("情報") },
        value = description,
        onValueChange = onDescriptionChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth().safeDrawingPadding()
            .padding(3.dp),
        label = { Text("出版社") },
        value = publisher,
        onValueChange = onPublisherChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth().safeDrawingPadding()
            .padding(3.dp),
        label = { Text("出版日") },
        value = publishedDate,
        placeholder = { Text(text = "yyyymmdd") },
        onValueChange = onPublishedDateChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
    OutlinedTextField(
        label = { Text("ISBN") },
        modifier = Modifier
            .fillMaxWidth().safeDrawingPadding()
            .padding(3.dp),
        value = isbn,
        onValueChange = onIsbnChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )

    OutlinedButton(
        onClick = onSubmit,
        modifier = Modifier
            .fillMaxWidth().safeDrawingPadding()
            .padding(3.dp)
    ) {
        Text(text = submitLabel, Modifier)
    }
}
