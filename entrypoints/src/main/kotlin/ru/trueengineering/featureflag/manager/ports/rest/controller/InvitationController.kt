package ru.trueengineering.featureflag.manager.ports.rest.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.trueengineering.featureflag.manager.ports.service.UserService
import java.util.UUID

@RestController
@RequestMapping("/api/invite")
class InvitationController(
    @Autowired val userService: UserService
) {

    @Operation(summary = "Invite current user to specified by uuid project")
    @PutMapping("/{uuid}")
    fun inviteToProject(
        @Parameter(description = "Invite uuid", example = "5cc175c9-2c68-4695-98f8-6157575c9a2f")
        @PathVariable("uuid") inviteId: UUID
    ): ResponseEntity<Any> {
        userService.inviteUserToProject(inviteId)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

}