package com.lue.live.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Lue on 2016/8/31.
 */
public class VideoEntity implements Parcelable
{
    //播放地址
    private String play_path;
    //视频名称
    private String name;
    //视频描述
    private String description;
    //是否是全景视频标志位
    private boolean vr_flag;
    //视频比例 height/width
    private double ratio;
    //视频缩略图地址
    private String thumbnail;
    //该视频对应的微信分享网页的地址
    private String share_path;


    protected VideoEntity(Parcel in)
    {
        play_path = in.readString();
        name = in.readString();
        description = in.readString();
        vr_flag = in.readByte() != 0;
        ratio = in.readDouble();
        thumbnail = in.readString();
        share_path = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(play_path);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeByte((byte) (vr_flag ? 1 : 0));
        dest.writeDouble(ratio);
        dest.writeString(thumbnail);
        dest.writeString(share_path);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Creator<VideoEntity> CREATOR = new Creator<VideoEntity>()
    {
        @Override
        public VideoEntity createFromParcel(Parcel in)
        {
            return new VideoEntity(in);
        }

        @Override
        public VideoEntity[] newArray(int size)
        {
            return new VideoEntity[size];
        }
    };

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPlay_path()
    {
        return play_path;
    }

    public void setPlay_path(String play_path)
    {
        this.play_path = play_path;
    }

    public double getRatio()
    {
        return ratio;
    }

    public void setRatio(double ratio)
    {
        this.ratio = ratio;
    }

    public String getShare_path()
    {
        return share_path;
    }

    public void setShare_path(String share_path)
    {
        this.share_path = share_path;
    }

    public boolean isVr_flag()
    {
        return vr_flag;
    }

    public void setVr_flag(boolean vr_flag)
    {
        this.vr_flag = vr_flag;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getThumbnail()
    {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail)
    {
        this.thumbnail = thumbnail;
    }
}
