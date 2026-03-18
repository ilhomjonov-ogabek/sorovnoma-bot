package spring.boot.bankaccountmanagementsystem.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import spring.boot.bankaccountmanagementsystem.entity.Account;

public interface AccountRepository extends JpaRepository<Account, UUID> {

}