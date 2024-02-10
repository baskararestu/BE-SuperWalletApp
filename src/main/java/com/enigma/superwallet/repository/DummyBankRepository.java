package com.enigma.superwallet.repository;

import com.enigma.superwallet.entity.DummyBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DummyBankRepository extends JpaRepository<DummyBank,String> {
}
