package com.tit.nimonsapp.model

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.tit.nimonsapp.model.PinnedFamily

class MyDatabaseHelper(
    context: Context,
) : SQLiteOpenHelper(context, "database_pinned.db", null, 1) {
    companion object {
        private var instance: MyDatabaseHelper? = null

        fun getInstance(context: Context): MyDatabaseHelper {
            if (instance == null) {
                instance = MyDatabaseHelper(context.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE ${PinnedFamily.TABLE_PINNED} (
                ${PinnedFamily.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${PinnedFamily.FAMILY_ID} INTEGER UNIQUE
            )
            """.trimIndent(),
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int,
    ) {
        db.execSQL("DROP TABLE IF EXISTS ${PinnedFamily.TABLE_PINNED}")
        onCreate(db)
    }

    fun insertPinned(familyId: Int) {
        val db = writableDatabase
        val values =
            android.content.ContentValues().apply {
                put(PinnedFamily.FAMILY_ID, familyId)
            }
        db.insert(PinnedFamily.TABLE_PINNED, null, values)
    }

    fun getAllPinned(): List<PinnedFamily> {
        val db = readableDatabase
        val result = mutableListOf<PinnedFamily>()

        val cursor = db.rawQuery("SELECT * FROM ${PinnedFamily.TABLE_PINNED}", null)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(PinnedFamily.ID))
            val familyId = cursor.getInt(cursor.getColumnIndexOrThrow(PinnedFamily.FAMILY_ID))
            result.add(PinnedFamily(id, familyId))
        }

        cursor.close()
        return result
    }

    fun deleteAllPinned() {
        val db = writableDatabase
        db.delete(PinnedFamily.TABLE_PINNED, null, null)
    }

    fun isPinned(familyId: Int): Boolean {
        val db = readableDatabase
        val cursor =
            db.rawQuery(
                "SELECT 1 FROM ${PinnedFamily.TABLE_PINNED} WHERE ${PinnedFamily.FAMILY_ID} = ? LIMIT 1",
                arrayOf(familyId.toString()),
            )

        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    fun insertPinnedIfNotExists(familyId: Int) {
        if (!isPinned(familyId)) {
            insertPinned(familyId)
        }
    }

    fun deletePinned(familyId: Int) {
        val db = writableDatabase
        db.delete(
            PinnedFamily.TABLE_PINNED,
            "${PinnedFamily.FAMILY_ID} = ?",
            arrayOf(familyId.toString()),
        )
    }

    fun getPinnedFamilyIds(): List<Int> {
        val db = readableDatabase
        val result = mutableListOf<Int>()

        val cursor =
            db.rawQuery(
                "SELECT ${PinnedFamily.FAMILY_ID} FROM ${PinnedFamily.TABLE_PINNED}",
                null,
            )

        while (cursor.moveToNext()) {
            result.add(
                cursor.getInt(cursor.getColumnIndexOrThrow(PinnedFamily.FAMILY_ID)),
            )
        }

        cursor.close()
        return result
    }
}

val Context.database: MyDatabaseHelper
    get() = MyDatabaseHelper.getInstance(applicationContext)
