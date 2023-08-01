package ru.trueengineering.featureflag.manager.ports.rest.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.trueengineering.featureflag.manager.core.domen.user.UserStatus
import ru.trueengineering.featureflag.manager.ports.service.UserService
import java.time.Instant

@RestController
@RequestMapping("/api/session/user")
class UserController(val userService: UserService) {

    @GetMapping()
    fun getUser(): UserDto? {
        return userService.fetchUser()
    }

    @Operation(summary = "Activate user")
    @PostMapping("activate/{userId}/{projectId}")
    fun activate(
        @Parameter(description = "User id")
        @PathVariable userId: Long,
        @Parameter(description = "Project id")
        @PathVariable projectId: Long
    ): ResponseEntity<Any> {
        userService.activate(userId, projectId)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}

data class UserDto(
    val userName: String,
    val email: String,
    var avatarUrl: String? = null,
    var authorities: List<String>? = ArrayList(),
    var id: Long? = null,
    var lastLogin: Instant? = null,
    var status: UserStatus? = null,
    val userSettings: UserSettings?
)

data class UserSettings(
    var defaultProjectId: Long?
)