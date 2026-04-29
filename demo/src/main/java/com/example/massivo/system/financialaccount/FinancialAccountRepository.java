package com.example.massivo.system.financialaccount;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancialAccountRepository extends JpaRepository<FinancialAccount, UUID> {
    List<FinancialAccount> findAllByCompanyId(UUID companyId);
    Optional<FinancialAccount> findByCompanyIdAndAccountNumber(UUID companyId, String accountNumber);
}
