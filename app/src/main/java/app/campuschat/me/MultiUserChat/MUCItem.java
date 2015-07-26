package app.campuschat.me.MultiUserChat;

public class MUCItem {

    String sender;
    String message;
    String displayName;

    public MUCItem(String sender, String displayName, String message) {
        this.sender = sender;
        this.message = message;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}