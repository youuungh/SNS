package com.ninezero.domain.usecase

import com.ninezero.domain.model.ApiResult
import com.ninezero.domain.model.Image

interface FileUseCase {
    suspend fun getImage(uri: String): Image?
    suspend fun uploadImage(image: Image): ApiResult<String>
}