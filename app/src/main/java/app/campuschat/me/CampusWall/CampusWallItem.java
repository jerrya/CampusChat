package app.campuschat.me.CampusWall;

public class CampusWallItem {

    String id, trueID, imageurl, rating, date;
    int comments;

    public CampusWallItem(String id, String trueID, String imageurl, String date, int comments, String rating) {
        this.id = id;
        this.trueID = trueID;
        this.imageurl = imageurl;
        this.rating = rating;
        this.comments = comments;
        this.date = date;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }
}
