package hcmute.spkt.nhom19.chatappproject.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import hcmute.spkt.nhom19.chatappproject.Model.Users;
import hcmute.spkt.nhom19.chatappproject.R;

public class FindFriendActivity extends AppCompatActivity {

    //Khởi tạo các biến ánh xạ với view
    private Toolbar toolbar;
    private RecyclerView recyclerView;

    //Khởi tạo database
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        recyclerView = (RecyclerView) findViewById(R.id.recyclerviewFindFriend);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        toolbar = (Toolbar) findViewById(R.id.find_friend_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Tìm kiếm bạn bè");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    //Hiển thị danh sách người dùng
    @Override
    protected void onStart() {
        super.onStart();

            FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>().setQuery(databaseReference, Users.class).build();

            FirebaseRecyclerAdapter<Users, FindFriendViewHolder> adapter = new FirebaseRecyclerAdapter<Users, FindFriendViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Users model) {
                    holder.tvUserName.setText(model.getName());
                    holder.tvStatus.setText(model.getStatus());
                    Picasso.get().load(model.getImage()).placeholder(R.drawable.user_image).into(holder.imgUser);

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String uid = getRef(position).getKey();

                            Intent intent = new Intent(FindFriendActivity.this, UserInfoActivity.class);
                            intent.putExtra("uid", uid);
                            startActivity(intent);

                        }
                    });

                }

                @NonNull
                @Override
                public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout, parent, false);
                    FindFriendViewHolder viewHolder = new FindFriendViewHolder(view);
                    return viewHolder;
                }
            };

            recyclerView.setAdapter(adapter);

            adapter.startListening();

    }

    //Tạo View Holder
    public static class FindFriendViewHolder extends RecyclerView.ViewHolder {

        TextView tvUserName, tvStatus;
        CircleImageView imgUser;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUserName = itemView.findViewById(R.id.textviewNameUser);
            tvStatus = itemView.findViewById(R.id.textviewStatus);
            imgUser = itemView.findViewById(R.id.imageviewUser);

        }
    }
}