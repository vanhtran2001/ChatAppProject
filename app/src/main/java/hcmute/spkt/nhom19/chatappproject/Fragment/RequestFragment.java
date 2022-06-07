package hcmute.spkt.nhom19.chatappproject.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import hcmute.spkt.nhom19.chatappproject.Model.Users;
import hcmute.spkt.nhom19.chatappproject.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RequestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RequestFragment extends Fragment {

    //Khởi tạo biến ánh xạ view
    private View RequestFragmentView;
    private RecyclerView recyclerView;

    //Khởi tạo biến database
    private DatabaseReference friendRequestReference, userReference, contactReference;
    private FirebaseAuth mAuth;
    //Khởi tạo biến xác định người đăng nhập
    private String currentUid;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RequestFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RequestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RequestFragment newInstance(String param1, String param2) {
        RequestFragment fragment = new RequestFragment();
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
        RequestFragmentView = inflater.inflate(R.layout.fragment_request, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getCurrentUser().getUid();
        friendRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        contactReference = FirebaseDatabase.getInstance().getReference().child("Contacts");

        recyclerView = (RecyclerView) RequestFragmentView.findViewById(R.id.recyclerViewRequest);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return RequestFragmentView;
    }

    //Xử lý yêu cầu kết bạn
    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>().setQuery(friendRequestReference.child(currentUid), Users.class).build();

        FirebaseRecyclerAdapter<Users, RequestViewHolder> adapter = new FirebaseRecyclerAdapter<Users, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull Users model) {
                holder.itemView.findViewById(R.id.buttonAccept).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.buttonDecline).setVisibility(View.VISIBLE);

                final String uid = getRef(position).getKey();

                DatabaseReference requestTypeReference = getRef(position).child("request").getRef();

                requestTypeReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            String type = snapshot.getValue().toString();

                            if(type.equals("received")) {
                                userReference.child(uid).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.hasChild("image")) {
                                            final String image = snapshot.child("image").getValue().toString();

                                            Picasso.get().load(image).placeholder(R.drawable.user_image).into(holder.imgUser);
                                        }

                                        final String name = snapshot.child("name").getValue().toString();
                                        final String status = snapshot.child("status").getValue().toString();

                                        holder.tvName.setText(name);
                                        holder.tvStatus.setText("Muốn kết bạn với bạn");

                                        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                contactReference.child(currentUid).child(uid).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()) {
                                                            contactReference.child(uid).child(currentUid).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()) {
                                                                        friendRequestReference.child(currentUid).child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()) {
                                                                                    friendRequestReference.child(uid).child(currentUid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if(task.isSuccessful()) {

                                                                                            }
                                                                                        }
                                                                                    });
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        });

                                        holder.btnDecline.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                friendRequestReference.child(currentUid).child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()) {
                                                            friendRequestReference.child(uid).child(currentUid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()) {

                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                            if (type.equals("sent")) {
                                userReference.child(uid).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.hasChild("image")) {
                                            final String image = snapshot.child("image").getValue().toString();

                                            Picasso.get().load(image).placeholder(R.drawable.user_image).into(holder.imgUser);
                                        }
                                        final String name = snapshot.child("name").getValue().toString();
                                        final String status = snapshot.child("status").getValue().toString();

                                        holder.tvName.setText(name);
                                        holder.tvStatus.setText("Đã gửi yêu cầu kết bạn");
                                        holder.btnAccept.setVisibility(View.INVISIBLE);
                                        holder.btnAccept.setEnabled(false);
                                        holder.btnDecline.setText("Hủy");
                                        holder.btnDecline.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                friendRequestReference.child(currentUid).child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()) {
                                                            friendRequestReference.child(uid).child(currentUid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()) {

                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout, parent, false);
                RequestViewHolder viewHolder = new RequestViewHolder(view);
                return viewHolder;
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    //Hàm khởi tại ViewHolder
    public static class RequestViewHolder extends RecyclerView.ViewHolder{

        TextView tvName, tvStatus;
        CircleImageView imgUser;
        Button btnAccept, btnDecline;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.textviewNameUser);
            tvStatus = itemView.findViewById(R.id.textviewStatus);
            imgUser = itemView.findViewById(R.id.imageviewUser);
            btnAccept = itemView.findViewById(R.id.buttonAccept);
            btnDecline = itemView.findViewById(R.id.buttonDecline);

        }
    }
}