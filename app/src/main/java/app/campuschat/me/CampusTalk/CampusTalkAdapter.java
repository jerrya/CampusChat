package app.campuschat.me.CampusTalk;

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

import java.util.ArrayList;

import app.campuschat.me.MainActivity;
import app.campuschat.me.MyRatings;
import app.campuschat.me.R;
import app.campuschat.me.Utility.UtilityClass;

public class CampusTalkAdapter extends ArrayAdapter<CampusTalkItem> {

    private static final String TAG = "CampusTalkAdapter";
    private MyRatings mRemove;

    private static class ViewHolder {
        TextView message;
        ImageView imageView;
        TextView talkInfoDate;
        TextView talkCommentCount;
    }

    public CampusTalkAdapter(Context context, ArrayList<CampusTalkItem> wallItemList) {
        super(context, 0, wallItemList);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final CampusTalkItem wallItem = getItem(position);

        final ViewHolder viewHolder;
        if(convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.campus_talk_item, parent, false);
            viewHolder.message = (TextView) convertView.findViewById(R.id.wallItemText);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image_view);
            viewHolder.talkInfoDate = (TextView) convertView.findViewById(R.id.talkInfoDate);
            viewHolder.talkCommentCount = (TextView) convertView.findViewById(R.id.talkCommentCount);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ColorGenerator generator = ColorGenerator.MATERIAL;
        final int color = generator.getColor(wallItem.trueID);
        TextDrawable drawable = TextDrawable.builder().buildRoundRect(wallItem.rating, color, 10);

        if(wallItem.comments == 1) {
            viewHolder.talkCommentCount.setText(wallItem.comments + " COMMENT");
        } else if(wallItem.comments > 1) {
            viewHolder.talkCommentCount.setText(wallItem.comments + " COMMENTS");
        }

        viewHolder.imageView.setImageDrawable(drawable);

        String agoDate = UtilityClass.getTimeAgo(UtilityClass.getDateInMillis(wallItem.date));
        viewHolder.talkInfoDate.setText(agoDate);

        viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean mContainsRating = false;
                final int intRating = Integer.parseInt(wallItem.rating);

                for(MyRatings ratings : MainActivity.myRatings) {
                    if(ratings.trueId.equals(wallItem.trueID)) {
                        mRemove = ratings;
                        mContainsRating = true;
                    }
                }

                if(mContainsRating) {
                    int newRating = intRating - 1;
                    wallItem.setRating(String.valueOf(newRating));
                    TextDrawable drawable = TextDrawable.builder().buildRoundRect(wallItem.rating, color, 10);
                    viewHolder.imageView.setImageDrawable(drawable);
                    MainActivity.myRatings.remove(mRemove);

                    MainActivity.instance().deleteMyRating(wallItem.trueID, false);
                } else {
                    int newRating = intRating + 1;
                    wallItem.setRating(String.valueOf(newRating));
                    TextDrawable drawable = TextDrawable.builder().buildRoundRect(wallItem.rating, color, 10);
                    viewHolder.imageView.setImageDrawable(drawable);
                    MainActivity.myRatings.add(new MyRatings(wallItem.trueID, "1"));

                    MainActivity.instance().insertMyRating(wallItem.trueID, false);
                }
            }
        });

        viewHolder.message.setText(wallItem.message);

        return convertView;
    }
}
