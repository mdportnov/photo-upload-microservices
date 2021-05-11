package com.example.authservice.controller

import com.example.authservice.exception.UserAlreadyCreatedException
import com.example.authservice.model.user.UserDTO
import com.example.authservice.security.jwt.JwtRequest
import com.example.authservice.security.jwt.JwtResponse
import com.example.authservice.security.jwt.JwtTokenUtil
import com.example.authservice.service.FileStorageService
import com.example.authservice.service.JwtUserDetailsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
class AuthController {
    @Autowired
    lateinit var authenticationManager: AuthenticationManager

    @Autowired
    lateinit var jwtTokenUtil: JwtTokenUtil

    @Autowired
    lateinit var userDetailsService: JwtUserDetailsService

    @Autowired
    lateinit var fileStorageService: FileStorageService

    @PostMapping(value = ["/register"])
    @Throws(java.lang.Exception::class)
    fun saveUser(@RequestBody user: UserDTO): ResponseEntity<*>? {
        if (isUsernameAlreadyInUse(user.username))
            throw UserAlreadyCreatedException("User already exists")

        // TODO вызвать image-service для создание папки
        fileStorageService.createNewUserDir(user.username)

        return ResponseEntity.ok(userDetailsService.save(user))
    }

    @PostMapping("/authenticate")
    @Throws(Exception::class)
    fun createAuthenticationToken(@RequestBody authenticationRequest: JwtRequest): ResponseEntity<*> {
        authenticate(authenticationRequest.username, authenticationRequest.password)
        val userDetails: UserDetails = userDetailsService
            .loadUserByUsername(authenticationRequest.username)
        val token = jwtTokenUtil.generateToken(userDetails)
        return ResponseEntity.ok(JwtResponse(token))
    }

    fun isUsernameAlreadyInUse(username: String): Boolean {
        var userInDb = true
        if (userDetailsService.userRepository.findByUsername(username) == null) userInDb = false
        return userInDb
    }

    private fun authenticate(username: String, password: String) {
        try {
            authenticationManager.authenticate(UsernamePasswordAuthenticationToken(username, password))
        } catch (e: DisabledException) {
            throw Exception("USER_DISABLED", e)
        } catch (e: BadCredentialsException) {
            throw Exception("INVALID_CREDENTIALS", e)
        }
    }
}