package com.example.prototype
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

open class Expense (type : String = "", name : String = "", price : Double = 0.0, date : String = "") {

    var type = type
    var name = name
    var price = price
    var date = date
}

class ExpenseDB (id : Int = -1, type : String = "", name : String = "", price : Double = 0.0, date : String = "") :
    Expense(type, name, price, date) {
    var id = id
}
