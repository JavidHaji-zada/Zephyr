package javidhaji_zada.Zephyr;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ViewPagerAdapter extends PagerAdapter {

    private ArrayList<MatchPartnerUser> contents;
    private Context mContext;
    public ViewPagerAdapter(ArrayList<MatchPartnerUser> contents, Context context) {
        this.contents = contents;
        mContext = context;
    }

    @Override
    public int getCount() {
        return contents.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        LayoutInflater inflater = (LayoutInflater)(mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        final View view = inflater.inflate(R.layout.fragment_card, container,false);
        container.addView(view);
        ImageView photo = view.findViewById(R.id.partner_image);
        ImageButton info = view.findViewById(R.id.matchInfo);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,UserProfileActivity.class);
                intent.putExtra("ID",contents.get(position).ID);
                intent.putExtra("Username",contents.get(position).username);
                mContext.startActivity(intent);
            }
        });
        TextView username = view.findViewById(R.id.partner_username);
        if (contents.get(position).image != null)
            Picasso.get().load(contents.get(position).image).fit().into(photo);
        username.setText(contents.get(position).username);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}