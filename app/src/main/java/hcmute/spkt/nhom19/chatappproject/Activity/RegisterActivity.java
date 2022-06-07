package hcmute.spkt.nhom19.chatappproject.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import hcmute.spkt.nhom19.chatappproject.R;

public class RegisterActivity extends AppCompatActivity {

    //Khởi tạo các biến để ánh xạ view
    private Button btnRegister;
    private EditText edtName, edtEmail, edtPassword;
    private TextView tvLogin;

    //Khởi tạo biến database
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    //Khởi tạo biến thanh chờ
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        Init();

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToLoginActivity();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }
        });
    }

    //Hàm chuyển intent đến Login Activity
    private void SendUserToLoginActivity() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    //Hàm đăng kí
    private void CreateNewAccount() {
        String email = edtEmail.getText().toString();
        String pass = edtPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Vui lòng nhập email!", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(pass)){
            Toast.makeText(this, "Vui lòng nhập mật khẩu!", Toast.LENGTH_SHORT).show();
        }
        else {
            progressDialog.setTitle("Đang tạo tài khoản");
            progressDialog.setMessage("Vui lòng đợi trong giây lát...");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();
            mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        String currentUid = mAuth.getCurrentUser().getUid();
                        databaseReference.child("Users").child(currentUid).setValue("");

                        SendUserToLoginActivity();
                        Toast.makeText(RegisterActivity.this, "Tạo tài khoản thành công!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    } else {
                        Toast.makeText(RegisterActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }

    //Hàm ánh xạ các biến với các view
    private void Init() {
        btnRegister = (Button) findViewById(R.id.buttonRegister);
        edtEmail = (EditText) findViewById(R.id.edittextEmailRegister);
        edtPassword = (EditText) findViewById(R.id.edittextPasswordRegister);
        tvLogin = (TextView) findViewById(R.id.textviewLoginRegister);

        progressDialog = new ProgressDialog(this);
    }
}