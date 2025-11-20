package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    private final Invoice invoice;
    private final Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    private int getAmount(Performance performance) {
        Play play = plays.get(performance.getPlayID());
        int result = 0;
        switch (play.getType()) {
            case "tragedy":
                result = Constants.TRAGEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.TRAGEDY_AUDIENCE_THRESHOLD);
                }
                break;
            case "comedy":
                result = Constants.COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                result += Constants.COMEDY_AMOUNT_PER_AUDIENCE * performance.getAudience();
                break;
            default:
                throw new RuntimeException(String.format("unknown type: %s", play.getType()));
        }
        return result;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        final StringBuilder result = new StringBuilder("Statement for " 
            + invoice.getCustomer() + System.lineSeparator());

        for (Performance p : invoice.getPerformances()) {
            // print line for this order
            result.append(String.format("  %s: %s (%s seats)%n", 
                plays.get(p.getPlayID()).getName(), usd(getAmount(p)), p.getAudience()));
        }
        result.append(String.format("Amount owed is %s%n", usd(getTotalAmount())));
        result.append(String.format("You earned %s credits%n", getTotalVolumeCredits()));
        return result.toString();
    }

    private int getVolumeCredits(Performance performance) {
        int credits = Math.max(performance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
        if ("comedy".equals(plays.get(performance.getPlayID()).getType())) {
            credits += performance.getAudience() / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }
        return credits;
    }

    private int getTotalAmount() {
        int total = 0;
        for (Performance p : invoice.getPerformances()) {
            total += getAmount(p);
        }
        return total;
    }

    private int getTotalVolumeCredits() {
        int total = 0;
        for (Performance p : invoice.getPerformances()) {
            total += getVolumeCredits(p);
        }
        return total;
    }

    private String usd(int amount) {
        final NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        return formatter.format(amount / (double) Constants.PERCENT_FACTOR);
    }
}
