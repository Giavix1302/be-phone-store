package fit.se.be_phone_store.config;

import fit.se.be_phone_store.entity.User;
import fit.se.be_phone_store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data Initialization Configuration
 * Creates default admin and user accounts on application startup
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitConfig implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createDefaultUsers();
    }

    private void createDefaultUsers() {
        // Create admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@phonestore.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("Administrator");
            admin.setPhone("0123456789"); // Changed from setPhoneNumber to setPhone
            admin.setAddress("123 Admin Street, Ho Chi Minh City"); // Using address field
            admin.setRole(User.Role.ADMIN);
            admin.setEnabled(true); // Changed from setIsActive to setEnabled

            userRepository.save(admin);
            log.info("Default admin user created: username=admin, password=admin123");
        }

        // Create test user if not exists
        if (!userRepository.existsByUsername("user")) {
            User user = new User();
            user.setUsername("user");
            user.setEmail("user@phonestore.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setFullName("Test User");
            user.setPhone("0987654321"); // Changed from setPhoneNumber to setPhone
            user.setAddress("456 User Street, Ho Chi Minh City"); // Using address field
            user.setRole(User.Role.USER);
            user.setEnabled(true); // Changed from setIsActive to setEnabled

            userRepository.save(user);
            log.info("Default test user created: username=user, password=user123");
        }

        log.info("User initialization completed");
    }
}