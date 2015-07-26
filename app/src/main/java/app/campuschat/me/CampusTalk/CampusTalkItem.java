package app.campuschat.me.CampusTalk;

public class CampusTalkItem {

    String id;
    String trueID;
    String message;
    String rating;
    String date;
    int comments;

    public CampusTalkItem(String id, String trueID, String message, String date, int comments, String rating) {
        this.id = id;
        this.trueID = trueID;
        this.message = message;
        this.rating = rating;
        this.comments = comments;
        this.date = date;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }
}
