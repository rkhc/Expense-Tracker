package com.example.prototype.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import com.example.prototype.Expense

object FeedReaderContract {
    // Table contents are grouped together in an anonymous object.
    object FeedEntry : BaseColumns {
        const val TABLE_NAME = "Expense"
        const val COLUMN_TYPE = "type"
        const val COLUMN_NAME = "name"
        const val COLUMN_PRICE = "price"
        const val COLUMN_DATE = "date"
    }
}

private const val SQL_CREATE_ENTRIES =
    "CREATE TABLE ${FeedReaderContract.FeedEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${FeedReaderContract.FeedEntry.COLUMN_TYPE} TEXT," +
            "${FeedReaderContract.FeedEntry.COLUMN_NAME} TEXT," +
            "${FeedReaderContract.FeedEntry.COLUMN_PRICE} REAL," +
            "${FeedReaderContract.FeedEntry.COLUMN_DATE} TEXT)"

private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${FeedReaderContract.FeedEntry.TABLE_NAME}"

class FeedReaderDbHelper(context: Context) : SQLiteOpenHelper(context,
    DATABASE_NAME, null,
    DATABASE_VERSION
) {

    override fun onCreate(db : SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "ExpenseReader.db"
    }

    private fun extractAllExceptDate(db: SQLiteDatabase){
        val projection = arrayOf(
            BaseColumns._ID,
            FeedReaderContract.FeedEntry.COLUMN_TYPE,
            FeedReaderContract.FeedEntry.COLUMN_NAME,
            FeedReaderContract.FeedEntry.COLUMN_PRICE
        )

        // Filter results WHERE "title" = 'My Title'
        val selection = "${FeedReaderContract.FeedEntry.COLUMN_TYPE} = ?"
        //val selectionArgs = arrayOf("Roger")
        val selectionArgs = arrayOf("BILLS")

        // How you want the results sorted in the resulting Cursor
        val sortOrder = "${FeedReaderContract.FeedEntry.COLUMN_PRICE} DESC"

        val cursor = db?.query(
            FeedReaderContract.FeedEntry.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            selectionArgs,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            sortOrder               // The sort order
        )

        var counter = 1;
        val itemIds = mutableListOf<String>()
        with(cursor) {
            while (moveToNext()) {

                val type = getString(getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_TYPE))
                val name = getString(getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_NAME))
                val price = getString(getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_PRICE))
                //do not need date
                itemIds.add("$counter: $type $name $price")
                ++counter
            }
        }

        for(elem in itemIds) {
            Log.e("roger", elem)
        }

        cursor.close()
    }
}