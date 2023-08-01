package ru.trueengineering.featureflag.manager

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import ru.trueengineering.featureflag.manager.authorization.annotation.WithAdminRole
import ru.trueengineering.featureflag.manager.core.domen.organization.CreateOrganizationCommand
import ru.trueengineering.featureflag.manager.core.domen.organization.CreateOrganizationUseCase
import ru.trueengineering.featureflag.manager.core.domen.organization.FetchAllOrganizationsUseCase
import java.util.Optional

@Component
class OrganizationInitializingBean(
    @Autowired private val createOrganizationUseCase: CreateOrganizationUseCase,
    @Autowired private val fetchAllOrganizationsUseCase: FetchAllOrganizationsUseCase
) {

    @Value("\${featureFlag.portal.default.organization.name}")
    private lateinit var defaultOrganizationName: Optional<String>;

    @WithAdminRole
    @Transactional(propagation = Propagation.REQUIRED)
    @org.springframework.context.event.EventListener(value = [ApplicationReadyEvent::class])
    fun initOrganization() {
        defaultOrganizationName.ifPresent { organizationName ->
            if (fetchAllOrganizationsUseCase.search().none { it.name == organizationName }) {
                createOrganizationUseCase.execute(CreateOrganizationCommand(organizationName))
            }
        }
    }
}