package hcmute.spkt.nhom19.chatappproject.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import hcmute.spkt.nhom19.chatappproject.Adapter.MessageAdapter;
import hcmute.spkt.nhom19.chatappproject.Model.Messages;
import hcmute.spkt.nhom19.chatappproject.R;

public class ChatActivity extends AppCompatActivity {

    //Khởi tạo biến nhận intent id, tên, ảnh người nhận tin nhắn
    private String uid, name, image;

    //Khởi tạo các biến ánh xạ với view
    private TextView tvName, tvLastSeen;
    private CircleImageView imgUser;
    private Toolbar toolbar;
    private ImageButton btnSend, btnSendFile;
    private EditText edtMessage;
    private RecyclerView recyclerView;

    //Khởi tạo biến database
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private StorageTask uploadTask;

    //Khởi tạo biến người đang đăng nhập
    private String currentUid;

    //Khởi tạo danh sách tin nhắn và adapter
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    //Khởi tạo biến lưu thời gian
    private String currentTime, currentDate;

    //Khởi tạo biến check file và link ảnh
    private String checker = "", url = "";

    //Khởi tạo biến lưu ảnh
    private Uri fileUri;

    //Khởi tạo biến thanh chờ
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        uid = getIntent().getExtras().get("uid").toString();
        name = getIntent().getExtras().get("name").toString();
        image = getIntent().getExtras().get("image").toString();

        Init();

        tvName.setText(name);
        Picasso.get().load(image).placeholder(R.drawable.user_image).into(imgUser);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });

        displayUserStatus();

        imgUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, UserInfoActivity.class);
                intent.putExtra("uid", uid);
                startActivity(intent);
            }
        });

        btnSendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checker = "image";

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 438);
            }
        });
    }

    //Hàm khởi tạo ánh xạ các biến với các view
    private void Init() {

        toolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.chat_bar, null);
        actionBar.setCustomView(actionBarView);

        imgUser = (CircleImageView) findViewById(R.id.imageviewUserChat);
        tvName = (TextView) findViewById(R.id.textviewNameChat);
        tvLastSeen = (TextView) findViewById(R.id.textviewLastSeenChat);

        btnSend = (ImageButton) findViewById(R.id.buttonSendMessage);
        btnSendFile = (ImageButton) findViewById(R.id.buttonSendFile);
        edtMessage = (EditText) findViewById(R.id.edittextChat);

        messageAdapter = new MessageAdapter(messagesList);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerviewMessage);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(messageAdapter);

        dialog = new ProgressDialog(this);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        currentDate = dateFormat.format(calendar.getTime());

        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa");
        currentTime = timeFormat.format(calendar.getTime());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            dialog.setTitle("Đang gửi");
            dialog.setMessage("Vui lòng đợi trong giây lát...");
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();

            fileUri = data.getData();

            if(!checker.equals("image")) {

            } else if(checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                String msgSenderReference = "Messages/" + currentUid + "/" + uid;
                String msgReceiverReference = "Messages/" + uid + "/" + currentUid;

                //Khởi tạo biến key cho Message trên database
                DatabaseReference userMsgKeyReference = databaseReference.child("Messages").child(currentUid).child(uid).push();

                final String msgPushId = userMsgKeyReference.getKey();

                StorageReference file = storageReference.child(msgPushId + "." + "jpg");

                uploadTask = file.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return file.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()) {
                            Uri downloadUrl = task.getResult();
                            url = downloadUrl.toString();

                            Map msgText = new HashMap();
                            msgText.put("message", url);
                            msgText.put("name", fileUri.getLastPathSegment());
                            msgText.put("type", checker);
                            msgText.put("from", currentUid);
                            msgText.put("to", uid);
                            msgText.put("messageId", msgPushId);
                            msgText.put("time", currentTime);
                            msgText.put("date", currentDate);

                            Map msgDetail = new HashMap();
                            msgDetail.put(msgSenderReference + "/" + msgPushId, msgText);
                            msgDetail.put(msgReceiverReference + "/" + msgPushId, msgText);

                            databaseReference.updateChildren(msgDetail).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful()) {
                                        dialog.dismiss();

                                        edtMessage.setText("");
                                    }
                                }
                            });
                        }
                    }
                });
            } else {
                dialog.dismiss();
                Toast.makeText(this, "Bạn chưa chọn ảnh!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Hàm check trạng thái hoạt động
    private void displayUserStatus() {
        databaseReference.child("Users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("userState").hasChild("state")) {
                    String state = snapshot.child("userState").child("state").getValue().toString();

                    if(state.equals("online")) {
                        tvLastSeen.setText("Online");
                    }
                    if(state.equals("offline")) {
                        tvLastSeen.setText("Offline");
                    }

                } else {
                    tvLastSeen.setText("Offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        databaseReference.child("Messages").child(currentUid).child(uid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages messages = snapshot.getValue(Messages.class);

                messagesList.add(messages);

                messageAdapter.notifyDataSetChanged();

                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

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

    //Hàm lưu tin nhắn vào databasse
    private void SendMessage() {
        String msg = edtMessage.getText().toString();

        if(TextUtils.isEmpty(msg)) {
            Toast.makeText(this, "Bạn chưa nhập tin nhắn", Toast.LENGTH_SHORT).show();
        } else {
            String msgSenderReference = "Messages/" + currentUid + "/" + uid;
            String msgReceiverReference = "Messages/" + uid + "/" + currentUid;

            //Khởi tạo biến key cho Message trên database
            DatabaseReference userMsgKeyReference = databaseReference.child("Messages").child(currentUid).child(uid).push();

            String msgPushId = userMsgKeyReference.getKey();

            //Đẩy tin nhắn lên
            Map msgText = new HashMap();
            msgText.put("message", msg);
            msgText.put("type", "text");
            msgText.put("from", currentUid);
            msgText.put("to", uid);
            msgText.put("messageId", msgPushId);
            msgText.put("time", currentTime);
            msgText.put("date", currentDate);

            Map msgDetail = new HashMap();
            msgDetail.put(msgSenderReference + "/" + msgPushId, msgText);
            msgDetail.put(msgReceiverReference + "/" + msgPushId, msgText);

            databaseReference.updateChildren(msgDetail).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()) {
                        edtMessage.setText("");
                    }
                }
            });
        }
    }
}