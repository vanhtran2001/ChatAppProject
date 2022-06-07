package hcmute.spkt.nhom19.chatappproject.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import hcmute.spkt.nhom19.chatappproject.R;

public class UserInfoActivity extends AppCompatActivity {

    //Khởi tạo biến id người dùng, id người đang đăng nhập, trạng thái ban đầu
    private String uid, currentUid, state;
    //Khởi tạo các biến để ánh xạ view
    private CircleImageView imgUser;
    private TextView tvName, tvStatus;
    private Button btnSendFriendReq, btnDeclineFriendReq;
    private ImageView imgBack;

    //Khởi tạo biến database
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference, friendRequestReference, contactReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        friendRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contactReference = FirebaseDatabase.getInstance().getReference().child("Contacts");

        uid = getIntent().getExtras().get("uid").toString();

        imgUser = (CircleImageView) findViewById(R.id.imageviewProfile_imageVisit);
        tvName = (TextView) findViewById(R.id.textviewNameUserVisit);
        tvStatus = (TextView) findViewById(R.id.textviewStatusUserVisit);
        btnSendFriendReq = (Button) findViewById(R.id.buttonSendFriendRequest);
        imgBack = (ImageView) findViewById(R.id.imageviewBackUserVisit);
        btnDeclineFriendReq = (Button)findViewById(R.id.buttonDeclineFriendRequest);
        state = "new";

        RetrieveUserInfo();

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserInfoActivity.this, FindFriendActivity.class);
                startActivity(intent);
            }
        });

    }

    //Hàm hiển thị thông tin người dùng
    private void RetrieveUserInfo() {
        databaseReference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if((snapshot.exists()) && (snapshot.hasChild("image"))) {
                    String image = snapshot.child("image").getValue().toString();
                    String name = snapshot.child("name").getValue().toString();
                    String status = snapshot.child("status").getValue().toString();

                    Picasso.get().load(image).placeholder(R.drawable.user_image).into(imgUser);
                    tvName.setText(name);
                    tvStatus.setText(status);

                    ManageFriendRequest();

                } else {
                    String name = snapshot.child("name").getValue().toString();
                    String status = snapshot.child("status").getValue().toString();

                    tvName.setText(name);
                    tvStatus.setText(status);

                    ManageFriendRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Hàm quản lý yêu cầu gửi kết bạn
    private void ManageFriendRequest() {

        friendRequestReference.child(currentUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(uid)) {
                    String request = snapshot.child(uid).child("request").getValue().toString();

                    if(request.equals("sent")) {
                        state = "request_sent";
                        btnSendFriendReq.setText("Hủy yêu cầu");
                    } else if(request.equals("received")) {
                        state = "request_received";
                        btnSendFriendReq.setText("Đồng ý");
                        btnDeclineFriendReq.setVisibility(View.VISIBLE);
                        btnDeclineFriendReq.setEnabled(true);
                        btnDeclineFriendReq.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CancelFriendRequest();
                            }
                        });
                    }
                } else {
                    contactReference.child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChild(uid)) {
                                state = "friend";
                                btnSendFriendReq.setText("Hủy kết bạn");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(!currentUid.equals(uid)) {
            btnSendFriendReq.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnSendFriendReq.setEnabled(false);

                    if(state.equals("new")) {
                        SendFriendRequest();
                    }

                    if(state.equals("request_sent")) {
                        CancelFriendRequest();
                    }

                    if(state.equals("request_received")) {
                        AcceptFriendRequest();
                    }

                    if(state.equals("friend")) {
                        RemoveFriend();
                    }

                }
            });
        } else {
            btnSendFriendReq.setVisibility(View.INVISIBLE);
        }
    }

    //Hàm hủy bạn bè
    private void RemoveFriend() {
        contactReference.child(currentUid).child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    contactReference.child(uid).child(currentUid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                btnSendFriendReq.setEnabled(true);
                                state = "new";
                                btnSendFriendReq.setText("Gửi yêu cầu kết bạn");

                                btnDeclineFriendReq.setVisibility(View.INVISIBLE);
                                btnDeclineFriendReq.setEnabled(false);

                            }
                        }
                    });
                }
            }
        });
    }

    //Hàm đồng ý kết bạn
    private void AcceptFriendRequest() {
        contactReference.child(currentUid).child(uid).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    contactReference.child(uid).child(currentUid).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                friendRequestReference.child(currentUid).child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        friendRequestReference.child(uid).child(currentUid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                btnSendFriendReq.setEnabled(true);
                                                state = "friend";
                                                btnSendFriendReq.setText("Hủy kết bạn");
                                                btnDeclineFriendReq.setVisibility(View.INVISIBLE);
                                                btnDeclineFriendReq.setEnabled(false);
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    //Hàm từ chối kết bạn
    private void CancelFriendRequest() {
        friendRequestReference.child(currentUid).child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    friendRequestReference.child(uid).child(currentUid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                btnSendFriendReq.setEnabled(true);
                                state = "new";
                                btnSendFriendReq.setText("Gửi yêu cầu kết bạn");

                                btnDeclineFriendReq.setVisibility(View.INVISIBLE);
                                btnDeclineFriendReq.setEnabled(false);

                            }
                        }
                    });
                }
            }
        });
    }

    //Hàm gửi yêu cầu kết bạn
    private void SendFriendRequest() {
        friendRequestReference.child(currentUid).child(uid).child("request").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    friendRequestReference.child(uid).child(currentUid).child("request").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                btnSendFriendReq.setEnabled(true);
                                state = "request_sent";
                                btnSendFriendReq.setText("Hủy yêu cầu");
                            }
                        }
                    });
                }
            }
        });
    }
}