package net.skykingdoms.backend.base.mysql;

import com.zaxxer.hikari.HikariDataSource;
import net.skykingdoms.backend.base.util.concurrent.SimpleExecutor;

import java.sql.*;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Database {

    private static final ExecutorService EXECUTOR = ForkJoinPool.commonPool();
    private static final AtomicBoolean TERMINATED = new AtomicBoolean();

    private static final Map<DatabaseCredentials, DatabaseCredentialsManager> REGISTRY
            = new ConcurrentHashMap<>();

    private final HikariDataSource src;
    private Connection con;

    public Database(DatabaseCredentials credentials) {
        DatabaseCredentialsManager manager = REGISTRY.computeIfAbsent(credentials, this::initManager);
        src = manager.getSource();

        int count = manager.getUsages().incrementAndGet();
        src.setMaximumPoolSize(Math.min(count, credentials.getMaximumPoolSize()));
    }

    public static void terminate() {
        TERMINATED.set(true);
        EXECUTOR.shutdown();

        try {
            EXECUTOR.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private DatabaseCredentialsManager initManager(DatabaseCredentials credentials) {
        return new DatabaseCredentialsManager(initSource(credentials), new AtomicInteger());
    }

    protected abstract HikariDataSource initSource(DatabaseCredentials credentials);

    private Executor getExecutor() {
        return TERMINATED.get() ? SimpleExecutor.INSTANCE : EXECUTOR;
    }

    /**
     * Close connection
     */
    public void close() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException t) {
                throw new IllegalStateException(t);
            }

            con = null;
        }
    }

    /**
     * Get or create connection
     */
    public Connection getConnection() throws SQLException {
        synchronized (this) {
            return src.getConnection();
        }
    }

    private DatabaseStatement makeStatement(int keys, String query, Object... objects) throws SQLException {
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(query, keys);

        if (objects != null) {
            for (int i = 0; i < objects.length; i++) {
                Object value = objects[i];

                if (value instanceof LocalDate) {
                    value = Date.valueOf((LocalDate) value);
                }

                if (value == null) {
                    statement.setNull(i + 1, Types.NULL);
                } else {
                    statement.setObject(i + 1, value);
                }
            }
        }

        return new DatabaseStatement(connection, statement);
    }

    /**
     * Execute <code>async</code> query to the database
     *
     * @return Future
     */
    public CompletableFuture<BufferedQuery> executeQuery(String sql, Object... objects) {
        return CompletableFuture.supplyAsync(() -> {
            try (DatabaseStatement statement = makeStatement(Statement.NO_GENERATED_KEYS, sql, objects);
                 ResultSet rs = statement.executeQuery()) {
                return new BufferedQuery(rs);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    /**
     * Execute <code>async</code> update to the database and return generated keys
     *
     * @return Future
     */
    public CompletableFuture<BufferedExecutionWithGeneratedKeys> executeUpdate(String sql, Object... objects) {
        return CompletableFuture.supplyAsync(() -> {
            try (DatabaseStatement statement = makeStatement(Statement.RETURN_GENERATED_KEYS, sql, objects)) {
                int affectedRows = statement.executeUpdate();

                try (ResultSet rs = statement.getGeneratedKeys()) {
                    return new BufferedExecutionWithGeneratedKeys(
                            rs.next() ? rs.getInt(1) : 0,
                            affectedRows
                    );
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new IllegalStateException(e);
            }
        });
    }

    /**
     * Execute <code>async</code> update to the database
     *
     * @return Future
     */
    public CompletableFuture<BufferedExecution> executeVoidUpdate(String sql, Object... objects) {
        return CompletableFuture.supplyAsync(() -> {
            try (DatabaseStatement statement = makeStatement(Statement.NO_GENERATED_KEYS, sql, objects)) {
                return new BufferedExecution(statement.executeUpdate());
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
    }

}
