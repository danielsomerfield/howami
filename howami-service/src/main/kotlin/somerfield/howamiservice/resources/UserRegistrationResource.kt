package somerfield.howamiservice.resources

import somerfield.howamiservice.wire.*
import javax.validation.Valid
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/api/v1/user-registrations")
public class UserRegistrationResource() {

    @POST()
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    fun register(@Valid command: CommandWireType<UserRegistrationWireType>): Response {
        return Response.ok(
                CommandResponseWireType(
                        CommandResponseHeaderWireType(command.header.requestId),
                        UserRegistrationResponseWireType("NYI")
                )
        ).build()
    }

//    private fun fromWireType(orderWireType: OrderWireType): Order {
//        return Order(
//            orderWireType.items.map { fromWireType(it) }
//        )
//    }
//
//    private fun fromWireType(orderItemWireType: OrderItemWireType) = OrderItem(
//            itemCode = orderItemWireType.itemCode,
//            quantity = orderItemWireType.quantity
//    )

}