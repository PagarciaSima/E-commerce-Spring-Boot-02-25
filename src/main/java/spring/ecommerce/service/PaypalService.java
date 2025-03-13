package spring.ecommerce.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for handling PayPal payment operations.
 */
@Service
@AllArgsConstructor
@Slf4j
public class PaypalService {

    private final APIContext apiContext;

    /**
     * Creates a PayPal payment.
     *
     * @param total       the total amount of the payment
     * @param currency    the currency of the payment (e.g., "USD")
     * @param method      the payment method (e.g., "paypal")
     * @param intent      the payment intent (e.g., "sale", "authorize")
     * @param description a description of the payment
     * @param cancelUrl   the URL to redirect the user to if the payment is canceled
     * @param successUrl  the URL to redirect the user to if the payment is successful
     * @return the created Payment object
     * @throws PayPalRESTException if an error occurs during payment creation
     */
    public Payment createPayment(
            Double total,
            String currency,
            String method,
            String intent,
            String description,
            String cancelUrl,
            String successUrl
    ) throws PayPalRESTException {
        log.info("Starting payment creation: total={}, currency={}, method={}, intent={}",
                total, currency, method, intent);

        Amount amount = new Amount();
        amount.setCurrency(currency);
        amount.setTotal(String.format(Locale.forLanguageTag(currency), "%.2f", total));

        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod(method);

        Payment payment = new Payment();
        payment.setIntent(intent);
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setReturnUrl(successUrl);
        redirectUrls.setCancelUrl(cancelUrl);
        payment.setRedirectUrls(redirectUrls);

        return payment.create(apiContext);
    }

    /**
     * Executes an approved PayPal payment.
     *
     * @param paymentId the ID of the payment to execute
     * @param payerId   the ID of the payer who is approving the payment
     * @return the executed Payment object
     * @throws PayPalRESTException if an error occurs during payment execution
     */
    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        log.info("Executing payment: paymentId={}, payerId={}", paymentId, payerId);

        Payment payment = new Payment();
        payment.setId(paymentId);

        PaymentExecution execution = new PaymentExecution();
        execution.setPayerId(payerId);
        return payment.execute(apiContext, execution);
    }
}
