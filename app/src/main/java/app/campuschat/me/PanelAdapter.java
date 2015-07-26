package app.campuschat.me;

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

public class PanelAdapter extends ArrayAdapter<String> {

    private static class ViewHolder {
        TextView navigationTitle;
        ImageView image_view_comment;
    }

    public PanelAdapter(Context context, ArrayList<String> navList) {
        super(context, 0, navList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String value = getItem(position).trim();

        ViewHolder viewHolder;
        if(convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.panel_list_layout, parent, false);
            viewHolder.navigationTitle = (TextView) convertView.findViewById(R.id.navigationTitle);
            viewHolder.image_view_comment = (ImageView) convertView.findViewById(R.id.image_view_comment);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(value);

        if(value.length() > 0) {
            String firstChar = "" + value.charAt(0);
            TextDrawable drawable = TextDrawable.builder().buildRoundRect(firstChar, color, 10);
            viewHolder.image_view_comment.setImageDrawable(drawable);

            viewHolder.navigationTitle.setText(value);
        } else {
            TextDrawable drawable = TextDrawable.builder().buildRoundRect("N", color, 10);
            viewHolder.image_view_comment.setImageDrawable(drawable);
            viewHolder.navigationTitle.setText("");
        }

        return convertView;
    }
}