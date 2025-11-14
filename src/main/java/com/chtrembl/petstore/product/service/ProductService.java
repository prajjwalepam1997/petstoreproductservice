package com.chtrembl.petstore.product.service;

import com.chtrembl.petstore.product.model.Product;

import java.util.List;
import java.util.Optional;


public interface ProductService {

    List<Product> findProductsByStatus(List<String> status);

    Optional<Product> findProductById(Long productId);

    List<Product> getAllProducts();

    int getProductCount();
}