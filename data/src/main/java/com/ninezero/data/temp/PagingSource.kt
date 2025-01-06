package com.ninezero.data.temp

//class FeedPagingSource @Inject constructor(
//    private val postService: PostService
//) : PagingSource<Int, Post>() {
//    override fun getRefreshKey(state: PagingState<Int, Post>): Int? {
//        return state.anchorPosition?.let { anchorPosition ->
//            val anchorPage = state.closestPageToPosition(anchorPosition)
//            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
//        }
//    }
//
//    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
//        return try {
//            val page = params.key ?: 1
//            val loadSize = params.loadSize
//
//            if (page > 1) { delay(1000) } // Test Delay
//
//            val response = postService.getPosts(page = page, size = loadSize)
//            val posts = response.data.map { it.toDomain() }
//
//            LoadResult.Page(
//                data = posts,
//                prevKey = null,
//                nextKey = if (posts.size == loadSize) page + 1 else null
//            )
//        } catch (e: Exception) {
//            LoadResult.Error(e)
//        }
//    }
//}