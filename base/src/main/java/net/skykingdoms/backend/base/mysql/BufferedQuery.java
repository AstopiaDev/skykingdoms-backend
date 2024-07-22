package net.skykingdoms.backend.base.mysql;

import lombok.NonNull;
import lombok.val;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public final class BufferedQuery extends LinkedList<BufferedRow> {

    public BufferedQuery(final @NonNull ResultSet rs) throws SQLException {
        val resultSetMetaData = rs.getMetaData();

        while (rs.next()) {
            val row = new BufferedRow();
            val columns = resultSetMetaData.getColumnCount();

            for (var i = 0; i < columns; i++) {
                val idx = i + 1;

                val name = resultSetMetaData.getColumnName(idx);
                val object = rs.getObject(idx);

                if (object != null) {
                    row.put(name, object);
                }
            }

            add(row);
        }
    }

}