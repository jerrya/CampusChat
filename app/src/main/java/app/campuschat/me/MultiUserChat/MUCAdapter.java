package app.campuschat.me.MultiUserChat;

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

import app.campuschat.me.MainActivity;
import app.campuschat.me.R;

public class MUCAdapter extends ArrayAdapter<MUCItem> {

    private static final String TAG = "Mucarrayadapter";

    private class ViewHolder {
        TextView message;
        ImageView avatar;
        TextView displayName;
    }

    public MUCAdapter(Context context, ArrayList<MUCItem> mucChatList) {
        super(context, 0, mucChatList);
    }

    @Override
    public int getItemViewType(int position) {
        MUCItem mucChat = getItem(position);
        String myName = MainActivity.myNickname;
        if(mucChat.sender != null && mucChat.sender.equals(myName)) {
            return 0;
        }

        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MUCItem mucItem = getItem(position);

        ViewHolder viewHolder;
        if(convertView == null) {
            viewHolder = new ViewHolder();
            if(getItemViewType(position) == 0) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.sending_muc_item, parent, false);
                viewHolder.message = (TextView) convertView.findViewById(R.id.mucMessageSending);
                viewHolder.avatar = (ImageView) convertView.findViewById(R.id.mucAvatar2);
            } else {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.receiving_muc_item, parent, false); // change back to 1
                viewHolder.message = (TextView) convertView.findViewById(R.id.mucMessageReceiving);
                viewHolder.avatar = (ImageView) convertView.findViewById(R.id.mucAvatar);
                viewHolder.displayName = (TextView) convertView.findViewById(R.id.displayName);
            }
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(mucItem.sender);

        String firstChar = "" + mucItem.displayName.charAt(0);

        TextDrawable drawable = TextDrawable.builder().buildRoundRect(firstChar, color, 10);
        viewHolder.avatar.setImageDrawable(drawable);

        if(getItemViewType(position) == 1) {
            viewHolder.displayName.setText(mucItem.getDisplayName());
        }

        viewHolder.message.setText(mucItem.getMessage());

        return convertView;
    }
}
