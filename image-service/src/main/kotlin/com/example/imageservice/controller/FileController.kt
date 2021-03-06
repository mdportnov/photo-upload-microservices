package com.example.imageservice.controller

import com.example.fileuploadservice.exception.FileNotFoundException
import com.example.imageservice.model.FileResponse
import com.example.imageservice.service.FileStorageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.io.IOException
import java.util.stream.Collectors
import javax.servlet.http.HttpServletRequest


@CrossOrigin(origins = ["*"])
@RestController
class FileController {
    @Autowired
    lateinit var env: Environment

    @RequestMapping("/")
    fun home(): String? {
        // This is useful for debugging
        // When having multiple instance of gallery service running at different ports.
        // We load balance among them, and display which instance received the request.
        return "Hello from Gallery Service running at port: " + env.getProperty("local.server.port")
    }

    @Autowired
    lateinit var fileStorageService: FileStorageService

    @Value("\${jwt.secret}")
    lateinit var secret: String

    @PutMapping("/{username}")
    fun uploadFile(
        @RequestParam("file") file: MultipartFile,
        @PathVariable("username") username: String
    ): ResponseEntity<FileResponse> {
        val fileName = fileStorageService.storeFile(username, file)
        val fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path(fileName)
            .toUriString()

        val fileResponse = FileResponse(fileName, fileDownloadUri, file.contentType, file.size)
        return ResponseEntity<FileResponse>(fileResponse, HttpStatus.OK)
    }

    @GetMapping("/{username}/{filename:.+}")
    fun downloadFile(
        @PathVariable username: String,
        @PathVariable filename: String,
        @RequestParam(required = false) jwtToken: String?,
        request: HttpServletRequest
    ): ResponseEntity<Resource> {
        if (username == getUsernameFromRequest(request, secret)
            || getUsernameFromToken(jwtToken, secret) == username
        ) {
            val resource = fileStorageService.loadFileAsResource("$username/$filename")
            var contentType: String? = null

            try {
                contentType = request.servletContext.getMimeType(resource.file.absolutePath)
            } catch (ex: IOException) {
                println("Couldn't determine fileType")
            }

            if (contentType == null)
                contentType = "application/octet-stream"

            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                .body(resource)
        } else
            throw FileNotFoundException("This file doesn't exists or you don't have permissions")
    }

    @GetMapping("/{filename:.+}")
    @ResponseBody
    fun getFile(@PathVariable filename: String): ResponseEntity<Resource?>? {
        val file: Resource = fileStorageService.loadFileAsResource(filename)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.filename + "\"").body(file)
    }

    @GetMapping("/all/{username}")
    @ResponseBody
    fun getListFiles(@PathVariable username: String): ResponseEntity<List<FileResponse>> {
        val fileInfos: List<FileResponse> = fileStorageService.loadAll(username).map { path ->
            val filename: String = "http://localhost:8762/photos/" + username + "/" + path.fileName
            val url = MvcUriComponentsBuilder
                .fromMethodName(FileController::class.java, "getFile", path.fileName.toString()).build()
                .toString()
            FileResponse(filename, url)
        }.collect(Collectors.toList())

        return ResponseEntity.status(HttpStatus.OK).body(fileInfos)
    }

    @PostMapping(value = ["/register"])
    fun createNewDirectory(@RequestParam username: String) {
        fileStorageService.createNewUserDir(username)
    }
}