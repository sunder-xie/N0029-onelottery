package com.peersafe.chainbet.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;


// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "ONE_LOTTERY".
*/
public class OneLotteryDao extends AbstractDao<OneLottery, String> {

    public static final String TABLENAME = "ONE_LOTTERY";

    /**
     * Properties of entity OneLottery.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property LotteryId = new Property(0, String.class, "lotteryId", true, "LOTTERY_ID");
        public final static Property NewTxId = new Property(1, String.class, "newTxId", false, "NEW_TX_ID");
        public final static Property Version = new Property(2, Integer.class, "version", false, "VERSION");
        public final static Property LotteryName = new Property(3, String.class, "lotteryName", false, "LOTTERY_NAME");
        public final static Property RuleType = new Property(4, String.class, "ruleType", false, "RULE_TYPE");
        public final static Property RuleId = new Property(5, String.class, "ruleId", false, "RULE_ID");
        public final static Property PublisherHash = new Property(6, String.class, "publisherHash", false, "PUBLISHER_HASH");
        public final static Property PublisherName = new Property(7, String.class, "publisherName", false, "PUBLISHER_NAME");
        public final static Property CreateTime = new Property(8, java.util.Date.class, "createTime", false, "CREATE_TIME");
        public final static Property UpdateTime = new Property(9, java.util.Date.class, "updateTime", false, "UPDATE_TIME");
        public final static Property StartTime = new Property(10, java.util.Date.class, "startTime", false, "START_TIME");
        public final static Property CloseTime = new Property(11, java.util.Date.class, "closeTime", false, "CLOSE_TIME");
        public final static Property MinBetCount = new Property(12, Integer.class, "minBetCount", false, "MIN_BET_COUNT");
        public final static Property RewardCountDownTime = new Property(13, java.util.Date.class, "rewardCountDownTime", false, "REWARD_COUNT_DOWN_TIME");
        public final static Property MaxBetCount = new Property(14, Integer.class, "maxBetCount", false, "MAX_BET_COUNT");
        public final static Property OneBetCost = new Property(15, Long.class, "oneBetCost", false, "ONE_BET_COST");
        public final static Property Description = new Property(16, String.class, "description", false, "DESCRIPTION");
        public final static Property LastCloseTime = new Property(17, java.util.Date.class, "lastCloseTime", false, "LAST_CLOSE_TIME");
        public final static Property CurBetAmount = new Property(18, Long.class, "curBetAmount", false, "CUR_BET_AMOUNT");
        public final static Property BetTotalAmount = new Property(19, Long.class, "betTotalAmount", false, "BET_TOTAL_AMOUNT");
        public final static Property CurBetCount = new Property(20, Integer.class, "curBetCount", false, "CUR_BET_COUNT");
        public final static Property RewardNumbers = new Property(21, String.class, "rewardNumbers", false, "REWARD_NUMBERS");
        public final static Property State = new Property(22, Integer.class, "state", false, "STATE");
        public final static Property BetTxnIDs = new Property(23, String.class, "betTxnIDs", false, "BET_TXN_IDS");
        public final static Property CurBlockHeight = new Property(24, Long.class, "curBlockHeight", false, "CUR_BLOCK_HEIGHT");
        public final static Property PrevBlockHeight = new Property(25, Long.class, "prevBlockHeight", false, "PREV_BLOCK_HEIGHT");
        public final static Property LastUpadteBlockHeight = new Property(26, Long.class, "lastUpadteBlockHeight", false, "LAST_UPADTE_BLOCK_HEIGHT");
        public final static Property UpdateFlag = new Property(27, String.class, "updateFlag", false, "UPDATE_FLAG");
        public final static Property PictureIndex = new Property(28, Integer.class, "pictureIndex", false, "PICTURE_INDEX");
        public final static Property Progress = new Property(29, Integer.class, "progress", false, "PROGRESS");
        public final static Property PrizeTxID = new Property(30, String.class, "prizeTxID", false, "PRIZE_TX_ID");
    };


    public OneLotteryDao(DaoConfig config) {
        super(config);
    }
    
    public OneLotteryDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"ONE_LOTTERY\" (" + //
                "\"LOTTERY_ID\" TEXT PRIMARY KEY NOT NULL ," + // 0: lotteryId
                "\"NEW_TX_ID\" TEXT," + // 1: newTxId
                "\"VERSION\" INTEGER," + // 2: version
                "\"LOTTERY_NAME\" TEXT," + // 3: lotteryName
                "\"RULE_TYPE\" TEXT," + // 4: ruleType
                "\"RULE_ID\" TEXT," + // 5: ruleId
                "\"PUBLISHER_HASH\" TEXT," + // 6: publisherHash
                "\"PUBLISHER_NAME\" TEXT," + // 7: publisherName
                "\"CREATE_TIME\" INTEGER," + // 8: createTime
                "\"UPDATE_TIME\" INTEGER," + // 9: updateTime
                "\"START_TIME\" INTEGER," + // 10: startTime
                "\"CLOSE_TIME\" INTEGER," + // 11: closeTime
                "\"MIN_BET_COUNT\" INTEGER," + // 12: minBetCount
                "\"REWARD_COUNT_DOWN_TIME\" INTEGER," + // 13: rewardCountDownTime
                "\"MAX_BET_COUNT\" INTEGER," + // 14: maxBetCount
                "\"ONE_BET_COST\" INTEGER," + // 15: oneBetCost
                "\"DESCRIPTION\" TEXT," + // 16: description
                "\"LAST_CLOSE_TIME\" INTEGER," + // 17: lastCloseTime
                "\"CUR_BET_AMOUNT\" INTEGER," + // 18: curBetAmount
                "\"BET_TOTAL_AMOUNT\" INTEGER," + // 19: betTotalAmount
                "\"CUR_BET_COUNT\" INTEGER," + // 20: curBetCount
                "\"REWARD_NUMBERS\" TEXT," + // 21: rewardNumbers
                "\"STATE\" INTEGER," + // 22: state
                "\"BET_TXN_IDS\" TEXT," + // 23: betTxnIDs
                "\"CUR_BLOCK_HEIGHT\" INTEGER," + // 24: curBlockHeight
                "\"PREV_BLOCK_HEIGHT\" INTEGER," + // 25: prevBlockHeight
                "\"LAST_UPADTE_BLOCK_HEIGHT\" INTEGER," + // 26: lastUpadteBlockHeight
                "\"UPDATE_FLAG\" TEXT," + // 27: updateFlag
                "\"PICTURE_INDEX\" INTEGER," + // 28: pictureIndex
                "\"PROGRESS\" INTEGER," + // 29: progress
                "\"PRIZE_TX_ID\" TEXT);"); // 30: prizeTxID
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"ONE_LOTTERY\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, OneLottery entity) {
        stmt.clearBindings();
 
        String lotteryId = entity.getLotteryId();
        if (lotteryId != null) {
            stmt.bindString(1, lotteryId);
        }
 
        String newTxId = entity.getNewTxId();
        if (newTxId != null) {
            stmt.bindString(2, newTxId);
        }
 
        Integer version = entity.getVersion();
        if (version != null) {
            stmt.bindLong(3, version);
        }
 
        String lotteryName = entity.getLotteryName();
        if (lotteryName != null) {
            stmt.bindString(4, lotteryName);
        }
 
        String ruleType = entity.getRuleType();
        if (ruleType != null) {
            stmt.bindString(5, ruleType);
        }
 
        String ruleId = entity.getRuleId();
        if (ruleId != null) {
            stmt.bindString(6, ruleId);
        }
 
        String publisherHash = entity.getPublisherHash();
        if (publisherHash != null) {
            stmt.bindString(7, publisherHash);
        }
 
        String publisherName = entity.getPublisherName();
        if (publisherName != null) {
            stmt.bindString(8, publisherName);
        }
 
        java.util.Date createTime = entity.getCreateTime();
        if (createTime != null) {
            stmt.bindLong(9, createTime.getTime());
        }
 
        java.util.Date updateTime = entity.getUpdateTime();
        if (updateTime != null) {
            stmt.bindLong(10, updateTime.getTime());
        }
 
        java.util.Date startTime = entity.getStartTime();
        if (startTime != null) {
            stmt.bindLong(11, startTime.getTime());
        }
 
        java.util.Date closeTime = entity.getCloseTime();
        if (closeTime != null) {
            stmt.bindLong(12, closeTime.getTime());
        }
 
        Integer minBetCount = entity.getMinBetCount();
        if (minBetCount != null) {
            stmt.bindLong(13, minBetCount);
        }
 
        java.util.Date rewardCountDownTime = entity.getRewardCountDownTime();
        if (rewardCountDownTime != null) {
            stmt.bindLong(14, rewardCountDownTime.getTime());
        }
 
        Integer maxBetCount = entity.getMaxBetCount();
        if (maxBetCount != null) {
            stmt.bindLong(15, maxBetCount);
        }
 
        Long oneBetCost = entity.getOneBetCost();
        if (oneBetCost != null) {
            stmt.bindLong(16, oneBetCost);
        }
 
        String description = entity.getDescription();
        if (description != null) {
            stmt.bindString(17, description);
        }
 
        java.util.Date lastCloseTime = entity.getLastCloseTime();
        if (lastCloseTime != null) {
            stmt.bindLong(18, lastCloseTime.getTime());
        }
 
        Long curBetAmount = entity.getCurBetAmount();
        if (curBetAmount != null) {
            stmt.bindLong(19, curBetAmount);
        }
 
        Long betTotalAmount = entity.getBetTotalAmount();
        if (betTotalAmount != null) {
            stmt.bindLong(20, betTotalAmount);
        }
 
        Integer curBetCount = entity.getCurBetCount();
        if (curBetCount != null) {
            stmt.bindLong(21, curBetCount);
        }
 
        String rewardNumbers = entity.getRewardNumbers();
        if (rewardNumbers != null) {
            stmt.bindString(22, rewardNumbers);
        }
 
        Integer state = entity.getState();
        if (state != null) {
            stmt.bindLong(23, state);
        }
 
        String betTxnIDs = entity.getBetTxnIDs();
        if (betTxnIDs != null) {
            stmt.bindString(24, betTxnIDs);
        }
 
        Long curBlockHeight = entity.getCurBlockHeight();
        if (curBlockHeight != null) {
            stmt.bindLong(25, curBlockHeight);
        }
 
        Long prevBlockHeight = entity.getPrevBlockHeight();
        if (prevBlockHeight != null) {
            stmt.bindLong(26, prevBlockHeight);
        }
 
        Long lastUpadteBlockHeight = entity.getLastUpadteBlockHeight();
        if (lastUpadteBlockHeight != null) {
            stmt.bindLong(27, lastUpadteBlockHeight);
        }
 
        String updateFlag = entity.getUpdateFlag();
        if (updateFlag != null) {
            stmt.bindString(28, updateFlag);
        }
 
        Integer pictureIndex = entity.getPictureIndex();
        if (pictureIndex != null) {
            stmt.bindLong(29, pictureIndex);
        }
 
        Integer progress = entity.getProgress();
        if (progress != null) {
            stmt.bindLong(30, progress);
        }
 
        String prizeTxID = entity.getPrizeTxID();
        if (prizeTxID != null) {
            stmt.bindString(31, prizeTxID);
        }
    }

    /** @inheritdoc */
    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public OneLottery readEntity(Cursor cursor, int offset) {
        OneLottery entity = new OneLottery( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // lotteryId
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // newTxId
            cursor.isNull(offset + 2) ? null : cursor.getInt(offset + 2), // version
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // lotteryName
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // ruleType
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // ruleId
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // publisherHash
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // publisherName
            cursor.isNull(offset + 8) ? null : new java.util.Date(cursor.getLong(offset + 8)), // createTime
            cursor.isNull(offset + 9) ? null : new java.util.Date(cursor.getLong(offset + 9)), // updateTime
            cursor.isNull(offset + 10) ? null : new java.util.Date(cursor.getLong(offset + 10)), // startTime
            cursor.isNull(offset + 11) ? null : new java.util.Date(cursor.getLong(offset + 11)), // closeTime
            cursor.isNull(offset + 12) ? null : cursor.getInt(offset + 12), // minBetCount
            cursor.isNull(offset + 13) ? null : new java.util.Date(cursor.getLong(offset + 13)), // rewardCountDownTime
            cursor.isNull(offset + 14) ? null : cursor.getInt(offset + 14), // maxBetCount
            cursor.isNull(offset + 15) ? null : cursor.getLong(offset + 15), // oneBetCost
            cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16), // description
            cursor.isNull(offset + 17) ? null : new java.util.Date(cursor.getLong(offset + 17)), // lastCloseTime
            cursor.isNull(offset + 18) ? null : cursor.getLong(offset + 18), // curBetAmount
            cursor.isNull(offset + 19) ? null : cursor.getLong(offset + 19), // betTotalAmount
            cursor.isNull(offset + 20) ? null : cursor.getInt(offset + 20), // curBetCount
            cursor.isNull(offset + 21) ? null : cursor.getString(offset + 21), // rewardNumbers
            cursor.isNull(offset + 22) ? null : cursor.getInt(offset + 22), // state
            cursor.isNull(offset + 23) ? null : cursor.getString(offset + 23), // betTxnIDs
            cursor.isNull(offset + 24) ? null : cursor.getLong(offset + 24), // curBlockHeight
            cursor.isNull(offset + 25) ? null : cursor.getLong(offset + 25), // prevBlockHeight
            cursor.isNull(offset + 26) ? null : cursor.getLong(offset + 26), // lastUpadteBlockHeight
            cursor.isNull(offset + 27) ? null : cursor.getString(offset + 27), // updateFlag
            cursor.isNull(offset + 28) ? null : cursor.getInt(offset + 28), // pictureIndex
            cursor.isNull(offset + 29) ? null : cursor.getInt(offset + 29), // progress
            cursor.isNull(offset + 30) ? null : cursor.getString(offset + 30) // prizeTxID
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, OneLottery entity, int offset) {
        entity.setLotteryId(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setNewTxId(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setVersion(cursor.isNull(offset + 2) ? null : cursor.getInt(offset + 2));
        entity.setLotteryName(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setRuleType(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setRuleId(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setPublisherHash(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setPublisherName(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setCreateTime(cursor.isNull(offset + 8) ? null : new java.util.Date(cursor.getLong(offset + 8)));
        entity.setUpdateTime(cursor.isNull(offset + 9) ? null : new java.util.Date(cursor.getLong(offset + 9)));
        entity.setStartTime(cursor.isNull(offset + 10) ? null : new java.util.Date(cursor.getLong(offset + 10)));
        entity.setCloseTime(cursor.isNull(offset + 11) ? null : new java.util.Date(cursor.getLong(offset + 11)));
        entity.setMinBetCount(cursor.isNull(offset + 12) ? null : cursor.getInt(offset + 12));
        entity.setRewardCountDownTime(cursor.isNull(offset + 13) ? null : new java.util.Date(cursor.getLong(offset + 13)));
        entity.setMaxBetCount(cursor.isNull(offset + 14) ? null : cursor.getInt(offset + 14));
        entity.setOneBetCost(cursor.isNull(offset + 15) ? null : cursor.getLong(offset + 15));
        entity.setDescription(cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16));
        entity.setLastCloseTime(cursor.isNull(offset + 17) ? null : new java.util.Date(cursor.getLong(offset + 17)));
        entity.setCurBetAmount(cursor.isNull(offset + 18) ? null : cursor.getLong(offset + 18));
        entity.setBetTotalAmount(cursor.isNull(offset + 19) ? null : cursor.getLong(offset + 19));
        entity.setCurBetCount(cursor.isNull(offset + 20) ? null : cursor.getInt(offset + 20));
        entity.setRewardNumbers(cursor.isNull(offset + 21) ? null : cursor.getString(offset + 21));
        entity.setState(cursor.isNull(offset + 22) ? null : cursor.getInt(offset + 22));
        entity.setBetTxnIDs(cursor.isNull(offset + 23) ? null : cursor.getString(offset + 23));
        entity.setCurBlockHeight(cursor.isNull(offset + 24) ? null : cursor.getLong(offset + 24));
        entity.setPrevBlockHeight(cursor.isNull(offset + 25) ? null : cursor.getLong(offset + 25));
        entity.setLastUpadteBlockHeight(cursor.isNull(offset + 26) ? null : cursor.getLong(offset + 26));
        entity.setUpdateFlag(cursor.isNull(offset + 27) ? null : cursor.getString(offset + 27));
        entity.setPictureIndex(cursor.isNull(offset + 28) ? null : cursor.getInt(offset + 28));
        entity.setProgress(cursor.isNull(offset + 29) ? null : cursor.getInt(offset + 29));
        entity.setPrizeTxID(cursor.isNull(offset + 30) ? null : cursor.getString(offset + 30));
     }
    
    /** @inheritdoc */
    @Override
    protected String updateKeyAfterInsert(OneLottery entity, long rowId) {
        return entity.getLotteryId();
    }
    
    /** @inheritdoc */
    @Override
    public String getKey(OneLottery entity) {
        if(entity != null) {
            return entity.getLotteryId();
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
