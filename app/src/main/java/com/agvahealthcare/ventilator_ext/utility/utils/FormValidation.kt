package com.agvahealthcare.ventilator_ext.utility.utils;

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import com.agvahealthcare.ventilator_ext.utility.LOG_TYPE_DEBUG

object FormValidation  : Activity(){
    val emailPattern =
        "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"

    var MobilePattern = "[0-9]{10}"
    val Password = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@\$%^&*-]).{8,}\$"
    val Name = "^[A-Za-z]+$"
    val UserName = "^(?=[a-zA-Z0-9._]{8,20}\$)(?!.*[_.]{2})[^_.].*[^_.]\$"
    var any_Number = "[0-9]"
    var link =
        "(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,})"
    var otp_regex = "^[0-9]{6}\$"
    var IFSC_REGEX = "^[A-Z]{4}0[A-Z0-9]{6}$"

    fun service_details_validations(etName : EditText,etPhone : EditText,etEmail : EditText,etHospital : EditText,etWardNo : EditText,etDepartment : EditText) : Boolean{

        if (etName.text.isEmpty()) return false
        else if (etName.text.startsWith(" ")) return false
        else if (etName.text.length < 5) return false

        else if (etPhone.text.isEmpty()) return false
        else if (etPhone.text.startsWith("0")) return false
        else if (etPhone.text.length < 10) return false

        else if(etEmail.text.isEmpty()) return false
        else if(!etEmail.text.matches(Regex(emailPattern))) return false

        else if (etHospital.text.isEmpty()) return false
        else if (etHospital.text.startsWith(" ")) return false

        else if (etWardNo.text.isEmpty()) return false

        else if (etDepartment.text.isEmpty()) return false
        else if (etDepartment.text.startsWith(" ")) return false

        return true
    }

    fun bank_details_validation(
        bankName : EditText,
        tvBankName :TextView,
        account_number: EditText,
        tv_account_number: TextView,
        account_name: EditText,
        tv_account_name: TextView,
        bank_ifsc: EditText,
        tv_bank_ifsc: TextView,
        account_type:Spinner,
        tv_account_type:TextView

    ):Boolean {

        if (bankName.text.isEmpty()){
            tvBankName.visibility = View.VISIBLE
            tvBankName.text = "*Please enter your bank name."
        }
        else if (account_number.text.isEmpty()) {
            tvBankName.visibility = View.GONE
            tv_account_number.visibility=View.VISIBLE
            tv_account_number.text = "*Please enter your account number."

        } else if (account_number.text.length < 8) {
            tvBankName.visibility = View.GONE
            tv_account_number.visibility=View.VISIBLE
            tv_account_number.text = "*Please enter valid account number."

        }else if (account_name.text.isEmpty()) {
            tvBankName.visibility = View.GONE
            tv_account_number.visibility=View.GONE
            tv_account_name.visibility=View.VISIBLE
            tv_account_name.text = "*Please enter your name."
        }

        else if (account_name.text.length < 2) {
            tvBankName.visibility = View.GONE
            tv_account_number.visibility=View.GONE
            tv_account_name.visibility=View.VISIBLE
            tv_account_name.text = "*Please enter valid name."

        } else if(bank_ifsc.text.isEmpty()) {
            tvBankName.visibility = View.GONE
            tv_account_number.visibility=View.GONE
            tv_account_name.visibility=View.GONE
            tv_bank_ifsc.visibility=View.VISIBLE
            tv_bank_ifsc.text="*Please enter your bank ifsc code."

        }else if(bank_ifsc.text.startsWith(" ")) {
            tvBankName.visibility = View.GONE
            tv_account_number.visibility=View.GONE
            tv_account_name.visibility=View.GONE
            tv_bank_ifsc.visibility=View.VISIBLE
            tv_bank_ifsc.text="*Please enter valid bank ifsc code."
        }

        else if(bank_ifsc.text.length < 11){
            tvBankName.visibility = View.GONE
            tv_account_number.visibility=View.GONE
            tv_account_name.visibility=View.GONE
            tv_bank_ifsc.visibility=View.VISIBLE
            tv_bank_ifsc.text="*Please enter valid bank ifsc code."
        }
        else if (account_type.selectedItem.equals("Select")) {
            tvBankName.visibility = View.GONE
            tv_account_number.visibility=View.GONE
            tv_account_name.visibility=View.GONE
            tv_bank_ifsc.visibility=View.GONE
            tv_account_type.visibility=View.VISIBLE
            tv_account_type.text = "*Please select account type."

        }else{
            tvBankName.visibility = View.GONE
            tv_account_number.visibility=View.GONE
            tv_account_name.visibility=View.GONE
            tv_bank_ifsc.visibility=View.GONE
            tv_account_type.visibility=View.GONE

            return true
        }
        return false
    }

    fun add_Address(
        name_et: EditText,
        name_tv: TextView,
        Mobile_et: EditText,
        Mobile_tv: TextView,
        Address_et: EditText,
        Address_tv: TextView,
        pincode_et:EditText,
        pincode_tv:TextView,
        Country_et: TextView,
        Country_tv: TextView,
        State_et: TextView,
        State_tv: TextView,
        City_et: TextView,
        City_tv: TextView,
        Landmark_et: EditText,
        landmark_tv: TextView
    ) {
        if (name_et.text.isEmpty()) {
            name_tv.visibility=View.VISIBLE
            name_tv.text = "*Please enter your name."

        }else if (name_et.text.startsWith(" ")) {
            name_tv.visibility=View.VISIBLE
            name_tv.text = "*Please enter valid name."

        } else if (name_et.text.length < 2) {
            name_tv.visibility=View.VISIBLE
            name_tv.text = "*Please enter valid name."

        }else if (Mobile_et.text.isEmpty()) {
            name_tv.visibility=View.GONE
            Mobile_tv.visibility=View.VISIBLE
            Mobile_tv.text = "*Please enter your mobile number."

        }else if (Mobile_et.text.startsWith("0")) {
            name_tv.visibility=View.GONE
            Mobile_tv.visibility=View.VISIBLE
            Mobile_tv.text = "*Please enter valid mobile number."
        }
        else if (Mobile_et.text.length < 10) {
            name_tv.visibility=View.GONE
            Mobile_tv.visibility=View.VISIBLE
            Mobile_tv.text = "*Please enter valid mobile number."

        } else if(Address_et.text.isEmpty()) {
            name_tv.visibility=View.GONE
            Mobile_tv.visibility=View.GONE
            Address_tv.visibility=View.VISIBLE
            Address_tv.text="*Please enter your address."

        }else if(Address_et.text.startsWith(" ")) {
            name_tv.visibility=View.GONE
            Mobile_tv.visibility=View.GONE
            Address_tv.visibility=View.VISIBLE
            Address_tv.text="*Please enter valid address."
        }
        else if(pincode_et.text.isEmpty()){
            name_tv.visibility=View.GONE
            Mobile_tv.visibility=View.GONE
            Address_tv.visibility=View.GONE
            pincode_tv.visibility=View.VISIBLE
            pincode_tv.text="*Please enter your pincode."
        }
        else if(pincode_et.text.length < 6){
            name_tv.visibility=View.GONE
            Mobile_tv.visibility=View.GONE
            Address_tv.visibility=View.GONE
            pincode_tv.visibility=View.VISIBLE
            pincode_tv.text="*Please enter valid pincode."
        }

        else if (Country_et.text.toString().equals("")) {
            Country_et.requestFocus()
            name_tv.visibility=View.GONE
            Mobile_tv.visibility=View.GONE
            Address_tv.visibility=View.GONE
            pincode_tv.visibility=View.GONE
            Country_tv.visibility=View.VISIBLE
            Country_tv.text = "*Please select your country."
        }

        else if (State_et.text.toString().equals("")) {
            State_et.requestFocus()
            name_tv.visibility=View.GONE
            Mobile_tv.visibility=View.GONE
            Address_tv.visibility=View.GONE
            pincode_tv.visibility=View.GONE
            Country_tv.visibility=View.GONE
            State_tv.visibility=View.VISIBLE
            State_tv.text = "*Please select your state."
        }

        else if (City_et.text.toString().equals("")) {
            City_et.requestFocus()
            name_tv.visibility=View.GONE
            Mobile_tv.visibility=View.GONE
            Address_tv.visibility=View.GONE
            pincode_tv.visibility=View.GONE
            Country_tv.visibility=View.GONE
            State_tv.visibility=View.GONE
            City_tv.visibility=View.VISIBLE
            City_tv.text = "*Please select your city."
        }

        else if (Landmark_et.text.isEmpty()) {
            name_tv.visibility=View.GONE
            Mobile_tv.visibility=View.GONE
            Address_tv.visibility=View.GONE
            pincode_tv.visibility=View.GONE
            Country_tv.visibility=View.GONE
            State_tv.visibility=View.GONE
            City_tv.visibility=View.GONE
            landmark_tv.visibility=View.VISIBLE
            landmark_tv.text = "*Please enter your landmark."
        }
        else if (Landmark_et.text.startsWith(" ")) {
            name_tv.visibility=View.GONE
            Mobile_tv.visibility=View.GONE
            Address_tv.visibility=View.GONE
            pincode_tv.visibility=View.GONE
            Country_tv.visibility=View.GONE
            State_tv.visibility=View.GONE
            City_tv.visibility=View.GONE
            landmark_tv.visibility=View.VISIBLE
            landmark_tv.text = "*Please enter valid landmark."
        }else{
            name_tv.visibility=View.GONE
            Mobile_tv.visibility=View.GONE
            Address_tv.visibility=View.GONE
            pincode_tv.visibility=View.GONE
            Country_tv.visibility=View.GONE
            State_tv.visibility=View.GONE
            City_tv.visibility=View.GONE
            landmark_tv.visibility=View.GONE
        }
    }

    fun editProfile(
        etName: EditText,
        tvName: TextView,
        etPhone: EditText,
        tvMobile: TextView,
        etEmail: EditText,
        tvEmail: TextView,
        etPincode: EditText,
        tvPincode: TextView,
        etAddress: EditText, tvAddress: TextView,
        txtCountry: TextView, tvCountry: TextView,
        txtState: TextView, tvState: TextView,
        txtCity: TextView, tvCity: TextView,
        txtUploadedFile: TextView, tvUploadDocument: TextView
    ) : Boolean{

        if (etName.text.isEmpty()) {
            tvName.visibility=View.VISIBLE
            tvName.text = "*Please enter your name."

        }else if (etName.text.startsWith(" ")) {
            tvName.visibility=View.VISIBLE
            tvName.text = "*Please enter your valid name."

        } else if (etName.text.length < 2) {
            tvName.visibility=View.VISIBLE
            tvName.text = "*Please enter valid name."

        }else if (etPhone.text.isEmpty()) {
            tvName.visibility=View.GONE
            tvMobile.visibility=View.VISIBLE
            tvMobile.text = "*Please enter your mobile number."

        }else if (etPhone.text.startsWith("0")) {
            tvName.visibility=View.GONE
            tvMobile.visibility=View.VISIBLE
            tvMobile.text = "*Please enter valid mobile number."
        }
        else if (etPhone.text.length < 10) {
            tvName.visibility=View.GONE
            tvMobile.visibility=View.VISIBLE
            tvMobile.text = "*Please enter valid mobile number."

        } else if(etEmail.text.isEmpty()) {
            tvName.visibility=View.GONE
            tvMobile.visibility=View.GONE
            tvEmail.visibility=View.VISIBLE
            tvEmail.text="*Please enter your email address."
        }
        else if(!etEmail.text.matches(Regex(emailPattern))) {
            tvName.visibility=View.GONE
            tvMobile.visibility=View.GONE
            tvEmail.visibility=View.VISIBLE
            tvEmail.text="*Please enter valid email address."
        }
        else if (etPincode.text.isEmpty()) {
            tvName.visibility=View.GONE
            tvEmail.visibility=View.GONE
            tvMobile.visibility=View.GONE
            tvPincode.visibility=View.VISIBLE
            tvPincode.text = "*Please enter your pincode."

        }else if (etPincode.text.length < 6) {
            tvName.visibility=View.GONE
            tvEmail.visibility=View.GONE
            tvMobile.visibility=View.GONE
            tvPincode.visibility=View.VISIBLE
            tvPincode.text = "*Please enter valid pincode."

        } else if(etAddress.text.isEmpty()) {
            tvName.visibility=View.GONE
            tvMobile.visibility=View.GONE
            tvPincode.visibility=View.GONE
            tvEmail.visibility=View.GONE
            tvAddress.visibility=View.VISIBLE
            tvAddress.text="*Please enter your address."
        }
        else if (txtCountry.text.isEmpty()) {
            txtCountry.requestFocus()
            tvName.visibility=View.GONE
            tvMobile.visibility=View.GONE
            tvPincode.visibility=View.GONE
            tvEmail.visibility=View.GONE
            tvAddress.visibility=View.GONE
            tvCountry.visibility=View.VISIBLE
            tvCountry.text = "*Please select your country."
        }
        else if (txtState.text.isEmpty()) {
            txtState.requestFocus()
            tvName.visibility=View.GONE
            tvMobile.visibility=View.GONE
            tvPincode.visibility=View.GONE
            tvEmail.visibility=View.GONE
            tvAddress.visibility=View.GONE
            tvCountry.visibility=View.GONE
            tvState.visibility=View.VISIBLE
            tvState.text = "*Please select your state."
        }
        else if (txtCity.text.isEmpty()) {
            txtCity.requestFocus()
            tvName.visibility=View.GONE
            tvMobile.visibility=View.GONE
            tvPincode.visibility=View.GONE
            tvEmail.visibility=View.GONE
            tvAddress.visibility=View.GONE
            tvCountry.visibility=View.GONE
            tvState.visibility=View.GONE
            tvCity.visibility=View.VISIBLE
            tvCity.text = "*Please select your city."
        }
        else if (txtUploadedFile.text.toString().equals("")) {
            tvName.visibility=View.GONE
            tvMobile.visibility=View.GONE
            tvPincode.visibility=View.GONE
            tvEmail.visibility=View.GONE
            tvAddress.visibility=View.GONE
            tvCountry.visibility=View.GONE
            tvState.visibility=View.GONE
            tvCity.visibility=View.GONE
            tvUploadDocument.visibility=View.VISIBLE
            tvUploadDocument.text = "*Please select your document."

        }else{
            tvName.visibility=View.GONE
            tvMobile.visibility=View.GONE
            tvPincode.visibility=View.GONE
            tvEmail.visibility=View.GONE
            tvAddress.visibility=View.GONE
            tvCountry.visibility=View.GONE
            tvState.visibility=View.GONE
            tvCity.visibility=View.GONE
            tvUploadDocument.visibility=View.GONE

            return true
        }
        return false
    }

    fun paymentCard(
        etCardNumber: EditText,
        tvCard: TextView,
        etCardExpiryMM: String,
        tvCardExpiryDate: TextView,
        etCardExpiryYY: String,
        etCVV: EditText,
        tvCardCVV: TextView,
        etCardHolderName: EditText,
        tvCardHolderName: TextView

    ): Boolean {

        tvCard.text = ""
        tvCardExpiryDate.text = ""
        tvCardCVV.text = ""
        tvCardHolderName.text = ""

        val etCardNumber = etCardNumber.text.toString()
        val etCVV = etCVV.text.toString()
        val etCardHolderName = etCardHolderName.text.toString()

        if (etCardNumber.isEmpty()) {
//            etCardNumber.requestFocus()
            tvCard.visibility = View.VISIBLE
            tvCard.text = "*Please enter card number."

        }  else if (etCardNumber.length < 16 || etCardNumber.length > 16) {
//            etCardNumber.requestFocus()
            tvCard.visibility = View.VISIBLE
            tvCard.text = "*Please enter valid card number."

        }
        else if (etCardExpiryMM.equals("Select Month")) {

            tvCardExpiryDate.isVisible = true
            tvCard.visibility = View.GONE
            tvCardExpiryDate.visibility = View.VISIBLE
            tvCardExpiryDate.text = "*Please enter valid card expiry month."
        }
        else if (etCardExpiryYY.equals("Select Year")) {

            tvCardExpiryDate.isVisible = true
            tvCard.visibility = View.GONE
            tvCardExpiryDate.visibility = View.VISIBLE
            tvCardExpiryDate.text = "*Please enter valid card expiry year."
        }

        else if (etCVV.length < 3) {
//            etCVV.requestFocus()
            tvCardCVV.visibility = View.VISIBLE
            tvCard.visibility = View.GONE
            tvCardExpiryDate.visibility = View.GONE
            tvCardCVV.text = "*Please enter valid CVV."

        } else if (etCardHolderName.length < 2) {
//            etCardHolderName.requestFocus()
            tvCardHolderName.visibility = View.VISIBLE
            tvCard.visibility = View.GONE
            tvCardCVV.visibility = View.GONE
            tvCardExpiryDate.visibility = View.GONE
            tvCardHolderName.text = "*Please enter valid card holder name."

        } else {
            tvCard.text = ""
            tvCardExpiryDate.text = ""
            tvCardCVV.text = ""
            tvCardHolderName.text = ""
            tvCard.visibility = View.GONE
            tvCardExpiryDate.visibility = View.GONE
            tvCardCVV.visibility = View.GONE
            tvCardHolderName.visibility = View.GONE

            return true
        }

        return false
    }


    fun ChangePassword(
        OldPasswordET: EditText,
        OldPasswordTV: TextView,
        PasswordET: EditText,
        NewPasswordTV: TextView,
        ConfirmPasswordET: EditText,
        ConfirmPasswordTv: TextView

    ): Boolean {

        if (OldPasswordET.text.isEmpty()) {
            OldPasswordTV.visibility = View.VISIBLE
//            v1.setBackgroundColor(resources.getColor(R.color.error_color))
            OldPasswordTV.text = "*Please enter your old password."

        } else if (PasswordET.text.isEmpty()) {
            NewPasswordTV.visibility = View.VISIBLE
            OldPasswordTV.visibility = View.GONE
//            v1.setBackgroundColor(resources.getColor(R.color.HomeThemeColor))
//            v2.setBackgroundColor(resources.getColor(R.color.error_color))
            NewPasswordTV.text = "*Please enter new password."

        }else if (PasswordET.text.startsWith(" ")) {
            NewPasswordTV.visibility = View.VISIBLE
            OldPasswordTV.visibility = View.GONE
//            v1.setBackgroundColor(resources.getColor(R.color.HomeThemeColor))
//            v2.setBackgroundColor(resources.getColor(R.color.error_color))
            NewPasswordTV.text = "*Please enter valid password."

        }else if (PasswordET.text.toString() == OldPasswordET.text.toString()) {
            NewPasswordTV.visibility = View.VISIBLE
            OldPasswordTV.visibility = View.GONE
//            v1.setBackgroundColor(resources.getColor(R.color.HomeThemeColor))
//            v2.setBackgroundColor(resources.getColor(R.color.error_color))
            NewPasswordTV.text = "*New password must not be same as old password."

        } else if (!PasswordET.text.matches(Regex(Password))) {
            NewPasswordTV.visibility = View.VISIBLE
            OldPasswordTV.visibility = View.GONE
//            v1.setBackgroundColor(resources.getColor(R.color.HomeThemeColor))
//            v2.setBackgroundColor(resources.getColor(R.color.error_color))
            NewPasswordTV.text = "*Please enter strong password."

        } else if (ConfirmPasswordET.text.isEmpty()) {
            ConfirmPasswordTv.visibility = View.VISIBLE
            NewPasswordTV.visibility = View.GONE
            OldPasswordTV.visibility = View.GONE
//            v1.setBackgroundColor(resources.getColor(R.color.HomeThemeColor))
//            v2.setBackgroundColor(resources.getColor(R.color.HomeThemeColor))
//            v3.setBackgroundColor(resources.getColor(R.color.error_color))
            ConfirmPasswordTv.setText("*Please enter confirm password.")

        } else if (!PasswordET.text.toString().equals(ConfirmPasswordET.text.toString())) {
            ConfirmPasswordTv.visibility = View.VISIBLE
            NewPasswordTV.visibility = View.GONE
            OldPasswordTV.visibility = View.GONE
//            v1.setBackgroundColor(resources.getColor(R.color.HomeThemeColor))
//            v2.setBackgroundColor(resources.getColor(R.color.HomeThemeColor))
//            v3.setBackgroundColor(resources.getColor(R.color.error_color))
            ConfirmPasswordTv.setText("*Confirm password should be same with the password field.")

        } else {
            ConfirmPasswordTv.visibility = View.GONE
            NewPasswordTV.visibility = View.GONE
            OldPasswordTV.visibility = View.GONE
//            v1.setBackgroundColor(resources.getColor(R.color.HomeThemeColor))
//            v2.setBackgroundColor(resources.getColor(R.color.HomeThemeColor))
//            v3.setBackgroundColor(resources.getColor(R.color.HomeThemeColor))

            return true
        }
        return false
    }



    fun EmailValidations(
        emailET: EditText,
        emailTV: TextView,
        passwordET: EditText,
        passwordTv: TextView

    ): Boolean {

        emailTV.text = ""
        passwordTv.text = ""

        val email = emailET.text.toString()
        val password = passwordET.text.toString()

        if (email.isEmpty()) {
            emailTV.visibility = View.VISIBLE
            emailTV.text = "*Please enter email address."


        }else if (email.startsWith(" ")) {
            emailTV.visibility = View.VISIBLE
            emailTV.text = "*Please enter valid email address."

        } else if (!isEmail(email)) {
            emailTV.visibility = View.VISIBLE
            emailTV.text = "*Please enter valid email address."

        } else if (password.isEmpty()) {
            emailTV.visibility = View.GONE
            passwordTv.visibility = View.VISIBLE
            passwordTv.isVisible = true
            passwordTv.text = "*Please enter password."

        }else if (password.startsWith(" ")) {
            emailTV.visibility = View.GONE
            passwordTv.visibility = View.VISIBLE
            passwordTv.isVisible = true
            passwordTv.text = "*Please enter valid password."

        } else if (password.length < 8) {
            emailTV.visibility = View.GONE
            passwordTv.visibility = View.VISIBLE
            passwordTv.text = "*Please enter valid password."

        } else {
            emailTV.text = ""
            passwordTv.text = ""
            emailTV.visibility = View.GONE
            passwordTv.visibility = View.GONE
            return true
        }

        return false
    }

    fun DeliveryValidations(
        EtDeliveryId: EditText,
        tvDeliveryId: TextView,
        passwordET: EditText,
        passwordTv: TextView

    ): Boolean {

        tvDeliveryId.text = ""
        passwordTv.text = ""

        val deliveryId = EtDeliveryId.text.toString()
        val password = passwordET.text.toString()

        if (deliveryId.isEmpty()) {
            tvDeliveryId.visibility = View.VISIBLE
            tvDeliveryId.text = "*Please enter delivery partner id."

        } else if (deliveryId.length < 8) {
            tvDeliveryId.visibility = View.VISIBLE
            tvDeliveryId.text = "*Please enter valid delivery partner id."

        }else if (deliveryId.startsWith(" ")) {
            tvDeliveryId.visibility = View.VISIBLE
            tvDeliveryId.text = "*Please enter valid delivery partner id."

        } else if (password.isEmpty()) {
            tvDeliveryId.visibility = View.GONE
            passwordTv.visibility = View.VISIBLE
            passwordTv.isVisible = true
            passwordTv.text = "*Please enter password."

        }else if (password.startsWith(" ")) {
            tvDeliveryId.visibility = View.GONE
            passwordTv.visibility = View.VISIBLE
            passwordTv.isVisible = true
            passwordTv.text = "*Please enter valid password."

        } else if (password.length < 8) {
            tvDeliveryId.visibility = View.GONE
            passwordTv.visibility = View.VISIBLE
            passwordTv.text = "*Please enter valid password."

        } else {
            tvDeliveryId.text = ""
            passwordTv.text = ""
            tvDeliveryId.visibility = View.GONE
            passwordTv.visibility = View.GONE
            return true
        }

        return false
    }

    fun PhoneValidations(
        phoneET: EditText,
        phoneTV: TextView,
        tvEmail: TextView,
        passwordET: EditText,
        passwordTv: TextView

    ): Boolean {

        phoneTV.text = ""
        passwordTv.text = ""

        val phone = phoneET.text.toString()
        val password = passwordET.text.toString()

        if (phone.isEmpty()) {
            phoneTV.visibility = View.VISIBLE
            tvEmail.visibility = View.GONE
            phoneTV.text = "*Please enter phone number."

        }else if (phone.startsWith(" ")) {

            phoneTV.visibility = View.VISIBLE
            tvEmail.visibility = View.GONE
            phoneTV.text = "*Please enter valid phone number."

        } else if (phone.length < 10 || phone.length > 10) {

            phoneTV.visibility = View.VISIBLE
            tvEmail.visibility = View.GONE
            phoneTV.text = "*Please enter valid phone number."

        }else if (phone.startsWith("0")) {

            phoneET.requestFocus()
            tvEmail.visibility = View.GONE
            phoneTV.visibility = View.VISIBLE
            phoneTV.text = "*Please enter valid phone number."

        } else if (password.isEmpty()) {

            passwordTv.isVisible = true
            phoneTV.visibility = View.GONE
            tvEmail.visibility = View.GONE
            passwordTv.visibility = View.VISIBLE
            passwordTv.text = "*Please enter password."

        }else if (password.startsWith(" ")) {

            passwordTv.isVisible = true
            phoneTV.visibility = View.GONE
            passwordTv.visibility = View.VISIBLE
            passwordTv.text = "*Please enter password."

        } else if (password.length < 8) {

            passwordTv.visibility = View.VISIBLE
            tvEmail.visibility = View.GONE
            phoneTV.visibility = View.GONE
            passwordTv.text = "*Please enter valid password."

        } else {
            phoneTV.text = ""
            passwordTv.text = ""
            tvEmail.text = ""
            tvEmail.visibility = View.GONE
            phoneTV.visibility = View.GONE
            passwordTv.visibility = View.GONE

            return true
        }

        return false
    }

    fun locationValidations(
        locationEt: EditText,
        locationTv: TextView,
        termCb: CheckBox,
        termTv: TextView,

    ): Boolean {

        locationTv.text = ""
        termTv.text = ""

        val location = locationEt.text.toString()

        if (location.isEmpty()) {
            locationEt.requestFocus()
            locationTv.visibility = View.VISIBLE
            locationTv.text = "*Please enter location."

        }else if (location.startsWith(" ")) {
            locationEt.requestFocus()
            locationTv.visibility = View.VISIBLE
            locationTv.text = "*Please enter valid location."

        }else if (!termCb.isChecked) {
            termCb.requestFocus()
            termTv.visibility = View.VISIBLE
            termTv.text = "*Please accept terms and conditions."

        }  else {
            locationTv.text = ""
            locationTv.visibility = View.GONE

            return true
        }

        return false
    }

    fun forgetPasswordPhone(
        phoneEt: EditText,
        phoneTV: TextView

    ): Boolean {

        phoneTV.text = ""

        val phone = phoneEt.text.toString()

        if (phone.isEmpty()) {
            phoneTV.visibility = View.VISIBLE
            phoneTV.text = "*Please enter phone number."

        } else if (phone.length < 10) {
            phoneTV.visibility = View.VISIBLE
            phoneTV.text = "*Please enter valid phone number."

        }else if (phone.startsWith(" ")) {
            phoneTV.visibility = View.VISIBLE
            phoneTV.text = "*Please enter valid phone number."

        }else if (phone.startsWith("0")) {
            phoneTV.visibility = View.VISIBLE
            phoneTV.text = "*Please enter valid phone number."

        }else {
            phoneTV.text = ""
            phoneTV.visibility = View.GONE

            return true
        }

        return false
    }

    fun forgetPasswordEmail(
        emailET: EditText,
        emailTV: TextView

    ): Boolean {

        emailTV.text = ""

        val email = emailET.text.toString()

        if (email.isEmpty()) {
            emailTV.visibility = View.VISIBLE
            emailTV.text = "*Please enter email address."

        } else if (!isEmail(email)) {
            emailTV.visibility = View.VISIBLE
            emailTV.text = "*Please enter valid email address."

        }else if (email.startsWith(" ")) {
            emailTV.visibility = View.VISIBLE
            emailTV.text = "*Please enter valid email address."

        }else {
            emailTV.text = ""
            emailTV.visibility = View.GONE

            return true
        }

        return false
    }

    fun otpValidation(
        etOtp1 : EditText,
        etOtp2: EditText,
        etOtp3: EditText,
        etOtp4: EditText,
        tvOtp : TextView
    ):Boolean{

        tvOtp.text = ""

        val otp1 = etOtp1.text.toString()
        val otp2 = etOtp2.text.toString()
        val otp3 = etOtp3.text.toString()
        val otp4 = etOtp4.text.toString()

        if (otp1.isEmpty()){
            etOtp1.requestFocus()
            tvOtp.visibility = View.VISIBLE
            tvOtp.text = "*Please enter valid OTP."

        }else if (otp2.isEmpty()){
            etOtp2.requestFocus()
            tvOtp.visibility = View.VISIBLE
            tvOtp.text = "*Please enter valid OTP."

        }else if (otp3.isEmpty()){
            etOtp3.requestFocus()
            tvOtp.visibility = View.VISIBLE
            tvOtp.text = "*Please enter valid OTP."

        }else if (otp4.isEmpty()){
            etOtp4.requestFocus()
            tvOtp.visibility = View.VISIBLE
            tvOtp.text = "*Please enter valid OTP."

        }
        else{
            tvOtp.text = ""
            tvOtp.visibility = View.GONE

            return true
        }


        return false
    }

    fun resetPassword(
        passwordET: EditText,
        passwordTv: TextView,
        confirmPasswordEt: EditText,
        confirmPasswordTv: TextView
    ) : Boolean{

        confirmPasswordTv.text = ""
        passwordTv.text = ""

        val password = passwordET.text.toString()
        val confirmPassword = confirmPasswordEt.text.toString()


        if (password.isEmpty()) {
            passwordTv.visibility = View.VISIBLE
            passwordTv.text = "*Please enter password."

        }else if (password.startsWith(" ")) {
            passwordTv.visibility = View.VISIBLE
            passwordTv.text = "*Please enter valid password."

        } else if (password.length < 8) {
            passwordTv.visibility = View.VISIBLE
            passwordTv.text = "*Please enter valid password."

        }else if (confirmPassword.isEmpty()) {
            passwordTv.visibility = View.GONE
            confirmPasswordTv.visibility = View.VISIBLE
            confirmPasswordTv.text = "*Please enter confirm password."

        } else if (!confirmPassword.equals(password)) {
            passwordTv.visibility = View.GONE
            confirmPasswordTv.visibility = View.VISIBLE
            confirmPasswordTv.text = "*Confirm password must be same as password."
        }
        else {
            passwordTv.text = ""
            confirmPasswordTv.text = ""
            passwordTv.visibility = View.GONE
            confirmPasswordTv.visibility = View.GONE

            return true
        }


        return false
    }


    fun SignUpCustomer(
        nameEt : EditText,
        nameTv :TextView,
        phoneEt : EditText,
        phoneTv :TextView,
        emailEt: EditText,
        emailTV: TextView,
        pincodeEt : EditText,
        pincodeTv : TextView,
        country : TextView,
        countryTv : TextView,
        state : TextView,
        stateTv : TextView,
        city : TextView,
        cityTv : TextView,
        addressEt : EditText,
        addressTv : TextView,
        passEt : EditText,
        passwordTv: TextView,
        confirmPasswordEt: EditText,
        confirmPasswordTv : TextView,
        termCb : CheckBox,
        termTv : TextView
    ): Boolean{

        nameTv.text = ""
        phoneTv.text = ""
        emailTV.text = ""
        pincodeTv.text = ""
        countryTv.text = ""
        stateTv.text = ""
        cityTv.text = ""
        addressTv.text = ""
        passwordTv.text = ""
        confirmPasswordTv.text = ""
        termTv.text = ""

        val name = nameEt.text.toString()
        val phone = phoneEt.text.toString()
        val email = emailEt.text.toString()
        val pincode = pincodeEt.text.toString()
        val address = addressEt.text.toString()
        val password = passEt.text.toString()
        val confirmPassword = confirmPasswordEt.text.toString()

        if (name.isEmpty()){
            nameEt.requestFocus()
            nameTv.visibility = View.VISIBLE
            nameTv.text = "*Please enter your name."

        }else if (name.length < 2){
            nameEt.requestFocus()
            nameTv.visibility = View.VISIBLE
            nameTv.text = "*Please enter valid name."

        }else if (name.startsWith(" ")){
            nameEt.requestFocus()
            nameTv.visibility = View.VISIBLE
            nameTv.text = "*Please enter valid name."

        }else if (phone.isEmpty()) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.VISIBLE
            phoneTv.text = "*Please enter phone number."

        }else if (phone.startsWith("0")) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.VISIBLE
            phoneTv.text = "*Please enter valid phone number."

        } else if (phone.length < 10) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.VISIBLE
            phoneTv.text = "*Please enter valid phone number."

        }else if (email.isEmpty()) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.VISIBLE
            emailTV.text = "*Please enter email address."

        }else if (email.startsWith(" ")) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.VISIBLE
            emailTV.text = "*Please enter valid email address."

        } else if (!isEmail(email)) {

            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.VISIBLE
            emailTV.text = "*Please enter valid email address."
        }
        else if (pincode.isEmpty()) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.VISIBLE
            pincodeTv.text = "*Please enter pin code."

        } else if (pincode.length < 6) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.VISIBLE
            pincodeTv.text = "*Please enter valid pin code."
        }
        else if(country.text == ""){

            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.GONE
            countryTv.visibility = View.VISIBLE
            countryTv.text = "*Please select country."

        }else if(state.text == ""){
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.GONE
            countryTv.visibility = View.GONE
            stateTv.visibility = View.VISIBLE
            stateTv.text = "*Please select state."

        }else if(city.text == ""){
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.GONE
            countryTv.visibility = View.GONE
            stateTv.visibility = View.GONE
            cityTv.visibility = View.VISIBLE
            cityTv.text = "*Please select city."

        }else if(address.isEmpty()){
            addressEt.requestFocus()
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.GONE
            countryTv.visibility = View.GONE
            stateTv.visibility = View.GONE
            cityTv.visibility = View.GONE
            addressTv.visibility = View.VISIBLE
            addressTv.text = "*Please enter address."

        }else if(address.startsWith(" ")){
            addressEt.requestFocus()
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.GONE
            countryTv.visibility = View.GONE
            stateTv.visibility = View.GONE
            cityTv.visibility = View.GONE
            addressTv.visibility = View.VISIBLE
            addressTv.text = "*Please enter valid address."

        }else if (password.isEmpty()) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.GONE
            countryTv.visibility = View.GONE
            stateTv.visibility = View.GONE
            cityTv.visibility = View.GONE
            addressTv.visibility = View.GONE
            passwordTv.visibility = View.VISIBLE
            passwordTv.text = "*Please enter password."

        }else if (password.startsWith(" ")) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.GONE
            countryTv.visibility = View.GONE
            stateTv.visibility = View.GONE
            cityTv.visibility = View.GONE
            addressTv.visibility = View.GONE
            passwordTv.visibility = View.VISIBLE
            passwordTv.text = "*Please enter valid password."

        } else if (password.length < 8) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.GONE
            countryTv.visibility = View.GONE
            stateTv.visibility = View.GONE
            cityTv.visibility = View.GONE
            addressTv.visibility = View.GONE
            passwordTv.visibility = View.VISIBLE
            passwordTv.text = "*Please enter valid password."

        }else if (confirmPassword.isEmpty()) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.GONE
            countryTv.visibility = View.GONE
            stateTv.visibility = View.GONE
            cityTv.visibility = View.GONE
            addressTv.visibility = View.GONE
            passwordTv.visibility = View.GONE
            confirmPasswordTv.visibility = View.VISIBLE
            confirmPasswordTv.text = "*Please enter confirm password."

        } else if (!confirmPassword.equals(password)) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.GONE
            countryTv.visibility = View.GONE
            stateTv.visibility = View.GONE
            cityTv.visibility = View.GONE
            addressTv.visibility = View.GONE
            passwordTv.visibility = View.GONE
            confirmPasswordTv.visibility = View.VISIBLE
            confirmPasswordTv.text = "*Confirm password must be as password."
        }
        else if (!termCb.isChecked) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.GONE
            countryTv.visibility = View.GONE
            stateTv.visibility = View.GONE
            cityTv.visibility = View.GONE
            addressTv.visibility = View.GONE
            passwordTv.visibility = View.GONE
            confirmPasswordTv.visibility = View.GONE
            termTv.visibility = View.VISIBLE
            termTv.text = "*Please accept terms and conditions."
        }
        else {
            nameTv.text = ""
            phoneTv.text = ""
            emailTV.text = ""
            pincodeTv.text = ""
            countryTv.text = ""
            stateTv.text = ""
            cityTv.text = ""
            addressTv.text = ""
            passwordTv.text = ""
            confirmPasswordTv.text = ""
            termTv.text = ""

            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.GONE
            countryTv.visibility = View.GONE
            stateTv.visibility = View.GONE
            cityTv.visibility = View.GONE
            addressTv.visibility = View.GONE
            passwordTv.visibility = View.GONE
            confirmPasswordTv.visibility = View.GONE
            termTv.visibility = View.GONE

            return true
        }

        return false
    }

    fun SignUpDeliveryPartner(

        nameEt : EditText,
        nameTv :TextView,
        phoneEt : EditText,
        phoneTv :TextView,
        emailEt: EditText,
        emailTV: TextView,
        pincodeEt : EditText,
        pincodeTv : TextView,
        country : TextView,
        countryTv : TextView,
        state : TextView,
        stateTv : TextView,
        city : TextView,
        cityTv : TextView,
        addressEt : EditText,
        addressTv : TextView,
        txtUploadDocument : TextView,
        tvUploadDocument : TextView,
        passEt : EditText,
        passwordTv: TextView,
        confirmPasswordEt: EditText,
        confirmPasswordTv : TextView,
        termCb : CheckBox,
        termTv : TextView

    ): Boolean{

        nameTv.text = ""
        phoneTv.text = ""
        emailTV.text = ""
        pincodeTv.text = ""
        countryTv.text = ""
        stateTv.text = ""
        cityTv.text = ""
        addressTv.text = ""
        passwordTv.text = ""
        confirmPasswordTv.text = ""
        termTv.text = ""

        val name = nameEt.text.toString()
        val phone = phoneEt.text.toString()
        val email = emailEt.text.toString()
        val pincode = pincodeEt.text.toString()
        val address = addressEt.text.toString()
        val password = passEt.text.toString()
        val confirmPassword = confirmPasswordEt.text.toString()

        if (name.isEmpty()){
            nameEt.requestFocus()
            nameTv.visibility = View.VISIBLE
            nameTv.text = "*Please enter your name."

        }else if (name.length < 2){
            nameEt.requestFocus()
            nameTv.visibility = View.VISIBLE
            nameTv.text = "*Please enter valid name."

        }else if (name.startsWith(" ")){
            nameEt.requestFocus()
            nameTv.visibility = View.VISIBLE
            nameTv.text = "*Please enter valid name."

        }else if (phone.isEmpty()) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.VISIBLE
            phoneTv.text = "*Please enter phone number."

        }else if (phone.startsWith("0")) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.VISIBLE
            phoneTv.text = "*Please enter valid phone number."

        } else if (phone.length < 10) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.VISIBLE
            phoneTv.text = "*Please enter valid phone number."

        }else if (email.isEmpty()) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.VISIBLE
            emailTV.text = "*Please enter email address."

        }else if (email.startsWith(" ")) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.VISIBLE
            emailTV.text = "*Please enter valid email address."

        } else if (!isEmail(email)) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.VISIBLE
            emailTV.text = "*Please enter valid email address."
        }
        else if (pincode.isEmpty()) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.VISIBLE
            pincodeTv.text = "*Please enter pin code."

        } else if (pincode.length < 6) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.VISIBLE
            pincodeTv.text = "*Please enter valid pin code."
        }
        else if(country.text == ""){
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.GONE
            countryTv.visibility = View.VISIBLE
            countryTv.text = "*Please select country."

        }else if(state.text == ""){
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.GONE
            countryTv.visibility = View.GONE
            stateTv.visibility = View.VISIBLE
            stateTv.text = "*Please select state."

        }else if(city.text == ""){
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.GONE
            countryTv.visibility = View.GONE
            stateTv.visibility = View.GONE
            cityTv.visibility = View.VISIBLE
            cityTv.text = "*Please select city."

        }else if(address.isEmpty()){
            addressEt.requestFocus()
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.GONE
            countryTv.visibility = View.GONE
            stateTv.visibility = View.GONE
            cityTv.visibility = View.GONE
            addressTv.visibility = View.VISIBLE
            addressTv.text = "*Please enter address."

        }else if(address.startsWith(" ")){
            addressEt.requestFocus()
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.GONE
            countryTv.visibility = View.GONE
            stateTv.visibility = View.GONE
            cityTv.visibility = View.GONE
            addressTv.visibility = View.VISIBLE
            addressTv.text = "*Please enter valid address."

        }else if(txtUploadDocument.text.equals("")){
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.GONE
            countryTv.visibility = View.GONE
            stateTv.visibility = View.GONE
            cityTv.visibility = View.GONE
            addressTv.visibility = View.GONE
            tvUploadDocument.visibility = View.VISIBLE
            tvUploadDocument.text = "*Please select your document."

        }else if (password.isEmpty()) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.GONE
            countryTv.visibility = View.GONE
            stateTv.visibility = View.GONE
            cityTv.visibility = View.GONE
            addressTv.visibility = View.GONE
            tvUploadDocument.visibility = View.GONE
            passwordTv.visibility = View.VISIBLE
            passwordTv.text = "*Please enter password."

        }else if (password.startsWith(" ")) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.GONE
            countryTv.visibility = View.GONE
            stateTv.visibility = View.GONE
            cityTv.visibility = View.GONE
            addressTv.visibility = View.GONE
            tvUploadDocument.visibility = View.GONE
            passwordTv.visibility = View.VISIBLE
            passwordTv.text = "*Please enter valid password."

        } else if (password.length < 8) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.GONE
            countryTv.visibility = View.GONE
            stateTv.visibility = View.GONE
            cityTv.visibility = View.GONE
            addressTv.visibility = View.GONE
            tvUploadDocument.visibility = View.GONE
            passwordTv.visibility = View.VISIBLE
            passwordTv.text = "*Please enter valid password."

        }else if (confirmPassword.isEmpty()) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.GONE
            countryTv.visibility = View.GONE
            stateTv.visibility = View.GONE
            cityTv.visibility = View.GONE
            addressTv.visibility = View.GONE
            passwordTv.visibility = View.GONE
            tvUploadDocument.visibility = View.GONE
            confirmPasswordTv.visibility = View.VISIBLE
            confirmPasswordTv.text = "*Please enter confirm password."

        } else if (!confirmPassword.equals(password)) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.GONE
            countryTv.visibility = View.GONE
            stateTv.visibility = View.GONE
            cityTv.visibility = View.GONE
            addressTv.visibility = View.GONE
            passwordTv.visibility = View.GONE
            tvUploadDocument.visibility = View.GONE
            confirmPasswordTv.visibility = View.VISIBLE
            confirmPasswordTv.text = "*Confirm password must be as password."
        }
        else if (!termCb.isChecked) {
            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.GONE
            countryTv.visibility = View.GONE
            stateTv.visibility = View.GONE
            cityTv.visibility = View.GONE
            addressTv.visibility = View.GONE
            passwordTv.visibility = View.GONE
            confirmPasswordTv.visibility = View.GONE
            tvUploadDocument.visibility = View.GONE
            termTv.visibility = View.VISIBLE
            termTv.text = "*Please accept terms and conditions."
        }
        else {
            nameTv.text = ""
            phoneTv.text = ""
            emailTV.text = ""
            pincodeTv.text = ""
            countryTv.text = ""
            stateTv.text = ""
            cityTv.text = ""
            addressTv.text = ""
            tvUploadDocument.text = ""
            passwordTv.text = ""
            confirmPasswordTv.text = ""
            termTv.text = ""

            nameTv.visibility = View.GONE
            phoneTv.visibility = View.GONE
            emailTV.visibility = View.GONE
            pincodeTv.visibility = View.GONE
            countryTv.visibility = View.GONE
            stateTv.visibility = View.GONE
            cityTv.visibility = View.GONE
            addressTv.visibility = View.GONE
            tvUploadDocument.visibility = View.GONE
            passwordTv.visibility = View.GONE
            confirmPasswordTv.visibility = View.GONE
            termTv.visibility = View.GONE

            return true
        }


        return false
    }

    fun isEmail(email: String): Boolean {
        val returnvalue: Boolean
        returnvalue = email.matches(Regex(emailPattern))
        return returnvalue
    }


}