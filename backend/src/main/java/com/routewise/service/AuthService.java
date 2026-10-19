package com.routewise.service;

import com.routewise.dto.auth.AuthResponse;
import com.routewise.dto.auth.LoginRequest;
import com.routewise.dto.auth.RegisterRequest;
import com.routewise.dto.auth.UserResponse;
import com.routewise.entity.User;
import com.routewise.entity.UserPreference;
import com.routewise.exception.BadRequestException;
import com.routewise.exception.ResourceNotFoundException;
import com.routewise.mapper.EntityDtoMapper;
import com.routewise.repository.UserPreferenceRepository;
import com.routewise.repository.UserRepository;
import com.routewise.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final EntityDtoMapper mapper;

    public AuthService(
            UserRepository userRepository,
            UserPreferenceRepository userPreferenceRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider,
            EntityDtoMapper mapper
    ) {
        this.userRepository = userRepository;
        this.userPreferenceRepository = userPreferenceRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.mapper = mapper;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase().trim())) {
            throw new BadRequestException("An account with email " + request.email() + " already exists");
        }

        User user = new User(
                request.email().toLowerCase().trim(),
                passwordEncoder.encode(request.password()),
                request.fullName().trim(),
                request.companyName() != null ? request.companyName().trim() : null
        );

        User savedUser = userRepository.save(user);

        UserPreference preference = new UserPreference(savedUser);
        userPreferenceRepository.save(preference);
        savedUser.setPreference(preference);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().toLowerCase().trim(), request.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        return new AuthResponse(jwt, tokenProvider.getExpirationMs() / 1000, mapper.toUserResponse(savedUser));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().toLowerCase().trim(), request.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        User user = userRepository.findByEmail(request.email().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return new AuthResponse(jwt, tokenProvider.getExpirationMs() / 1000, mapper.toUserResponse(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return mapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse updatePreferences(Long userId, String depotAddress, Double depotLat, Double depotLng, String distanceUnit, String theme) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        UserPreference pref = user.getPreference();
        if (pref == null) {
            pref = new UserPreference(user);
        }

        if (depotAddress != null) pref.setDefaultDepotAddress(depotAddress);
        if (depotLat != null) pref.setDefaultDepotLat(depotLat);
        if (depotLng != null) pref.setDefaultDepotLng(depotLng);
        if (distanceUnit != null) pref.setDistanceUnit(distanceUnit);
        if (theme != null) pref.setTheme(theme);

        userPreferenceRepository.save(pref);
        user.setPreference(pref);
        return mapper.toUserResponse(user);
    }
}
