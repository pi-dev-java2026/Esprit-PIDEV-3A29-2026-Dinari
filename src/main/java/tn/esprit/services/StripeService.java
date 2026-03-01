package tn.esprit.services;

import com.stripe.Stripe;
import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodCreateParams;

public class StripeService {

    private static final String SECRET_KEY = System.getenv("STRIPE_SECRET_KEY");

    public static class ResultatPaiement {
        public final boolean succes;
        public final String  message;
        public final String  paymentIntentId;
        public final String  statut;

        public ResultatPaiement(boolean succes, String message,
                                String paymentIntentId, String statut) {
            this.succes          = succes;
            this.message         = message;
            this.paymentIntentId = paymentIntentId;
            this.statut          = statut;
        }
    }

    public static ResultatPaiement effectuerPaiement(
            double montantTND,
            String numeroCarte,
            String moisExp,
            String anneeExp,
            String cvv,
            String nomTitulaire) {

        if (SECRET_KEY == null || SECRET_KEY.isBlank()) {
            return new ResultatPaiement(false,
                    "Erreur : clé API Stripe manquante.",
                    null, "error");
        }

        try {
            Stripe.apiKey = SECRET_KEY;

            String carteClean = numeroCarte.replaceAll("\\s+", "");
            String tokenId = mapperToken(carteClean);

            PaymentMethodCreateParams pmParams = PaymentMethodCreateParams.builder()
                    .setType(PaymentMethodCreateParams.Type.CARD)
                    .setCard(PaymentMethodCreateParams.Token.builder()
                            .setToken(tokenId)
                            .build())
                    .setBillingDetails(PaymentMethodCreateParams.BillingDetails.builder()
                            .setName(nomTitulaire)
                            .build())
                    .build();

            PaymentMethod pm = PaymentMethod.create(pmParams);

            long centimes = Math.round(montantTND * 30);

            PaymentIntentCreateParams piParams = PaymentIntentCreateParams.builder()
                    .setAmount(centimes)
                    .setCurrency("eur")
                    .setPaymentMethod(pm.getId())
                    .setConfirm(true)
                    .setReturnUrl("https://dinari.app/success")
                    .setDescription("Abonnement Dinari — " + nomTitulaire)
                    .build();

            PaymentIntent intent = PaymentIntent.create(piParams);

            return switch (intent.getStatus()) {
                case "succeeded" -> new ResultatPaiement(
                        true,
                        "Paiement accepté ! ID : " + intent.getId(),
                        intent.getId(),
                        "succeeded");
                case "requires_action" -> new ResultatPaiement(
                        false,
                        "Authentification 3D Secure requise.",
                        intent.getId(),
                        "requires_action");
                default -> new ResultatPaiement(
                        false,
                        "Statut inattendu : " + intent.getStatus(),
                        intent.getId(),
                        intent.getStatus());
            };

        } catch (CardException e) {
            return new ResultatPaiement(false, tradireErreurCarte(e.getCode()), null, "failed");
        } catch (StripeException e) {
            return new ResultatPaiement(false, "Erreur Stripe : " + e.getMessage(), null, "error");
        } catch (Exception e) {
            return new ResultatPaiement(false, "Erreur : " + e.getMessage(), null, "error");
        }
    }

    private static String mapperToken(String carte) {
        return switch (carte) {
            case "4000000000000002" -> "tok_chargeDeclined";
            case "4000000000009995" -> "tok_chargeDeclinedInsufficientFunds";
            case "4000000000000069" -> "tok_chargeDeclinedExpiredCard";
            case "4000000000000127" -> "tok_chargeDeclinedIncorrectCvc";
            case "4000000000000101" -> "tok_chargeDeclinedProcessingError";
            default                -> "tok_visa";
        };
    }

    private static String tradireErreurCarte(String code) {
        if (code == null) return "Paiement refusé.";
        return switch (code) {
            case "card_declined"      -> "Carte refusée par la banque.";
            case "insufficient_funds" -> "Fonds insuffisants sur la carte.";
            case "incorrect_cvc"      -> "CVV incorrect.";
            case "expired_card"       -> "Carte expirée.";
            case "incorrect_number"   -> "Numéro de carte invalide.";
            case "lost_card"          -> "Carte déclarée perdue.";
            case "stolen_card"        -> "Carte déclarée volée.";
            case "do_not_honor"       -> "Transaction refusée par la banque.";
            default                   -> "Paiement refusé (" + code + ").";
        };
    }
}