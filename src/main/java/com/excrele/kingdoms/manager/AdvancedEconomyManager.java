package com.excrele.kingdoms.manager;

import com.excrele.kingdoms.KingdomsPlugin;
import com.excrele.kingdoms.model.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages advanced economy features: loans, salaries, investments, sanctions, embargoes, exchange rates
 */
public class AdvancedEconomyManager {
    private final KingdomsPlugin plugin;
    private final Map<String, KingdomLoan> loans; // loanId -> loan
    private final Map<String, List<String>> kingdomLoans; // kingdom -> loan IDs
    private final Map<String, MemberSalary> salaries; // player -> salary
    private final Map<String, List<String>> kingdomSalaries; // kingdom -> player names
    private final Map<String, KingdomInvestment> investments; // investmentId -> investment
    private final Map<String, List<String>> kingdomInvestments; // kingdom -> investment IDs
    private final Map<String, EconomicSanction> sanctions; // sanctionId -> sanction
    private final Map<String, List<String>> kingdomSanctions; // kingdom -> sanction IDs (imposed)
    private final Map<String, List<String>> targetSanctions; // kingdom -> sanction IDs (target)
    private final Map<String, CurrencyExchangeRate> exchangeRates; // fromCurrency:toCurrency -> rate
    
    public AdvancedEconomyManager(KingdomsPlugin plugin) {
        this.plugin = plugin;
        this.loans = new ConcurrentHashMap<>();
        this.kingdomLoans = new ConcurrentHashMap<>();
        this.salaries = new ConcurrentHashMap<>();
        this.kingdomSalaries = new ConcurrentHashMap<>();
        this.investments = new ConcurrentHashMap<>();
        this.kingdomInvestments = new ConcurrentHashMap<>();
        this.sanctions = new ConcurrentHashMap<>();
        this.kingdomSanctions = new ConcurrentHashMap<>();
        this.targetSanctions = new ConcurrentHashMap<>();
        this.exchangeRates = new ConcurrentHashMap<>();
    }
    
    /**
     * Create a loan for a kingdom
     */
    public KingdomLoan createLoan(String kingdomName, String lenderKingdom, double amount, 
                                  double interestRate, long durationSeconds, long paymentInterval) {
        String loanId = UUID.randomUUID().toString().substring(0, 8);
        KingdomLoan loan = new KingdomLoan(loanId, kingdomName, lenderKingdom, amount, 
                                          interestRate, durationSeconds, paymentInterval);
        
        loans.put(loanId, loan);
        kingdomLoans.computeIfAbsent(kingdomName, k -> new ArrayList<>()).add(loanId);
        
        // Transfer funds
        if (lenderKingdom != null) {
            // Kingdom-to-kingdom loan
            if (plugin.getBankManager() != null) {
                double lenderBalance = plugin.getBankManager().getBalance(lenderKingdom);
                if (lenderBalance >= amount) {
                    plugin.getBankManager().withdraw(lenderKingdom, amount);
                    plugin.getBankManager().deposit(kingdomName, amount);
                }
            }
        } else {
            // Bank loan - just deposit to kingdom
            if (plugin.getBankManager() != null) {
                plugin.getBankManager().deposit(kingdomName, amount);
            }
        }
        
        return loan;
    }
    
    /**
     * Process loan payments
     */
    public void processLoanPayments() {
        for (KingdomLoan loan : loans.values()) {
            if (loan.isPaidOff() || loan.isOverdue()) continue;
            
            if (loan.isPaymentDue()) {
                double paymentAmount = loan.calculateNextPayment();
                
                // Try to pay from kingdom bank
                if (plugin.getBankManager() != null) {
                    double balance = plugin.getBankManager().getBalance(loan.getKingdomName());
                    if (balance >= paymentAmount) {
                        plugin.getBankManager().withdraw(loan.getKingdomName(), paymentAmount);
                        
                        // Pay to lender or bank
                        if (loan.getLenderKingdom() != null) {
                            plugin.getBankManager().deposit(loan.getLenderKingdom(), paymentAmount);
                        }
                        
                        loan.setRemainingBalance(loan.getRemainingBalance() - paymentAmount);
                        loan.setLastPaymentTime(System.currentTimeMillis() / 1000);
                    }
                }
            }
        }
    }
    
    /**
     * Set a member salary
     */
    public void setMemberSalary(String player, String kingdomName, double amount, long paymentInterval) {
        MemberSalary salary = new MemberSalary(player, kingdomName, amount, paymentInterval);
        salaries.put(player, salary);
        kingdomSalaries.computeIfAbsent(kingdomName, k -> new ArrayList<>()).add(player);
    }
    
    /**
     * Remove a member salary
     */
    public void removeMemberSalary(String player) {
        MemberSalary salary = salaries.remove(player);
        if (salary != null) {
            List<String> kingdomSal = kingdomSalaries.get(salary.getKingdomName());
            if (kingdomSal != null) {
                kingdomSal.remove(player);
            }
        }
    }
    
    /**
     * Process salary payments
     */
    public void processSalaryPayments() {
        for (MemberSalary salary : salaries.values()) {
            if (!salary.isActive()) continue;
            
            if (salary.isPaymentDue()) {
                // Pay from kingdom bank
                if (plugin.getBankManager() != null) {
                    double balance = plugin.getBankManager().getBalance(salary.getKingdomName());
                    if (balance >= salary.getAmount()) {
                        plugin.getBankManager().withdraw(salary.getKingdomName(), salary.getAmount());
                        
                        // Pay to player
                        org.bukkit.entity.Player player = plugin.getServer().getPlayer(salary.getPlayer());
                        if (player != null && player.isOnline() && 
                            com.excrele.kingdoms.util.EconomyManager.isEnabled()) {
                            com.excrele.kingdoms.util.EconomyManager.deposit(player, salary.getAmount());
                        }
                        
                        salary.setLastPaymentTime(System.currentTimeMillis() / 1000);
                    }
                }
            }
        }
    }
    
    /**
     * Create an investment
     */
    public KingdomInvestment createInvestment(String kingdomName, KingdomInvestment.InvestmentType type, 
                                             double principal, double expectedReturnRate, long durationSeconds) {
        String investmentId = UUID.randomUUID().toString().substring(0, 8);
        KingdomInvestment investment = new KingdomInvestment(investmentId, kingdomName, type, 
                                                            principal, expectedReturnRate, durationSeconds);
        
        investments.put(investmentId, investment);
        kingdomInvestments.computeIfAbsent(kingdomName, k -> new ArrayList<>()).add(investmentId);
        
        // Deduct from kingdom bank
        if (plugin.getBankManager() != null) {
            plugin.getBankManager().withdraw(kingdomName, principal);
        }
        
        return investment;
    }
    
    /**
     * Process investments and check maturity
     */
    public void processInvestments() {
        for (KingdomInvestment investment : investments.values()) {
            if (investment.checkMaturity() && !investment.isMatured()) {
                // Investment matured, return funds
            if (plugin.getBankManager() != null) {
                double returnAmount = investment.calculateCurrentValue();
                plugin.getBankManager().deposit(investment.getKingdomName(), returnAmount);
            }
            }
        }
    }
    
    /**
     * Impose an economic sanction
     */
    public EconomicSanction imposeSanction(String imposingKingdom, String targetKingdom, 
                                          EconomicSanction.SanctionType type, double penaltyRate, 
                                          long durationSeconds, String reason) {
        String sanctionId = UUID.randomUUID().toString().substring(0, 8);
        EconomicSanction sanction = new EconomicSanction(sanctionId, imposingKingdom, targetKingdom, 
                                                        type, penaltyRate, durationSeconds, reason);
        
        sanctions.put(sanctionId, sanction);
        kingdomSanctions.computeIfAbsent(imposingKingdom, k -> new ArrayList<>()).add(sanctionId);
        targetSanctions.computeIfAbsent(targetKingdom, k -> new ArrayList<>()).add(sanctionId);
        
        return sanction;
    }
    
    /**
     * Check if a transaction should be penalized due to sanctions
     */
    public double calculateSanctionPenalty(String fromKingdom, String toKingdom, double amount) {
        double totalPenalty = 0;
        
        // Check sanctions from toKingdom against fromKingdom
        List<String> sanctionsList = targetSanctions.get(toKingdom);
        if (sanctionsList != null) {
            for (String sanctionId : sanctionsList) {
                EconomicSanction sanction = sanctions.get(sanctionId);
                if (sanction != null && !sanction.isExpired() && 
                    sanction.getImposingKingdom().equals(fromKingdom)) {
                    totalPenalty += sanction.calculatePenalty(amount);
                }
            }
        }
        
        return totalPenalty;
    }
    
    /**
     * Set currency exchange rate
     */
    public void setExchangeRate(String fromCurrency, String toCurrency, double rate) {
        String key = fromCurrency + ":" + toCurrency;
        exchangeRates.put(key, new CurrencyExchangeRate(fromCurrency, toCurrency, rate));
    }
    
    /**
     * Get currency exchange rate
     */
    public CurrencyExchangeRate getExchangeRate(String fromCurrency, String toCurrency) {
        String key = fromCurrency + ":" + toCurrency;
        return exchangeRates.get(key);
    }
    
    /**
     * Convert currency
     */
    public double convertCurrency(double amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }
        
        CurrencyExchangeRate rate = getExchangeRate(fromCurrency, toCurrency);
        if (rate != null) {
            return rate.convert(amount);
        }
        
        // Try reverse rate
        CurrencyExchangeRate reverseRate = getExchangeRate(toCurrency, fromCurrency);
        if (reverseRate != null) {
            return amount / reverseRate.getRate();
        }
        
        return amount; // No rate found, return original
    }
    
    /**
     * Get all loans for a kingdom
     */
    public List<KingdomLoan> getKingdomLoans(String kingdomName) {
        List<String> loanIds = kingdomLoans.get(kingdomName);
        if (loanIds == null) return new ArrayList<>();
        
        List<KingdomLoan> result = new ArrayList<>();
        for (String loanId : loanIds) {
            KingdomLoan loan = loans.get(loanId);
            if (loan != null) {
                result.add(loan);
            }
        }
        return result;
    }
    
    /**
     * Get all investments for a kingdom
     */
    public List<KingdomInvestment> getKingdomInvestments(String kingdomName) {
        List<String> investmentIds = kingdomInvestments.get(kingdomName);
        if (investmentIds == null) return new ArrayList<>();
        
        List<KingdomInvestment> result = new ArrayList<>();
        for (String investmentId : investmentIds) {
            KingdomInvestment investment = investments.get(investmentId);
            if (investment != null) {
                result.add(investment);
            }
        }
        return result;
    }
    
    /**
     * Get all sanctions affecting a kingdom
     */
    public List<EconomicSanction> getKingdomSanctions(String kingdomName) {
        List<String> sanctionIds = targetSanctions.get(kingdomName);
        if (sanctionIds == null) return new ArrayList<>();
        
        List<EconomicSanction> result = new ArrayList<>();
        for (String sanctionId : sanctionIds) {
            EconomicSanction sanction = sanctions.get(sanctionId);
            if (sanction != null && !sanction.isExpired()) {
                result.add(sanction);
            }
        }
        return result;
    }
}

