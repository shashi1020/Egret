package com.ai.egret.data.mapper

import com.ai.egret.models.MarketPriceEntity
import com.ai.egret.models.MarketPriceResponse


fun MarketPriceResponse.toEntity(): MarketPriceEntity {

    return MarketPriceEntity(
        state = state,
        district = district,
        market = market,
        commodity = commodity,
        variety = variety,
        grade = grade,
        arrivalDate = arrivalDate,
        minPrice = minPrice,
        maxPrice = maxPrice,
        modalPrice = modalPrice,
        commodityCode = commodityCode
    )
}

fun MarketPriceEntity.toResponse(): MarketPriceResponse {
    return MarketPriceResponse(
        state = state,
        district = district,
        market = market,
        commodity = commodity,
        variety = variety,
        grade = grade,
        arrivalDate = arrivalDate,
        minPrice = minPrice,
        maxPrice = maxPrice,
        modalPrice = modalPrice,
        commodityCode = commodityCode
    )
}

