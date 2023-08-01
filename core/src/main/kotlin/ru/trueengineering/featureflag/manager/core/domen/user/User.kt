package ru.trueengineering.featureflag.manager.core.domen.user

import org.apache.commons.codec.digest.DigestUtils
import java.security.Principal
import java.time.Instant

data class User(
    val userName: String,
    val email: String,
    var id: Long? = null,
    var lastLogin: Instant? = null,
    @Deprecated("unused")
    var status: UserStatus? = null,
    var authorities: List<String>? = ArrayList(),
    var defaultProjectId: Long? = null
) : Principal {
    val avatarUrl: String = this.generateGravatarUrl(this.email)

    private fun generateGravatarUrl(email: String): String {
        val hash = DigestUtils.md5Hex(email)
        return "https://www.gravatar.com/avatar/${hash}.jpg?d=404"
    }

    override fun getName(): String = email
}
enum class UserStatus {
    PENDING,
    ACTIVE,
    BLOCKED,
    DELETED

}