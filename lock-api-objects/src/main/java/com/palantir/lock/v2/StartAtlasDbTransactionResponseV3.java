/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
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

package com.palantir.lock.v2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableStartAtlasDbTransactionResponseV3.class)
@JsonDeserialize(as = ImmutableStartAtlasDbTransactionResponseV3.class)
public abstract class StartAtlasDbTransactionResponseV3 {
    @Value.Parameter
    public abstract LockImmutableTimestampResponse immutableTimestamp();

    @Value.Parameter
    public abstract TimestampAndPartition startTimestampAndPartition();

    @Value.Parameter
    public abstract Lease getLease();

    @JsonIgnore
    public StartIdentifiedAtlasDbTransactionResponse toStartTransactionResponse() {
        return ImmutableStartIdentifiedAtlasDbTransactionResponse.of(
                immutableTimestamp(), startTimestampAndPartition());
    }

    public static StartAtlasDbTransactionResponseV3 of(
            LockImmutableTimestampResponse lockImmutableTimestampResponse,
            TimestampAndPartition timestampAndPartition,
            Lease lease) {
        return ImmutableStartAtlasDbTransactionResponseV3.builder()
                .immutableTimestamp(lockImmutableTimestampResponse)
                .startTimestampAndPartition(timestampAndPartition)
                .lease(lease)
                .build();
    }
}
