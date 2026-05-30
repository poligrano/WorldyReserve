package edu.fauser.tpsit.progettotpsit.entity;

import org.json.JSONObject;

import java.util.ArrayList;

public class UserPOIMeta
{
    private final POIMeta poi;
    private final boolean doesLike;
    private final boolean favourite;
    private final boolean reserved;
    private final ArrayList<Comment> ownComments;
    public UserPOIMeta(boolean doesLike, boolean favourite, boolean reserved, ArrayList<Comment> ownComments, POIMeta poi)
    {
        this.doesLike = doesLike;
        this.favourite = favourite;
        this.ownComments = ownComments;
        this.reserved = reserved;
        this.poi = poi;
    }
    public JSONObject toJSON()
    {
        JSONObject json = new JSONObject();
        json.put("does_like", doesLike);
        json.put("favourite", favourite);
        json.put("reserved", reserved);
        json.put("own_comments", Comment.toJSONArray(ownComments));
        json.put("poi", poi.toJSON());
        return json;
    }
}