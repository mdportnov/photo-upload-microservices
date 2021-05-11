package com.example.imageservice.model

class FileResponse(
    var filename: String,
    var url: String,
    var fileType: String? = null,
    var size: Long? = null
) 