package shopflow.api.w12.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data generates the implementation of this interface at startup. EX-12-01: add your query methods here. */
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
}
