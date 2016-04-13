/**
 * Copyright 2015 Palantir Technologies
 *
 * Licensed under the BSD-3 License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.atlasdb.keyvalue.api;

import java.util.Arrays;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.palantir.atlasdb.encoding.PtBytes;

public class SweepResults {
    private final @Nullable byte[] nextStartRow;
    private final long cellsExamined;
    private final long cellsSwept;
    private final long sweptTimestamp;

    public static SweepResults createEmptySweepResult(long sweptTimestamp) {
        return new SweepResults(null, 0, 0, sweptTimestamp);
    }

    public SweepResults(@Nullable byte[] nextStartRow, long cellsExamined, long cellsSwept, long sweptTimestamp) {
        this.nextStartRow = nextStartRow;
        this.cellsExamined = cellsExamined;
        this.cellsSwept = cellsSwept;
        this.sweptTimestamp = sweptTimestamp;
    }

    public Optional<byte[]> getNextStartRow() {
        return Optional.fromNullable(nextStartRow);
    }

    public long getCellsExamined() {
        return cellsExamined;
    }

    public long getCellsDeleted() {
        return cellsSwept;
    }

    public long getSweptTimestamp() {
        return sweptTimestamp;
    }

    @Override
    public String toString() {
        return "SweepResults [nextStartRow=" + PtBytes.encodeHexString(nextStartRow)
                + ", cellsExamined=" + cellsExamined
                + ", cellsSwept=" + cellsSwept
                + ", sweptTimestamp=" + sweptTimestamp + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SweepResults results = (SweepResults) o;

        if (cellsExamined != results.cellsExamined) return false;
        if (cellsSwept != results.cellsSwept) return false;
        if (sweptTimestamp != results.sweptTimestamp) return false;
        return Arrays.equals(nextStartRow, results.nextStartRow);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(nextStartRow);
        result = 31 * result + (int) (cellsExamined ^ (cellsExamined >>> 32));
        result = 31 * result + (int) (cellsSwept ^ (cellsSwept >>> 32));
        result = 31 * result + (int) (sweptTimestamp ^ (sweptTimestamp >>> 32));
        return result;
    }
}
