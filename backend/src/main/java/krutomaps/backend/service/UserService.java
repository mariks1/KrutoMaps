package krutomaps.backend.service;

import jakarta.persistence.EntityNotFoundException;
import krutomaps.backend.entity.UserEntity;
import krutomaps.backend.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;


    @Transactional(readOnly = true)
    public UserEntity getByUsername(String username) {
        Optional<UserEntity> user = userRepository.findByUsername(username);
        if (user.isEmpty()) throw new EntityNotFoundException("Пользователь не найден");
        return user.get();
    }

    @Transactional(rollbackFor = Exception.class)
    public UserEntity create(UserEntity user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new EntityNotFoundException("Пользователь с таким именем уже существует");
        }
        return save(user);
    }


    @Transactional(rollbackFor = Exception.class)
    public UserEntity save(UserEntity user) {
        return userRepository.save(user);
    }

    public UserDetailsService userDetailsService() {
        return this::getByUsername;
    }

    public UserEntity getCurrentUser() {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        return getByUsername(username);
    }


}
