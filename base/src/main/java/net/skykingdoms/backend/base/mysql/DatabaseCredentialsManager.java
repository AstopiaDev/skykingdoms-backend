package net.skykingdoms.backend.base.mysql;

import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.concurrent.atomic.AtomicInteger;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Data
public final class DatabaseCredentialsManager {

    HikariDataSource source;
    AtomicInteger usages;

}
