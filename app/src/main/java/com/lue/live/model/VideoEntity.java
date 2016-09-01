package com.lue.live.model;

/**
 * Created by Lue on 2016/8/31.
 */
public class VideoEntity
{
    //播放地址
    private String play_path;
    //视频名称
    private String name;
    //是否是全景视频标志位
    private boolean vr_flag;
    //视频比例 height/width
    private double ratio;
    //该视频对应的微信分享网页的地址
    private String share_path;

    public VideoEntity()
    {
    }


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
}
