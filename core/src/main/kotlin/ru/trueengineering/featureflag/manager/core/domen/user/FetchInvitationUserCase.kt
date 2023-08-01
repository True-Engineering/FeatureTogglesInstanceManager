package ru.trueengineering.featureflag.manager.core.domen.user

interface FetchInvitationUserCase {

    fun search(query: FetchInvitationQuery) : Invitation
}

data class FetchInvitationQuery(val projectId: Long)