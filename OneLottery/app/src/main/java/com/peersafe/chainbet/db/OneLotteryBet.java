package com.peersafe.chainbet.db;

import de.greenrobot.dao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "ONE_LOTTERY_BET".
 */
public class OneLotteryBet {

    private String ticketId;
    private String lotteryName;
    private String attendeeHash;
    private String attendeeName;
    private String betNumbers;
    private Long betCost;
    private Integer betCount;
    private Integer prizeLevel;
    private Long bonus;
    private java.util.Date createTime;
    private String lotteryId;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient OneLotteryBetDao myDao;

    private OneLottery oneLottery;
    private String oneLottery__resolvedKey;


    public OneLotteryBet() {
    }

    public OneLotteryBet(String ticketId) {
        this.ticketId = ticketId;
    }

    public OneLotteryBet(String ticketId, String lotteryName, String attendeeHash, String attendeeName, String betNumbers, Long betCost, Integer betCount, Integer prizeLevel, Long bonus, java.util.Date createTime, String lotteryId) {
        this.ticketId = ticketId;
        this.lotteryName = lotteryName;
        this.attendeeHash = attendeeHash;
        this.attendeeName = attendeeName;
        this.betNumbers = betNumbers;
        this.betCost = betCost;
        this.betCount = betCount;
        this.prizeLevel = prizeLevel;
        this.bonus = bonus;
        this.createTime = createTime;
        this.lotteryId = lotteryId;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getOneLotteryBetDao() : null;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getLotteryName() {
        return lotteryName;
    }

    public void setLotteryName(String lotteryName) {
        this.lotteryName = lotteryName;
    }

    public String getAttendeeHash() {
        return attendeeHash;
    }

    public void setAttendeeHash(String attendeeHash) {
        this.attendeeHash = attendeeHash;
    }

    public String getAttendeeName() {
        return attendeeName;
    }

    public void setAttendeeName(String attendeeName) {
        this.attendeeName = attendeeName;
    }

    public String getBetNumbers() {
        return betNumbers;
    }

    public void setBetNumbers(String betNumbers) {
        this.betNumbers = betNumbers;
    }

    public Long getBetCost() {
        return betCost;
    }

    public void setBetCost(Long betCost) {
        this.betCost = betCost;
    }

    public Integer getBetCount() {
        return betCount;
    }

    public void setBetCount(Integer betCount) {
        this.betCount = betCount;
    }

    public Integer getPrizeLevel() {
        return prizeLevel;
    }

    public void setPrizeLevel(Integer prizeLevel) {
        this.prizeLevel = prizeLevel;
    }

    public Long getBonus() {
        return bonus;
    }

    public void setBonus(Long bonus) {
        this.bonus = bonus;
    }

    public java.util.Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(java.util.Date createTime) {
        this.createTime = createTime;
    }

    public String getLotteryId() {
        return lotteryId;
    }

    public void setLotteryId(String lotteryId) {
        this.lotteryId = lotteryId;
    }

    /** To-one relationship, resolved on first access. */
    public OneLottery getOneLottery() {
        String __key = this.lotteryId;
        if (oneLottery__resolvedKey == null || oneLottery__resolvedKey != __key) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            OneLotteryDao targetDao = daoSession.getOneLotteryDao();
            OneLottery oneLotteryNew = targetDao.load(__key);
            synchronized (this) {
                oneLottery = oneLotteryNew;
            	oneLottery__resolvedKey = __key;
            }
        }
        return oneLottery;
    }

    public void setOneLottery(OneLottery oneLottery) {
        synchronized (this) {
            this.oneLottery = oneLottery;
            lotteryId = oneLottery == null ? null : oneLottery.getLotteryId();
            oneLottery__resolvedKey = lotteryId;
        }
    }

    /** Convenient call for {@link AbstractDao#delete(Object)}. Entity must attached to an entity context. */
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.delete(this);
    }

    /** Convenient call for {@link AbstractDao#update(Object)}. Entity must attached to an entity context. */
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.update(this);
    }

    /** Convenient call for {@link AbstractDao#refresh(Object)}. Entity must attached to an entity context. */
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.refresh(this);
    }

}