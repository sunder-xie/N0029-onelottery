package com.peersafe.chainbet.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "USER_INFO".
*/
public class UserInfoDao extends AbstractDao<UserInfo, String> {

    public static final String TABLENAME = "USER_INFO";

    /**
     * Properties of entity UserInfo.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property UserId = new Property(0, String.class, "userId", true, "USER_ID");
        public final static Property WalletAddr = new Property(1, String.class, "walletAddr", false, "WALLET_ADDR");
        public final static Property Balance = new Property(2, Long.class, "balance", false, "BALANCE");
        public final static Property IsCurUser = new Property(3, Boolean.class, "isCurUser", false, "IS_CUR_USER");
        public final static Property CurBlockHeight = new Property(4, Long.class, "curBlockHeight", false, "CUR_BLOCK_HEIGHT");
        public final static Property PrevBlockHeight = new Property(5, Long.class, "prevBlockHeight", false, "PREV_BLOCK_HEIGHT");
        public final static Property TxnIDs = new Property(6, String.class, "txnIDs", false, "TXN_IDS");
        public final static Property LastGetBlockHeight = new Property(7, Long.class, "lastGetBlockHeight", false, "LAST_GET_BLOCK_HEIGHT");
    };


    public UserInfoDao(DaoConfig config) {
        super(config);
    }
    
    public UserInfoDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"USER_INFO\" (" + //
                "\"USER_ID\" TEXT PRIMARY KEY NOT NULL ," + // 0: userId
                "\"WALLET_ADDR\" TEXT," + // 1: walletAddr
                "\"BALANCE\" INTEGER," + // 2: balance
                "\"IS_CUR_USER\" INTEGER," + // 3: isCurUser
                "\"CUR_BLOCK_HEIGHT\" INTEGER," + // 4: curBlockHeight
                "\"PREV_BLOCK_HEIGHT\" INTEGER," + // 5: prevBlockHeight
                "\"TXN_IDS\" TEXT," + // 6: txnIDs
                "\"LAST_GET_BLOCK_HEIGHT\" INTEGER);"); // 7: lastGetBlockHeight
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"USER_INFO\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, UserInfo entity) {
        stmt.clearBindings();
 
        String userId = entity.getUserId();
        if (userId != null) {
            stmt.bindString(1, userId);
        }
 
        String walletAddr = entity.getWalletAddr();
        if (walletAddr != null) {
            stmt.bindString(2, walletAddr);
        }
 
        Long balance = entity.getBalance();
        if (balance != null) {
            stmt.bindLong(3, balance);
        }
 
        Boolean isCurUser = entity.getIsCurUser();
        if (isCurUser != null) {
            stmt.bindLong(4, isCurUser ? 1L: 0L);
        }
 
        Long curBlockHeight = entity.getCurBlockHeight();
        if (curBlockHeight != null) {
            stmt.bindLong(5, curBlockHeight);
        }
 
        Long prevBlockHeight = entity.getPrevBlockHeight();
        if (prevBlockHeight != null) {
            stmt.bindLong(6, prevBlockHeight);
        }
 
        String txnIDs = entity.getTxnIDs();
        if (txnIDs != null) {
            stmt.bindString(7, txnIDs);
        }
 
        Long lastGetBlockHeight = entity.getLastGetBlockHeight();
        if (lastGetBlockHeight != null) {
            stmt.bindLong(8, lastGetBlockHeight);
        }
    }

    /** @inheritdoc */
    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public UserInfo readEntity(Cursor cursor, int offset) {
        UserInfo entity = new UserInfo( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // userId
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // walletAddr
            cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2), // balance
            cursor.isNull(offset + 3) ? null : cursor.getShort(offset + 3) != 0, // isCurUser
            cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4), // curBlockHeight
            cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5), // prevBlockHeight
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // txnIDs
            cursor.isNull(offset + 7) ? null : cursor.getLong(offset + 7) // lastGetBlockHeight
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, UserInfo entity, int offset) {
        entity.setUserId(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setWalletAddr(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setBalance(cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2));
        entity.setIsCurUser(cursor.isNull(offset + 3) ? null : cursor.getShort(offset + 3) != 0);
        entity.setCurBlockHeight(cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4));
        entity.setPrevBlockHeight(cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5));
        entity.setTxnIDs(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setLastGetBlockHeight(cursor.isNull(offset + 7) ? null : cursor.getLong(offset + 7));
     }
    
    /** @inheritdoc */
    @Override
    protected String updateKeyAfterInsert(UserInfo entity, long rowId) {
        return entity.getUserId();
    }
    
    /** @inheritdoc */
    @Override
    public String getKey(UserInfo entity) {
        if(entity != null) {
            return entity.getUserId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
