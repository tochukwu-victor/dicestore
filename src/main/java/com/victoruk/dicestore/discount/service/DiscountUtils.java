package com.victoruk.dicestore.discount.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import com.victoruk.dicestore.discount.entity.Discount;

public class DiscountUtils {

    private DiscountUtils() {} // prevent instantiation

    /**
     * Determines if a discount should be considered active based on start/end dates.
     */
    public static boolean calculateActiveStatus(Discount discount) {
        if (discount == null) return false;

        LocalDate now = LocalDate.now();
        return (discount.getStartDate() == null || !now.isBefore(discount.getStartDate()))
                && (discount.getEndDate() == null || !now.isAfter(discount.getEndDate()));
    }


    /**
     * Calculate discounted price given a base price and discount.
     */
    public static BigDecimal applyDiscount(BigDecimal basePrice, Discount discount) {
        if (discount == null || !calculateActiveStatus(discount)) {
            return basePrice;
        }

        BigDecimal discountAmount = basePrice.multiply(
                discount.getPercentage().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
        );

        return basePrice.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
    }

}
