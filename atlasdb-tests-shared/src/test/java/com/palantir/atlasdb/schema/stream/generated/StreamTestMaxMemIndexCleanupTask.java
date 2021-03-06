package com.palantir.atlasdb.schema.stream.generated;

import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;
import com.palantir.atlasdb.cleaner.api.OnCleanupTask;
import com.palantir.atlasdb.encoding.PtBytes;
import com.palantir.atlasdb.keyvalue.api.BatchColumnRangeSelection;
import com.palantir.atlasdb.keyvalue.api.Cell;
import com.palantir.atlasdb.keyvalue.api.Namespace;
import com.palantir.atlasdb.transaction.api.Transaction;
import com.palantir.common.base.BatchingVisitable;

public class StreamTestMaxMemIndexCleanupTask implements OnCleanupTask {

    private final StreamTestTableFactory tables;

    public StreamTestMaxMemIndexCleanupTask(Namespace namespace) {
        tables = StreamTestTableFactory.of(namespace);
    }

    @Override
    public boolean cellsCleanedUp(Transaction t, Set<Cell> cells) {
        StreamTestMaxMemStreamIdxTable usersIndex = tables.getStreamTestMaxMemStreamIdxTable(t);
        Set<StreamTestMaxMemStreamIdxTable.StreamTestMaxMemStreamIdxRow> rows = Sets.newHashSetWithExpectedSize(cells.size());
        for (Cell cell : cells) {
            rows.add(StreamTestMaxMemStreamIdxTable.StreamTestMaxMemStreamIdxRow.BYTES_HYDRATOR.hydrateFromBytes(cell.getRowName()));
        }
        BatchColumnRangeSelection oneColumn = BatchColumnRangeSelection.create(
                PtBytes.EMPTY_BYTE_ARRAY, PtBytes.EMPTY_BYTE_ARRAY, 1);
        Map<StreamTestMaxMemStreamIdxTable.StreamTestMaxMemStreamIdxRow, BatchingVisitable<StreamTestMaxMemStreamIdxTable.StreamTestMaxMemStreamIdxColumnValue>> existentRows
                = usersIndex.getRowsColumnRange(rows, oneColumn);
        Set<StreamTestMaxMemStreamIdxTable.StreamTestMaxMemStreamIdxRow> rowsInDb = Sets.newHashSetWithExpectedSize(cells.size());
        for (Map.Entry<StreamTestMaxMemStreamIdxTable.StreamTestMaxMemStreamIdxRow, BatchingVisitable<StreamTestMaxMemStreamIdxTable.StreamTestMaxMemStreamIdxColumnValue>> rowVisitable
                : existentRows.entrySet()) {
            rowVisitable.getValue().batchAccept(1, columnValues -> {
                if (!columnValues.isEmpty()) {
                    rowsInDb.add(rowVisitable.getKey());
                }
                return false;
            });
        }
        Set<Long> toDelete = Sets.newHashSetWithExpectedSize(rows.size() - rowsInDb.size());
        for (StreamTestMaxMemStreamIdxTable.StreamTestMaxMemStreamIdxRow rowToDelete : Sets.difference(rows, rowsInDb)) {
            toDelete.add(rowToDelete.getId());
        }
        StreamTestMaxMemStreamStore.of(tables).deleteStreams(t, toDelete);
        return false;
    }
}