package com.dd.web.carcrawler.repositories;

import com.dd.web.carcrawler.entities.TypeYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeYearRepository extends JpaRepository<TypeYear, Long> {
}
