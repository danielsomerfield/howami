package somerfield.howamiservice.domain

class UserEventProducer {
    fun userRegistered(userRegistrationEvent: UserRegistrationEvent) {
        //TODO: hook into kafka
    }

}

data class UserRegistrationEvent(
        val userId: String,
        val emailAddress: String,
        val confirmationCode: String
)