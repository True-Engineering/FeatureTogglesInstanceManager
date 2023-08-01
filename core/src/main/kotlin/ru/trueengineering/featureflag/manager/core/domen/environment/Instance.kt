package ru.trueengineering.featureflag.manager.core.domen.environment

import java.time.Instant

class Instance(var id: Long? = null, val name: String, val updated: Instant, var status: InstanceConnectionStatus)