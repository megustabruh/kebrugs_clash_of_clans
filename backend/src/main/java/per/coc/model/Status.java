package per.coc.model;

public enum Status {
    ACTIVE,
    INACTIVE,
    BANNED,
    UNKNOWN;

    public static Status fromString(String status) {
        if (status == null) {
            return UNKNOWN;
        }
        try {
            return Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
