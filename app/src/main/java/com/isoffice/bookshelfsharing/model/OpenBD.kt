package com.isoffice.bookshelfsharing.model

import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.NavType
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
@Parcelize
data class OpenBD(
    val onix: Onix,
    val summary: Summary
):Parcelable

@Serializable
@Parcelize
data class Onix(
    @SerialName("CollateralDetail")
    val collateralDetail: CollateralDetail,
    @SerialName("DescriptiveDetail")
    val descriptiveDetail: DescriptiveDetail,
):Parcelable

@Serializable
@Parcelize
data class DescriptiveDetail(
    @SerialName("Collection")
    val collection: Collection? = null,
    @SerialName("TitleDetail")
    val titleDetail: TitleDetail,
):Parcelable

@Serializable
@Parcelize
data class Collection(
    @SerialName("TitleDetail")
    val titleDetail:CollectionTitleDetail
):Parcelable

@Serializable
@Parcelize
data class CollectionTitleDetail(
    @SerialName("TitleElement")
    val titleElement: List<CollectionTitleElement>
):Parcelable

@Serializable
@Parcelize
data class CollectionTitleElement(
    @SerialName("TitleElementLevel")
    val titleElementLevel: String = "",
    @SerialName("PartNumber")
    val partNumber : String = "",
    @SerialName("TitleText")
    val titleText: CollectionTitleText,
):Parcelable

@Serializable
@Parcelize
data class CollectionTitleText(
    val content:String = ""
):Parcelable

@Serializable
@Parcelize
data class TitleDetail(
    @SerialName("TitleElement")
    val titleElement: TitleElement
):Parcelable


@Serializable
@Parcelize
data class TitleElement(
    @SerialName("Subtitle")
    val subtitle : Subtitle? = null,
):Parcelable

@Serializable
@Parcelize
data class Subtitle(
    val content: String = "",
):Parcelable

@Serializable
@Parcelize
data class CollateralDetail(
    @SerialName("TextContent")
    val textContent: List<TextContent>
):Parcelable

@Serializable
@Parcelize
data class TextContent(
    @SerialName("TextType")
    val textType : String = "",
    @SerialName("Text")
    val text : String = ""
):Parcelable


@Serializable
@Parcelize
data class Summary(
    val isbn : String,
    val title : String,
    val series : String?,
    val publisher : String?,
    val pubdate : String?,
    val cover : String?,
    val author: String?,
):Parcelable


//Compose受け渡し用カスタムNavType
@OptIn(ExperimentalSerializationApi::class)
val OpenBDNavType = object: NavType<OpenBD>(isNullableAllowed = true){
    override fun put(bundle: Bundle, key: String, value: OpenBD) {
        bundle.putParcelable(key,value)
    }

    override fun get(bundle: Bundle, key: String): OpenBD? {
        return bundle.getParcelable(key)
    }

    override fun parseValue(value: String): OpenBD {
        val formatter = Json
        return formatter.decodeFromString(OpenBD.serializer(), value)
    }
}