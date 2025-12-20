package ru.itmo.codetogether.model;

import lombok.Getter;

@Getter
public enum SessionRole {
    OWNER("owner"),
    EDITOR("editor"),
    VIEWER("viewer");

    private final String value;

    SessionRole(String value) {
        this.value = value;
    }

    public static SessionRole fromString(String raw) {
        if (raw == null) {
            return EDITOR;
        }
        for (SessionRole role : values()) {
            if (role.value.equalsIgnoreCase(raw)) {
                return role;
            }
        }
        return EDITOR;
    }
}
