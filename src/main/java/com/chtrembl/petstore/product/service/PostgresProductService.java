package com.chtrembl.petstore.product.service;

import com.chtrembl.petstore.product.dao.Product;
import com.chtrembl.petstore.product.repo.ProductRepository;
import com.chtrembl.petstore.product.model.Category;
import com.chtrembl.petstore.product.model.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("PostgresProductService")
@Slf4j
@RequiredArgsConstructor
public class PostgresProductService implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public List<com.chtrembl.petstore.product.model.Product> findProductsByStatus(List<String> status) {
        log.info("Finding products with status: {} from Postgres", status);

        return productRepository.findByStatusIn(status).stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public Optional<com.chtrembl.petstore.product.model.Product> findProductById(Long productId) {
        log.info("Finding product with id: {} from Postgres", productId);

        return productRepository.findById(productId)
                .map(this::toModel);
    }

    @Override
    public List<com.chtrembl.petstore.product.model.Product> getAllProducts() {
        log.info("Getting all products from Postgres");
        return productRepository.findAll().stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public int getProductCount() {
        return (int) productRepository.count();
    }

    private com.chtrembl.petstore.product.model.Product toModel(Product entity) {
        Category category = Category.builder()
                .id(entity.getCategory().getId())
                .name(entity.getCategory().getName())
                .build();

        List<Tag> tags = entity.getTags().stream()
                .map(t -> Tag.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .build())
                .collect(Collectors.toList());

        com.chtrembl.petstore.product.model.Product.Status statusEnum =
                com.chtrembl.petstore.product.model.Product.Status.fromValue(entity.getStatus());

        return com.chtrembl.petstore.product.model.Product.builder()
                .id(entity.getId())
                .category(category)
                .name(entity.getName())
                .photoURL(entity.getPhotoURL())
                .tags(tags)
                .status(statusEnum)
                .build();
    }
}
