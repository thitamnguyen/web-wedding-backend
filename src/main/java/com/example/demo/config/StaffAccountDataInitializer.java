package com.example.demo.config;

import com.example.demo.model.Role;
import com.example.demo.model.MakeupArtist;
import com.example.demo.model.Profile;
import com.example.demo.model.User;
import com.example.demo.repository.MakeupArtistRepository;
import com.example.demo.repository.ProfileRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@Order(20)
public class StaffAccountDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProfileRepository profileRepository;
    private final MakeupArtistRepository makeupArtistRepository;
    private final PasswordEncoder passwordEncoder;

    public StaffAccountDataInitializer(
            UserRepository userRepository,
            RoleRepository roleRepository,
            ProfileRepository profileRepository,
            MakeupArtistRepository makeupArtistRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.profileRepository = profileRepository;
        this.makeupArtistRepository = makeupArtistRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Role staffRole = roleRepository.findByName("ROLE_STAFF").orElse(null);
        if (staffRole == null) {
            return;
        }

        Long photographerRefId = selectPhotographerRefId();
        Long makeupRefId = selectMakeupRefId();

        ensureStaffAccount(
                "photo.staff@luxeai.local",
                "123456",
                "Luxe Photo Staff",
                "PHOTOGRAPHER",
                photographerRefId,
                staffRole
        );

        ensureStaffAccount(
                "makeup.staff@luxeai.local",
                "123456",
                "Luxe Makeup Staff",
                "MAKEUP",
                makeupRefId,
                staffRole
        );
    }

    private void ensureStaffAccount(
            String email,
            String rawPassword,
            String fullName,
            String staffType,
            Long staffRefId,
            Role staffRole
    ) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail).orElseGet(User::new);
        user.setEmail(normalizedEmail);
        user.setFullName(fullName);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setStatus(1);
        user.setRole(staffRole);
        user.setStaffType(staffType);
        if (staffRefId != null) {
            user.setStaffRefId(staffRefId);
        }
        userRepository.save(user);
    }

    private Long selectPhotographerRefId() {
        return profileRepository.findAll().stream()
                .map(Profile::getUserId)
                .filter(id -> id != null)
                .sorted()
                .findFirst()
                .orElse(1L);
    }

    private Long selectMakeupRefId() {
        return makeupArtistRepository.findAll().stream()
                .map(MakeupArtist::getId)
                .filter(id -> id != null)
                .sorted()
                .findFirst()
                .orElse(1L);
    }
}
