package ru.trueengineering.featureflag.manager.core.impl.environment

import ru.trueengineering.featureflag.manager.core.domen.environment.Environment
import ru.trueengineering.featureflag.manager.core.domen.environment.EnvironmentProperties
import ru.trueengineering.featureflag.manager.core.domen.environment.Instance
import ru.trueengineering.featureflag.manager.core.domen.environment.InstanceConnectionStatus
import ru.trueengineering.featureflag.manager.core.domen.project.Project

interface EnvironmentRepository {

    fun findEnvironmentByAuthKeyHash(authKeyHash: String): Environment?
    fun setInstanceStatus(instance: Instance, instanceConnectionStatus: InstanceConnectionStatus)
    fun saveEnvironment(environment: Environment, project: Project): Environment?
    fun remove(environmentId: Long)
    fun getById(environmentId: Long): Environment
    fun saveAuthHash(environmentId: Long, authKeyHash: String)
    fun createInstance(environmentId: Long, agentName: String): Environment?
    fun removeInstance(instanceId: Long)
    fun getByProjectId(projectId: Long) : List<Environment>
    fun getByProjectIdAndName(projectId: Long, envName: String) : Environment?
    fun saveAll(environments: List<Environment>, project: Project) : List<Environment>
    fun checkAndUpdateInstanceStatus(instanceOutOfSyncTimeSec: Int)
    fun removeEmails(environmentId: Long, emails: List<String>)
    fun createEmails(environmentId: Long, emails: List<String>)
    fun updateSettings(environment: Environment, project: Project): Environment
    fun addProperties(environmentId: Long, newProperties: EnvironmentProperties): Environment
}