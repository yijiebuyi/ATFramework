package com.callme.platform.util.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.callme.platform.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司 版权所有
 *
 * @功能描述：数据库处理
 * @作者：mikeyou
 * @创建时间：2017-10-6
 * @修改人：
 * @修改描述：
 * @修改日期
 */
public class DBHelper extends SQLiteOpenHelper {
    /**
     * 数据库名称
     */
    public static final String DB_NAME = "callme.db";
    /**
     * 数据库版本
     */
    public static final int DB_VERSION = 1;

    /**
     * 省市区信息
     */
    public static final String TABLE_AREAINFO = "AreaTable";
    /**
     * 历史定位记录
     */
    public static final String TABLE_HISTORY_LOCATION = "HistoryLocationTable";


    private String SQL_AREA_INFO = "create table if not exists "
            + TABLE_AREAINFO
            + " (_Id INTEGER PRIMARY KEY AUTOINCREMENT, RegionId Integer, ParentId Integer, Name varchar, Level Integer, Spell varchar, Time Long)";
    private String SQL_HISTORY_LOCATION = "create table if not exists "
            + TABLE_HISTORY_LOCATION
            + "(_Id INTEGER PRIMARY KEY AUTOINCREMENT, UserName Varchar, Province Varchar, City Varchar, District Varchar, ProvinceId Integer, CityId Integer, DistrictId Integer, LocationType Integer, Lng NUMERIC, Lat NUMERIC, DetailAddress Varchar, PutRegionalType Integer, Time Long)";

    public final static String USER_NAME = "userName"; // 用户账号
    public final static String USER_PWD = "userPwd"; // 用户密码

    private static DBHelper instance = null;
    protected static Context mContext;

    public static DBHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DBHelper.class) {
                instance = new DBHelper(context.getApplicationContext(),
                        DB_NAME, null, DB_VERSION);
            }
            mContext = context.getApplicationContext();
        }
        return instance;
    }

    public DBHelper(Context context, String name, CursorFactory factory,
                    int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            if (db != null) {
                db.execSQL(SQL_AREA_INFO);
                db.execSQL(SQL_HISTORY_LOCATION);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            if (db != null) {
                db.execSQL("drop table if exists " + TABLE_AREAINFO);
                db.execSQL("drop table if exists " + TABLE_HISTORY_LOCATION);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        onCreate(db);
    }

    /**
     * 清除所有数据库中的数据
     */
    public static void clearAllTable(Context context) {
        try {
            DBHelper dbHelper = getInstance(context);
            dbHelper.clearTable(TABLE_AREAINFO);
            dbHelper.clearTable(TABLE_HISTORY_LOCATION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获得指定表的数据行数
     *
     * @param tableName
     * @return
     */
    public int getRowCount(String tableName) throws Exception {
        int count = 0;
        Cursor cur = null;
        try {
            StringBuilder builder = new StringBuilder("select count(1) from ");
            builder.append(tableName).append(";");
            cur = query(builder.toString());
            if (cur.moveToFirst()) {
                count = cur.getInt(0);
            }
        } finally {
            if (cur != null) {
                cur.close();
            }
        }

        return count;
    }

    public static int getRowCount(SQLiteDatabase db, String tableName,
                                  String selections) {
        int count = 0;
        Cursor cur = null;
        try {
            String sql = "select count(1) from " + tableName;
            if (selections != null) {
                sql += (" where " + selections);
            }

            cur = db.rawQuery(sql, null);
            if (cur.moveToFirst()) {
                count = cur.getInt(0);
            }
        } finally {
            if (cur != null) {
                cur.close();
            }
        }

        return count;
    }

    /**
     * 判断表在数据库中是否存在
     *
     * @param tableName
     * @return
     */
    public boolean exist(String tableName) throws Exception {
        return DBHelper.existTable(getWritableDatabase(), tableName);
    }

    public boolean isColumnExist(String tableName, String column) {
        if (TextUtils.isEmpty(column)) {
            return false;
        }

        boolean res = false;
        String querySql = "SELECT * FROM " + tableName + " LIMIT 0";
        Cursor c = null;

        try {
            c = getWritableDatabase().rawQuery(querySql, null);
            if (c != null) {
                String[] columns = c.getColumnNames();
                if (columns != null) {
                    int count = columns.length;
                    for (int i = 0; i < count; i++) {
                        if (column.equals(columns[i])) {
                            res = true;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return res;
    }

    public static boolean isColumnExist(SQLiteDatabase db, String tableName,
                                        String column) {
        if (db == null || TextUtils.isEmpty(tableName)
                || TextUtils.isEmpty(column)) {
            return false;
        }

        boolean res = false;
        String querySql = "SELECT * FROM " + tableName + " LIMIT 0";
        Cursor c = null;

        try {
            c = db.rawQuery(querySql, null);
            if (c != null) {
                String[] columns = c.getColumnNames();
                if (columns != null) {
                    int count = columns.length;
                    for (int i = 0; i < count; i++) {
                        if (column.equals(columns[i])) {
                            res = true;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return res;
    }

    public int getVersion() throws Exception {
        return getWritableDatabase().getVersion();
    }

    /**
     * 清空表内数据
     *
     * @param tableName
     */
    public void clearTable(String tableName) throws Exception {
        getWritableDatabase().execSQL("DELETE FROM " + tableName + ";");
    }

    public static void clearTable(SQLiteDatabase db, String tableName)
            throws Exception {
        db.execSQL("DELETE FROM " + tableName + ";");
    }

    public void clearTable(String tableName, String selection) throws Exception {
        if (TextUtils.isEmpty(selection)) {
            clearTable(tableName);
        } else {
            String sql = "DELETE FROM " + tableName + " WHERE " + selection
                    + ";";
            getWritableDatabase().execSQL(sql);
        }
    }

    public int clearTable(String tableName, String whereClause,
                          String[] whereArgs) throws Exception {
        return getWritableDatabase().delete(tableName, whereClause, whereArgs);
    }

    /**
     * 向表中插入一条数据
     *
     * @param tableName
     * @param values
     * @return id 如果成功，否则返回-1.
     */
    public int insert(String tableName, ContentValues values) throws Exception {
        int id = (int) getWritableDatabase().insert(tableName, "Null", values);
        return id;
    }

    /**
     * 向表中插入一数据
     *
     * @param tableName
     * @param valuesList
     * @return
     * @throws Exception
     */
    public int batchInsert(String tableName, List<ContentValues> valuesList)
            throws Exception {
        SQLiteDatabase db = getWritableDatabase();
        int id = -1;
        try {
            db.beginTransaction();
            for (ContentValues value : valuesList) {
                id = (int) db.insert(tableName, "Null", value);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return id;
    }

    /**
     * 替换一条数据
     *
     * @param tableName
     * @param values
     */
    public void replace(String tableName, ContentValues values)
            throws Exception {
        getWritableDatabase().replace(tableName, "Null", values);
    }

    /**
     * 执行sql查询
     *
     * @param sql
     * @return
     */
    public Cursor query(String sql) throws Exception {
        return getWritableDatabase().rawQuery(sql, null);
    }

    /**
     * @param distinct
     * @param table
     * @param selection
     * @param selectionArgs
     * @param groupBy
     * @param having
     * @param orderBy
     * @param limit
     * @return
     * @throws Exception
     */
    public Cursor query(boolean distinct, String table, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy, String limit) throws Exception {
        return getWritableDatabase().query(distinct, table,
                new String[]{"*"}, selection, selectionArgs, groupBy,
                having, orderBy, limit);
    }

    /**
     * 数据库查询
     *
     * @param distinct
     * @param table
     * @param selection
     * @param groupBy
     * @param having
     * @param orderBy
     * @param limit
     * @return
     */
    public Cursor query(boolean distinct, String table, String selection,
                        String groupBy, String having, String orderBy, String limit)
            throws Exception {
        return getWritableDatabase().query(distinct, table,
                new String[]{"*"}, selection, null, groupBy, having,
                orderBy, limit);
    }

    /**
     * 数据库查询
     *
     * @param table
     * @param orderBy
     * @return
     */
    public Cursor listAll(String table, String orderBy) throws Exception {
        return query(false, table, null, null, null, orderBy, null);
    }

    /**
     * 数据库查询，有条数限制
     *
     * @param table
     * @param orderBy
     * @return
     */
    public Cursor listLimit(String table, String orderBy, String limit)
            throws Exception {
        return query(false, table, null, null, null, orderBy, limit);
    }

    public Cursor rawQuery(String sql, String[] selectionArgs) throws Exception {
        return getWritableDatabase().rawQuery(sql, selectionArgs);
    }

    /**
     * 数据库查询
     *
     * @param table
     * @param selection
     * @return
     * @throws Exception
     */
    public Cursor query(String table, String selection) throws Exception {
        return query(false, table, selection, null, null, null, null);
    }

    /**
     * 数据库查询
     *
     * @param table
     * @param selection
     * @param orderBy
     * @return
     * @throws Exception
     */
    public Cursor query(String table, String selection, String orderBy)
            throws Exception {
        return query(false, table, selection, null, null, orderBy, null);
    }

    /**
     * Query database.
     */
    public Cursor query(String table, String[] columns, String selection,
                        String[] selectionArgs, String orderBy) throws Exception {
        return getWritableDatabase().query(table, columns, selection,
                selectionArgs, null, null, orderBy);
    }

    /**
     * 数据删除
     *
     * @param tableName
     * @param whereClause
     */
    public int delete(String tableName, String whereClause) throws Exception {
        return delete(tableName, whereClause, null);
    }

    /**
     * Delete data.
     */
    public int delete(String tableName, String whereClause, String[] whereArgs)
            throws Exception {
        int rowCount = getWritableDatabase().delete(tableName, whereClause,
                whereArgs);

        return rowCount;
    }

    /**
     * 更新数据
     *
     * @param table
     * @param values
     * @param whereClause
     */
    public int update(String table, ContentValues values, String whereClause)
            throws Exception {
        return update(table, values, whereClause, null);
    }

    /**
     * Update data.
     */
    public int update(String table, ContentValues values, String whereClause,
                      String[] whereArgs) throws Exception {
        int rowCount = getWritableDatabase().update(table, values, whereClause,
                whereArgs);

        return rowCount;
    }

    /**
     * Check whether specified table existed in the database
     *
     * @param db        数据库对象
     * @param tableName 指定表名称
     * @return
     */
    public static boolean existTable(SQLiteDatabase db, String tableName)
            throws Exception {
        StringBuilder builder = new StringBuilder(
                "select 1 from sqlite_master where type='table' and name='");
        builder.append(tableName).append("';");
        Cursor cur = null;
        try {
            cur = db.rawQuery(builder.toString(), null);
            return cur.moveToNext();
        } finally {
            if (cur != null) {
                cur.close();
            }
        }
    }

    /**
     * 向某个表中添加列
     *
     * @param db
     * @param table
     * @param columnName
     * @param columnType
     */
    public void insertColumn(String table, String columnName, String columnType) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("ALERT TABLE " + table + " ADD COLUMN " + columnName
                    + " " + columnType);
        } catch (Exception e) {

        }
    }

    /**
     * 数据库插入单条数据
     */
    public void insertDb(String table, ContentValues values) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.insert(table, null, values);
        } catch (Exception e) {

        }
    }

    /**
     * 数据库插入多条数据
     */
    public boolean insertListDb(Context context, String table,
                                List<ContentValues> values) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
        } catch (Exception e1) {

            e1.printStackTrace();
        }
        db.beginTransaction();
        try {
            for (int i = 0; i < values.size(); i++) {
                db.insert(table, null, values.get(i));
            }
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 数据库 查找表中所有数据
     */
    public List<ContentValues> findDb(String table) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        List<ContentValues> mListValues = new ArrayList<ContentValues>();
        Cursor cursor = null;
        String sql = "select * from '" + table + "'";
        try {
            cursor = db.rawQuery(sql, null);

            while (cursor != null && !cursor.isLast()) {
                ContentValues mValues = new ContentValues();
                cursor.moveToNext();

                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    mValues.put(cursor.getColumnNames()[i], cursor.getString(i));
                }
                mListValues.add(mValues);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<ContentValues>();
        } finally {
            if (cursor != null)
                cursor.close();
            cursor = null;
        }

        return mListValues;
    }

    /**
     * 数据库 按条件查找表中所有数据
     *
     * @param db
     * @param table
     * @param columName 列名
     * @param value     查询条件值
     * @return
     */
    public List<ContentValues> findDbByWhere(String table, String value,
                                             String columName) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        List<ContentValues> mListValues = new ArrayList<ContentValues>();
        Cursor cursor = null;
        String sql = "select * from " + table + " where " + columName + " = '"
                + value + "'";
        try {
            cursor = db.rawQuery(sql, null);
            while (cursor != null && cursor.moveToNext()) {
                ContentValues mValues = new ContentValues();

                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    mValues.put(cursor.getColumnNames()[i], cursor.getString(i));
                }
                mListValues.add(mValues);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<ContentValues>();
        } finally {
            if (cursor != null)
                cursor.close();
            cursor = null;
        }

        return mListValues;
    }

    /**
     * 数据库 更新表
     *
     * @param mContext
     * @param table
     * @param mValues
     * @param name     自己设计表 主键名 查找是否有数据做判断用
     */
    public void updateDb(String table, ContentValues values, String name) {
        SharedPreferencesUtil sharedPreferencesUtil = SharedPreferencesUtil
                .getInstance(mContext);

        try {
            SQLiteDatabase db = getWritableDatabase();
            List<ContentValues> mListValues = findDbByWhere(
                    table,
                    sharedPreferencesUtil.getString(USER_NAME, ""),
                    name);
            if (mListValues.size() > 0) {

                String where = name + "=?";
                String[] whereArgs = {sharedPreferencesUtil.getString(
                        USER_NAME, "")};
                db.update(table, values, where, whereArgs);

            } else {

                insertDb(table, values);

            }

        } catch (Exception e) {

        }

    }

    /**
     * 数据库 删除表
     */
    public void deleteDb(String table) {

        try {
            SQLiteDatabase db = getWritableDatabase();
            if (isExistTable(table))
                db.delete(table, null, null);
        } catch (Exception e) {

        }
    }

    /**
     * 数据库 根据条件删除数据
     */
    public void deleteDbByWhere(String table, String value, String columName) {
        try {
            SQLiteDatabase db = getWritableDatabase();

            if (isExistTable(table)) {
                String sql = "delete from " + table + " where " + columName
                        + " = '" + value + "'";
                db.execSQL(sql);
            }

        } catch (Exception e) {

        }

    }

    /**
     * 判断表是否存在
     */
    public boolean isExistTable(String table) {

        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        Cursor cursor = null;
        String sql = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='"
                + table + "'";
        try {
            cursor = db.rawQuery(sql, null);
            if (cursor != null && cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
            cursor = null;
        }

        return false;
    }
}
