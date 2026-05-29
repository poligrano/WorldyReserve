package edu.fauser.tpsit.progettotpsit.obj;

import org.json.JSONObject;

import java.util.ArrayList;

public class POIMeta
{
    private final long osmId;
    private final long likeNumber;
    private final ArrayList<String> comments;
    public POIMeta(long osmId, long likeNumber)
    {
        this.osmId = osmId;
        this.likeNumber = likeNumber;
        comments = new ArrayList<>();
    }
    public POIMeta(long osmId, long likeNumber, ArrayList<String> comments)
    {
        this.osmId = osmId;
        this.likeNumber = likeNumber;
        this.comments = comments;
    }
    public long getOsmId()
    {
        return osmId;
    }
    public long getLikeNumber()
    {
        return likeNumber;
    }
    public ArrayList<String> getComments()
    {
        return comments;
    }
    public JSONObject toJSON()
    {
        JSONObject json = new JSONObject();
        json.put("like_number", likeNumber);
        json.put("comments", comments);
        return json;
    }
}