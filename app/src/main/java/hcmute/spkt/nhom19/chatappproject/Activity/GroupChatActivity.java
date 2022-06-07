package hcmute.spkt.nhom19.chatappproject.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import hcmute.spkt.nhom19.chatappproject.R;

public class GroupChatActivity extends AppCompatActivity {

    //Khởi tạo các biến ánh xạ với view
    private Toolbar toolbar;
    private ImageButton imgSend;
    private EditText edtGroupChat;
    private ScrollView scrollView;
    private TextView tvMessages;

    //Khởi tạo database
    private FirebaseAuth mAuth;
    private DatabaseReference userReference, groupReference, groupMsgReference;

    //Khởi tạo các biến để đánh dấu nhóm, người đang dùng, ngày và thời gian nhắn
    private String currentGroupName, currentUid, currentUsername, currentDate, currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getCurrentUser().getUid();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        groupReference = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        Init();

        GetUserInfo();

        imgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessageToDB();

                edtGroupChat.setText("");

                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        groupReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()) {
                    DisplayMessages(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()) {
                    DisplayMessages(snapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //Hàm khởi tạo ánh xạ các biến với view
    private void Init() {
        toolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(currentGroupName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        imgSend = (ImageButton) findViewById(R.id.imageviewSendGroupChat);
        edtGroupChat = (EditText) findViewById(R.id.edittextGroupChat);
        tvMessages = (TextView) findViewById(R.id.textviewGroupChat);
        scrollView = (ScrollView) findViewById(R.id.scrollviewGroupChat);
    }

    //Hàm lấy thông tin người đang đăng nhập
    private void GetUserInfo() {
        userReference.child(currentUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    currentUsername = snapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Hàm lưu tin nhắn vào database
    private void SendMessageToDB() {
        String msg = edtGroupChat.getText().toString();
        String msgKey = groupReference.push().getKey();

        if(TextUtils.isEmpty(msg)) {
            Toast.makeText(this, "Bạn chưa nhập tin nhắn!", Toast.LENGTH_SHORT).show();
        } else {
            Calendar date = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            currentDate = currentDateFormat.format(date.getTime());

            Calendar time = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm aa");
            currentTime = currentTimeFormat.format(time.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            groupReference.updateChildren(groupMessageKey);

            groupMsgReference = groupReference.child(msgKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", currentUsername);
            messageInfoMap.put("message", msg);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);
            groupMsgReference.updateChildren(messageInfoMap);
        }
    }

    //Hàm hiển thị tin nhắn trong chat nhóm
    private void DisplayMessages(DataSnapshot snapshot) {
        Iterator iterator = snapshot.getChildren().iterator();

        while (iterator.hasNext()){
            String chatDate = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatMsg = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();

            tvMessages.append(chatName + " :\n" + chatMsg + "\n" + chatTime + " " + chatDate + "\n\n\n");

            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

}