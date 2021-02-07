package com.evandefd.logiccircuitsimulator.view.component

import com.evandefd.logiccircuitsimulator.view.exception.ContactAlreadyConnectedException
import com.evandefd.logiccircuitsimulator.view.exception.ContactSameGenderException
import java.io.Serializable
import java.lang.IllegalStateException
import java.util.*

abstract class Connectible : Serializable{

    data class Contact(
        val id: Int,
        val nickname: String,
        val gender: Int,
        val actualPositionX: Float,
        val actualPositionY: Float,
        val uuid: UUID = UUID.randomUUID()
    ) : Serializable {


        var contact : Contact? = null

        companion object {
            const val GENDER_UNSPECIFIED = 0
            const val GENDER_MALE = 1
            const val GENDER_FEMALE = 2
        }
    }

    /**
     * The contacts(like pinholes) of this circuit. It is connected by another contacts or wire
     */
    abstract val contacts: List<Contact>

    fun connect(srcContact: Contact, dstContact: Contact) {
        if(srcContact.contact != null) throw ContactAlreadyConnectedException()
        if(dstContact.contact != null) throw ContactAlreadyConnectedException()

        if(srcContact.gender == Contact.GENDER_MALE) {
            if(dstContact.gender == Contact.GENDER_MALE) throw ContactSameGenderException()
        } else if(srcContact.gender == Contact.GENDER_FEMALE) {
            if(dstContact.gender == Contact.GENDER_FEMALE) throw ContactSameGenderException()
        }

        srcContact.contact = dstContact
        dstContact.contact = srcContact
    }

    fun disconnect(contact: Contact) {
        contact.contact?.contact = null
        contact.contact = null
    }
}