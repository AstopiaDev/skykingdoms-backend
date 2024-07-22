package net.skykingdoms.backend.base.mysql;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class DatabaseStatement implements AutoCloseable {

    Connection connection;
    PreparedStatement statement;

    public DatabaseStatement(Connection connection, PreparedStatement statement) {
        this.connection = connection;
        this.statement = statement;
    }

    public int executeUpdate() throws SQLException {
        return statement.executeUpdate();
    }

    public ResultSet executeQuery() throws SQLException {
        return statement.executeQuery();
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        return statement.getGeneratedKeys();
    }

    @Override
    public void close() throws SQLException {
        connection.close();
        connection = null;
        statement.close();
        statement = null;
    }
}
