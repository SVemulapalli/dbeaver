/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2023 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.ext.clickhouse.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.generic.model.GenericCatalog;
import org.jkiss.dbeaver.ext.generic.model.GenericDataSource;
import org.jkiss.dbeaver.ext.generic.model.GenericTableBase;
import org.jkiss.dbeaver.model.DBPObjectStatisticsCollector;
import org.jkiss.dbeaver.model.DBPSystemObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSEntity;
import org.jkiss.dbeaver.model.struct.DBSObject;

import java.sql.SQLException;
import java.util.List;

public class ClickhouseCatalog extends GenericCatalog implements DBPObjectStatisticsCollector, DBPSystemObject {
    private boolean hasStatistics = true;


    public ClickhouseCatalog(@NotNull GenericDataSource dataSource, @NotNull String catalogName) {
        super(dataSource, catalogName);
    }

    @Override
    public List<ClickhouseTable> getPhysicalTables(DBRProgressMonitor monitor) throws DBException {
        return (List<ClickhouseTable>) super.getPhysicalTables(monitor);
    }

    @Override
    public List<ClickhouseTable> getTables(DBRProgressMonitor monitor) throws DBException {
        return (List<ClickhouseTable>) super.getTables(monitor);
    }

    @Override
    public boolean isStatisticsCollected() {
        return hasStatistics;
    }

    @Override
    public synchronized DBSObject refreshObject(@NotNull DBRProgressMonitor monitor) throws DBException {
        resetStatistics();
        return super.refreshObject(monitor);
    }

    void resetStatistics() {
        hasStatistics = false;
    }

    @Override
    public void collectObjectStatistics(DBRProgressMonitor monitor, boolean totalSizeOnly, boolean forceRefresh)
    throws DBException {
        if (hasStatistics && !forceRefresh) {
            return;
        }
        try (DBCSession session = DBUtils.openMetaSession(monitor, this, "Read catalog statistics")) {
            try (
                JDBCPreparedStatement dbStat = ((JDBCSession) session).prepareStatement(
                    "select table," + "sum(bytes) as table_size, " + "sum(rows) as table_rows, "
                        + "max(modification_time) as latest_modification," + "min(min_date) AS min_date,"
                        + "max(max_date) AS max_date " + "FROM system.parts\n" + "WHERE database=? AND active\n"
                        + "GROUP BY table")
            ) {
                dbStat.setString(1, getName());
                try (JDBCResultSet dbResult = dbStat.executeQuery()) {
                    while (dbResult.next()) {
                        String tableName = dbResult.getString(1);
                        GenericTableBase table = getTable(monitor, tableName);
                        if (table instanceof ClickhouseTable) {
                            ((ClickhouseTable) table).fetchStatistics(dbResult);
                        }
                    }
                }
            } catch (SQLException e) {
                throw new DBCException("Error reading catalog statistics", e);
            }
        } finally {
            hasStatistics = true;
        }
    }

    @NotNull
    @Override
    public Class<? extends DBSEntity> getPrimaryChildType(@Nullable DBRProgressMonitor monitor) throws DBException {
        return ClickhouseTable.class;
    }

    @Override
    public boolean isSystem() {
        return getName().equalsIgnoreCase("INFORMATION_SCHEMA") || getName().equals("system");
    }
}