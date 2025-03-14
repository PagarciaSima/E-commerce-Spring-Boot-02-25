package spring.ecommerce.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spring.ecommerce.dto.DataPaymentDto;
import spring.ecommerce.dto.UrlPaypalResponseDto;
import spring.ecommerce.service.PaypalService;

/**
 * Controller for handling PayPal payment operations.
 * Provides endpoints to create payments and handle payment statuses.
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/payments")
@Slf4j
@Tag(name = "Paypal", description = "API for managing Paypal endpoints")
public class PaypalController {

    private final PaypalService paypalService;
    private final String SUCCESS_URL = "http://localhost:8081/api/v1/payments/success";
    private final String CANCEL_URL = "http://localhost:8081/api/v1/payments/cancel";
    private final String ERROR_URL = "http://localhost:8081/api/v1/payments/error"; 

    /**
     * Creates a PayPal payment and returns the approval URL.
     *
     * @param dataPayment The payment details received from the frontend.
     * @return The PayPal approval URL or a default URL if an error occurs.
     */
    @Operation(
	    summary = "Create a PayPal payment",
	    description = "Creates a PayPal payment and returns the approval URL for the user to complete the transaction. Requires authentication.",
	    security = @SecurityRequirement(name = "bearerAuth"),
	    responses = {
	        @ApiResponse(
	            responseCode = "200",
	            description = "Payment created successfully, returns the approval URL",
	            content = @Content(
	                mediaType = "application/json",
	                schema = @Schema(implementation = UrlPaypalResponseDto.class)
	            )
	        ),
	        @ApiResponse(
	            responseCode = "400",
	            description = "Invalid amount format"
	        ),
	        @ApiResponse(
	            responseCode = "500",
	            description = "Internal server error when creating the PayPal payment"
	        )
	    }
	)
    @PostMapping
    public UrlPaypalResponseDto createPayment(@RequestBody DataPaymentDto dataPayment) {
        log.info("Received payment request: {}", dataPayment);

        try {
            Payment payment = paypalService.createPayment(
                Double.valueOf(dataPayment.getAmount()), 
                dataPayment.getCurrency(),
                dataPayment.getMethod(),
                "SALE",
                dataPayment.getDescription(),
                CANCEL_URL,
                SUCCESS_URL
            );

            log.info("Payment created successfully: {}", payment.getId());

            String approvalUrl = payment.getLinks().stream()
                    .filter(link -> "approval_url".equals(link.getRel()))
                    .map(link -> link.getHref())
                    .findFirst()
                    .orElse("");

            return new UrlPaypalResponseDto(approvalUrl);
        } catch (NumberFormatException e) {
            log.error("Invalid amount format: {}", dataPayment.getAmount(), e);
        } catch (PayPalRESTException e) {
            log.error("Error creating PayPal payment", e);
        }

        return new UrlPaypalResponseDto("http://localhost:4200");
    }
    
    /**
     * Handles successful PayPal payments.
     * Executes the payment and redirects the user based on the payment state.
     *
     * @param paymentId The PayPal payment ID.
     * @param payerId The PayPal payer ID.
     * @return A redirect to the appropriate URL based on payment success or failure.
     */
    @Operation(
	    summary = "Handle PayPal payment success",
	    description = "Executes a PayPal payment after user approval and redirects to the success or error page.",
	    responses = {
	        @ApiResponse(
	            responseCode = "302",
	            description = "Redirects the user to the success or error page depending on the payment state"
	        ),
	        @ApiResponse(
	            responseCode = "500",
	            description = "Internal server error while executing the PayPal payment"
	        )
	    }
	)
    @GetMapping("/success")
    public RedirectView paymentSuccess(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId
    ) {
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);
            if ("approved".equals(payment.getState())) {
                return new RedirectView("http://localhost:4200/payment/success");
            } else {
                return new RedirectView(ERROR_URL);
            }
        } catch (PayPalRESTException e) {
            log.error("Error while executing payment :: paymentSuccess ", e);
            return new RedirectView(ERROR_URL);  
        }
    }

    /**
     * Handles canceled PayPal payments.
     * Redirects the user to the home page upon payment cancellation.
     *
     * @return A redirect to the home page.
     */
    @Operation(
	    summary = "Handle PayPal payment cancellation",
	    description = "Redirects the user to the home page when a PayPal payment is cancelled.",
	    responses = {
	        @ApiResponse(
	            responseCode = "302",
	            description = "Redirects the user to the home page"
	        )
	    }
	)
    @GetMapping("/cancel")
    public RedirectView paymentCancelled() {
        log.info("Payment cancelled");
        return new RedirectView("http://localhost:4200");  
    }

    /**
     * Handles errors during PayPal payments.
     * Redirects the user to the error page upon payment failure.
     *
     * @return A redirect to the error page.
     */
    @Operation(
	    summary = "Handle PayPal payment errors",
	    description = "Redirects the user to the error page when a PayPal payment fails.",
	    responses = {
	        @ApiResponse(
	            responseCode = "302",
	            description = "Redirects the user to the payment error page"
	        )
	    }
	)
    @GetMapping("/error")
    public RedirectView paymentError() {
        log.info("Payment error");
        return new RedirectView("http://localhost:4200/payment/error");  
    }
}
