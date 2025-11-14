package com.chtrembl.petstore.product.repo;

import com.chtrembl.petstore.product.dao.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStatusIn(List<String> status);
}
