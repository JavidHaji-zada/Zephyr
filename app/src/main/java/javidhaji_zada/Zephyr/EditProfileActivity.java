package javidhaji_zada.Zephyr;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.media.ExifInterface;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    // constants
    private final int REQUEST_CAMERA = 1;
    private final int SELECT_FILE = 0;
    private final int STORAGE_PERMISSION_CODE = 1;

    private ImageView profile_photo6;
    private ArrayList<ImageView> profile_photos;
    private ArrayList<Uri> imageUris;
    private EditText about;
    private boolean gender;
    private StorageReference mStorageReference;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mUser;
    private Uri mUri;
    private int no_of_photos = 0;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // initialize
        mProgressDialog = new ProgressDialog(EditProfileActivity.this);
        ImageView back = findViewById(R.id.edit_profile_back);
        profile_photos = new ArrayList<>();
        imageUris = new ArrayList<>();
        ImageView profile_photo1 = findViewById(R.id.add_picture_1);
        ImageView profile_photo2 = findViewById(R.id.add_picture_2);
        ImageView profile_photo3 = findViewById(R.id.add_picture_3);
        ImageView profile_photo4 = findViewById(R.id.add_picture_4);
        ImageView profile_photo5 = findViewById(R.id.add_picture_5);
        profile_photo6 = findViewById(R.id.add_picture_6);
        profile_photos.add(profile_photo1);
        profile_photos.add(profile_photo2);
        profile_photos.add(profile_photo3);
        profile_photos.add(profile_photo4);
        profile_photos.add(profile_photo5);
        profile_photos.add(profile_photo6);
        about = findViewById(R.id.about_you);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        gender = getIntent().getBooleanExtra("Gender",true);
        mStorageReference = FirebaseStorage.getInstance().getReference().child("Users").child(mUser.getUid());
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Male")
                .child(mUser.getUid());
        if (gender)
            mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Female")
                    .child(mUser.getUid());

        getInformation();
        // set functions
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        for (int i = 0; i < profile_photos.size(); i++)
        {
            final int finalI = i;
            profile_photos.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (finalI < no_of_photos) {
                        final String[] items = new String[]{getResources().getString(R.string.delete), getResources().getString(R.string.addNewPhoto)};
                        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                        builder.setTitle(getResources().getString(R.string.chooseAction));
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0)
                                {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                                    builder.setTitle(getResources().getString(R.string.delete));
                                    builder.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mProgressDialog.setMessage(getResources().getString(R.string.deletingPhoto));
                                            mProgressDialog.show();
                                            deletePhoto(finalI);
                                            dialog.dismiss();
                                        }
                                    })
                                            .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                    builder.show();
                                }
                                else if(which == 1)
                                {
                                    if ((ContextCompat.checkSelfPermission(EditProfileActivity.this,
                                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(EditProfileActivity.this,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                                        selectImage();
                                    } else {
                                        requestStoragePermission();
                                    }
                                }
                            }
                        });
                        builder.show();
                    }
                    else
                    {
                        if ((ContextCompat.checkSelfPermission(EditProfileActivity.this,
                                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(EditProfileActivity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                            selectImage();
                        } else {
                            requestStoragePermission();
                        }
                    }
                }
            });
        }
    }

    private void deletePhoto(int finalI) {
        imageUris.remove(finalI);
    for (int i = 0; i < imageUris.size();i++)
    {
        int a = i+1;
        profile_photos.get(i).setImageURI(imageUris.get(i));
        mStorageReference.child("photo" + a).putFile(imageUris.get(i));
    }
    for (int i = imageUris.size()+1; i <= 6; i++)
    {
        mStorageReference.child("photo" + i).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }
        Toast.makeText(getApplicationContext(),R.string.deleted,Toast.LENGTH_SHORT).show();
        startActivity(new Intent(EditProfileActivity.this, EditProfileActivity.class).putExtra("Gender",gender));
        mProgressDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        saveChanges();
        Intent intent = new Intent(EditProfileActivity.this,MainActivity.class);
        startActivity(intent);    }

    public void getInformation()
    {
        TextView username = findViewById(R.id.usernameTextView);
        username.setText(mUser.getDisplayName());
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                about.setText(dataSnapshot.child("About").getValue(String.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        for ( int i = 1; i <= 6; i++)
        {
            final int finalI = i;
            mStorageReference.child("photo" + i).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    if ( uri!= null) {
                        imageUris.add(uri);
                        Picasso.get().load(uri).fit().into(profile_photos.get(finalI - 1));
                        no_of_photos++;
                    }
                }
            });
        }
    }
    public void saveChanges()
    {
        ProgressDialog dialog = new ProgressDialog(EditProfileActivity.this);
        dialog.setMessage(getResources().getString(R.string.updating_profile));
        Map<String,Object > map = new HashMap<>();
        map.put("About",about.getText().toString());
        mDatabaseReference.updateChildren(map);
    }
    public void selectImage()
    {
        final String[] items = {getResources().getString(R.string.camera), getResources().getString(R.string.gallery),getResources().getString(R.string.cancel)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.choose_photo));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0)
                {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, getResources().getString(R.string.new_picture));
                    values.put(MediaStore.Images.Media.DESCRIPTION, getResources().getString(R.string.from_camera));
                    mUri = getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
                    startActivityForResult(intent, REQUEST_CAMERA);
                }
                else if(which == 1)
                {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.choose_photo)), SELECT_FILE);
                }
                else if(which == 2)
                {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                try {
                    Bitmap thumbnail = MediaStore.Images.Media.getBitmap(
                            getContentResolver(), mUri);
                    ExifInterface ei = new ExifInterface(getRealPathFromURI(mUri));
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED);
                    Bitmap rotatedBitmap;
                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotatedBitmap = rotateImage(thumbnail, 90);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotatedBitmap = rotateImage(thumbnail, 180);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotatedBitmap = rotateImage(thumbnail, 270);
                            break;

                        case ExifInterface.ORIENTATION_NORMAL:
                        default:
                            rotatedBitmap = thumbnail;
                    }
                    if ( no_of_photos !=6)
                        profile_photos.get(no_of_photos).setImageBitmap(rotatedBitmap);
                    else
                        profile_photo6.setImageBitmap(rotatedBitmap);
                    mUri = getImageUri(EditProfileActivity.this,rotatedBitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == SELECT_FILE) {
                mUri = data.getData();
                if ( no_of_photos !=6)
                    profile_photos.get(no_of_photos).setImageURI(mUri);
                else
                    profile_photo6.setImageURI(mUri);
            }
            // save photo to Firebase
            no_of_photos++;
            mStorageReference.child("photo" + no_of_photos).putFile(mUri);
            imageUris.add(mUri);
        }
    }

    // convert Bitmap to Uri
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.access))
                    .setPositiveButton(getResources().getString(R.string.allow), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(EditProfileActivity.this,
                                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.deny), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this, getResources().getString(R.string.access_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
