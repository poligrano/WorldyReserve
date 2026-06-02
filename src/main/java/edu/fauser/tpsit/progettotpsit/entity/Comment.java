package edu.fauser.tpsit.progettotpsit.entity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Date;
import java.util.ArrayList;

public class Comment
{
    private final Long commentId;
    private final long leaverId;
    private final String displayName;
    private final String comment;
    private final int whenPosted;
    public Comment(Long commentId, long leaverId, String displayName, String comment, int whenPosted)
    {
        this.commentId = commentId;
        this.leaverId = leaverId;
        this.displayName = displayName;
        this.comment = comment;
        this.whenPosted = whenPosted;
    }
    public Comment(long leaverId, String displayName, String comment, int whenPosted)
    {
        this(null, leaverId, displayName, comment, whenPosted);
    }
    public JSONObject toJSON()
    {
        JSONObject json = new JSONObject();
        json.put("comment_id", commentId);
        json.put("leaver_id", leaverId);
        json.put("display_name", displayName);
        json.put("comment", comment);
        json.put("when_posted", whenPosted);
        return json;
    }
    public static JSONArray toJSONArray(ArrayList<Comment> arr)
    {
        JSONArray json = new JSONArray();
        for (Comment c : arr)
            json.put(c.toJSON());
        return json;
    }
}