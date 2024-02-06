package com.enigma.superwallet.repository;

import com.enigma.superwallet.dto.response.CustomerResponse;
import com.enigma.superwallet.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    @Query("SELECT c FROM Customer c JOIN c.userCredential u WHERE u.id = :userCredentialId")
    Customer findCustomerByUserCredentialId(@Param("userCredentialId") String userCredentialId);
}
