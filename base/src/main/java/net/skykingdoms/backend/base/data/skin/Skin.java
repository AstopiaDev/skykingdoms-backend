package net.skykingdoms.backend.base.data.skin;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.concurrent.TimeUnit;


@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ToString(exclude = {"value", "signature"})
@AllArgsConstructor
@Getter
public final class Skin {

    public static final Skin STEVE = new Skin("", "", "ewogICJ0aW1lc3RhbXAiIDogMTY4NDcwMTg1ODQ5MCwKICAicHJvZmlsZUlkIiA6ICIzY2YzYjgzNzhkYjc0ZDZlYmIxMDFjMDYzZmQ1MjVjNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJSeW9yaTE2IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzYwNDdiYjhhMGFlNzY1ZDQzMmVjYjUyOWFlNTJhZmFkMmNkNjYwYzM4ZjQxY2EyODcwYjZiOTFmN2Q0NTJmNTciCiAgICB9CiAgfQp9", "qX819T4aSi+6Vb2epdbnSgnUaXehxuqs0do2EsWjhdtvsH1gBWCApI1RkbNiMjQNKA0hCSF0cKNMmywxj9pIiUBTW4Qi8K5Pj7UbGZBgXX81+DY/aYObmY5yRq+g0jjDaHAMb+Qj/Mfvz3K7xy1g7tKSG8YCj8GVzqWCS205Ohev0FpeZT4udWxVxMW1fcdj1HuPmtf8ZTmArDEGFZjsxRFR/ZeDRma03y7y2H2ldu/9M+gvg52PiHH4obA9gcR6QbZg+ExCVQS/5dKG7yv96EAREt1cDDR7ne+2R1fh3VlDEkYxNySo2xJ90CkcQiZxLqeWNCQ3h7nc/ZMGySZ9qmt7ULPAk5gyWu/WRQmzBS78Sbof19UT3bkrAFahKsewymojKfaF7BPNIVgSJy1TDWmurMLk4mCwponNqYMWwecpCta6M37AJvYaJfXod1jnqahHdEJEHXgWkVENng6FNmM9u6QJTjG7HXpIgnZ7xAy90fQrMrkESAnje/Evfl0OFBeSui4rCfkm+Sp845U4Bf8yEnl3buWV1xipn9nDmWonpfXZ1faoSwpOMbc+Ke+OKprb8J/4cQ2du+4Zw/vPy8Nio5u0by5hHMgL3WKon8TtOYKtYsx5kYvSFgV32KVp6/GdU/0z9zsfOIgxJmxswFTuzkbEqxDQ/U8ejQ6+IMg=");
    public static final Skin ALEX = new Skin("", "", "ewogICJ0aW1lc3RhbXAiIDogMTY4Nzc0NDY3MTk3MCwKICAicHJvZmlsZUlkIiA6ICIwOWQ3YmJjNGYxZmU0ZjlkYjRiZmE1MTY2NDA3OTcyYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJlemtkIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzViYzJhODJhODZlOWVjYTJjOTJmOTkyMDY1YTE0ZTY0ZGNkYjY1YjIwMWQyZTdmZGE2ZjM3NTVkM2QxYzE5MDUiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==", "QwUmudSw7uMLAdwWgraVYfHJmWrrZeXCJXoGo8Iv7gDPNy1XgeHchhdY2z7J+3YPk9LotwTyIU9Ly4k+y1a67AO5P4O7rh/0WKUWWploTvL6UDJnHe2cAKKr1X8bVVi/VHrPylskX3X/wmwyaZyUG6lwaEll4uQHoXgt2Tp9aCFTqqnt7EL/j3XyJJA3LqBUBKT/PAKT1bGdv5gOdMckrSgGxNnF/Qf7FjpZ3fLx7n+VFLpRILqEOuM1mqe7AoIdiiMRjvH2cywD4Id7qmIxb9C4o4DFg5I89BK2oudyWBpaJpfJLf3pfNAiXLq6fVt+aKRzG1Z5ZxqhIiCMVSvNaUrnYsmEZdSJl+KpuAuZ3x6EPmHf9z6EeV8v+Kwfi+vc/hA/VMdWaVYOhJpatg/tgGGMnNIfFGP/FrWGNmo5pz6AeiJpBaEM9vmvk/RGROmFQNJHgkXPQe1R4NFwDgSa5vKYgQqvKlaD6pulrgEtgqpuepqnouWavUQyOtX5xFR418EtyqEeES/mGeBQpS/1DQUwCfs5raocx6ddVurnljLWKaB3ke9YVg45yyT/XmScnKG428qMDTb5gEbtuAZ1G0MjCcsB47x0u/gImCIYHSWum+J+0mWI8YyO9GGmeXoLH/ayOAt9POXJHkyuxsX395QsNAse2ovUXkA5v4iysKQ=");

    String name;
    String uuid;
    String value;
    String signature;
    long timestamp;

    public Skin(final @NonNull String name) {
        this(name, null, null, null);
    }

    public Skin(final @NonNull String name,
                final String uuid,
                final String value,
                final String signature) {
        this(name, uuid, value, signature, System.currentTimeMillis());
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - this.timestamp > TimeUnit.HOURS.toMillis(24);
    }

}