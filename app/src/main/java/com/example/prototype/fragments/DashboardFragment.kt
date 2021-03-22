package com.example.prototype.fragments

import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.prototype.ExpenseDB
import com.example.prototype.R
import com.example.prototype.database.DbQueryHelper
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DashboardFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val applicationCreatedYear = 2020

//    private var stringTimeUnit : String = "Month"
//    private var stringTimeValue : Int = DbQueryHelper.currentMonth
//    private var stringCategory : String? = "Any"
    private var stringTimeUnit : String = ""
    private var stringTimeValue : Int = -1
    private var stringCategory : String? = ""

    private var dashboardExpenseObjects : MutableList<ExpenseDB>? = null
    private var dashboardUnitSelected = Unit.Month.unitValue
    private var dashboardValueSelected = DbQueryHelper.currentMonth - 1 //-1 for dropdown index
    private var dashboardTypeSelected = DashboardExpenseType.All.expenseValue

    //private var pieChart : PieChart? = null;        //pie chart holds the pie data
    //private var pieData : PieData? = null;          //holds the piedataset
    //private var pieDataSet : PieDataSet? = null;    //holds all the entries


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Logger("Initializing")
        //default view of monthly expenses to see first
        //expenseObjects = DbQueryHelper.extractAllExpenseObjects()
        createPieChart(view, getPieEntryForAllExpensesByType())

        initializeDropdown(view)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DashboardFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DashboardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun getPieEntryForAllExpensesByType() : ArrayList<PieEntry>? {
        //first initialization should query db and get the values of the month
        if(dashboardExpenseObjects == null) {
            dashboardExpenseObjects = DbQueryHelper.filterDB(timeUnitTypes[Unit.Month.unitValue], DbQueryHelper.currentMonth, null)

            //if db query fails, do not continue
            if(dashboardExpenseObjects == null)
                return null
        }

        //the entires for the pie chart
        var entries = ArrayList<PieEntry>()

        //for each section of the pie chart
        var hashMap = HashMap<String, Double>()

        //split by type
        if(dashboardTypeSelected == DashboardExpenseType.All.expenseValue) {
            for (elem in dashboardExpenseObjects!!) {
                if (hashMap.containsKey(elem.type)) {
                    var currentPrice =
                        hashMap.get(elem.type)?.toDouble()?.plus(elem.price) as Double
                    hashMap[elem.type] = currentPrice
                }
                //add the item in the hashmap
                else {
                    hashMap.put(elem.type, elem.price)
                }
            }
        }
        //split by name
        else {
            for (elem in dashboardExpenseObjects!!) {
                if (hashMap.containsKey(elem.name)) {
                    var currentPrice =
                        hashMap.get(elem.name)?.toDouble()?.plus(elem.price) as Double
                    hashMap[elem.name] = currentPrice
                }
                //add the item in the hashmap
                else {
                    hashMap.put(elem.name, elem.price)
                }
            }
        }
        for(elem in hashMap) {
            entries.add(PieEntry(elem.value.toFloat(), elem.key))
        }

        return entries
    }

    private fun extractTimeValuesFromUnit(pos : Int) : ArrayList<String> {
        var mapping  = ArrayList<String>()

        if(timeUnitTypes[pos] == "Day") {
            val calendar = Calendar.getInstance()
            val maxDays = calendar.getActualMaximum((Calendar.DAY_OF_MONTH))

            for(i in 1..maxDays) {
                mapping.add(i.toString())
            }
        }
        else if (timeUnitTypes[pos] == "Month") {
            mapping = monthTypes
        }
        else if (timeUnitTypes[pos] == "Year") {
            //add number of years from when app was created to current year
            for(i in applicationCreatedYear..(DbQueryHelper.currentYear)) {
                mapping.add(i.toString())
            }
        }

        return mapping
    }

    private fun initializeDropdown(view : View) {
        //initialize unit array
        //never changes dropdown list
        var dropdownUnit = view.findViewById(R.id.dashboard_time_unit_dropdown) as Spinner
        dropdownUnit.adapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, timeUnitTypes)
        dropdownUnit.setSelection(dashboardUnitSelected)

        //initialize value array
        val valueArray = extractTimeValuesFromUnit(dashboardUnitSelected)
        var dropdownValue = view.findViewById(R.id.dashboard_time_value_dropdown) as Spinner
        dropdownValue?.adapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, valueArray)
        dropdownValue.setSelection(dashboardValueSelected)

        //initialize category array
        //never changes dropdown list
        var dropdownCategory = view.findViewById(R.id.dashboard_category_dropdown) as Spinner
        dropdownCategory.adapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, dashboardExpenseTypes)

        Logger("unit selected is: " + timeUnitTypes[dashboardUnitSelected])
        Logger("value selected is: " + dashboardValueSelected.toString())

        //item listener
        dropdownUnit?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            //var dropdownValue = view.findViewById(R.id.dashboard_time_value_dropdown) as Spinner

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.e("DashboardFragment::initializeDropdown", "Nothing selected")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                //a change in day/month/year
                if(timeUnitTypes[position] != timeUnitTypes[dashboardUnitSelected]) {
                    //adjust value to given date based on the unit
                    //-1 from date and month because dropdown list starts from 0
                    if(timeUnitTypes[position] == Unit.Day.name) {
                        dashboardValueSelected = DbQueryHelper.currentDay - 1
                    }
                    else if(timeUnitTypes[position] == Unit.Month.name) {
                        dashboardValueSelected = DbQueryHelper.currentMonth - 1
                    }
                    else if (timeUnitTypes[position] == Unit.Year.name) {
                        dashboardValueSelected = DbQueryHelper.currentYear - applicationCreatedYear
                    }
                    dashboardUnitSelected = position

                    //create new dropdown list for values as unit changed
                    val valueArray = extractTimeValuesFromUnit(dashboardUnitSelected)
                    dropdownValue?.adapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, valueArray)
                    dropdownValue.setSelection(dashboardValueSelected)
                    //refresh the layout if any changes to update dropdown unit adapter
                    refreshFragment()
                }
            }
        }

        dropdownValue?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            var dropdownUnit = view.findViewById(R.id.dashboard_time_unit_dropdown) as Spinner

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.e("DashboardFragment::initializeDropdown", "Nothing selected")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {


                //val unit = dropdownUnit.selectedItem.toString()
                dashboardValueSelected = position
                Logger("dropdown position in value is: " + dashboardValueSelected.toString())

                val value = getValueFromUnit(dropdownUnit.selectedItemPosition, dashboardValueSelected)

                filterDBQuery(timeUnitTypes[dashboardUnitSelected], value, dashboardExpenseTypes[dashboardTypeSelected])
            }
        }

        dropdownCategory?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            var dropdownUnit = view.findViewById(R.id.dashboard_time_unit_dropdown) as Spinner

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.e("DashboardFragment::initializeDropdown", "Nothing selected")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                dashboardTypeSelected = position

                val value = getValueFromUnit(dropdownUnit.selectedItemPosition, dashboardValueSelected)

                filterDBQuery(timeUnitTypes[dashboardUnitSelected], value, dashboardExpenseTypes[dashboardTypeSelected])
            }
        }

    }

    //get calendar value based on unit
    private fun getValueFromUnit(unit : Int, givenValue : Int) : Int {
        var value = 0

        if (unit == Unit.Day.unitValue) {
            value = givenValue + 1     //+1 as dropdown list starts from 0
        } else if (unit == Unit.Month.unitValue) {
            value = givenValue + 1    //+1 as dropdown list starts from 0
        } else if (unit == Unit.Year.unitValue) {
            value = applicationCreatedYear + givenValue   //get current year
        }

        return value
    }

    private fun filterDBQuery(unit : String, value : Int, type : String) {
        var changed : Boolean = false

        val  s = "new unit: " + unit + " value: " + value.toString() + " type: " + type
        val  s1 = "old unit: " + stringTimeUnit + " value: " + stringTimeValue.toString() + " type: " + stringCategory

        Log.e("roger", s1)
        Log.e("roger", s)

        if(stringTimeUnit != unit || stringTimeValue != value || stringCategory != type)
            changed = true;

        //any type set to null to not search for anything specific type
        if(unit != null) {
            stringTimeUnit = unit
        }

        if(value != null) {
            stringTimeValue = value
        }

        stringCategory =  type

        val stringCategoryDB = if(type == "All") null else type

        if(stringCategoryDB == null) {
            Logger("Sending null to db")
        }
        else {
            Logger("Not sending null to db")
        }
        //redraw the pie chart
        if(changed) {
            Logger("CHANGE")
            dashboardExpenseObjects = DbQueryHelper.filterDB(stringTimeUnit, stringTimeValue, stringCategoryDB)
            if(view != null) {
                createPieChart(view!!, getPieEntryForAllExpensesByType())
                //refresh page
                //refreshFragment(R.id.fragment_dashboard)
                refreshFragment()
            }
        }
    }

    private fun createPieChart(view : View, entries :  ArrayList<PieEntry>?) {


        var pieEntries = ArrayList<PieEntry>(); //multiple entries
        var title = "Total Spent: " //total price

        if(entries == null) {
            pieEntries.add(PieEntry(1f, "NIL"))
            title += "$0.0"
        }
        else {
            pieEntries = entries
            pieEntries.sortWith(compareByDescending<PieEntry> {it.value})
            title += DbQueryHelper.formatNumberCurrency(entries?.sumByDouble { it.value.toDouble()}.toString())
        }

        //dataset
        var pieDataSet = PieDataSet(pieEntries, "")
        pieDataSet?.colors = ColorTemplate.PASTEL_COLORS.toMutableList()
        pieDataSet?.valueTextColor = Color.WHITE;
        pieDataSet?.valueTextSize = 20f;
        //pieDataSet?.sliceSpace = 5f;

        //data
        var pieData = PieData(pieDataSet)

        //pie chart
        var pieChart = view.findViewById(R.id.dashboard_pieChart) as PieChart

        val desc = Description()
        desc.text = if(entries == null) "NIL" else title
        desc.textSize = 20f

        pieChart?.description = desc
        pieChart?.data = pieData
    }
}
