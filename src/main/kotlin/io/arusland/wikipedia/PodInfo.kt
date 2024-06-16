package io.arusland.wikipedia

data class PodInfo(
    /**
     * Image URL of maximum size.
     */
    val url: String,
    /**
     * Image URL of thumbnail size (1280px).
     */
    val thumbUrl: String,
    /**
     * Image raw url from html.
     */
    val originalImageUrl: String,
    /**
     * Image caption (html).
     */
    val caption: String
)
