package ir.morteza_aghighi.chargingalert.tools;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import ir.morteza_aghighi.chargingalert.R;

import java.util.Objects;


/*این کلاس را برای این نوشته ام که بتوان پیام توست سفارشی به فونت و استایل مخصوص تولید کرد
* البته در اندروید ۱۱ به بالا این قابلیت برداشته شده و دیگر نمیتوان توست سفارشی داشت*/
public class ToastMaker {
    private final Context context;
    public ToastMaker(Context context) {
        this.context = context;
    }
    /*این متد یک پیام This part runs" نمایش میدهد
    * کاربرد این متد برای دیباگینگ سریع یک بخش از کد است به صورتی که میتوان متوجه شد که این بحش از کد ران میشود یا خیر */
    public void msg(){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(context, "This part runs", Toast.LENGTH_SHORT);
                toast.show();
                //Android 11+ do not support custom Toast Style and will crash if
                //you try to apply a Toast with custom style
                //Setting custom toast typeface for android 10 and below
                if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)){
                    ViewGroup group = (ViewGroup) toast.getView();
                    TextView messageTextView = (TextView) Objects.requireNonNull(group).getChildAt(0);
                    /*درصورت تمایل میتوان فونت را جایگزین کرد کافیست فونت مورد نظر را داخل پوشه‌ی
                    * font قرار دهید
                    * فقط اسم فونت نباید فاصه یا - داشته باشد و برای فاصله گذاری از ـ استفاده میشود
                    * این مورد برای نام تمام فایل هایی که داخل پوشه های res هستند صادق است*/
                    Typeface typeface = ResourcesCompat.getFont(context, R.font.iranian_sans);
                    messageTextView.setTypeface(typeface);
                    toast.show();
                }
            }
        });
    }
    /*با این متد میتوان یک پیام استرینگ را از کلاسی که انرا کال کرده دریافت و نمایش داد*/
    public void msg(final String msg){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
                toast.show();
                //Android 11+ do not support custom Toast Style and will crash if
                //you try to apply a Toast with custom style
                //Setting custom toast typeface for android 10 and below
                if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)){
                    ViewGroup group = (ViewGroup) toast.getView();
                    TextView messageTextView = (TextView) Objects.requireNonNull(group).getChildAt(0);
                    Typeface typeface = ResourcesCompat.getFont(context, R.font.iranian_sans);
                    messageTextView.setTypeface(typeface);
                    toast.show();
                }
            }
        });
    }

    /*با این متد میتوان یک مقدار اعشاری را به صورت توست نمایش داد*/
    public void msg(final float message){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(context, String.valueOf(message), Toast.LENGTH_SHORT);
                toast.show();
                //Android 11+ do not support custom Toast Style and will crash if
                //you try to apply a Toast with custom style
                //Setting custom toast typeface for android 10 and below
                if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)){
                    ViewGroup group = (ViewGroup) toast.getView();
                    TextView messageTextView = (TextView) Objects.requireNonNull(group).getChildAt(0);
                    Typeface typeface = ResourcesCompat.getFont(context, R.font.iranian_sans);
                    messageTextView.setTypeface(typeface);
                    toast.show();
                }
            }
        });
    }
}
