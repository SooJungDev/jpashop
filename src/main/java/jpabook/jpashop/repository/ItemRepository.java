package jpabook.jpashop.repository;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    /**
     * 변경 감지 기능을 사용하면 원하는 속성만 선택해서 변경할 수 있지만 merge 사용할 경우 모든 속성이 변경된다.
     * 값이 없으면 null 로 업데이트 할 수 있는 위험이 있음
     * @param item
     */
    public void save(Item item) {
        if (item.getId() == null) {
            em.persist(item);
            return;
        }
        em.merge(item);
    }

    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
                 .getResultList();
    }
}
