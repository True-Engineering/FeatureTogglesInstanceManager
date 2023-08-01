package ru.trueengineering.featureflag.manager.infrastructure.db.repositoy

import org.springframework.stereotype.Repository
import ru.trueengineering.featureflag.manager.core.domen.user.User
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import ru.trueengineering.featureflag.manager.core.error.ServiceException
import ru.trueengineering.featureflag.manager.core.impl.user.UserRepository
import ru.trueengineering.featureflag.manager.infrastructure.db.entity.UserJpaRepository
import ru.trueengineering.featureflag.manager.infrastructure.db.repositoy.mapper.UserEntityMapper
import javax.transaction.Transactional

@Repository
@Transactional
class UserDatabaseRepository(
    private val userJpaRepository: UserJpaRepository,
    private val userEntityMapper: UserEntityMapper
) : UserRepository {

    override fun createUser(user: User): User {
        val entity = userEntityMapper.convertToEntity(user)
        return userEntityMapper.convertToDomain(userJpaRepository.save(entity))
    }

    override fun updateUser(user: User): User {
        val userEntity = userJpaRepository.getNonRemovedByEmail(user.email)
            ?: throw ServiceException(ErrorCode.USER_NOT_FOUND)
        userEntityMapper.updateEntity(userEntity, user)
        return userEntityMapper.convertToDomain(userJpaRepository.save(userEntity))
    }

    override fun getById(userId: Long): User? {
        val userEntity = userJpaRepository.getNonRemovedById(userId)
        return userEntity?.let(userEntityMapper::convertToDomain)
    }

    override fun getByEmail(email: String): User? {
        val userEntity = userJpaRepository.getNonRemovedByEmail(email)
        return userEntity?.let(userEntityMapper::convertToDomain)
    }

    override fun getByEmailList(emails: List<String>): List<User> {
        return userEntityMapper.convertToDomainList(userJpaRepository.getAllNonRemovedByEmailList(emails))
    }

    override fun getCountByEmailList(emails: List<String>): Int {
        return userJpaRepository.getNonRemovedCountByEmailList(emails)
    }
}