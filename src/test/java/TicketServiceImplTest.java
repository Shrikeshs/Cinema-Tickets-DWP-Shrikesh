import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;

public class TicketServiceImplTest {

    private static TicketServiceImpl ticketService;

    @BeforeClass
    public static void setup() {

        ticketService = new TicketServiceImpl(new TicketPaymentServiceImpl(), new SeatReservationServiceImpl());
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
