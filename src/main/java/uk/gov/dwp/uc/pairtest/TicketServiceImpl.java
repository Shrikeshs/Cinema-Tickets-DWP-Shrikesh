package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */

    private final TicketPaymentServiceImpl ticketPaymentService;
    private final SeatReservationServiceImpl seatReservationService;
    private final int ADULT_TICKET_COST = 25;
    private final int CHILD_TICKET_COST = 15;
    private final Logger LOGGER = Logger.getLogger(TicketServiceImpl.class.getName());

    public TicketServiceImpl(TicketPaymentServiceImpl ticketPaymentService, SeatReservationServiceImpl seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }


    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

        if (Objects.isNull(ticketTypeRequests)) {

            throw new IllegalArgumentException("sdasd");
        }
        LOGGER.info("TicketServiceImpl : Processing TicketTypeRequest of length "
                + ticketTypeRequests.length);
        preflightChecks(ticketTypeRequests.length, accountId);
        Map<TicketTypeRequest.Type, Integer> typeNumTicketsMap = buildTypeNumTicketsMap(ticketTypeRequests);
        validateRequests(typeNumTicketsMap);

//                        Considers the above objective, business rules, constraints & assumptions.
//
//                        Calculates the correct amount for the requested tickets and makes a
//                        payment request to the `TicketPaymentService
//
//                        Calculates the correct no of seats to reserve and
//                        makes a seat reservation request to the `SeatReservationService
//
//                        Rejects any invalid ticket purchase requests.
//                        It is up to you to identify what should be deemed as an invalid purchase request.

        int totalCost = calculateTotalAmountToPay(typeNumTicketsMap);
        ticketPaymentService.makePayment(accountId, totalCost);
        seatReservationService.reserveSeat(accountId, getTotalSeatsToAllocate(typeNumTicketsMap));

    }

    private int getTotalSeatsToAllocate(Map<TicketTypeRequest.Type, Integer> typeIntegerMap) {
        return typeIntegerMap.getOrDefault(TicketTypeRequest.Type.ADULT, 0) +
                typeIntegerMap.getOrDefault(TicketTypeRequest.Type.CHILD, 0);
    }

    private void validateRequests(Map<TicketTypeRequest.Type, Integer> typeIntegerMap) {
        Integer adultTicketCount = typeIntegerMap.getOrDefault(TicketTypeRequest.Type.ADULT, 0);
        Integer childTicketCount = typeIntegerMap.getOrDefault(TicketTypeRequest.Type.CHILD, 0);
        Integer infantTicketCount = typeIntegerMap.getOrDefault(TicketTypeRequest.Type.INFANT, 0);
        if ((childTicketCount > 0 && adultTicketCount == 0) || (infantTicketCount > 0 && adultTicketCount == 0)) {
            throw new IllegalArgumentException("Server has reached maximum number of requests to be processed");
        }
    }

    private void preflightChecks(int requestLength, Long accountId) {
        if (requestLength > 25) {
            throw new IllegalArgumentException("Server has reached maximum number of requests to be processed");
        }
        if (accountId < 0) {
            throw new IllegalArgumentException("sdasd");
        }
    }

    private int calculateTotalAmountToPay(Map<TicketTypeRequest.Type, Integer> typeNumTicketsMap) {
        return (typeNumTicketsMap.getOrDefault(TicketTypeRequest.Type.ADULT, 0) * ADULT_TICKET_COST) +
                (typeNumTicketsMap.getOrDefault(TicketTypeRequest.Type.CHILD, 0) * CHILD_TICKET_COST);
    }

    private Map<TicketTypeRequest.Type, Integer> buildTypeNumTicketsMap(TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .collect(Collectors.toMap(
                        TicketTypeRequest::getTicketType,
                        TicketTypeRequest::getNoOfTickets,
                        Integer::sum));
    }

}
