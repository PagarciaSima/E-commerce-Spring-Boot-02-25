package spring.ecommerce.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import spring.ecommerce.dto.OrderAndProductDto;
import spring.ecommerce.entity.OrderDetailEntity;
import spring.ecommerce.entity.UserEntity;

public interface OrderDetailDao extends CrudRepository<OrderDetailEntity, Integer>{

	public List<OrderDetailEntity> findByUser(UserEntity userEntity);

	public Page<OrderDetailEntity> findAll(Pageable pageable);

	public Page<OrderDetailEntity> findByOrderFullNameContainingIgnoreCase(String searchKey, Pageable pageable);

	public Page<OrderDetailEntity> findByUserAndOrderFullNameContainingIgnoreCase(UserEntity userEntity,
			String searchKey, Pageable pageable);

	public Page<OrderDetailEntity> findByUser(UserEntity userEntity, Pageable pageable);

	public Page<OrderDetailEntity> findByOrderFullNameContainingIgnoreCaseAndOrderStatus(String searchKey,
			String status, Pageable pageable);

	public Page<OrderDetailEntity> findByOrderStatus(String status, Pageable pageable);
	
	@Query("SELECT new spring.ecommerce.dto.OrderAndProductDto(o.orderId, o.orderDate, o.orderStatus, " +
		       "p.productName, p.productActualPrice, p.productDiscountedPrice) " +
		       "FROM OrderDetailEntity o JOIN o.product p " +
		       "ORDER BY o.orderId DESC")
	List<OrderAndProductDto> findLastFourOrders(Pageable pageable);

	@Query("SELECT FUNCTION('MONTHNAME', o.orderDate), SUM(o.orderAmount) FROM OrderDetailEntity o WHERE o.orderStatus = 'Delivered' GROUP BY FUNCTION('MONTHNAME', o.orderDate) ORDER BY MIN(o.orderDate)")
	List<Object[]> getSalesPerMonth();
	
	@Query("SELECT o.product.productId, o.product.productName, COUNT(o.product.productId) AS totalSales " +
	       "FROM OrderDetailEntity o " +
	       "WHERE o.orderDate >= :startDate " +
	       "AND o.orderStatus = 'Delivered' " +
	       "GROUP BY o.product.productId, o.product.productName " +
	       "ORDER BY totalSales DESC")
	List<Object[]> findTopSellingProducts(@Param("startDate") LocalDateTime startDate, Pageable pageable);

}
