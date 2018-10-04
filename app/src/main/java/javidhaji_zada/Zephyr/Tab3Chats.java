package javidhaji_zada.Zephyr;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class Tab3Chats extends Fragment {

    private final String MATCHES_PREFS = "matches_prefs";

    private ArrayList<MatchPartnerUser> matchedUsers;
    private ArrayList<ChatPartnerUser> chatUsers;
    private DatabaseReference mDatabaseReference;
    private StorageReference mFirebaseStorage;
    private FirebaseUser mUser;
    private RecyclerView matches;
    private RecyclerView chats;
    private boolean female;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mFirebaseStorage = FirebaseStorage.getInstance().getReference().child("Users");
    }

    @Override
    public void onResume() {
        super.onResume();
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.tab3_chat,container,false);
        matchedUsers = new ArrayList<>();
        female = Tab1Profile.female;
        chatUsers = new ArrayList<>();
        matches = rootView.findViewById(R.id.matchesList);
        chats = rootView.findViewById(R.id.chats);
        getMatches();
        getChatUsers();
        return rootView;
    }
    public void setMatchedUsers(MatchPartnerUser model)
    {
        matchedUsers.add(model);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        matches.setLayoutManager(layoutManager);
        MatchesRecyclerViewAdapter adapter = new MatchesRecyclerViewAdapter(getContext(), matchedUsers);
        matches.setAdapter(adapter);
    }
    public void getMatches() {
        if (female) {
            mDatabaseReference.child("Female").child(mUser.getUid()).keepSynced(true);
            mDatabaseReference.child("Female").child(mUser.getUid()).child("Matches").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (final DataSnapshot matchIDs : dataSnapshot.getChildren()) {
                        mDatabaseReference.child("Male").child(matchIDs.getKey()).child("Username").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull final DataSnapshot usernameData) {
                                mFirebaseStorage.child(matchIDs.getKey()).child("photo1").getDownloadUrl().
                                        addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                String id = matchIDs.getKey();
                                                String username = usernameData.getValue(String.class);
                                                MatchPartnerUser model = new MatchPartnerUser();
                                                model.image = uri;
                                                model.ID = id;
                                                model.username = username;
                                                setMatchedUsers(model);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        String id = matchIDs.getKey();
                                        String username = usernameData.getValue(String.class);
                                        MatchPartnerUser model = new MatchPartnerUser();
                                        model.image = null;
                                        model.ID = id;
                                        model.username = username;
                                        setMatchedUsers(model);
                                    }
                                });
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(getContext(),"Error: " + databaseError.toString(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(),"Error: " + databaseError.toString(),Toast.LENGTH_SHORT).show();
                }
            });
            mDatabaseReference.child("Female").child(mUser.getUid()).child("Matches").child("Recent").setValue(null);
        }
        else
        {
            mDatabaseReference.child("Male").child(mUser.getUid()).keepSynced(true);
            mDatabaseReference.child("Male").child(mUser.getUid()).child("Matches").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (final DataSnapshot matchIDs : dataSnapshot.getChildren()) {
                        mDatabaseReference.child("Female").child(matchIDs.getKey()).child("Username").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull final DataSnapshot usernameData) {
                                mFirebaseStorage.child(matchIDs.getKey()).child("photo1").getDownloadUrl().
                                        addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                String id = matchIDs.getKey();
                                                String username = usernameData.getValue(String.class);
                                                MatchPartnerUser model = new MatchPartnerUser();
                                                model.image = uri;
                                                model.ID = id;
                                                model.username = username;
                                                setMatchedUsers(model);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        String id = matchIDs.getKey();
                                        String username = usernameData.getValue(String.class);
                                        MatchPartnerUser model = new MatchPartnerUser();
                                        model.image = null;
                                        model.ID = id;
                                        model.username = username;
                                        setMatchedUsers(model);
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(getContext(),"Error: " + databaseError.toString(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(),"Error: " + databaseError.toString(),Toast.LENGTH_SHORT).show();
                }
            });
            mDatabaseReference.child("Male").child(mUser.getUid()).child("Matches").child("Recent").setValue(null);
        }
    }

    public void setChatUsers(ChatPartnerUser model)
    {
        if (!chatUsers.contains(model))
            chatUsers.add(model);
        ChatsRecyclerViewAdapter adapter = new ChatsRecyclerViewAdapter(getContext(), chatUsers);
        chats.setLayoutManager(new LinearLayoutManager(getContext()));
        DividerItemDecoration itemDecor = new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL);
        chats.addItemDecoration(itemDecor);
        chats.setAdapter(adapter);
    }
    private void getChatUsers()
    {
        if (female) {
            mDatabaseReference.child("Female").child(mUser.getUid()).child("Chats").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (final DataSnapshot chatID : dataSnapshot.getChildren()) {
                        mDatabaseReference.child("Male").child(chatID.getKey()).child("Username").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull final DataSnapshot usernameData) {
                                if (chatID.hasChild("Recent"))
                                {
                                    DatabaseReference ref = mDatabaseReference.child("Male").child(mUser.getUid()).child("Chats")
                                            .child(chatID.getKey()).child("Recent");
                                    ref.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            ChatMessage chatMessage = null;
                                            final ChatMessage finalMessage;
                                            for (DataSnapshot messages: dataSnapshot.getChildren())
                                            {
                                                chatMessage = new ChatMessage(messages.child("sender").getValue(String.class),
                                                        messages.child("message").getValue(String.class),
                                                        messages.child("time").getValue(String.class));

                                            }
                                            finalMessage = chatMessage;
                                            mFirebaseStorage.child(chatID.getKey()).child("photo1").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    String id = chatID.getKey();
                                                    String username = usernameData.getValue(String.class);
                                                    ChatPartnerUser model = new ChatPartnerUser();
                                                    model.image = uri;
                                                    model.ID = id;
                                                    model.username = username;
                                                    model.chatText = finalMessage.getMessage();
                                                    model.chatTime = finalMessage.getCreatedAt();
                                                    setChatUsers(model);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    String id = chatID.getKey();
                                                    String username = usernameData.getValue(String.class);
                                                    ChatPartnerUser model = new ChatPartnerUser();
                                                    model.image = null;
                                                    model.ID = id;
                                                    model.username = username;
                                                    model.chatText = finalMessage.getMessage();
                                                    model.chatTime = finalMessage.getCreatedAt();
                                                    setChatUsers(model);
                                                }
                                            });

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                                else
                                {
                                    DatabaseReference ref = mDatabaseReference.child("Female").child(mUser.getUid()).child("Chats")
                                            .child(chatID.getKey()).child("Sent");
                                    ref.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            ChatMessage chatMessage = null;
                                            final ChatMessage finalMessage;
                                            for (DataSnapshot messages: dataSnapshot.getChildren())
                                            {
                                                chatMessage = new ChatMessage(messages.child("sender").getValue(String.class),
                                                        messages.child("message").getValue(String.class),
                                                        messages.child("time").getValue(String.class));
                                            }
                                            finalMessage = chatMessage;
                                            mFirebaseStorage.child(chatID.getKey()).child("photo1").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    String id = chatID.getKey();
                                                    String username = usernameData.getValue(String.class);
                                                    ChatPartnerUser model = new ChatPartnerUser();
                                                    model.image = uri;
                                                    model.ID = id;
                                                    model.username = username;
                                                    model.chatText = finalMessage.getMessage();
                                                    model.chatTime = finalMessage.getCreatedAt();
                                                    setChatUsers(model);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    String id = chatID.getKey();
                                                    String username = usernameData.getValue(String.class);
                                                    ChatPartnerUser model = new ChatPartnerUser();
                                                    model.image = null;
                                                    model.ID = id;
                                                    model.username = username;
                                                    model.chatText = finalMessage.getMessage();
                                                    model.chatTime = finalMessage.getCreatedAt();
                                                    setChatUsers(model);
                                                }
                                            });

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(getContext(),"Error: " + databaseError.toString(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(),"Error: " + databaseError.toString(),Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
        {
            mDatabaseReference.child("Male").child(mUser.getUid()).child("Chats").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (final DataSnapshot chatID : dataSnapshot.getChildren()) {
                        mDatabaseReference.child("Female").child(chatID.getKey()).child("Username").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull final DataSnapshot usernameData) {
                                if (chatID.hasChild("Recent"))
                                {
                                    DatabaseReference ref = mDatabaseReference.child("Male").child(mUser.getUid()).child("Chats")
                                            .child(chatID.getKey()).child("Recent");
                                    ref.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            ChatMessage chatMessage = null;
                                            final ChatMessage finalMessage;
                                            for (DataSnapshot messages: dataSnapshot.getChildren())
                                            {
                                                chatMessage = new ChatMessage(messages.child("sender").getValue(String.class),
                                                        messages.child("message").getValue(String.class),
                                                        messages.child("time").getValue(String.class));

                                            }
                                            finalMessage = chatMessage;
                                            mFirebaseStorage.child(chatID.getKey()).child("photo1").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    String id = chatID.getKey();
                                                    String username = usernameData.getValue(String.class);
                                                    ChatPartnerUser model = new ChatPartnerUser();
                                                    model.image = uri;
                                                    model.ID = id;
                                                    model.username = username;
                                                    model.chatText = finalMessage.getMessage();
                                                    model.chatTime = finalMessage.getCreatedAt();
                                                    setChatUsers(model);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    String id = chatID.getKey();
                                                    String username = usernameData.getValue(String.class);
                                                    ChatPartnerUser model = new ChatPartnerUser();
                                                    model.image = null;
                                                    model.ID = id;
                                                    model.username = username;
                                                    model.chatText = finalMessage.getMessage();
                                                    model.chatTime = finalMessage.getCreatedAt();
                                                    setChatUsers(model);
                                                }
                                            });

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                                else
                                {
                                    DatabaseReference ref = mDatabaseReference.child("Male").child(mUser.getUid()).child("Chats")
                                            .child(chatID.getKey()).child("Sent");
                                    ref.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            ChatMessage chatMessage = null;
                                            final ChatMessage finalMessage;
                                            for (DataSnapshot messages: dataSnapshot.getChildren())
                                            {
                                                chatMessage = new ChatMessage(messages.child("sender").getValue(String.class),
                                                        messages.child("message").getValue(String.class),
                                                        messages.child("time").getValue(String.class));
                                            }
                                            finalMessage = chatMessage;
                                            mFirebaseStorage.child(chatID.getKey()).child("photo1").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    String id = chatID.getKey();
                                                    String username = usernameData.getValue(String.class);
                                                    ChatPartnerUser model = new ChatPartnerUser();
                                                    model.image = uri;
                                                    model.ID = id;
                                                    model.username = username;
                                                    model.chatText = finalMessage.getMessage();
                                                    model.chatTime = finalMessage.getCreatedAt();
                                                    setChatUsers(model);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    String id = chatID.getKey();
                                                    String username = usernameData.getValue(String.class);
                                                    ChatPartnerUser model = new ChatPartnerUser();
                                                    model.image = null;
                                                    model.ID = id;
                                                    model.username = username;
                                                    model.chatText = finalMessage.getMessage();
                                                    model.chatTime = finalMessage.getCreatedAt();
                                                    setChatUsers(model);
                                                }
                                            });

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(getContext(),"Error: " + databaseError.toString(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(),"Error: " + databaseError.toString(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private class MatchesRecyclerViewAdapter extends RecyclerView.Adapter<MatchesRecyclerViewAdapter.MatchesViewHolder>{

        private ArrayList<MatchPartnerUser> mUsers;
        private Context mContext;

        public MatchesRecyclerViewAdapter(Context context, ArrayList<MatchPartnerUser> users)
        {
            mContext = context;
            mUsers = users;
        }
        @NonNull
        @Override
        public MatchesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.matches_list_item,parent,false);
            return new MatchesViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MatchesViewHolder holder, @SuppressLint("RecyclerView") final int position) {
            if (mUsers.get(position).image != null)
                Picasso.get().load(mUsers.get(position).getImage()).fit().into(holder.match_image);
            holder.match_username.setText(mUsers.get(position).username);
            holder.match_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext,ChatActivity.class);
                    intent.putExtra("partnerID",mUsers.get(position).ID);
                    intent.putExtra("partnerUsername",mUsers.get(position).username);
                    if (mUser.getPhotoUrl() != null)
                        intent.putExtra("partnerPhoto",mUsers.get(position).image.toString());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mUsers.size();
        }

        public class MatchesViewHolder extends RecyclerView.ViewHolder {

            CircleImageView match_image;
            TextView match_username;
            public MatchesViewHolder(View itemView) {
                super(itemView);
                match_image = itemView.findViewById(R.id.match_photo);
                match_username = itemView.findViewById(R.id.match_username);
            }
        }
    }

    private class ChatsRecyclerViewAdapter extends RecyclerView.Adapter<ChatsRecyclerViewAdapter.ChatsViewHolder>{

        private ArrayList<ChatPartnerUser> mUsers;
        private Context mContext;

        public ChatsRecyclerViewAdapter(Context context, ArrayList<ChatPartnerUser> users)
        {
            mContext = context;
            mUsers = users;
        }
        @NonNull
        @Override
        public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chats_list_item,parent,false);
            return new ChatsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatsViewHolder holder, @SuppressLint("RecyclerView") final int position) {
            if (mUsers.get(position).image != null)
                Picasso.get().load(mUsers.get(position).getImage()).fit().into(holder.chat_image);
            holder.chat_username.setText(mUsers.get(position).username);
            holder.chat_text.setText(mUsers.get(position).chatText);
            holder.chat_time.setText(mUsers.get(position).chatTime);
            holder.mConstraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext,ChatActivity.class);
                    intent.putExtra("partnerID",mUsers.get(position).ID);
                    intent.putExtra("partnerUsername",mUsers.get(position).username);
                    if (mUser.getPhotoUrl() != null)
                        intent.putExtra("partnerPhoto",mUsers.get(position).image.toString());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mUsers.size();
        }

        public class ChatsViewHolder extends RecyclerView.ViewHolder {

            CircleImageView chat_image;
            TextView chat_username;
            TextView chat_text;
            TextView chat_time;
            ConstraintLayout mConstraintLayout;
            public ChatsViewHolder(View itemView) {
                super(itemView);
                chat_image = itemView.findViewById(R.id.chat_photo);
                chat_username = itemView.findViewById(R.id.chat_username);
                chat_text = itemView.findViewById(R.id.chat_text);
                chat_time = itemView.findViewById(R.id.chatTime);
                mConstraintLayout = itemView.findViewById(R.id.chatListLayout);
            }
        }
    }
}