package app.campuschat.me.CampusTalk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.ArrayList;

import app.campuschat.me.R;

public class CampusTalkInfoAdapter extends ArrayAdapter<String> {

    private static class ViewHolder {
        TextView talk_info_comment;
        ImageView talk_info_image_view;
    }

    public CampusTalkInfoAdapter(Context context, ArrayList<String> talkCommentList) {
        super(context, 0, talkCommentList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String comment = getItem(position);

        ViewHolder viewHolder;
        if(convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.campus_talk_info_item, parent, false);
            viewHolder.talk_info_comment = (TextView) convertView.findViewById(R.id.talk_info_comment);
            viewHolder.talk_info_image_view = (ImageView) convertView.findViewById(R.id.talk_info_image_view);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(comment);

        if(comment.length() > 0) {
            String firstChar = "" + comment.charAt(0);
            TextDrawable drawable = TextDrawable.builder().buildRoundRect(firstChar, color, 10);
            viewHolder.talk_info_image_view.setImageDrawable(drawable);

            viewHolder.talk_info_comment.setText(comment);
        } else {
            TextDrawable drawable = TextDrawable.builder().buildRoundRect("N", color, 10);
            viewHolder.talk_info_image_view.setImageDrawable(drawable);
            viewHolder.talk_info_comment.setText("");
        }

        return convertView;
    }
}
