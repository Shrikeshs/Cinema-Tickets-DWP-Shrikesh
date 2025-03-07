import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;

import static org.mockito.Mockito.when;

public class TicketServiceImplTest {

    private static TicketServiceImpl ticketService;

    @BeforeClass
    public static void setup() {



        TicketPaymentServiceImpl ticketPaymentService = Mockito.mock(TicketPaymentServiceImpl.class);;
        SeatReservationServiceImpl seatReservationService =        Mockito.mock(SeatReservationServiceImpl.class);
        //when(ticketPaymentService.makePayment(2L, )

        ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);
    }

    @Test
    public void test() {
        ticketService.purchaseTickets(2L, getTicketTypeRequests());
        ticketService.purchaseTickets(2L, null);
        assertThrows

    }

    private TicketTypeRequest[] getTicketTypeRequests() {
        TicketTypeRequest[] tickets = new TicketTypeRequest[2];
        tickets[0] = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 5);
        tickets[1] = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3);
        return tickets;
    }

}
