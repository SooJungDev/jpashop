package jpabook.jpashop.repository.order.simplequery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRespository {
    private final EntityManager em;

    /**
     * select 절에 원하는 데이터 직접 선택하므로 DB -> 애플리케이션 네트웍 용량 최적화(생각보다 미비, 성능이 좌지우지 되지 않음,,)
     * API 스펙에 맞춰서 코드가 짜져있음 재사용성이 떨어진다.
     * @return
     */
    public List<OrderSimpleQueryDto> findOrderDtos() {
        return em.createQuery("select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address) " +
                        "from Order o " +
                        "join o.member m " +
                        "join o.delivery d", OrderSimpleQueryDto.class)
                .getResultList();

    }
}
