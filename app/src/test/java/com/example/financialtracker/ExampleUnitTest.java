package com.example.financialtracker;

import org.junit.Test;
import static org.junit.Assert.*;

import com.example.financialtracker.models.Goal;
import com.example.financialtracker.models.Investment;
import com.example.financialtracker.models.User;
import com.example.financialtracker.utils.FormatUtils;

public class ExampleUnitTest {

    @Test
    public void testCurrencyFormatting() {
        String inr = FormatUtils.formatCurrency(50000.5, "INR");
        assertTrue("INR format should contain ₹", inr.contains("₹"));
        assertTrue("INR format should contain formatted amount", inr.contains("50,000.50"));

        String usd = FormatUtils.formatCurrency(1250.75, "USD");
        assertTrue("USD format should contain $", usd.contains("$"));
        assertTrue("USD format should contain formatted amount", usd.contains("1,250.75"));

        String eur = FormatUtils.formatCurrency(999.0, "EUR");
        assertTrue("EUR format should contain €", eur.contains("€"));

        String gbp = FormatUtils.formatCurrency(450.0, "GBP");
        assertTrue("GBP format should contain £", gbp.contains("£"));

        String aed = FormatUtils.formatCurrency(3500.0, "AED");
        assertTrue("AED format should contain AED", aed.contains("AED"));
    }

    @Test
    public void testDateFormatting() {
        String formatted = FormatUtils.formatDate("2026-10-14");
        assertTrue("Date format should contain Oct and 2026", formatted.contains("Oct") && formatted.contains("2026"));

        String today = FormatUtils.todayDateString();
        assertNotNull(today);
        assertTrue("Today should have yyyy-MM-dd format", today.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    public void testInvestmentCalculations() {
        Investment inv = new Investment();
        inv.setAmountInvested(100000.0);
        inv.setCurrentValue(125000.0);

        assertEquals(25000.0, inv.getProfitLoss(), 0.001);
        assertEquals(25.0, inv.getProfitLossPercentage(), 0.001);

        // Test loss
        Investment lossInv = new Investment();
        lossInv.setAmountInvested(50000.0);
        lossInv.setCurrentValue(40000.0);

        assertEquals(-10000.0, lossInv.getProfitLoss(), 0.001);
        assertEquals(-20.0, lossInv.getProfitLossPercentage(), 0.001);
    }

    @Test
    public void testGoalProgressPercentage() {
        Goal goal = new Goal();
        goal.setTargetAmount(200000.0);
        goal.setSavedAmount(50000.0);

        assertEquals(25, goal.getProgressPercent());

        // Test completed goal (clamped at 100%)
        goal.setSavedAmount(250000.0);
        assertEquals(100, goal.getProgressPercent());

        // Test 0 target edge case
        Goal zeroGoal = new Goal();
        zeroGoal.setTargetAmount(0);
        assertEquals(0, zeroGoal.getProgressPercent());
    }

    @Test
    public void testUserRoleCheck() {
        User regularUser = new User();
        regularUser.setRole("user");
        assertFalse(regularUser.isAdmin());

        User adminUser = new User();
        adminUser.setRole("admin");
        assertTrue(adminUser.isAdmin());

        User adminUpper = new User();
        adminUpper.setRole("ADMIN");
        assertTrue(adminUpper.isAdmin());
    }
}