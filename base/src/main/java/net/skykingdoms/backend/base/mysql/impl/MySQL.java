package net.skykingdoms.backend.base.mysql.impl;

import com.zaxxer.hikari.HikariDataSource;
import lombok.NonNull;
import net.skykingdoms.backend.base.mysql.Database;
import net.skykingdoms.backend.base.mysql.DatabaseCredentials;

import java.sql.SQLException;

public final class MySQL extends Database {

    public MySQL(final @NonNull DatabaseCredentials credentials) {
        super(credentials);
    }

    private static final String JDBC_URL = "jdbc:mysql://%s:%s/%s"
            + "?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true"
            + "&characterEncoding=UTF8&useUnicode=true";

    @Override
    public HikariDataSource initSource(final @NonNull DatabaseCredentials credentials) {
        var source = new HikariDataSource();
        source.setUsername(credentials.getUser());
        source.setPassword(credentials.getPassword());
        source.setJdbcUrl(String.format(JDBC_URL, credentials.getHost(), credentials.getPort(), credentials.getDatabase()));

        try {
            source.setLoginTimeout(60);
        } catch (SQLException e) {
            e.printStackTrace();
        }

//        Logging.IMP.info("U: {} @ P: {} @ H: {} @ PT: {}", credentials.getUser(), credentials.getPassword(),
//                credentials.getHost(), credentials.getPort());
        return source;
    }
}