//package com.ninezero.data.temp
//
//import kotlinx.coroutines.runBlocking
//import okhttp3.MediaType
//import okhttp3.RequestBody
//import okio.BufferedSink
//import okio.FileNotFoundException
//import okio.source
//import timber.log.Timber
//import java.io.InputStream
//
//class UriRequestBody(
//    private val getInputStream: suspend () -> Result<InputStream>,
//    private val contentType: MediaType? = null,
//    private val contentLength: Long
//) : RequestBody() {
//    override fun contentType(): MediaType? = contentType
//    override fun contentLength(): Long = contentLength
//    override fun writeTo(sink: BufferedSink) {
//        runBlocking {
//            try {
//                getInputStream().getOrThrow().use { inputStream ->
//                    sink.writeAll(inputStream.source())
//                }
//            } catch (e: FileNotFoundException) {
//                Timber.e(e)
//            }
//        }
//    }
//}