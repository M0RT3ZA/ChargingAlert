package ir.morteza_aghighi.chargingalert.tools;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Objects;

import ir.morteza_aghighi.chargingalert.R;


public class QuestionDialog extends BottomSheetDialogFragment {
    private final String tittle;
    private final String message;
    public QuestionDialog(String title, String message) {
        this.tittle = title;
        this.message = message;
    }

    private QuestionListener questionListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.question_dialog, container, false);
        TextView tvTittle = view.findViewById(R.id.tvQuestionAlertTitle);
        TextView tvMessage = view.findViewById(R.id.tvQuestionAlertMessage);
        tvTittle.setText(tittle);
        tvMessage.setText(message);
        Button btnConfirm = view.findViewById(R.id.btnConfirm);
        Button btnReject = view.findViewById(R.id.btnReject);


        btnConfirm.setOnClickListener(v -> {
            questionListener.onButtonClicked(true);
            dismiss();
        });

        btnReject.setOnClickListener(v -> {
            questionListener.onButtonClicked(false);
            dismiss();
        });

        return view;
    }

    public interface QuestionListener {
        void onButtonClicked(boolean result);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
//            tittle = getString(R.string.warning);
//            message = getString(R.string.explenation);
            questionListener = (QuestionListener) context;
        }catch (Exception ignored){}
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v;
        try {
            v = (View) requireView().getParent();
            if(v.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                CoordinatorLayout.LayoutParams layoutParams =
                        (CoordinatorLayout.LayoutParams) v.getLayoutParams();
                layoutParams.setMargins(60,-200,60,0);
            }else {
                CoordinatorLayout.LayoutParams layoutParams =
                        (CoordinatorLayout.LayoutParams) v.getLayoutParams();
                layoutParams.setMargins(60,-400,60,0);
            }

        }catch (Exception ignored){}
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog d = super.onCreateDialog(savedInstanceState);
        // view hierarchy is inflated after dialog is shown
        d.setOnShowListener(dialogInterface -> {
            //this disables outside touch
            try {
                Objects.requireNonNull(d.getWindow()).findViewById(R.id.touch_outside).setOnClickListener(null);
                //this prevents dragging behavior
                View content = d.getWindow().findViewById(R.id.design_bottom_sheet);
                ((CoordinatorLayout.LayoutParams) content.getLayoutParams()).setBehavior(null);
            }catch (Exception ignored){}
        });
        return d;
    }

}
