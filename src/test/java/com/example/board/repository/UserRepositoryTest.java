package com.example.board.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.board.entity.UserInfo;
import com.example.board.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindById() {
        UserInfo user = new UserInfo();
        user.setId(1);
        user.setLogin("testUser");
        user.setEmail("test@example.com");
        user.setImage("testImage.png");
        user.setNeedOtp(true);
        user.setIsVerified(false);

        UserInfo savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getLogin()).isEqualTo("testUser");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getImage()).isEqualTo("testImage.png");
        assertThat(savedUser.getNeedOtp()).isTrue();
        assertThat(savedUser.getIsVerified()).isFalse();
    }

    @Test
    void testDelete() {
        UserInfo user = new UserInfo();
        user.setId(2);
        user.setLogin("testUser");
        user.setEmail("test@example.com");
        userRepository.save(user);

        userRepository.deleteById(user.getId());

        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    void testFindAll() {
        UserInfo user1 = new UserInfo();
        user1.setId(3);
        user1.setLogin("user1");
        user1.setEmail("user1@example.com");
        user1.setImage("image1.png");
        user1.setNeedOtp(false);
        user1.setIsVerified(true);
        userRepository.save(user1);

        UserInfo user2 = new UserInfo();
        user2.setId(4);
        user2.setLogin("user2");
        user2.setEmail("user2@example.com");
        user2.setImage("image2.png");
        user2.setNeedOtp(true);
        user2.setIsVerified(false);
        userRepository.save(user2);

        Iterable<UserInfo> users = userRepository.findAll();

        assertThat(users).hasSize(2);
    }
}