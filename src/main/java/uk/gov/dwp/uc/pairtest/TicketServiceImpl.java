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

    public TicketServiceImpl(TicketPaymentServiceImpl ticketPaymentService,
                             SeatReservationServiceImpl seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    /**
     * Method to purchase tickets given account id and type of ticket.
     *
     * @param accountId          Account Id of the purchaser
     * @param ticketTypeRequests Array of TicketTypRequests
     * @throws InvalidPurchaseException
     */
    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        preFlightChecks(accountId, ticketTypeRequests);
        LOGGER.info("purchaseTickets : Processing TicketTypeRequest of length "
                + ticketTypeRequests.length);
        Map<TicketTypeRequest.Type, Integer> typeNumTicketsMap = buildTypeNumTicketsMap(ticketTypeRequests);
        validateRequests(typeNumTicketsMap);
        int totalCost = calculateTotalAmountToPay(typeNumTicketsMap);
        LOGGER.info("purchaseTickets : Processing payment request for account ID: " + accountId);
        ticketPaymentService.makePayment(accountId, totalCost);
        LOGGER.info("purchaseTickets : Processing seat reservation request for account ID: " + accountId);
        seatReservationService.reserveSeat(accountId, getTotalSeatsToAllocate(typeNumTicketsMap));

    }

    /**
     * Method to perform checks required prior to processing the request.
     *
     * @param accountId          Account Id of the purchaser
     * @param ticketTypeRequests Array of TicketTypRequests
     */
    private void preFlightChecks(Long accountId, TicketTypeRequest... ticketTypeRequests) {
        if (Objects.isNull(ticketTypeRequests)) {
            LOGGER.warning("preFlightChecks : ticketTypeRequests is null");
            throw new InvalidPurchaseException("purchaseTickets : The Given ticketTypeRequests is null");
        }

        if (accountId <= 0) {
            LOGGER.warning("preFlightChecks : Account Id is lesser than or equal to 0");
            throw new InvalidPurchaseException("Error in processing User account ID");
        }
    }

    /**
     * Method to calculate the total number of  seated to be allocated
     *
     * @param typeNumTicketsMap Map of Ticket type to number of tickets to be purchased
     * @return Total number of seats to be allocated.
     */
    private int getTotalSeatsToAllocate(Map<TicketTypeRequest.Type, Integer> typeNumTicketsMap) {
        return typeNumTicketsMap.getOrDefault(TicketTypeRequest.Type.ADULT, 0) +
                typeNumTicketsMap.getOrDefault(TicketTypeRequest.Type.CHILD, 0);
    }

    /**
     * Method to validate requests according to the given constraints.
     *
     * @param typeNumTicketsMap Map of Ticket type to number of tickets to be purchased
     */
    private void validateRequests(Map<TicketTypeRequest.Type, Integer> typeNumTicketsMap) {
        Integer adultTicketCount = typeNumTicketsMap.getOrDefault(TicketTypeRequest.Type.ADULT, 0);
        Integer childTicketCount = typeNumTicketsMap.getOrDefault(TicketTypeRequest.Type.CHILD, 0);
        Integer infantTicketCount = typeNumTicketsMap.getOrDefault(TicketTypeRequest.Type.INFANT, 0);
        int totalTicketCount = typeNumTicketsMap.values().stream().mapToInt(ticket -> ticket).sum();
        if (adultTicketCount < 0 || childTicketCount < 0 || infantTicketCount < 0) {
            LOGGER.warning("validateRequests : Negative ticket counts encountered");
            throw new InvalidPurchaseException("Error! Ticket count should be bigger than 0");
        }
        if (totalTicketCount > 25) {
            LOGGER.warning("validateRequests : Request of More than 25 tickets");
            throw new InvalidPurchaseException("Server has reached maximum number of requests to be processed");
        }
        if ((childTicketCount > 0 || infantTicketCount > 0) && adultTicketCount == 0) {
            LOGGER.warning("validateRequests : Infant or Child tickets without an Adult Ticket");
            throw new InvalidPurchaseException("Sorry! Please ensure Adult tickets are booked " +
                    "along with Child or Infant tickets");
        }
    }

    /**
     * Method to calculate th total amount to pay ( Infants ticket cost 0)
     *
     * @param typeNumTicketsMap Map of  Ticket type to number of tickets to be purchased
     * @return integer value of total amount to pay
     */
    private int calculateTotalAmountToPay(Map<TicketTypeRequest.Type, Integer> typeNumTicketsMap) {
        return (typeNumTicketsMap.getOrDefault(TicketTypeRequest.Type.ADULT, 0) * ADULT_TICKET_COST) +
                (typeNumTicketsMap.getOrDefault(TicketTypeRequest.Type.CHILD, 0) * CHILD_TICKET_COST);
    }

    /**
     * Method to build HashMap of Ticket Type -> Number of Tickets to be purchased.
     *
     * @param ticketTypeRequests var args of TicketTypeRequest
     * @return Map of  Ticket type to number of tickets to be purchased
     */
    private Map<TicketTypeRequest.Type, Integer> buildTypeNumTicketsMap(TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .collect(Collectors.toMap(
                        TicketTypeRequest::getTicketType,
                        TicketTypeRequest::getNoOfTickets,
                        Integer::sum));
    }

}
