package hcmute.spkt.nhom19.chatappproject.Fragment;

import android.annotation.SuppressLint;
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

import de.hdodenhof.circleimageview.CircleImageView;
import hcmute.spkt.nhom19.chatappproject.Activity.FindFriendActivity;
import hcmute.spkt.nhom19.chatappproject.Activity.UserInfoActivity;
import hcmute.spkt.nhom19.chatappproject.Model.Users;
import hcmute.spkt.nhom19.chatappproject.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ContactFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactFragment extends Fragment {

    //Khởi tạo biến ánh xạ view
    private View ContactView;
    private RecyclerView recyclerView;

    //Khởi tạo biến database
    private DatabaseReference contacReference, userReference;
    private FirebaseAuth mAuth;
    //Khởi tạo biến người dùng đang đăng nhập
    private String currentUid;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ContactFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ContactFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContactFragment newInstance(String param1, String param2) {
        ContactFragment fragment = new ContactFragment();
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
        ContactView = inflater.inflate(R.layout.fragment_contact, container, false);

        recyclerView = (RecyclerView) ContactView.findViewById(R.id.recyclerViewContact);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getCurrentUser().getUid();
        contacReference = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUid);
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");

        return ContactView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Users>().setQuery(contacReference, Users.class).build();

        FirebaseRecyclerAdapter<Users, ContactViewHolder> adapter = new FirebaseRecyclerAdapter<Users, ContactViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ContactViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Users model) {
                String uid = getRef(position).getKey();

                userReference.child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChild("image")) {
                            String image = snapshot.child("image").getValue().toString();
                            String name = snapshot.child("name").getValue().toString();
                            String status = snapshot.child("status").getValue().toString();

                            holder.tvName.setText(name);
                            holder.tvStatus.setText(status);
                            Picasso.get().load(image).placeholder(R.drawable.user_image).into(holder.imgUser);

                        } else {
                            String name = snapshot.child("name").getValue().toString();
                            String status = snapshot.child("status").getValue().toString();

                            holder.tvName.setText(name);
                            holder.tvStatus.setText(status);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String uid = getRef(position).getKey();

                        Intent intent = new Intent(holder.itemView.getContext(), UserInfoActivity.class);
                        intent.putExtra("uid", uid);
                        startActivity(intent);

                    }
                });

            }

            @NonNull
            @Override
            public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout, parent, false);
                ContactViewHolder viewHolder = new ContactViewHolder(view);
                return viewHolder;
            }
        };

        recyclerView.setAdapter(adapter);

        adapter.startListening();

    }

    //Hàm khởi tạo ViewHolder
    public static class ContactViewHolder extends RecyclerView.ViewHolder{

        TextView tvName, tvStatus;
        CircleImageView imgUser;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.textviewNameUser);
            tvStatus = itemView.findViewById(R.id.textviewStatus);
            imgUser = itemView.findViewById(R.id.imageviewUser);
        }
    }
}