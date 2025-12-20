package ru.itmo.codetogether.dto;

public final class CommonDto {

    private CommonDto() {
    }

    public static record ErrorResponse(String error, String details) {
    }
}
