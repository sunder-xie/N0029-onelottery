package com.peersafe.chainbet.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;
import de.greenrobot.dao.internal.SqlUtils;


// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "ONE_LOTTERY_BET".
*/
public class OneLotteryBetDao extends AbstractDao<OneLotteryBet, String> {

    public static final String TABLENAME = "ONE_LOTTERY_BET";

    /**
     * Properties of entity OneLotteryBet.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property TicketId = new Property(0, String.class, "ticketId", true, "TICKET_ID");
        public final static Property LotteryName = new Property(1, String.class, "lotteryName", false, "LOTTERY_NAME");
        public final static Property AttendeeHash = new Property(2, String.class, "attendeeHash", false, "ATTENDEE_HASH");
        public final static Property AttendeeName = new Property(3, String.class, "attendeeName", false, "ATTENDEE_NAME");
        public final static Property BetNumbers = new Property(4, String.class, "betNumbers", false, "BET_NUMBERS");
        public final static Property BetCost = new Property(5, Long.class, "betCost", false, "BET_COST");
        public final static Property BetCount = new Property(6, Integer.class, "betCount", false, "BET_COUNT");
        public final static Property PrizeLevel = new Property(7, Integer.class, "prizeLevel", false, "PRIZE_LEVEL");
        public final static Property Bonus = new Property(8, Long.class, "bonus", false, "BONUS");
        public final static Property CreateTime = new Property(9, java.util.Date.class, "createTime", false, "CREATE_TIME");
        public final static Property LotteryId = new Property(10, String.class, "lotteryId", false, "LOTTERY_ID");
    };

    private DaoSession daoSession;


    public OneLotteryBetDao(DaoConfig config) {
        super(config);
    }

    public OneLotteryBetDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"ONE_LOTTERY_BET\" (" + //
                "\"TICKET_ID\" TEXT PRIMARY KEY NOT NULL ," + // 0: ticketId
                "\"LOTTERY_NAME\" TEXT," + // 1: lotteryName
                "\"ATTENDEE_HASH\" TEXT," + // 2: attendeeHash
                "\"ATTENDEE_NAME\" TEXT," + // 3: attendeeName
                "\"BET_NUMBERS\" TEXT," + // 4: betNumbers
                "\"BET_COST\" INTEGER," + // 5: betCost
                "\"BET_COUNT\" INTEGER," + // 6: betCount
                "\"PRIZE_LEVEL\" INTEGER," + // 7: prizeLevel
                "\"BONUS\" INTEGER," + // 8: bonus
                "\"CREATE_TIME\" INTEGER," + // 9: createTime
                "\"LOTTERY_ID\" TEXT);"); // 10: lotteryId
        // Add Indexes
        db.execSQL("CREATE INDEX " + constraint + "IDX_ONE_LOTTERY_BET_LOTTERY_ID ON ONE_LOTTERY_BET" +
                " (\"LOTTERY_ID\");");
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"ONE_LOTTERY_BET\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, OneLotteryBet entity) {
        stmt.clearBindings();
 
        String ticketId = entity.getTicketId();
        if (ticketId != null) {
            stmt.bindString(1, ticketId);
        }
 
        String lotteryName = entity.getLotteryName();
        if (lotteryName != null) {
            stmt.bindString(2, lotteryName);
        }
 
        String attendeeHash = entity.getAttendeeHash();
        if (attendeeHash != null) {
            stmt.bindString(3, attendeeHash);
        }
 
        String attendeeName = entity.getAttendeeName();
        if (attendeeName != null) {
            stmt.bindString(4, attendeeName);
        }
 
        String betNumbers = entity.getBetNumbers();
        if (betNumbers != null) {
            stmt.bindString(5, betNumbers);
        }
 
        Long betCost = entity.getBetCost();
        if (betCost != null) {
            stmt.bindLong(6, betCost);
        }
 
        Integer betCount = entity.getBetCount();
        if (betCount != null) {
            stmt.bindLong(7, betCount);
        }
 
        Integer prizeLevel = entity.getPrizeLevel();
        if (prizeLevel != null) {
            stmt.bindLong(8, prizeLevel);
        }
 
        Long bonus = entity.getBonus();
        if (bonus != null) {
            stmt.bindLong(9, bonus);
        }
 
        java.util.Date createTime = entity.getCreateTime();
        if (createTime != null) {
            stmt.bindLong(10, createTime.getTime());
        }
 
        String lotteryId = entity.getLotteryId();
        if (lotteryId != null) {
            stmt.bindString(11, lotteryId);
        }
    }

    @Override
    protected void attachEntity(OneLotteryBet entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public OneLotteryBet readEntity(Cursor cursor, int offset) {
        OneLotteryBet entity = new OneLotteryBet( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // ticketId
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // lotteryName
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // attendeeHash
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // attendeeName
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // betNumbers
            cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5), // betCost
            cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6), // betCount
            cursor.isNull(offset + 7) ? null : cursor.getInt(offset + 7), // prizeLevel
            cursor.isNull(offset + 8) ? null : cursor.getLong(offset + 8), // bonus
            cursor.isNull(offset + 9) ? null : new java.util.Date(cursor.getLong(offset + 9)), // createTime
            cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10) // lotteryId
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, OneLotteryBet entity, int offset) {
        entity.setTicketId(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setLotteryName(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setAttendeeHash(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setAttendeeName(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setBetNumbers(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setBetCost(cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5));
        entity.setBetCount(cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6));
        entity.setPrizeLevel(cursor.isNull(offset + 7) ? null : cursor.getInt(offset + 7));
        entity.setBonus(cursor.isNull(offset + 8) ? null : cursor.getLong(offset + 8));
        entity.setCreateTime(cursor.isNull(offset + 9) ? null : new java.util.Date(cursor.getLong(offset + 9)));
        entity.setLotteryId(cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10));
     }
    
    /** @inheritdoc */
    @Override
    protected String updateKeyAfterInsert(OneLotteryBet entity, long rowId) {
        return entity.getTicketId();
    }
    
    /** @inheritdoc */
    @Override
    public String getKey(OneLotteryBet entity) {
        if(entity != null) {
            return entity.getTicketId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getOneLotteryDao().getAllColumns());
            builder.append(" FROM ONE_LOTTERY_BET T");
            builder.append(" LEFT JOIN ONE_LOTTERY T0 ON T.\"LOTTERY_ID\"=T0.\"LOTTERY_ID\"");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected OneLotteryBet loadCurrentDeep(Cursor cursor, boolean lock) {
        OneLotteryBet entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        OneLottery oneLottery = loadCurrentOther(daoSession.getOneLotteryDao(), cursor, offset);
        entity.setOneLottery(oneLottery);

        return entity;    
    }

    public OneLotteryBet loadDeep(Long key) {
        assertSinglePk();
        if (key == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(getSelectDeep());
        builder.append("WHERE ");
        SqlUtils.appendColumnsEqValue(builder, "T", getPkColumns());
        String sql = builder.toString();
        
        String[] keyArray = new String[] { key.toString() };
        Cursor cursor = db.rawQuery(sql, keyArray);
        
        try {
            boolean available = cursor.moveToFirst();
            if (!available) {
                return null;
            } else if (!cursor.isLast()) {
                throw new IllegalStateException("Expected unique result, but count was " + cursor.getCount());
            }
            return loadCurrentDeep(cursor, true);
        } finally {
            cursor.close();
        }
    }
    
    /** Reads all available rows from the given cursor and returns a list of new ImageTO objects. */
    public List<OneLotteryBet> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<OneLotteryBet> list = new ArrayList<OneLotteryBet>(count);
        
        if (cursor.moveToFirst()) {
            if (identityScope != null) {
                identityScope.lock();
                identityScope.reserveRoom(count);
            }
            try {
                do {
                    list.add(loadCurrentDeep(cursor, false));
                } while (cursor.moveToNext());
            } finally {
                if (identityScope != null) {
                    identityScope.unlock();
                }
            }
        }
        return list;
    }
    
    protected List<OneLotteryBet> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<OneLotteryBet> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
