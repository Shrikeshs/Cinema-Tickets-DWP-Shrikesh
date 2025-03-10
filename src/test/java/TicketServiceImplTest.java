
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TicketServiceImplTest {

    private static TicketServiceImpl ticketService;
    private static TicketPaymentServiceImpl ticketPaymentService;
    private static SeatReservationServiceImpl seatReservationService;
    private static final Long ACCOUNT_ID = 3L;
    private static final int TOTAL_COST = 170;
    private static final int SEATS_TO_BE_RESERVED = 8;
    private static final String NULL_ERROR_MESSAGE = "purchaseTickets : The Given ticketTypeRequests is null";
    private static final String INFANT_ERROR_MESSAGE = "Sorry! Please ensure Adult tickets are booked " +
            "along with Child or Infant tickets";
    private static final String MAX_REACHED_MESSAGE = "Server has reached maximum number of requests to be processed";
    private static final String ACCOUNT_ID_MESSAGE = "Error in processing User account ID";
    private static final String NEGATIVE_COUNT_MESSAGE = "Error! Ticket count should be bigger than 0";


    @BeforeClass
    public static void setup() {
        ticketPaymentService = Mockito.mock(TicketPaymentServiceImpl.class);
        seatReservationService = Mockito.mock(SeatReservationServiceImpl.class);
        doNothing().when(ticketPaymentService).makePayment(ACCOUNT_ID, TOTAL_COST);
        doNothing().when(seatReservationService).reserveSeat(ACCOUNT_ID, SEATS_TO_BE_RESERVED);
        ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);
    }

    @Test
    public void test() {
        // Assertion to be done following logs
        ticketService.purchaseTickets(2L, getTicketTypeRequests());

        // Assertion to be done following logs ( Var args )
        ticketService.purchaseTickets(545455L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 12));

        // Assertion to check infant validation ( Var args )
        InvalidPurchaseException argumentException = assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(2435L,
                        new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 12)));
        assertEquals(INFANT_ERROR_MESSAGE, argumentException.getMessage());

        // Assertion to validate null checks
        InvalidPurchaseException exception = assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(2L, null));
        assertEquals(NULL_ERROR_MESSAGE, exception.getMessage());

        // Assertion to validate Child or Infant booking without booking adult
        InvalidPurchaseException exception2 = assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(2L, getTicketTypeRequestWithoutAdult()));
        assertEquals(INFANT_ERROR_MESSAGE, exception2.getMessage());

        // Assertion to validate booking tickets more than 25
        InvalidPurchaseException exception3 = assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(2L, getTicketTypeRequestWithMax()));
        assertEquals(MAX_REACHED_MESSAGE, exception3.getMessage());

        // Assertion to validate account id
        InvalidPurchaseException exception4 = assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(0L, getTicketTypeRequests()));
        assertEquals(ACCOUNT_ID_MESSAGE, exception4.getMessage());


        InvalidPurchaseException exception5 = assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(555L, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, -1)));
        assertEquals(NEGATIVE_COUNT_MESSAGE, exception5.getMessage());

        // Assertion to validate 25 tickets
        ticketService.purchaseTickets(2L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 10),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 5));

        Map<TicketTypeRequest.Type, Integer> typeIntegerMap = buildTypeNumTicketsMap(
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10),
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 5));


    }

    private TicketTypeRequest[] getTicketTypeRequests() {
        TicketTypeRequest[] tickets = new TicketTypeRequest[2];
        tickets[0] = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 5);
        tickets[1] = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3);
        return tickets;
    }

    private TicketTypeRequest[] getTicketTypeRequestWithoutAdult() {
        TicketTypeRequest[] tickets = new TicketTypeRequest[2];
        tickets[0] = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 5);
        tickets[1] = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3);
        return tickets;
    }

    private TicketTypeRequest[] getTicketTypeRequestWithMax() {
        TicketTypeRequest[] tickets = new TicketTypeRequest[2];
        tickets[0] = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 50);
        tickets[1] = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3);
        return tickets;
    }

    private Map<TicketTypeRequest.Type, Integer> buildTypeNumTicketsMap(TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .collect(Collectors.toMap(
                        TicketTypeRequest::getTicketType,
                        TicketTypeRequest::getNoOfTickets,
                        Integer::sum));
    }

}
