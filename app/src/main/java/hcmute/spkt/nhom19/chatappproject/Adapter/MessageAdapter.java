package hcmute.spkt.nhom19.chatappproject.Adapter;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hcmute.spkt.nhom19.chatappproject.Activity.ChatActivity;
import hcmute.spkt.nhom19.chatappproject.Model.Messages;
import hcmute.spkt.nhom19.chatappproject.R;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    //Khởi tạo biến danh sách tin nhắn
    private List<Messages> messagesList;

    //Khởi tạo biến database;
    private FirebaseAuth mAuth;
    private DatabaseReference userReference;

    public MessageAdapter(List<Messages> messagesList) {
        this.messagesList = messagesList;
    }

    //Hàm khởi tạo ViewHolder
    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView tvMsgSend, tvMsgReceive, tvSendTime, tvReceiveTime, tvSendPicTime, tvReceivePicTime;
        public CircleImageView imgUser;
        public ImageView imgPicSend, imgPicReceive;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            tvMsgSend = (TextView) itemView.findViewById(R.id.textviewMessageSend);
            tvMsgReceive = (TextView) itemView.findViewById(R.id.textviewMessageReceive);
            imgUser = (CircleImageView) itemView.findViewById(R.id.imageviewMessageUser);
            imgPicSend = (ImageView) itemView.findViewById(R.id.imageviewMessageSend);
            imgPicReceive = (ImageView) itemView.findViewById(R.id.imageviewMessageReceive);
            tvSendTime = (TextView) itemView.findViewById(R.id.textviewSendTime);
            tvReceiveTime = (TextView) itemView.findViewById(R.id.textviewReceiveTime);
            tvSendPicTime = (TextView) itemView.findViewById(R.id.textviewSendPicTime);
            tvReceivePicTime = (TextView) itemView.findViewById(R.id.textviewReceivePicTime);

        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_layout, parent, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String currentUid = mAuth.getCurrentUser().getUid();
        Messages messages = messagesList.get(position);

        String fromUid = messages.getFrom();
        String fromType = messages.getType();

        userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUid);

        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("image")) {
                    String image = snapshot.child("image").getValue().toString();

                    Picasso.get().load(image).placeholder(R.drawable.user_image).into(holder.imgUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.tvMsgReceive.setVisibility(View.GONE);
        holder.imgUser.setVisibility(View.GONE);
        holder.tvMsgSend.setVisibility(View.GONE);

        holder.tvSendTime.setVisibility(View.GONE);
        holder.tvReceiveTime.setVisibility(View.GONE);

        holder.imgPicSend.setVisibility(View.GONE);
        holder.imgPicReceive.setVisibility(View.GONE);

        holder.tvSendPicTime.setVisibility(View.GONE);
        holder.tvReceivePicTime.setVisibility(View.GONE);

        if(fromType.equals("text")) {

            if(fromUid.equals(currentUid)) {
                holder.tvMsgSend.setVisibility(View.VISIBLE);
                holder.tvSendTime.setVisibility(View.VISIBLE);

                holder.tvMsgSend.setText(messages.getMessage());
                holder.tvSendTime.setText(messages.getDate() + " " + messages.getTime());
            } else {
                holder.imgUser.setVisibility(View.VISIBLE);
                holder.tvMsgReceive.setVisibility(View.VISIBLE);
                holder.tvReceiveTime.setVisibility(View.VISIBLE);

                holder.tvMsgReceive.setText(messages.getMessage());
                holder.tvReceiveTime.setText(messages.getDate() + " " + messages.getTime());

            }
        } else if(fromType.equals("image")) {
            if(fromUid.equals(currentUid)) {
                holder.tvSendPicTime.setVisibility(View.VISIBLE);
                holder.imgPicSend.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(holder.imgPicSend);
                holder.tvSendPicTime.setText(messages.getDate() + " " + messages.getTime());
            } else {
                holder.imgUser.setVisibility(View.VISIBLE);
                holder.imgPicReceive.setVisibility(View.VISIBLE);
                holder.tvReceivePicTime.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(holder.imgPicReceive);
                holder.tvReceivePicTime.setText(messages.getDate() + " " + messages.getTime());
            }
        }

        if(fromUid.equals(currentUid)) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    if(messagesList.get(position).getType().equals("text")) {
                        CharSequence option[] = new CharSequence[] {
                                "Gỡ với bạn", "Gỡ với mọi người", "Hủy"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Gỡ tin nhắn");
                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(position == 0) {
                                    deleteSentMessages(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(),ChatActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                if(position == 1) {
                                    deleteMessagesForEveryOne(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(),ChatActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                if(position == 2) {

                                }
                            }
                        });
                        builder.show();
                    }

                    if(messagesList.get(position).getType().equals("image")) {
                        CharSequence option[] = new CharSequence[] {
                                "Gỡ với bạn", "Gỡ với mọi người", "Hủy"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Gỡ hình ảnh");
                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(position == 0) {
                                    deleteSentMessages(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(),ChatActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                if(position == 1) {
                                    deleteMessagesForEveryOne(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(),ChatActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                if(position == 2) {

                                }
                            }
                        });
                        builder.show();
                    }

                    return false;
                }
            });
        } else {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    if(messagesList.get(position).getType().equals("text")) {
                        CharSequence option[] = new CharSequence[] {
                                "Gỡ với bạn" , "Hủy"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Gỡ tin nhắn");
                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(position == 0) {
                                    deleteReceivedMessages(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(),ChatActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }

                                if(position == 1) {

                                }
                            }
                        });
                        builder.show();
                    }

                    if(messagesList.get(position).getType().equals("image")) {
                        CharSequence option[] = new CharSequence[] {
                                "Gỡ với bạn", "Hủy"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Gỡ hình ảnh");
                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(position == 0) {
                                    deleteReceivedMessages(position, holder);

                                    Intent intent = new Intent(holder.itemView.getContext(),ChatActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                if(position == 1) {

                                }
                            }
                        });
                        builder.show();
                    }

                    return false;
                }
            });
        }
    }
    
    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    //Hàm gỡ tin nhắn gửi
    private void deleteSentMessages(final int position, final MessageViewHolder holder) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Messages").child(messagesList.get(position).getFrom()).child(messagesList.get(position).getTo()).child(messagesList.get(position).getMessageId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "Đã xóa tin nhắn", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    //Hàm gỡ tin nhắn nhận
    private void deleteReceivedMessages(final int position, final MessageViewHolder holder) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Messages").child(messagesList.get(position).getTo()).child(messagesList.get(position).getFrom()).child(messagesList.get(position).getMessageId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "Đã xóa tin nhắn", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Hàm gỡ tin nhắn với mọi người
    private void deleteMessagesForEveryOne(final int position, final MessageViewHolder holder) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Messages").child(messagesList.get(position).getTo()).child(messagesList.get(position).getFrom()).child(messagesList.get(position).getMessageId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    databaseReference.child("Messages").child(messagesList.get(position).getFrom()).child(messagesList.get(position).getTo()).child(messagesList.get(position).getMessageId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(holder.itemView.getContext(), "Đã gỡ tin nhắn", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(holder.itemView.getContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    Toast.makeText(holder.itemView.getContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
