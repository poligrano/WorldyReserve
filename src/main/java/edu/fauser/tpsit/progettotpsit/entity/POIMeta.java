package edu.fauser.tpsit.progettotpsit.entity;

import org.json.JSONObject;

import java.util.ArrayList;

public class POIMeta
{
    private final long likeNumber;
    private final ArrayList<Comment> comments;
    public POIMeta(long likeNumber)
    {
        this.likeNumber = likeNumber;
        comments = new ArrayList<>();
    }
    public POIMeta(long likeNumber, ArrayList<Comment> comments)
    {
        this.likeNumber = likeNumber;
        this.comments = comments;
    }
    public JSONObject toJSON()
    {
        JSONObject json = new JSONObject();
        json.put("like_number", likeNumber);
        json.put("comments", Comment.toJSONArray(comments));
        return json;
    }
}