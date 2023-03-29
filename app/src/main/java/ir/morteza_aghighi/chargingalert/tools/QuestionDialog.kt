package ir.morteza_aghighi.chargingalert.tools

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ir.morteza_aghighi.chargingalert.R
import java.util.*

class QuestionDialog(private val tittle: String, private val message: String) :
    BottomSheetDialogFragment() {
    private var questionListener: QuestionListener? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.question_dialog, container, false)
        val tvTittle = view.findViewById<TextView>(R.id.tvQuestionAlertTitle)
        val tvMessage = view.findViewById<TextView>(R.id.tvQuestionAlertMessage)
        tvTittle.text = tittle
        tvMessage.text = message
        val btnConfirm = view.findViewById<Button>(R.id.btnConfirm)
        val btnReject = view.findViewById<Button>(R.id.btnReject)
        btnConfirm.setOnClickListener {
            questionListener!!.onButtonClicked(true)
            dismiss()
        }
        btnReject.setOnClickListener {
            questionListener!!.onButtonClicked(false)
            dismiss()
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
//            tittle = getString(R.string.warning);
//            message = getString(R.string.explenation);
            questionListener = context as QuestionListener
        } catch (ignored: Exception) {
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val v: View
        try {
            v = requireView().parent as View
            if (v.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                val layoutParams = v.layoutParams as CoordinatorLayout.LayoutParams
                layoutParams.setMargins(60, -200, 60, 0)
            } else {
                val layoutParams = v.layoutParams as CoordinatorLayout.LayoutParams
                layoutParams.setMargins(60, -400, 60, 0)
            }
        } catch (ignored: Exception) {
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val d = super.onCreateDialog(savedInstanceState)
        // view hierarchy is inflated after dialog is shown
        d.setOnShowListener {
            //this disables outside touch
            try {
                Objects.requireNonNull(d.window)?.findViewById<View>(R.id.touch_outside)
                    ?.setOnClickListener(null)
                //this prevents dragging behavior
                val content = d.window!!.findViewById<View>(R.id.design_bottom_sheet)
                (content.layoutParams as CoordinatorLayout.LayoutParams).behavior = null
            } catch (ignored: Exception) {
            }
        }
        return d
    }

    interface QuestionListener {
        fun onButtonClicked(result: Boolean)
    }
}