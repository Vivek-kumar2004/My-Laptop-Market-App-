package com.mylaptopmarket.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ProductPhotoTest {
    @Test
    fun frontPhotoIsTheListingImageAndGalleryKeepsTheOthers() {
        val product = sample(
            front = "https://cdn.example/front.jpg",
            photos = listOf("https://cdn.example/side.jpg", "https://cdn.example/back.jpg")
        )

        assertEquals("https://cdn.example/front.jpg", product.coverImageUrl())
        assertEquals(
            listOf(
                "https://cdn.example/front.jpg",
                "https://cdn.example/side.jpg",
                "https://cdn.example/back.jpg"
            ),
            product.galleryMedia().map { it.url }
        )
    }

    @Test
    fun listingUsesTheFirstGalleryPhotoWhenNoFrontPhotoWasSet() {
        val product = sample(front = null, photos = listOf("https://cdn.example/a.jpg"))

        assertEquals("https://cdn.example/a.jpg", product.coverImageUrl())
        assertEquals(listOf("https://cdn.example/a.jpg"), product.galleryMedia().map { it.url })
    }

    private fun sample(front: String?, photos: List<String>) = Product(
        id = "1",
        name = "ThinkPad",
        description = "saved",
        stockStatus = StockStatus.AVAILABLE,
        stockCount = 1,
        providerId = "4",
        providerName = "Shop",
        providerNumber = "9999999999",
        media = photos.map { Media(it, MediaType.IMAGE) },
        frontImageUrl = front
    )
}
