package hcmute.spkt.nhom19.chatappproject.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import hcmute.spkt.nhom19.chatappproject.Adapter.TabAccessorAdapter;
import hcmute.spkt.nhom19.chatappproject.R;

public class MainActivity extends AppCompatActivity {

    //Khởi tạo các biến để ánh xạ
    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    //Khởi tạo adapter
    private TabAccessorAdapter tabAccessorAdapter;

    //Khởi tạo biến database
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;


    //Khởi tạo biến xác định id người đang đăng nhập
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        toolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chat App");

        viewPager = (ViewPager) findViewById(R.id.main_tab_pager);
        tabAccessorAdapter = new TabAccessorAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabAccessorAdapter);

        tabLayout = (TabLayout) findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null) {
            SendUserToLoginActivity();
        } else {

            updateUserStatus("online");

            VerifyUserExistance();
        }
    }

    //Hàm check thông tin người dùng
    private void VerifyUserExistance() {
        String curruntUid = mAuth.getCurrentUser().getUid();

        databaseReference.child("Users").child(curruntUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("name").exists()){

                } else {
                    SendUserToSettingActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Hàm chuyển intent đến Login Acivity
    private void SendUserToLoginActivity() {
        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
    }

    //Hàm khởi tạo menu tùy chọn
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu, menu);

        return true;
    }

    //Hàm chọn các tùy chọn trong menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_logout_options){
            updateUserStatus("offline");

            mAuth.signOut();
            SendUserToLoginActivity();
        }
        if(item.getItemId() == R.id.main_setting_options){
            SendUserToSettingActivity();
        }
        if(item.getItemId() == R.id.main_create_group_options){
            RequestNewGroup();
        }
        if(item.getItemId() == R.id.main_find_friends_options){
            SendUserToFindFriendActivity();
        }
        return true;
    }

    //Hàm tạo tên nhóm
    private void RequestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Nhập tên nhóm:");

        final EditText edtGroupName = new EditText(MainActivity.this);
        edtGroupName.setHint("Tên nhóm");
        builder.setView(edtGroupName);

        builder.setPositiveButton("Tạo", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName = edtGroupName.getText().toString();
                if(TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this, "Vui lòng nhập tên nhóm!", Toast.LENGTH_SHORT).show();
                } else {
                    CreateNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    //Hàm tạo nhóm chat
    private void CreateNewGroup(String groupName) {
        databaseReference.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, groupName + " đã được tạo!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Hàm chuyển intent đến Setting Activity
    private void SendUserToSettingActivity() {
        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
        startActivity(intent);
    }

    //Hàm chuyển intent đến Find Friend Activity
    private void SendUserToFindFriendActivity() {
        Intent intent = new Intent(MainActivity.this, FindFriendActivity.class);
        startActivity(intent);
    }

    //Hàm check cập nhật hoạt động
    private void updateUserStatus(String state) {
        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("state", state);

        currentUid = mAuth.getCurrentUser().getUid();

        databaseReference.child("Users").child(currentUid).child("userState").updateChildren(onlineStateMap);

    }
}