package com.leafcutters.antbuildz.repositories;

import com.leafcutters.antbuildz.models.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

}
