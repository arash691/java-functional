package shopflow.api.w12.catalog;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    Optional<ProductEntity> findBySku(String sku);

    List<ProductEntity> findByNameContainingIgnoreCaseOrderByNameAsc(String fragment);
}
