package com.ai.egret.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse(
    @Json(name = "records")
    val records: List<MarketPriceResponse> = emptyList()  // default so Moshi doesn't crash
)

@JsonClass(generateAdapter = true)
data class MarketPriceResponse(

    @Json(name = "state") val state: String = "",
    @Json(name = "district") val district: String = "",
    @Json(name = "market") val market: String = "",
    @Json(name = "commodity") val commodity: String = "",
    @Json(name = "variety") val variety: String = "",
    @Json(name = "grade") val grade: String = "",

    @Json(name = "arrival_date") val arrivalDate: String = "",
    @Json(name = "min_price") val minPrice: String = "",
    @Json(name = "max_price") val maxPrice: String = "",
    @Json(name = "modal_price") val modalPrice: String = "",

    // 🔹 change here: non-null with default empty string
    @Json(name = "commodity_code") val commodityCode: String = ""
) {
    val location: String
        get() = "$state $district"
}
