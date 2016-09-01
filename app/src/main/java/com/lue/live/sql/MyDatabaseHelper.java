package com.lue.live.sql;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Lue on 2016/7/28.
 */
public class MyDatabaseHelper extends SQLiteOpenHelper
{
    private static final int VERSION = 1;
    private static final String CREATE_TABLE = "create table movies(path, name)";

    private static final String DB_NAME = "MyLocalMovies.db";
    public static final String LOCAL_MOVIES_TABLE_NAME = "Movies";
    public static final String SEARCH_HISTORY_TABLE_NAME = "Search_History";

    /**
     * 在SQLiteOpenHelper的子类当中，必须有该构造函数
     * @param context    上下文对象
     * @param name        数据库名称
     * @param factory
     * @param version    当前数据库的版本，值必须是整数并且是递增的状态
     */
    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
    {
        super(context, name, factory, version);
    }

    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler)
    {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String sql = "create table if not exists " + LOCAL_MOVIES_TABLE_NAME + " (file_path text primary key, file_name text)";

        db.execSQL(sql);

        sql = "create table if not exists " + SEARCH_HISTORY_TABLE_NAME + " (file_path text primary key, file_name text)";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        String sql = "DROP TABLE IF EXISTS " + LOCAL_MOVIES_TABLE_NAME;
        db.execSQL(sql);
        onCreate(db);

        sql = "DROP TABLE IF EXISTS " + SEARCH_HISTORY_TABLE_NAME;
        db.execSQL(sql);
        onCreate(db);
    }
}
