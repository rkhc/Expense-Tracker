package com.example.prototype.fragments

import android.util.Log
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.prototype.ExpenseDB
import com.example.prototype.R

open class BaseFragment : Fragment() {

    enum class Unit(val unitValue : Int) {
        Day(0),
        Month( 1),
        Year(2)
    }

    enum class Month(val monthValue : Int) {
        Jan(0),
        Feb( 1),
        Mar( 2),
        Apr( 3),
        May( 4),
        Jun( 5),
        Jul( 6),
        Aug( 7),
        Sep( 8),
        Oct( 9),
        Nov( 10),
        Dec(11),
    }

    enum class CreateExpenseType(val expenseValue : Int) {
        Food(0),
        Entertainment( 1),
        Bills( 2),
        Transport( 3),
        Others( 4),
    }

    enum class DashboardExpenseType(val expenseValue : Int) {
        All(0),
        Food(1),
        Entertainment( 2),
        Bills( 3),
        Transport( 4),
        Others( 5),
    }

    val timeUnitTypes = arrayListOf<String>(Unit.Day.name, Unit.Month.name, Unit.Year.name)

    val monthTypes = arrayListOf<String>(Month.Jan.name, Month.Feb.name, Month.Mar.name, Month.Apr.name, Month.May.name,
        Month.Jun.name, Month.Jul.name, Month.Aug.name, Month.Sep.name, Month.Oct.name, Month.Nov.name, Month.Dec.name)

    val expenseTypes = arrayListOf<String>(CreateExpenseType.Food.name, CreateExpenseType.Entertainment.name, CreateExpenseType.Bills.name,
        CreateExpenseType.Transport.name, CreateExpenseType.Others.name)

    val dashboardExpenseTypes = arrayListOf<String>(DashboardExpenseType.All.name, DashboardExpenseType.Food.name, DashboardExpenseType.Entertainment.name, DashboardExpenseType.Bills.name,
        DashboardExpenseType.Transport.name, DashboardExpenseType.Others.name)

    //to switch between fragments
    protected fun openFragment(fragment: Fragment)
    {
        val transaction = getActivity()?.supportFragmentManager?.beginTransaction() as FragmentTransaction
        if(transaction != null) {
            transaction.replace(R.id.fragment_container, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    protected fun openFragmentNoBackStack(fragment: Fragment)
    {
        val transaction = getActivity()?.supportFragmentManager?.beginTransaction() as FragmentTransaction
        if(transaction != null) {
            transaction.replace(R.id.fragment_container, fragment)
            getActivity()?.supportFragmentManager?.popBackStack()
            transaction.commit()
        }
    }

    protected fun removeBackStack() {
        val fm =  getActivity()?.supportFragmentManager as FragmentManager
        for(i in 0 until fm.backStackEntryCount) {
            fm.popBackStack();
        }
    }

    protected fun addBackButtonForToolbar(toolbarID : Int, view : View?) {
        val toolbar = view?.findViewById(toolbarID) as Toolbar
        toolbar?.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar?.setNavigationOnClickListener { activity!!.onBackPressed() }
    }

    protected fun refreshFragment () {

        //fragmentManager!!.beginTransaction().detach(this).attach(this).commit()

        //val fragment = getActivity()?.supportFragmentManager?.findFragmentById(fragmentID)
        val fragment = this
        val transaction : FragmentTransaction? = getActivity()?.supportFragmentManager?.beginTransaction() as FragmentTransaction

        if(transaction != null && fragment != null) {
            //transaction.detach(fragment)
            //transaction.attach(fragment)
            //transaction.commit()
            Logger("Redrawing")
            transaction.detach(fragment).attach(fragment).commitAllowingStateLoss()
        }
        else
        {
            if(fragment == null)
                Logger("fragment is null")

            if(transaction == null)
                Logger("transaction is null")
        }
    }

    protected fun Logger(text : String) {
        Log.e("roger", text)
    }
}