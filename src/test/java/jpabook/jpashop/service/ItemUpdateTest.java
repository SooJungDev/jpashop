package jpabook.jpashop.service;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jpabook.jpashop.domain.item.Book;

@SpringBootTest
public class ItemUpdateTest {

    @Autowired
    EntityManager em;

    @Test
    void updateTest() throws Exception {

        Book book = em.find(Book.class, 1L);

        //TX
        book.setName("test");

        //변경감지 == dirty checking
        // TX commit

    }
}
