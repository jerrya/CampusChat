package app.campuschat.me.CampusWall;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import app.campuschat.me.MainActivity;
import app.campuschat.me.MyRatings;
import app.campuschat.me.R;
import app.campuschat.me.Utility.Config;
import app.campuschat.me.Utility.SquareImageView;

public class CampusWallAdapter extends ArrayAdapter<CampusWallItem> {

    private static final String TAG = "CampusWallAdapter";
    private MyRatings mRemove;

    private class ViewHolder {
        SquareImageView picture;
        ImageView image_view_grid;
        TextView wallCommentCount;
    }

    public CampusWallAdapter(Context context, ArrayList<CampusWallItem> pictureItemList) {
        super(context, 0, pictureItemList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final CampusWallItem pictureItem = getItem(position);

        final ViewHolder viewHolder;
        if(convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.campus_wall_item, parent, false);
            viewHolder.picture = (SquareImageView) convertView.findViewById(R.id.picture);
            viewHolder.image_view_grid = (ImageView) convertView.findViewById(R.id.image_view_grid);
            viewHolder.wallCommentCount = (TextView) convertView.findViewById(R.id.wallCommentCount);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String imageUrl = Config.WALL_PREFIX + pictureItem.imageurl;

        ColorGenerator generator = ColorGenerator.MATERIAL;
        final int color = generator.getColor(pictureItem.trueID);
        TextDrawable drawable = TextDrawable.builder().buildRoundRect(pictureItem.rating, color, 10);
        viewHolder.image_view_grid.setImageDrawable(drawable);

        if(pictureItem.comments == 1) {
            viewHolder.wallCommentCount.setText("" + pictureItem.comments);
        } else if(pictureItem.comments > 1) {
            viewHolder.wallCommentCount.setText("" + pictureItem.comments);
        } else {
            viewHolder.wallCommentCount.setVisibility(View.INVISIBLE);
        }

        Picasso.with(getContext()).load(imageUrl).into(viewHolder.picture);

        viewHolder.image_view_grid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean mContainsRating = false;
                final int intRating = Integer.parseInt(pictureItem.rating);

                for(MyRatings ratings : MainActivity.myRatings) {
                    if(ratings.trueId.equals(pictureItem.trueID)) {
                        mRemove = ratings;
                        mContainsRating = true;
                    }
                }

                if(mContainsRating) {
                    int newRating = intRating - 1;
                    pictureItem.setRating(String.valueOf(newRating));
                    TextDrawable drawable = TextDrawable.builder().buildRoundRect(pictureItem.rating, color, 10);
                    viewHolder.image_view_grid.setImageDrawable(drawable);
                    MainActivity.myRatings.remove(mRemove);

                    MainActivity.instance().deleteMyRating(pictureItem.trueID, true);
                } else {
                    int newRating = intRating + 1;
                    pictureItem.setRating(String.valueOf(newRating));
                    TextDrawable drawable = TextDrawable.builder().buildRoundRect(pictureItem.rating, color, 10);
                    viewHolder.image_view_grid.setImageDrawable(drawable);
                    MainActivity.myRatings.add(new MyRatings(pictureItem.trueID, "1"));

                    MainActivity.instance().insertMyRating(pictureItem.trueID, true);
                }
            }
        });

        return convertView;
    }
}
