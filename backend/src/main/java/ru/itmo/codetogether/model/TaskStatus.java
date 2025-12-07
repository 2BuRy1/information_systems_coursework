package ru.itmo.codetogether.model;

public enum TaskStatus {
    OPEN("open"),
    IN_PROGRESS("in_progress"),
    DONE("done");

    private final String value;

    TaskStatus(String value) {
        this.value = value;
    }

    public static TaskStatus fromString(String raw) {
        if (raw == null) {
            return OPEN;
        }
        for (TaskStatus status : values()) {
            if (status.value.equalsIgnoreCase(raw)) {
                return status;
            }
        }
        return OPEN;
    }

    public String getValue() {
        return value;
    }
}
