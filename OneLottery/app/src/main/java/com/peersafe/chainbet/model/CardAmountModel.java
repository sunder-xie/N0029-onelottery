package com.peersafe.chainbet.model;

import com.peersafe.chainbet.db.WithdrawBankCard;

/**
 * Created by sunhaitao on 17/4/9.
 */

public class CardAmountModel
{
    WithdrawBankCard bankCard;
    Double amount;

    public CardAmountModel(WithdrawBankCard bankCard, double amount)
    {
        this.bankCard = bankCard;
        this.amount = amount;
    }

    public WithdrawBankCard getBankCard()
    {
        return bankCard;
    }

    @Override
    public String toString()
    {
        return "CardAmountModel{" +
                "bankCard=" + bankCard +
                ", amount=" + amount +
                '}';
    }

    public void setBankCard(WithdrawBankCard bankCard)
    {
        this.bankCard = bankCard;
    }

    public CardAmountModel()
    {
    }

    public double getAmount()
    {
        return amount;
    }

    public void setAmount(double amount)
    {
        this.amount = amount;
    }
}
