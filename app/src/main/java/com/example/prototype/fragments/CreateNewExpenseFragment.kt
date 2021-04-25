package com.example.prototype.fragments

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.prototype.Expense
import com.example.prototype.NumericFilter
import com.example.prototype.R
import com.example.prototype.database.DbQueryHelper
import com.example.prototype.database.FeedReaderContract
import com.example.prototype.database.FeedReaderDbHelper
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CreateNewActivityFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateNewExpenseFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    var text_name : EditText? = null
    var text_price : EditText? = null
    private var db : SQLiteDatabase? = null

    private var created_expense: Expense =
        Expense("", "", 0.0, "")

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
        return inflater.inflate(R.layout.fragment_create_new_expense, container, false)
    }

    //view has been created and xml objects can be accessed
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addBackButtonForToolbar(R.id.create_new_expense_toolbar, view)

        //extract the data from the form
        text_name = view.findViewById(R.id.create_text_name_value) as EditText
        text_price = view.findViewById(R.id.create_text_price_value) as EditText

        //format to 2 decimal places
        text_price?.setFilters(arrayOf<InputFilter>(NumericFilter(9, 2)))

        initializeDropdown(view)

        //link the back button
        val cancelbutton = view.findViewById(R.id.create_cancel_button) as Button
        cancelbutton.setOnClickListener {
            //go back to the home fragment after insertion
            activity!!.onBackPressed()
        }

        //link the create button
        val submitbutton = view.findViewById(R.id.create_add_button) as Button
        submitbutton.setOnClickListener {

            created_expense.name = text_name!!.text.toString()

            try {
                var priceString = text_price!!.text.toString()
                if(priceString == "")
                    priceString = "0.00"

                //round the values
                created_expense.price = BigDecimal(priceString.toDouble()).setScale(2, RoundingMode.HALF_EVEN).toDouble()
            }
            catch (e: Exception) {
                Log.e("CreateNewExpenseFragment::onViewCreated", "invalid price")
                created_expense.price = 0.0
            }

            created_expense.date = DbQueryHelper.getDate()

            if(created_expense.type != "")
            {
                //insertEntryToDB(created_expense.type, created_expense.name, created_expense.price, created_expense.date)
                DbQueryHelper.insertExpenseObject(created_expense)

                //go back to the home fragment after insertion
                activity!!.onBackPressed()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CreateNewTeamFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateNewExpenseFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun initializeDropdown(view : View) {
        val dropdown = view.findViewById(R.id.create_text_type_value) as Spinner
        dropdown.adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_list_item_1, expenseTypes)

        dropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.e("CreateNewExpenseFragment::initializeDropdown", "Nothing selected")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                created_expense.type = parent?.getItemAtPosition((position)) as String
            }
        }
    }
}
