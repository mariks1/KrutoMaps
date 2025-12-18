package krutomaps.backend.service;

import jakarta.persistence.EntityNotFoundException;
import krutomaps.backend.entity.RoleEntity;
import krutomaps.backend.entity.UserEntity;
import krutomaps.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserEntity sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = UserEntity.builder()
                .id(1L)
                .username("john")
                .password("pw")
                .role(RoleEntity.builder().roleName("USER").build())
                .build();
    }

    @Test
    void getByUsernameThrowsWhenMissing() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getByUsername("missing"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getByUsernameReturnsUser() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(sampleUser));
        assertThat(userService.getByUsername("john")).isEqualTo(sampleUser);
    }

    @Test
    void createThrowsIfDuplicateUsername() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(sampleUser));
        assertThatThrownBy(() -> userService.create(sampleUser))
                .isInstanceOf(EntityNotFoundException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void createSavesWhenUnique() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
        when(userRepository.save(sampleUser)).thenReturn(sampleUser);

        UserEntity saved = userService.create(sampleUser);
        assertThat(saved).isEqualTo(sampleUser);
    }
}
