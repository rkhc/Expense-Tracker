package com.example.prototype.activity

import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

open class BaseActivity : AppCompatActivity() {

    //toolbar id, textview id for toolbar
    protected fun enableParentToolbar(toolbar : Toolbar?, toolbarTitle : TextView?, text : CharSequence?)
    {
        if(toolbar == null || toolbarTitle == null)
            return

        setSupportActionBar(toolbar)

        //set the textview text with the xml value in the toolbar
        if(text != null)
            toolbarTitle.setText(text)
        else
            toolbarTitle.setText("")

        //do not show action bar text
        //? means it can be null
        getSupportActionBar()?.setDisplayShowTitleEnabled(false)
    }

    protected fun enableChildToolbar(toolbar : Toolbar, toolbarTitle : TextView, text : CharSequence?)
    {
        enableParentToolbar(toolbar, toolbarTitle, text)

        //show back arrow
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()?.setDisplayShowHomeEnabled(true);
    }
}