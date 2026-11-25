package com.bibliogest.backend.config;

import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.RowLockStrategy;

public class TiDBCompatibleDialect extends MySQLDialect {

    @Override
    protected boolean supportsAliasLocks() {
        return false;
    }

    @Override
    public RowLockStrategy getWriteRowLockStrategy() {
        return RowLockStrategy.NONE;
    }

    @Override
    public RowLockStrategy getReadRowLockStrategy() {
        return RowLockStrategy.NONE;
    }
}
