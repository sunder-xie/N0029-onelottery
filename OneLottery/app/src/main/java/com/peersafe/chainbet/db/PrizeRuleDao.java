package com.peersafe.chainbet.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "PRIZE_RULE".
*/
public class PrizeRuleDao extends AbstractDao<PrizeRule, String> {

    public static final String TABLENAME = "PRIZE_RULE";

    /**
     * Properties of entity PrizeRule.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property RuleId = new Property(0, String.class, "ruleId", true, "RULE_ID");
        public final static Property RuleName = new Property(1, String.class, "ruleName", false, "RULE_NAME");
        public final static Property Percentage = new Property(2, Integer.class, "percentage", false, "PERCENTAGE");
        public final static Property UpdateFlag = new Property(3, String.class, "updateFlag", false, "UPDATE_FLAG");
        public final static Property IsHidden = new Property(4, Boolean.class, "isHidden", false, "IS_HIDDEN");
    };


    public PrizeRuleDao(DaoConfig config) {
        super(config);
    }
    
    public PrizeRuleDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"PRIZE_RULE\" (" + //
                "\"RULE_ID\" TEXT PRIMARY KEY NOT NULL ," + // 0: ruleId
                "\"RULE_NAME\" TEXT," + // 1: ruleName
                "\"PERCENTAGE\" INTEGER," + // 2: percentage
                "\"UPDATE_FLAG\" TEXT," + // 3: updateFlag
                "\"IS_HIDDEN\" INTEGER);"); // 4: isHidden
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"PRIZE_RULE\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, PrizeRule entity) {
        stmt.clearBindings();
 
        String ruleId = entity.getRuleId();
        if (ruleId != null) {
            stmt.bindString(1, ruleId);
        }
 
        String ruleName = entity.getRuleName();
        if (ruleName != null) {
            stmt.bindString(2, ruleName);
        }
 
        Integer percentage = entity.getPercentage();
        if (percentage != null) {
            stmt.bindLong(3, percentage);
        }
 
        String updateFlag = entity.getUpdateFlag();
        if (updateFlag != null) {
            stmt.bindString(4, updateFlag);
        }
 
        Boolean isHidden = entity.getHidden();
        if (isHidden != null) {
            stmt.bindLong(5, isHidden ? 1L: 0L);
        }
    }

    /** @inheritdoc */
    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public PrizeRule readEntity(Cursor cursor, int offset) {
        PrizeRule entity = new PrizeRule( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // ruleId
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // ruleName
            cursor.isNull(offset + 2) ? null : cursor.getInt(offset + 2), // percentage
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // updateFlag
            cursor.isNull(offset + 4) ? null : cursor.getShort(offset + 4) != 0 // isHidden
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, PrizeRule entity, int offset) {
        entity.setRuleId(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setRuleName(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setPercentage(cursor.isNull(offset + 2) ? null : cursor.getInt(offset + 2));
        entity.setUpdateFlag(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setHidden(cursor.isNull(offset + 4) ? null : cursor.getShort(offset + 4) != 0);
     }
    
    /** @inheritdoc */
    @Override
    protected String updateKeyAfterInsert(PrizeRule entity, long rowId) {
        return entity.getRuleId();
    }
    
    /** @inheritdoc */
    @Override
    public String getKey(PrizeRule entity) {
        if(entity != null) {
            return entity.getRuleId();
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
