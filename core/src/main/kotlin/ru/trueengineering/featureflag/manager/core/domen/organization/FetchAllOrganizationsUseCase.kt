package ru.trueengineering.featureflag.manager.core.domen.organization

interface FetchAllOrganizationsUseCase {

    fun search() : List<Organization>

}