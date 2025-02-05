package com.agvahealthcare.ventilator_ext.utility.utils

import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import com.agvahealthcare.ventilator_ext.R

//
class GenericKeyEvent internal constructor(private val currentView: EditText, private val previousView: EditText?) : TextWatcher{
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(cs: CharSequence?, p1: Int, p2: Int, p3: Int) {

        if (cs.toString().isEmpty()){
            previousView?.requestFocus()
        }
    }

    override fun afterTextChanged(p0: Editable?) {
    }
}

class GenericTextWatcher internal constructor(private val currentView: View, private val nextView: View?) : TextWatcher {
    override fun afterTextChanged(editable: Editable) { // TODO Auto-generated method stub
        val text = editable.toString()
        when (currentView.id) {
            R.id.etOtp1 -> if (text.length == 1) nextView!!.requestFocus()
            R.id.etOtp2 -> if (text.length == 1) nextView!!.requestFocus()
            R.id.etOtp3 -> if (text.length == 1) nextView!!.requestFocus()
         //   R.id.et_4 -> if (text.length == 1) nextView!!.requestFocus()

            //You can use EditText4 same as above to hide the keyboard
        }
    }

    override fun beforeTextChanged(
        arg0: CharSequence,
        arg1: Int,
        arg2: Int,
        arg3: Int
    ) { // TODO Auto-generated method stub
    }

    override fun onTextChanged(
        arg0: CharSequence,
        arg1: Int,
        arg2: Int,
        arg3: Int
    ) { // TODO Auto-generated method stub
    }

}