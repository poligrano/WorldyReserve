package edu.fauser.tpsit.progettotpsit.obj;

import org.json.JSONObject;

public class UserPOIMeta
{
    private final long uid;
    private final POIMeta poi;
    private final boolean doesLike;
    private final boolean favourite;
    private final String ownComment;
    public UserPOIMeta(long uid, boolean doesLike, boolean favourite, String ownComment, POIMeta poi)
    {
        this.uid = uid;
        this.doesLike = doesLike;
        this.favourite = favourite;
        this.ownComment = ownComment;
        this.poi = poi;
    }
    public boolean getDoesLike()
    {
        return doesLike;
    }
    public boolean isFavourite()
    {
        return favourite;
    }
    public String getOwnComment()
    {
        return ownComment;
    }
    public long getUid()
    {
        return uid;
    }
    public POIMeta getPoi()
    {
        return poi;
    }
    public JSONObject toJSON()
    {
        JSONObject json = new JSONObject();
        json.put("does_like", doesLike);
        json.put("favourite", false);
        json.put("own_comment", ownComment);
        json.put("poi", poi.toJSON());
        return json;
    }
}