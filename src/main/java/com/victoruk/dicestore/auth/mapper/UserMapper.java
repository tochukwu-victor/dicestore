package com.victoruk.dicestore.auth.mapper;

import com.victoruk.dicestore.auth.dto.RegisterRequestDto;
import com.victoruk.dicestore.user.entity.User;

/**
 * Explicit, safe mapper between DTOs and entities.
 *
 * Replaces BeanUtils.copyProperties which is dangerous because:
 *   - It maps ANY matching field name by convention, silently.
 *   - Adding a field to the DTO (e.g. `roles`, `enabled`, `createdAt`) could
 *     overwrite sensitive entity fields with no compile-time or runtime warning.
 *
 * This mapper only maps exactly the fields we intend to copy.
 * Password is intentionally excluded — the caller encodes it separately.
 */
public final class UserMapper {

    private UserMapper() {}

    /**
     * Maps registration fields from the DTO to a new User entity.
     * Password is NOT mapped here — encode and set it explicitly in the service.
     *
     * @param dto the validated registration request
     * @return a new User with name, email, and mobileNumber populated
     */
    public static User toUser(RegisterRequestDto dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setMobileNumber(dto.getMobileNumber());
        // passwordHash is intentionally NOT set here — see AuthServiceImpl.register()
        return user;
    }
}