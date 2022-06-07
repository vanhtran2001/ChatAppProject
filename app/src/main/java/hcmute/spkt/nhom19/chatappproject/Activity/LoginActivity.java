package hcmute.spkt.nhom19.chatappproject.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import hcmute.spkt.nhom19.chatappproject.R;

public class LoginActivity extends AppCompatActivity {

    //Khởi tạo biến thanh chờ
    private ProgressDialog progressDialog;

    //Khởi tạo các biến ánh xạ với view
    private Button btnLogin;
    private EditText edtEmail, edtPassword;
    private TextView tvRegister;
    private CheckBox cbRemember;

    //Khởi tạo biến để lưu thông tin đăng nhập
    SharedPreferences sharedPreferences;

    //Khởi tạo biến database
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        Init();

        sharedPreferences = getSharedPreferences("dataLogin",MODE_PRIVATE);

        edtEmail.setText(sharedPreferences.getString("email",""));
        edtPassword.setText(sharedPreferences.getString("password",""));
        cbRemember.setChecked(sharedPreferences.getBoolean("checked",false));

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowUserToLogin();
            }
        });
    }

    //Hàm check đăng nhập
    private void AllowUserToLogin() {
        String email = edtEmail.getText().toString();
        String pass = edtPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Vui lòng nhập email!", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(pass)){
            Toast.makeText(this, "Vui lòng nhập mật khẩu!", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog.setTitle("Đang đăng nhập");
            progressDialog.setMessage("Vui lòng đợi trong giây lát...");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();

            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        SendUserToMainActivity();
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        if(cbRemember.isChecked()){
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("email",edtEmail.getText().toString());
                            editor.putString("password",edtPassword.getText().toString());
                            editor.putBoolean("checked",true);
                            editor.commit();
                        } else {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.remove("email");
                            editor.remove("password");
                            editor.remove("checked");
                            editor.commit();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }

    //Hàm khởi tạo ánh xạ các biến với view
    private void Init() {
        btnLogin = (Button) findViewById(R.id.buttonLogin);
        edtEmail = (EditText) findViewById(R.id.edittextEmailLogin);
        edtPassword = (EditText) findViewById(R.id.edittextPasswordLogin);
        tvRegister = (TextView) findViewById(R.id.textviewRegisterLogin);
        cbRemember = (CheckBox) findViewById(R.id.checkboxRemember);
        progressDialog = new ProgressDialog(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentUser != null){
            SendUserToMainActivity();
        }
    }

    //Hàm chuyển intent đến Main Activity
    private void SendUserToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }
}