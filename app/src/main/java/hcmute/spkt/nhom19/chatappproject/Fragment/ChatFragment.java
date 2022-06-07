package hcmute.spkt.nhom19.chatappproject.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import hcmute.spkt.nhom19.chatappproject.Activity.ChatActivity;
import hcmute.spkt.nhom19.chatappproject.Model.Users;
import hcmute.spkt.nhom19.chatappproject.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {

    //Khơi tạo biến view cho Fragment
    private View ChatView;
    private RecyclerView recyclerView;
    //Khởi tạo database
    private DatabaseReference chatReference, userReference, msgReference;
    //Khởi tạo biến xác định người đang đăng nhập
    private FirebaseAuth mAuth;
    private String currentUid;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ChatView =  inflater.inflate(R.layout.fragment_chat, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getCurrentUser().getUid();
        chatReference = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUid);
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        msgReference = FirebaseDatabase.getInstance().getReference().child("Messages");

        recyclerView = (RecyclerView) ChatView.findViewById(R.id.recycerviewChat);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return ChatView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>().setQuery(chatReference, Users.class).build();

        FirebaseRecyclerAdapter<Users, ChatViewHolder> adapter = new FirebaseRecyclerAdapter<Users, ChatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatViewHolder holder, int position, @NonNull Users model) {
                final String uid = getRef(position).getKey();
                final String[] image = {"default"};

                userReference.child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if(snapshot.hasChild("image")) {
                                image[0] = snapshot.child("image").getValue().toString();

                                Picasso.get().load(image[0]).placeholder(R.drawable.user_image).into(holder.imgUser);
                            }

                            final String name = snapshot.child("name").getValue().toString();
                            final String status = snapshot.child("status").getValue().toString();

                            holder.tvName.setText(name);

                            msgReference.child(currentUid).child(uid).orderByChild("date").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.hasChildren()) {
                                        for(DataSnapshot snapshot1 : snapshot.getChildren()) {
                                            if(currentUid.equals(snapshot1.child("from").getValue().toString())) {
                                                if(snapshot1.child("type").getValue().toString().equals("text")) {
                                                    holder.tvStatus.setText("Bạn: " + snapshot1.child("message").getValue().toString());
                                                } else if(snapshot1.child("type").getValue().toString().equals("image")) {
                                                    holder.tvStatus.setText("Bạn đã gửi một hình ảnh");
                                                }
                                            } else {
                                                if(snapshot1.child("type").getValue().toString().equals("text")) {
                                                    holder.tvStatus.setText(snapshot1.child("message").getValue().toString());
                                                } else if(snapshot1.child("type").getValue().toString().equals("image")){
                                                    holder.tvStatus.setText("Hình ảnh");
                                                }
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(getContext(), ChatActivity.class);
                                    intent.putExtra("uid", uid);
                                    intent.putExtra("name", name);
                                    intent.putExtra("image", image[0]);
                                    startActivity(intent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout, parent, false);
                return new ChatViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    //Hàm khởi tạo ViewHolder
    public static class ChatViewHolder extends RecyclerView.ViewHolder{

        CircleImageView imgUser;
        TextView tvName, tvStatus;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            imgUser = itemView.findViewById(R.id.imageviewUser);
            tvName = itemView.findViewById(R.id.textviewNameUser);
            tvStatus = itemView.findViewById(R.id.textviewStatus);

        }
    }
}