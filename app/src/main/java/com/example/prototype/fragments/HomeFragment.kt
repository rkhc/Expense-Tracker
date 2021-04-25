package com.example.prototype.fragments

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.example.prototype.Expense
import com.example.prototype.ExpenseDB
import com.example.prototype.database.FeedReaderDbHelper
import com.example.prototype.R
import com.example.prototype.database.DbQueryHelper
import java.text.DecimalFormat

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        removeBackStack()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //context = container?.context;
        val listView = view.findViewById(R.id.team_list_view) as ListView

        populateArray(activity, listView)

        //link add image to create new team
        val image = view.findViewById(R.id.home_toolbar_add_sign) as ImageView
        image.setOnClickListener {
            // your code to perform when the user clicks on the ImageView
            openFragment(CreateNewExpenseFragment.newInstance("", ""))
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.extractAllExpenseObjects
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun populateArray(context : Context?, listView : ListView) {

        //var expenses = DbQueryHelper.extractAllExpenseObjects()
        val expenses = DbQueryHelper.filterDB("Month",DbQueryHelper.currentMonth,null)

        if(expenses != null) {
            //expense name the user will see
            val expense_names = arrayOfNulls<String>(expenses.size) as Array<String>
            for (i in 0 until expense_names.size) {
                expense_names[i] =
                    expenses[i].name + " - " + DbQueryHelper.formatNumberCurrency(expenses[i].price.toString())
            }

            //param1, your context
            //param2, the template layout of the list, there are preset templates to choose from
            //param3, the textview arguments
            val adapter =
                ArrayAdapter<String>(context!!, android.R.layout.simple_list_item_1, expense_names)
            listView.adapter = adapter

            //change fragments when team name is selected
            listView.setOnItemClickListener { parent, view, position, id ->
                //pass the expense name to the selected fragment
                openFragment(SelectedExpenseFragment.newInstance(expenses[position].id))
            }
        }
    }
}
