package com.finance.fee.repository;
import com.finance.fee.model.FeeRecord;
import java.util.List;

public interface FeeRepository {
    FeeRecord save(FeeRecord record);
    List<FeeRecord> findByCustomerId(String customerId);
}
