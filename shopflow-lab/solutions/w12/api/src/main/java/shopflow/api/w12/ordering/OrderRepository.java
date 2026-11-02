package shopflow.api.w12.ordering;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    /** One statement: orders, their items and the items' products. */
    @Query("select distinct o from OrderEntity o join fetch o.items i join fetch i.product")
    List<OrderEntity> findAllWithItems();
}
