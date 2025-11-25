package org.khr.microservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.khr.microservice.User;
import org.khr.microservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * ユーザーサービス
 * Spring 7の新機能: より強化されたトランザクション管理
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        log.info("全ユーザーを取得");
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        log.info("ユーザーを取得: ID={}", id);
        return userRepository.findById(id);
    }

    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("メールアドレスが既に存在します: " + user.getEmail());
        }
        log.info("新規ユーザーを作成: {}", user.getEmail());
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません: ID=" + id));

        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());

        log.info("ユーザーを更新: ID={}", id);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("ユーザーが見つかりません: ID=" + id);
        }
        log.info("ユーザーを削除: ID={}", id);
        userRepository.deleteById(id);
    }
}
