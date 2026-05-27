package com.example.contacts

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Миграция от версия 1 към версия 2.
// Добавя новите колони БЕЗ да губи съществуващите контакти.
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE contacts ADD COLUMN firebaseId TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE contacts ADD COLUMN version INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE contacts ADD COLUMN isDirty INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE contacts ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE contacts ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")

        // Добавяне на уникален индекс за име и телефон
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_contacts_name_phone ON contacts (name, phone)")
    }
}