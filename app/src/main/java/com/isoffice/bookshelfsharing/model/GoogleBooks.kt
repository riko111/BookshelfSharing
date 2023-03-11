package com.isoffice.bookshelfsharing.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoogleBooks(
    val items: List<Volume>? = null
)

@Serializable
@Parcelize
data class Volume(
    val volumeInfo: VolumeInfo

):Parcelable

@Serializable
@Parcelize
data class VolumeInfo(
    val title: String,
    val subtitle:String? = null,
    val description: String? = null,
    val authors: List<String>? = null,
    val publisher: String? = null,
    val publishedDate: String? = null,
    val pageCount: Int? = null,
    val imageLinks: ImageLinks? = null,
    val industryIdentifiers: List<IndustryIdentifier>? = null
):Parcelable

@Serializable
@Parcelize
data class ImageLinks(
    val smallThumbnail: String?,
    val thumbnail: String?
):Parcelable

@Serializable
@Parcelize
data class IndustryIdentifier(
    val type:String,
    val identifier:String
):Parcelable