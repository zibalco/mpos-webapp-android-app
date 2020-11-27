package ir.zibal.webapp.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import ir.zibal.zibalsdk.ZibalActivity;
import ir.zibal.zibalsdk.ZibalResponseEnum;

public class MainActivity extends AppCompatActivity {

    final static int PAYMENT_REQUEST_CODE = 2000;
    EditText et_zibalId;
    Button btn_payment;
    Button btn_backUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handleIntent(getIntent());

        btn_payment = findViewById(R.id.payBtn);
        btn_backUrl = findViewById(R.id.backUrlButton);
        et_zibalId = findViewById(R.id.zibalIdEditText);
        btn_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callPaymentIntent();
            }
        });

    }

    protected void callPaymentIntent(){
        String zibalId = et_zibalId.getText().toString();
        if(zibalId.length() == 0){
            Toast.makeText(MainActivity.this,"لطفا شناسه زیبال را وارد کنید.",Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent intent = new Intent(MainActivity.this, ZibalActivity.class);
            intent.putExtra("zibalId",zibalId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent,PAYMENT_REQUEST_CODE);
        } catch (Exception e) {
            Log.d("error happened", e.toString());
        }
    }

    private void handleIntent(Intent intent) {

        et_zibalId = findViewById(R.id.zibalIdEditText);
        btn_backUrl = findViewById(R.id.backUrlButton);

        String zibalId;
        Uri data = intent.getData();

        if(intent.getData() == null)
            return;

        try {
            if( data.getQueryParameter("zibalId")!= null) {
                zibalId = data.getQueryParameter("zibalId");
                et_zibalId.setText(zibalId);

                //create back button if redirect url is in params
                if(intent.getExtras().getString("backUrl") != null){
                    String backUrl = intent.getExtras().getString("backUrl");
                    btn_backUrl.setVisibility(View.VISIBLE);

                    if (!backUrl.startsWith("http://") && !backUrl.startsWith("https://"))
                        backUrl = "https://" + backUrl;

                    String finalBackUrl = backUrl;
                    btn_backUrl.setOnClickListener(view -> {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalBackUrl));
                        startActivity(browserIntent);
                    });
                }
                //call payment
                callPaymentIntent();
            }else{
                finish();
            }
        } catch (Exception ex) {
            Log.e("exception",ex.toString());
            finish();
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYMENT_REQUEST_CODE) {
            switch (resultCode) {
                case ZibalResponseEnum.RESULT_DEVICE_CONNECTION_FAILED:
                    Toast.makeText(MainActivity.this,"اتصال با دستگاه برقرار نشد.",Toast.LENGTH_SHORT).show();
                    break;
                case ZibalResponseEnum.RESULT_USER_CANCELED:
                    Toast.makeText(MainActivity.this,"کاربر از پرداخت منصرف شده است",Toast.LENGTH_SHORT).show();
                    break;
                case ZibalResponseEnum.RESULT_PAYMENT_SUCCESSFUL:
                    Toast.makeText(MainActivity.this,"پرداخت با موفقیت انجام شد.",Toast.LENGTH_SHORT).show();
                    break;
                case ZibalResponseEnum.RESULT_ERROR_IN_PAYMENT:
                    Toast.makeText(MainActivity.this,"خطای عملیات پرداخت",Toast.LENGTH_SHORT).show();
                    break;
                case ZibalResponseEnum.RESULT_ZIBAL_ID_ALREADY_PAID:
                    Toast.makeText(MainActivity.this,"شناسه قبلا پرداخت شده.",Toast.LENGTH_SHORT).show();
                    break;
                case ZibalResponseEnum.RESULT_INVALID_ZIBAL_ID:
                    Toast.makeText(MainActivity.this,"شناسه زیبال نامعتبر است.",Toast.LENGTH_SHORT).show();
                    break;
                case ZibalResponseEnum.RESULT_UNREACHABLE_ZIBAL_SERVER:
                    Toast.makeText(MainActivity.this,"عدم دسترسی به سرور زیبال",Toast.LENGTH_SHORT).show();
                    break;

            }
        }

    }
}