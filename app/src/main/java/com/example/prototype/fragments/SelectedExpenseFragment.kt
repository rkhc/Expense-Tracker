package com.example.prototype.fragments

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.prototype.R
import com.example.prototype.database.DbQueryHelper
import com.example.prototype.database.FeedReaderDbHelper

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
// what to bundle this key name to
private const val ARG_ID = "selected_id"

/**
 * A simple [Fragment] subclass.
 * Use the [SelectedTeamFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SelectedExpenseFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var selectedExpenseID = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedExpenseID = it.getInt(ARG_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_selected_expense, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addBackButtonForToolbar(R.id.selected_expense_toolbar, view)

        var toolbarText = "Invalid Expense"
        var typeText = "NIL"
        var priceText = "NIL"
        var dateText = "NIL"

        if(selectedExpenseID != -1) {
            //extract selected expense from db
            val expense = DbQueryHelper.extractExpenseObjectFromID(selectedExpenseID)
            toolbarText = expense.name
            typeText = expense.type
            priceText = DbQueryHelper.formatNumberCurrency(expense.price.toString())
            dateText = expense.date
        }

        var toolbarView = view.findViewById(R.id.selected_expense_toolbar_text) as TextView
        var typeView = view.findViewById(R.id.selected_text_type_value) as TextView
        var priceView = view.findViewById(R.id.selected_text_price_value) as TextView
        var dateView = view.findViewById(R.id.selected_text_date_value) as TextView

        toolbarView.text = toolbarText
        typeView.text = typeText
        priceView.text = priceText
        dateView.text = DbQueryHelper.formatDate(dateText)

        initializeButtons(view)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment SelectedTeamFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(id: Int) =
            SelectedExpenseFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ID, id)
                }
            }
    }

    private fun initializeButtons(view :View) {
        //link the back button
        val editButton = view.findViewById(R.id.selected_back_button) as Button
        editButton.setOnClickListener {
            //go back to the home fragment after insertion
            openFragment(HomeFragment.newInstance("",""))
        }

        //link the create button
        val deleteButton = view.findViewById(R.id.selected_edit_button) as Button
        deleteButton.setOnClickListener {
            //go to the edit fragment
            openFragment(EditExpenseFragment.newInstance(selectedExpenseID))
        }
    }
}
