package com.iftm.client.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.iftm.client.entities.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
	
	@Query("SELECT DISTINCT obj FROM Client obj WHERE "
			+ "obj.income >= :income")
	Page<Client> findByIncome(Double income, Pageable pageable);
	
}
