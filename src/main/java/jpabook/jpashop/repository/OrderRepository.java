package jpabook.jpashop.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

import static jpabook.jpashop.domain.QMember.member;
import static jpabook.jpashop.domain.QOrder.order;


@Repository
public class OrderRepository {
    private final EntityManager em;
    private final JPAQueryFactory query;

    public OrderRepository(EntityManager em) {
        this.em = em;
        this.query = new JPAQueryFactory(em);
    }

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    /**
     * JPAQL
     * 실무에서 사용하지 않음 QueryDsl 추천
     *
     * @param orderSearch
     * @return
     */
    public List<Order> findAllByString(OrderSearch orderSearch) {

        String jpql = "select o from Order o join o.member m";
        boolean isFirstCondition = true;

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000);

        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }

        return query.getResultList();
    }

    /**
     * JPA Criteria
     * 실무에서 사용하지 않음 QueryDsl 추천
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Object, Object> m = o.join("member", JoinType.INNER);

        List<Predicate> criteria = new ArrayList<>();

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);
        }

        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);
        return query.getResultList();
    }

    /**
     * 직관적인 문법
     * 컴파일 시점에 빠른 문법 오류 발견
     * 코드 자동완성
     * @param orderSearch
     * @return
     */
    public List<Order> findAll(OrderSearch orderSearch) {
        return query
                .select(order)
                .from(order)
                .join(order.member, member)
                .where(statusEq(orderSearch.getOrderStatus()), nameLike(orderSearch.getMemberName()))
                .limit(1000)
                .fetch();

    }

    private BooleanExpression nameLike(String memberName) {
        if (!StringUtils.hasText(memberName)) {
            return null;
        }
        System.out.println(memberName);
        return member.name.like(memberName);
    }

    private BooleanExpression statusEq(OrderStatus statusCond) {
        if (statusCond == null) {
            return null;
        }
        return order.status.eq(statusCond);
    }

    /**
     * @return
     */
    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery("select o from Order  o" +
                        " join fetch o.member m " +
                        " join fetch o.delivery", Order.class)
                .getResultList();
    }

    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery("select o from Order  o" +
                        " join fetch o.member m " +
                        " join fetch o.delivery", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }


    /**
     * 데이터베이스 입장에서는 2개이기 때문에 뻥튀기됨
     * jpa distinct SQL에 distinct 추가하고, 더해서 같은 엔티티가 조회되면
     * 애플리케이션에서 중복을 걸러준다.
     * 일대다 페치조인하는 순간 단점 페이징 불가능
     * 일대다 조인하는 순가 오더의 기준자체가 틀어짐
     * 결과 4개  오더의 사이즈가 정확하게 안나옴 오더 기준으로 페이징하고 싶은데 데이터가 뻥튀기가 되서 오더 아이템 기준으로 되어버림
     * 일대다 페치조인 페이징 못함 실무에서 못씀
     *
     * @return
     */
    public List<Order> findAllWithItem() {
        return em.createQuery(
                        "select distinct o from Order o " +
                                "join fetch o.member m " +
                                "join fetch o.delivery d " +
                                "join fetch o.orderItems oi " +
                                "join fetch oi.item i", Order.class)
                .getResultList();

    }
}
