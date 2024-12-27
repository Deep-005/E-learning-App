package com.example.courses.Data

data class Internship(
    val name: String = "",
    val brief: String = "",
    val price: String = "",
    val image: String = "",
    val time: String = "",
    val type: String= "",
    val category: String = "",
    val tag: String = "",
    val instructor: String = "",
    val about: String = "",
    val demoVideo: String ="",
    val videos: List<String> = listOf(),
    val pdf: List<String> = listOf()
)
