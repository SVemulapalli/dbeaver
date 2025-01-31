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

package org.jkiss.dbeaver.tools.transfer.stream;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.tools.transfer.IDataTransferConsumer;
import org.jkiss.dbeaver.tools.transfer.IDataTransferProcessor;

import java.io.InputStream;
import java.util.List;

/**
 * IStreamDataImporter
 */
public interface IStreamDataImporter extends IDataTransferProcessor {

    void init(@NotNull IStreamDataImporterSite site) throws DBException;

    @NotNull
    List<StreamDataImporterColumnInfo> readColumnsInfo(StreamEntityMapping entityMapping, @NotNull InputStream inputStream) throws DBException;

    void runImport(
        @NotNull DBRProgressMonitor monitor,
        @NotNull DBPDataSource streamDataSource,
        @NotNull InputStream inputStream,
        @NotNull IDataTransferConsumer consumer) throws DBException;

    void dispose();

}
