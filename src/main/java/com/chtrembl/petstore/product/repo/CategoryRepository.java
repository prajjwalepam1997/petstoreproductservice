package com.chtrembl.petstore.product.repo;

import com.chtrembl.petstore.product.dao.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
