package com.example.prototype.fragments

import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.prototype.Expense
import com.example.prototype.ExpenseDB

import com.example.prototype.R
import com.example.prototype.database.DbQueryHelper
import com.example.prototype.database.FeedReaderDbHelper
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_ID_EDIT = "param1"

/**
 * A simple [Fragment] subclass.
 * Use the [EditExpenseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditExpenseFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var editExpenseID = -1
    private var edit_expense: ExpenseDB = ExpenseDB(-1, "", "", 0.0, "")

    private var mDateSetListener : DatePickerDialog.OnDateSetListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            editExpenseID = it.getInt(ARG_ID_EDIT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_expense, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addBackButtonForToolbar(R.id.edit_expense_toolbar, view)

        initializeDropdown(view)

        var toolbarText = "Invalid Expense"
        var typeText = "NIL"
        var nameText = "NIL"
        var priceText = "NIL"
        var dateText = "NIL"

        if(editExpenseID != -1) {
            //extract selected expense from db
            edit_expense = DbQueryHelper.extractExpenseObjectFromID(editExpenseID)

            toolbarText = "Edit " + edit_expense.name
            typeText = edit_expense.type
            nameText = edit_expense.name
            priceText = edit_expense.price.toString()
            dateText = edit_expense.date
        }

        val toolbarView = view.findViewById(R.id.edit_expense_toolbar_text) as TextView
        val typeSpinner = view.findViewById(R.id.edit_text_type_value) as Spinner
        val nameView = view.findViewById(R.id.edit_text_name_value) as TextView
        val priceView = view.findViewById(R.id.edit_text_price_value) as TextView
        val dateView = view.findViewById(R.id.edit_text_date_value) as TextView

        toolbarView.text = toolbarText
        val spinnerStrings = typeSpinner.adapter as ArrayAdapter<String>
        typeSpinner.setSelection(spinnerStrings.getPosition(typeText))
        nameView.text = nameText
        priceView.text = priceText
        dateView.text = DbQueryHelper.formatDate(dateText)

        initializeDatePicker(view, dateView)
        initializeButtons(view, nameView, priceView, dateView)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EditExpenseFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Int) =
            EditExpenseFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ID_EDIT, param1)
                }
            }
    }

    private fun initializeDropdown(view : View) {
        val dropdown = view.findViewById(R.id.edit_text_type_value) as Spinner
        dropdown.adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_list_item_1, expenseTypes)

        dropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.e("EditExpenseFragment::initializeDropdown", "Nothing selected")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                edit_expense.type = parent?.getItemAtPosition((position)) as String
            }
        }
    }

    private fun initializeDatePicker(view : View, dateView: TextView) {

        dateView.setOnClickListener(View.OnClickListener {
            val cal = Calendar.getInstance()
            val year = cal[Calendar.YEAR]
            val month = cal[Calendar.MONTH]
            val day = cal[Calendar.DAY_OF_MONTH]
            val dialog = DatePickerDialog(
                context,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                mDateSetListener,
                year, month, day
            )
            dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        })

        mDateSetListener =
            DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
                var monthdate = month
                monthdate = monthdate + 1
                val date = "$day/$monthdate/$year"
                dateView.text = date
            }
    }

    private fun initializeButtons(view : View, nameView : TextView, priceView : TextView, dateView : TextView) {
        //link the create button
        val deleteButton = view.findViewById(R.id.edit_delete_button) as Button
        deleteButton.setOnClickListener {

            //edit expense object
            DbQueryHelper.deleteExpenseObject(edit_expense.id)

            //go back to home fragment after deleting expense entry
            openFragmentNoBackStack(HomeFragment.newInstance("",""))
        }

        val updateButton = view.findViewById(R.id.edit_update_button) as Button
        updateButton.setOnClickListener {

            edit_expense.name = nameView.text.toString()

            //get rounded price
            try {
                var priceString = priceView.text.toString()
                if(priceString == "")
                    priceString = "0.00"

                //round the values
                edit_expense.price = BigDecimal(priceString.toDouble()).setScale(2, RoundingMode.HALF_EVEN).toDouble()
            }
            catch (e: Exception) {
                Log.e("CreateNewExpenseFragment::onViewCreated", "invalid price")
                edit_expense.price = 0.0
            }

            //continue from here. for some reason the date is not being put into the database
            //maybe its the format or something
            //get date

            Log.e("EditExpenseFragment::initializeButtons", "Current Date: " + edit_expense.date + " Edited Date: " + DbQueryHelper.formatDBDate(dateView.text.toString()))

            //edit_expense.date = dateView.text.toString()
            val date : String? = DbQueryHelper.formatDBDate(dateView.text.toString())
            if(date != null)
                edit_expense.date = date

            //edit expense object
            DbQueryHelper.editExpenseObject(edit_expense)

            //go back to the view page
            activity!!.onBackPressed()
        }
    }
}
