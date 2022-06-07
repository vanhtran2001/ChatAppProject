package hcmute.spkt.nhom19.chatappproject.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import hcmute.spkt.nhom19.chatappproject.R;

public class SettingActivity extends AppCompatActivity {

    //Khởi tại các biến để ánh xạ view
    private Button btnUpdate;
    private EditText edtName, edtStatus;
    private CircleImageView imgUser;
    private ImageView imgBack;
    //Khởi tạo biến xác định người đang đăng nhập
    private String currentUid;
    //Khởi tạo biến database
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private StorageReference storage;
    private FirebaseDatabase database;
    //Khơi tạo biến xác định mở thư viện trong máy
    private static final int library = 1;
    private FirebaseStorage storageReference;
    //Khởi tạo biến thanh chờ
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance().getReference().child("Image Users");
        storageReference = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        Init();

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToMainActivity();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSettings();
            }
        });

        RetrieveUserInfo();

        imgUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, library);
            }
        });

    }

    //Hàm chuyển intent đến Main Activity
    private void SendUserToMainActivity() {
        Intent intent = new Intent(SettingActivity.this, MainActivity.class);
        startActivity(intent);
    }

    //Hàm khởi tạo ánh xạ các biến với các view
    private void Init() {
        btnUpdate = (Button) findViewById(R.id.buttonUpdate);
        edtName = (EditText) findViewById(R.id.edittextName);
        edtStatus = (EditText) findViewById(R.id.edittextStatus);
        imgUser = (CircleImageView) findViewById(R.id.imageviewProfile_image);
        imgBack = (ImageView) findViewById(R.id.imageviewBackSetting);
        dialog = new ProgressDialog(this);
    }

    //Hàm lưu ảnh đại diện vào database
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == library && resultCode == RESULT_OK && data != null) {
            dialog.setTitle("Đang cập nhật ảnh đại diện");
            dialog.setMessage("Vui lòng đợi trong giây lát...");
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();

            Uri imageUri = data.getData();
            imgUser.setImageURI(imageUri);

            StorageReference file = storage.child(currentUid + ".jpg");

            file.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(SettingActivity.this, "Cập nhật ảnh đại diện thành công!", Toast.LENGTH_SHORT).show();

                        file.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                database.getReference().child("Users").child(currentUid).child("image").setValue(uri.toString());
                                dialog.dismiss();
                            }
                        });

                    } else {
                        Toast.makeText(SettingActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
            });

        }

    }

    //Hàm cập nhật tên và trạng thái cho người dùng
    private void UpdateSettings() {
        String username = edtName.getText().toString();
        String status = edtStatus.getText().toString();

        if(TextUtils.isEmpty(username)){
            Toast.makeText(this, "Vui lòng nhập tên người dùng!", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(status)){
            Toast.makeText(this, "Vui lòng nhập trạng thái!", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> user = new HashMap<>();
            user.put("uid", currentUid);
            user.put("name", username);
            user.put("status", status);
            databaseReference.child("Users").child(currentUid).updateChildren(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        SendUserToMainActivity();
                        Toast.makeText(SettingActivity.this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SettingActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    //Hàm hiển thị thông tin người dùng
    private void RetrieveUserInfo() {
        databaseReference.child("Users").child(currentUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.hasChild("name") && snapshot.hasChild("image")){
                    String name = snapshot.child("name").getValue().toString();
                    String status = snapshot.child("status").getValue().toString();
                    String image = snapshot.child("image").getValue().toString();

                    edtName.setText(name);
                    edtStatus.setText(status);
                    Picasso.get().load(image).placeholder(R.drawable.user_image).into(imgUser);

                } else if (snapshot.exists() && snapshot.hasChild("name")){
                    String name = snapshot.child("name").getValue().toString();
                    String status = snapshot.child("status").getValue().toString();

                    edtName.setText(name);
                    edtStatus.setText(status);
                } else {
                    Toast.makeText(SettingActivity.this, "Vui lòng thiết lập thông tin của bạn!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}