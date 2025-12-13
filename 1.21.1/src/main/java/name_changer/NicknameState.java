package name_changer;

public final class NicknameState {
    private static String nick;

    public static String get() {
        return nick;
    }

    public static void set(String value) {
        nick = (value == null || value.isBlank()) ? null : value;
    }
}
