package com.agvahealthcare.ventilator_ext.utility

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.view.*
import androidx.annotation.IdRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.agvahealthcare.ventilator_ext.maneuvers.ExpiratoryInspiratoryFragmentDialog
import com.agvahealthcare.ventilator_ext.model.DataStoreModelV2

fun avgDataStoreModels( dsList: List<DataStoreModelV2>): DataStoreModelV2 {

    // Solution 2
    return dsList.reduce { acc, model -> acc + model }.apply {
        val size = dsList.size
        this.pressure = pressure / size
        this.volume = volume / size
        this.rr = rr / size
        this.fio2 = fio2 / size
        this.mve = mve / size
        this.vte = vte / size
        this.leak = leak / size
        this.peep = peep / size
        this.ieRatio = ieRatio / size
    }
    // Solution 1
//       DataStoreModel().apply {
//           pressure =  dsList.map { it.pressure }.average()
//           volume =  dsList.map { it.volume }.average()
//           rr =  dsList.map { it.rr }.average()
//           fiO2 = dsList.map { it.fiO2 }.average()
//           mve = dsList.map { it.mve }.average()
//           vte = dsList.map { it.vte }.average()
//           leak = dsList.map { it.leak }.average()
//           peep = dsList.map { it.peep }.average()
//           ieRatio = dsList.map { it.ieRatio }.average()
//       }
}

fun DialogFragment.setWidthPercent(percentage: Int) {
    val percent = percentage.toFloat() / 100
    val dm = Resources.getSystem().displayMetrics
    val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
    val percentWidth = rect.width() * percent
    dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
}


fun DialogFragment.replaceFragment(fragment: Fragment, tag : String, @IdRes containerId :Int) {
    childFragmentManager.beginTransaction()
        .replace(containerId, fragment,tag ).commit()
}

fun ExpiratoryInspiratoryFragmentDialog.setHeightWidth(heightDialog: Int?, widthDialog: Int?){
    dialog?.window?.apply {
            setGravity(Gravity.START or Gravity.BOTTOM)
            decorView.apply {
                val params: WindowManager.LayoutParams = attributes

                params.x = 220
                params.y = 140
                params.dimAmount = 0.0F
                params.screenBrightness = 5.0F
                params.width = widthDialog!!
                params.height = heightDialog!!
                attributes = params
            }
    }

    hideSystemUI()


}


fun DialogFragment.setHeightWidthPercent(heightDialog: Int?, widthDialog: Int?, status: Boolean?) {


    dialog?.window?.apply {
        if (status == true) {
            setGravity(Gravity.CENTER_HORIZONTAL)
            decorView.apply {
                val params: WindowManager.LayoutParams = attributes

                params.x = -58
                params.y = -15
                params.dimAmount = 0.0F
                params.screenBrightness = 5.0F
                params.width = widthDialog!! + 30
                params.height = heightDialog!! + 30
                attributes = params
            }
        } else {
            setGravity(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM)
            decorView.apply {
                val params: WindowManager.LayoutParams = attributes

                params.dimAmount = 0.0F
                params.screenBrightness = 1.0F

                params.width = widthDialog!! + 20
                params.height = heightDialog!! + 20
                attributes = params
            }
        }

    }

    hideSystemUI()

}

fun dip2px(context: Context, dpValue: Float): Float {
    val scale = context.resources.displayMetrics.density
    return dpValue * scale + 0.2f
}


fun DialogFragment.hideSystemUI() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        dialog?.window?.setDecorFitsSystemWindows(false)
        val controller = dialog?.window?.insetsController
        if (controller != null) {
            if (controller.systemBarsBehavior == 0) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                controller.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_TOUCH
            }
        }

    } else {
        @Suppress("DEPRECATION")
        dialog?.window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)

    }

}

fun Fragment.hideSystemUI() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        activity?.window?.setDecorFitsSystemWindows(false)
        val controller = activity?.window?.insetsController
        if (controller != null) {
            if (controller.systemBarsBehavior == 0) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                controller.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_TOUCH
            }
        }

    } else {
        @Suppress("DEPRECATION")
        activity?.window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)

    }

}

/*   fun internetCheck(c: Context): Boolean {
           val cmg = c.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
               // Android 10+
               cmg.getNetworkCapabilities(cmg.activeNetwork)?.let { networkCapabilities ->
                   return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                           || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
               }
           } else {
               return cmg.activeNetworkInfo?.isConnectedOrConnecting == true
           }
           return false
       }*/


fun floatingPointFix(num: Float) : Float = Math.round(num * 10) / 10f



