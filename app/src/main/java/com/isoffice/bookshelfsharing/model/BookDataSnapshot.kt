package com.isoffice.bookshelfsharing.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.StringBufferInputStream

/*
 Firebase RealtimeDB からのデータ
 */
@Parcelize
@Serializable
data class BookDataSnapshot(
    val key:String,
    val value: Value,
): Parcelable

@Parcelize
@Serializable
data class Value(
    val thumbnail:String?,
    val author:String,
    val isbn: String,
    val publisher:String?,
    val publishedDate: String?,
    val series: String?,
    val ownerId:String,
    val title:String,
    val subtitle: String?,
    val ownerIcon : String?,
    val deleteFlag : Boolean,
):Parcelable