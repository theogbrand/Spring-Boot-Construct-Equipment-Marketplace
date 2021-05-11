package com.leafcutters.antbuildz.services;

import java.util.List;
import java.util.Optional;

import com.leafcutters.antbuildz.models.Role;
import com.leafcutters.antbuildz.models.User;
import com.leafcutters.antbuildz.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final static String USER_NOT_FOUND_MSG = "User with email %s not found";
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG, email)));
    }

    public void signUpUser(User user, Role role) {
        boolean userExists = userRepository.findByEmail(user.getEmail()).isPresent();
        if (userExists) {
            throw new IllegalStateException("Email already taken");
        } else {
            // String token = UUID.randomUUID().toString();
            user.setAppUserRole(role);
            String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            userRepository.save(user);
        }
    }

    public void enableAppUser(String email) {
        userRepository.enableAppUser(email);
    }

    public User findByEmail(String email) {
        Optional<User> result = userRepository.findByEmail(email);
        User theUser;
        if (result.isPresent()) {
            theUser = result.get();
        } else {
            throw new RuntimeException("Did not find the user with email: " + email);
        }
        return theUser;

    }

    public List<User> getPartnerList() {
        return userRepository.findAllPartners();
    }

    public User findByName(String username) {
        return userRepository.findByName(username);
    }

}
