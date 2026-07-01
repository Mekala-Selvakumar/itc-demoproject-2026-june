package com.finance.fee.service;

import com.finance.fee.model.Customer;
import com.finance.fee.model.FeeRecord;
import com.finance.fee.repository.FeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service 
@RequiredArgsConstructor 
@Slf4j
public class FeeCalculatorService {

    private final FeeCalculator  feeCalculator;
    private final FeeRepository  feeRepository;

    public FeeRecord calculateAndSave(Customer customer, double amount) {
        double fee = feeCalculator.calculateFee(amount, customer.getTier(), customer.isPremium());
        FeeRecord record = FeeRecord.builder()
                .customerId(customer.getCustomerId())
                .amount(amount).tier(customer.getTier())
                .isPremium(customer.isPremium()).fee(fee).build();
        log.info("Saving fee record: {}", record);
        return feeRepository.save(record);
    }
}
