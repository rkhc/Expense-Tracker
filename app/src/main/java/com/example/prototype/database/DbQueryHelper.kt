package com.example.prototype.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import android.util.Log
import com.example.prototype.Expense
import com.example.prototype.ExpenseDB
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

//the controller that talks to the database
object DbQueryHelper {

    private var db : SQLiteDatabase? = null

    val currentDate = LocalDateTime.now()
    val currentYear : Int = currentDate.year
    val currentMonth : Int = currentDate.monthValue
    val currentDay : Int = currentDate.dayOfMonth

    //initialize with writable database for both read and write purposes
    fun initializeDB(context : Context) {
        val dbHelper = FeedReaderDbHelper(context)
        db = dbHelper?.writableDatabase
    }

    fun closeDB() {
        db?.close()
    }

    //create
    fun insertExpenseObject(expense : Expense) {
        //insert to database by passing content value
        val values = ContentValues().apply {
            put(FeedReaderContract.FeedEntry.COLUMN_TYPE, expense.type)
            put(FeedReaderContract.FeedEntry.COLUMN_NAME, expense.name)
            put(FeedReaderContract.FeedEntry.COLUMN_PRICE, expense.price)
            put(FeedReaderContract.FeedEntry.COLUMN_DATE, expense.date)
        }

        //first arg is table name
        //second arg is telling db to do nothing if values is null
        //third arg is is the contentvalues
        //returns the new row for the inserted data. -1 if error
        val newRowId = db?.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null , values)

        if(newRowId == -1L)
            Log.e("CreateNewExpenseFragment::insertEntryToDB", "Error when inserting into db")
    }

    //read all
    fun extractAllExpenseObjects() : MutableList<ExpenseDB>? {
        if(db == null) {
            Log.e("DbQueryHelper::extractAllExpenseObjects", "Invalid database")
            return null
        }

        //sort by date
        val sortOrder = "${FeedReaderContract.FeedEntry.COLUMN_DATE} DESC"

        val cursor = db!!.query(
            FeedReaderContract.FeedEntry.TABLE_NAME,   // The table to query
            null,             // The array of columns to return (pass null to get all)
            null,              // The columns for the WHERE clause
            null,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            sortOrder               // The sort order
        )

        val items = mutableListOf<ExpenseDB>()
        with(cursor) {
            while (moveToNext()) {

                items.add(
                    ExpenseDB(
                        cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)),
                        getString(getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_TYPE)),
                        getString(getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_NAME)),
                        getString(getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_PRICE)).toDouble(),
                        getString(getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_DATE))
                    )
                )
            }
        }

        cursor.close()

        return items
    }

    //read
    fun extractExpenseObjectFromID(id : Int) : ExpenseDB {
        //return invalid object
        if(db == null) {
            Log.e("DbQueryHelper::extractExpenseObjectFromID", "Invalid database")
            return ExpenseDB(-1, "", "", 0.0, "")
        }

        //selecting all, most fields can be set to null
        val projection = arrayOf(
            BaseColumns._ID,
            FeedReaderContract.FeedEntry.COLUMN_TYPE,
            FeedReaderContract.FeedEntry.COLUMN_NAME,
            FeedReaderContract.FeedEntry.COLUMN_PRICE,
            FeedReaderContract.FeedEntry.COLUMN_DATE
        )

        val selection = BaseColumns._ID.toString()  + "= $id"

        //sort by date
        val sortOrder = "${FeedReaderContract.FeedEntry.COLUMN_DATE} DESC"

        val cursor = db!!.query(
            FeedReaderContract.FeedEntry.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            null,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            sortOrder               // The sort order
        )

        var expense = ExpenseDB()

        with(cursor) {
            while (moveToNext()) {
                //return the single object received
                expense.id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID))
                expense.type = getString(getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_TYPE))
                expense.name = getString(getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_NAME))
                expense.price = getString(getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_PRICE)).toDouble()
                expense.date = getString(getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_DATE))
            }
        }

        cursor.close()

        return expense
    }

    //edit
    fun editExpenseObject(expense : ExpenseDB) {
        if(db == null) {
            Log.e("DbQueryHelper::editExpenseObject", "Invalid database")
            return
        }

        //insert to database by passing content value
        val values = ContentValues().apply {
            put(FeedReaderContract.FeedEntry.COLUMN_TYPE, expense.type)
            put(FeedReaderContract.FeedEntry.COLUMN_NAME, expense.name)
            put(FeedReaderContract.FeedEntry.COLUMN_PRICE, expense.price)
            put(FeedReaderContract.FeedEntry.COLUMN_DATE, expense.date)
        }

        //1st arg is the table name
        //second arg is the values to update to
        //third arg is the where clause to match
        val editedID = db!!.update(
            FeedReaderContract.FeedEntry.TABLE_NAME,
            values,
            BaseColumns._ID.toString()  + "=" + expense.id.toString(),
            null)

        if(editedID == 0)
            Log.e("DbQueryHelper::editExpenseObject", "Error when editing " + expense.name + " row " +
                    expense.id.toString() + " into db")
    }

    //delete
    fun deleteExpenseObject(id: Int) {
        if(db == null) {
            Log.e("DbQueryHelper::deleteExpenseObject", "Invalid database")
            return
        }

        //1st arg is the table name
        //second arg is the values to update to
        //third arg is the where clause to delete
        val deletedID = db!!.delete(
            FeedReaderContract.FeedEntry.TABLE_NAME,
            BaseColumns._ID.toString()  + "=" + id.toString(),
            null)

        if(deletedID == 0)
            Log.e("DbQueryHelper::editExpenseObject", "Did not delete anything")
    }

    //only type can be null for to extract all expenses
    fun filterDB(unit : String, value : Int, type : String?) : MutableList<ExpenseDB>? {
        //return invalid object
        if(db == null) {
            Log.e("DbQueryHelper::extractExpenseObjectFromID", "Invalid database")
            return null
        }

        //selecting all, most fields can be set to null
        val projection = arrayOf(
            BaseColumns._ID,
            FeedReaderContract.FeedEntry.COLUMN_TYPE,
            FeedReaderContract.FeedEntry.COLUMN_NAME,
            FeedReaderContract.FeedEntry.COLUMN_PRICE,
            FeedReaderContract.FeedEntry.COLUMN_DATE
        )

        var datePair = if(unit == "Day") {
            getDateRangeDay(currentYear, currentMonth, value)
        }
        else if (unit == "Month") {
            getDateRangeMonth(currentYear, value)
        }
        else if (unit == "Year") {
            getDateRangeYear(value)
        }
        //none of the dropdowns were selected, search for nothing
        else {
            Pair<String,String>("","")
        }

        //date range to extract records
        var selection = FeedReaderContract.FeedEntry.COLUMN_DATE + " >= " +
                '"' + datePair.first + '"' + " AND " + FeedReaderContract.FeedEntry.COLUMN_DATE +
                " < " + '"' + datePair.second + '"'

        //filter by type
        if(type != null) {
            selection += " AND " + FeedReaderContract.FeedEntry.COLUMN_TYPE + " = " + '"' + type + '"'
        }

        //sort by date
        val sortOrder = "${FeedReaderContract.FeedEntry.COLUMN_DATE} DESC"

        val cursor = db!!.query(
            FeedReaderContract.FeedEntry.TABLE_NAME,   // The table to query
            projection,             // The array of columns to return (pass null to get all)
            selection,              // The columns for the WHERE clause
            null,          // The values for the WHERE clause
            null,                   // don't group the rows
            null,                   // don't filter by row groups
            sortOrder               // The sort order
        )

        val items = mutableListOf<ExpenseDB>()
        with(cursor) {
            while (moveToNext()) {

                items.add(
                    ExpenseDB(
                        cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)),
                        getString(getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_TYPE)),
                        getString(getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_NAME)),
                        getString(getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_PRICE)).toDouble(),
                        getString(getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_DATE))
                    )
                )
            }
        }

        cursor.close()

        return items
    }


    fun formatNumberCurrency(number : String) : String {
        val formatter = DecimalFormat("###,###,##0.00")
        return "$" + formatter.format(number.toDouble())
    }

    val dateDBFormat = "yyyy-MM-dd HH:mm:ss"
    val dateCalendarFormat = "dd/MM/yyyy"

    fun getDate() : String {
        //get current date
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return current.format(formatter)
    }

    fun getDateRangeYear(year : Int) : Pair<String, String> {
        val specificDate = LocalDateTime.of(year, 1, 1, 0, 0)
        val nextDate = specificDate.plusYears(1)

        val formatter = DateTimeFormatter.ofPattern(dateDBFormat)
        return Pair<String,String>(specificDate.format(formatter), nextDate.format(formatter))
    }

    fun getDateRangeMonth(year : Int, month : Int) : Pair<String, String> {
        val specificDate = LocalDateTime.of(year, month, 1, 0, 0)
        val nextDate = specificDate.plusMonths(1)

        val formatter = DateTimeFormatter.ofPattern(dateDBFormat)
        return Pair<String,String>(specificDate.format(formatter), nextDate.format(formatter))
    }

    fun getDateRangeDay(year : Int, month : Int, day : Int) : Pair<String, String> {
        val specificDate = LocalDateTime.of(year, month, day, 0, 0)
        val nextDate = specificDate.plusDays(1)

        val formatter = DateTimeFormatter.ofPattern(dateDBFormat)
        return Pair<String,String>(specificDate.format(formatter), nextDate.format(formatter))
    }

    fun formatDate(fdate: String?): String? {
        var datetime: String? = null
        val inputFormat: DateFormat = SimpleDateFormat(dateDBFormat)
        val d = SimpleDateFormat(dateCalendarFormat)
        try {
            val convertedDate: Date = inputFormat.parse(fdate)
            datetime = d.format(convertedDate)
        } catch (e: ParseException) {
            Log.e("DbQueryHelper::formatDate", "Cannot convert date format")
        }
        return datetime
    }

    fun formatDBDate(fdate: String?): String? {
        var datetime: String? = null
        val inputFormat: DateFormat = SimpleDateFormat(dateCalendarFormat)
        val d = SimpleDateFormat(dateDBFormat)
        try {
            val convertedDate: Date = inputFormat.parse(fdate)
            datetime = d.format(convertedDate)
        } catch (e: ParseException) {
            Log.e("DbQueryHelper::formatDate", "Cannot convert date format")
        }
        return datetime
    }


}