package com.ninezero.presentation.post

sealed class PostRoute(val route: String) {
    object PostImage: PostRoute("post_image")
    object Post: PostRoute("post")
}