package com.clenson.nestbook.security;

public record CurrentUser(
        Long userId,
        Long familyId,
        String mpOpenid,
        String role
) {
}

