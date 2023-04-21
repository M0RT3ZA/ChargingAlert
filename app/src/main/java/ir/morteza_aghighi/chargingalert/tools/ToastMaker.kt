package ir.morteza_aghighi.chargingalert.tools

import android.content.Context
import android.os.Looper
import android.widget.Toast

class ToastMaker(private val context: Context, val message:Any = "This part runs", val longShow: Boolean = false) {
    fun sh(){
        android.os.Handler(Looper.getMainLooper()).post {
            if (longShow) Toast.makeText(context, message.toString(),Toast.LENGTH_LONG).show()
            else Toast.makeText(context, message.toString(),Toast.LENGTH_SHORT).show()
        }
    }
}