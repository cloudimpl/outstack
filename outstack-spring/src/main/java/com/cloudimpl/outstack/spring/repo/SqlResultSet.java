package com.cloudimpl.outstack.spring.repo;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author OS Hewawitharana
 */
public class SqlResultSet implements AutoCloseable {
    private java.sql.ResultSet resultSet;
    private java.sql.Connection connection;

    public SqlResultSet(Connection connection, ResultSet resultSet) {
        this.resultSet = resultSet;
        this.connection = connection;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    @Override
    public void close() {
        try {
            this.connection.close();
            this.resultSet.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }
}
