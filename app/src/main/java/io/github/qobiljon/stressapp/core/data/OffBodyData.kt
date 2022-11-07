package io.github.qobiljon.stressapp.core.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class OffBodyData(
    @PrimaryKey val timestamp: Long,
    @ColumnInfo(name = "is_off_body") val is_off_body: Boolean,
)