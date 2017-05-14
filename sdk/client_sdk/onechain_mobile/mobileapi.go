package onechainmobile

import (
	"fmt"
	"time"

	"github.com/hyperledger/fabric/core/crypto/primitives"
	"peersafe.com/onelottery/sdk/client_sdk"
	"peersafe.com/onelottery/sdk/client_sdk/onechain"
)

const (
	SUCCESS      = 0
	PARA_ERR     = -1
	INTERNAL_ERR = -2

	DEPLOY_SECURE_NOT_LOGIN = -3

	REGISTR_ALREADY_EXIST = 1 //The user has already registered.

	ENROLL_ALREADY_EXIST = 2 //The user has already enrolled.
)

func init() {
	onechain.IsClient = true
}

type OneChainCallBack interface {
	OneChainTransferCallback(retMsg string, txId string)
	OneChainZxCoinWithdraw(retMsg string, txId string)
	OneChainZxCoinWithdrawRecall(retMsg string, txId string)
	OneChainZxCoinWithdrawFail(retMsg string, txId string)
	OneChainZxCoinWithdrawRemitSucces(retMsg string, txId string)
	OneChainZxCoinWithdrawConfirm(retMsg string, txId string)
	OneChainZxCoinWithdrawAppeal(retMsg string, txId string)
	OneChainZxCoinWithdrawAppealDone(retMsg string, txId string)

	// onechain callback
	OneChainPrizeRuleAddCallback(retMsg string, txId string)
	OneChainPrizeRuleModifyCallback(retMsg string, txId string)
	OneChainPrizeRuleDeleteCallback(retMsg string, txId string)
	OneChainOneLotteryAddCallback(retMsg string, txId string)
	OneChainOneLotteryModifyCallback(retMsg string, txId string)
	OneChainOneLotteryDeleteCallback(retMsg string, txId string)
	OneChainOneLotteryBetCallback(retMsg string, txId string)
	OneChainOneLotteryOpenRewardCallback(retMsg string, txId string)
	OneChainOneLotteryRefundCallback(retMsg string, txId string)

	OneChainDeaemonStartNotify(result int)
	OneChainDeaemonShutDownNotify()
	OneChainTransferNotify(retMsg string, txId string)
	OneChainPrizeRuleAddNotify(retMsg string, txId string)
	OneChainPrizeRuleModifyNotify(retMsg string, txId string)
	OneChainPrizeRuleDeleteNotify(retMsg string, txId string)
	OneChainOneLotteryAddNotify(retMsg string, txId string)
	OneChainOneLotteryModifyNotify(retMsg string, txId string)
	OneChainOneLotteryDeleteNotify(retMsg string, txId string)
	OneChainOneLotteryBetNotify(retMsg string, txId string)
	OneChainOneLotteryOpenRewardNotify(retMsg string, txId string)
	OneChainOneLotteryRefundNotify(retMsg string, txId string)

	OneChainZxCoinWithdrawApplyNotify(retMsg string, txId string)
	OneChainZxCoinWithdrawFailNotify(retMsg string, txId string)
	OneChainZxCoinWithdrawRemitSuccesNotify(retMsg string, txId string)
	OneChainZxCoinWithdrawConfirmNotify(retMsg string, txId string)
	OneChainZxCoinWithdrawAppealDoneNotify(retMsg string, txId string)
}

var (
	cmdSep string = "&X&"
)

/**
 * Description Init one chain
 *
 * @param configPath      The client_sdk.yaml dir
 * @param fileSystemPath  The location where file will put in.
 * @return ret ret>=0:success
 */
func OneChainInit(configPath string, fileSystemPath string) (ret int) {
	return onechain.OneChainInit(configPath, fileSystemPath, "client_sdk", false)
}

/**
 * Description Start the daemon to listen event.Must call this method in a single thread
 *
 * @param configPath      The client_sdk.yaml dir
 * @param fileSystemPath  The location where file will put in.
 * @return ret ret>=0:success
 */
func OneChainDaemon(callBack OneChainCallBack) (ret int) {
	return onechain.OneChainDaemon(callBack)
}

func OneChainDaemonStop() {
	onechain.OneChainDaemonStop()
}

/**
 * Description Register a user
 *
 * @param userId The account of a user to register.
 * @param affiliation The affiliation of a user to register.
 * @return ret&X&enrollPwd  ret>=0:success
 */
func OneChainRegister(userId string, affiliation string) (retMsg string) {
	ret, enrollId := onechain.OneChainRegister(0, userId, affiliation)
	return fmt.Sprintf("%d%s%s", ret, cmdSep, enrollId)
}

/**
* Description enroll a user
*
* @param enrollID
* @param enrollPWD
* @param pwd
* @return ret ret>=0:success
 */
func OneChainEnroll(enrollID string, enrollPWD string, pwd string) (ret int) {
	return onechain.OneChainEnroll(enrollID, enrollPWD, []byte(pwd))
}

/**
 * Description Register and then enroll a user
 *
 * @param userId The account of a user to register.
 * @param affiliation The affiliation of a user to register.
 * @param pwd
 * @return ret ret>=0:success
 */
func OneChainRegisterAndEnroll(userId string, affiliation string, pwd string) (ret int) {
	ret = onechain.OneChainRegisterAndEnroll(0, userId, affiliation, []byte(pwd))
	if client_sdk.REGISTR_ALREADY_EXIST == ret ||
		client_sdk.REGISTR_ALREADY_EXIST_IN_CHAIN == ret ||
		client_sdk.ENROLL_ALREADY_EXIST == ret {
		ret = client_sdk.LOGIN_USER_ALREADY_EXIST
	}
	return ret
}

/**
 * Description Get the user public key hash.
 *
 * @param userId The account of a user.
 * @return ret&X&pubHash  ret>=0:success
 */
func OneChainGetPubkeyHash(userId string) (retMsg string) {
	ret, pubHash := onechain.OneChainGetPubkeyHash(userId)
	return fmt.Sprintf("%d%s%s", ret, cmdSep, pubHash)
}

/**
* Description Onechain transfer account.
*
* @param nameTo The account of the user transfer to
* @param addressTo The address(public key hash) to transfer account.
* @param amount The amount of zxcoin to transfer account.
* @param transferType // 0:normal transfer and need minus fee towards， 1:consume
* @param remark
* @param time The time of transfer account.
* @param pwd
* @return ret&X&txId  ret>=0:success
 */
func OneChainTransferAccount(nameTo string, addressTo string,
	amount int64, transferType int32, remark string, time int64, pwd string) (retMsg string) {

	ret, txId := onechain.OneChainTransferAccount(onechain.CurrentUserId, "", nameTo, addressTo,
		uint64(amount), uint32(transferType), remark, uint64(time), []byte(pwd))

	return fmt.Sprintf("%d%s%s", ret, cmdSep, txId)
}

/**
 * Description Query the zxcoin balance.
 *
 * @return retMsg The return json msg of query
 */
func OneChainBalanceQuery() (retMsg string) {
	return onechain.OneChainBalanceQuery(onechain.CurrentUserId)
}

/**
 * Description withdraw from balance.
 *
 * @param platform the platform of payment.1 weixin ; 2 zhifubao
 * @param accountName the userName of payment platform
 * @param accountId   the userid of payment platform
 * @param amount      The amount of zxcoin to withdraw account.
 * @param pwd
 * @return ret&X&txId  ret>=0:success
 */
func OneChainZxCoinWithdraw(bankName, accountName, accountId string, amount int64, pwd string) string {
	ret, txId := onechain.ZxCoinWithdraw(bankName, accountName, accountId, onechain.CurrentUserId, uint64(amount), []byte(pwd))
	return fmt.Sprintf("%d%s%s", ret, cmdSep, txId)
}

/**
 * Description Recall the withdraw transaction of the txid.
 *
 * @param txid
 * @param pwd
 * @return ret&X&txId  ret>=0:success
 */
// func OneChainZxCoinWithdrawRecall(txid, pwd string) string {
// 	ret, txId := onechain.ZxCoinWithdrawRecall(txid, onechain.CurrentUserId, []byte(pwd))
// 	return fmt.Sprintf("%d%s%s", ret, cmdSep, txId)
// }

/**
 * Description Confirm the withdraw transaction of the txid.
 *
 * @param txid
 * @param pwd
 * @return ret&X&txId  ret>=0:success
 */
func OneChainZxCoinWithdrawConfirm(txid, pwd string) string {
	ret, txId := onechain.ZxCoinWithdrawConfirm(txid, onechain.CurrentUserId, "", []byte(pwd))
	return fmt.Sprintf("%d%s%s", ret, cmdSep, txId)
}

/**
 * Description Appeal the withdraw transaction of the txid.
 *
 * @param txid
 * @param remark the appeal information
 * @param pwd
 * @return ret&X&txId  ret>=0:success
 */
func OneChainZxCoinWithdrawAppeal(txid, remark, pwd string) string {
	ret, txId := onechain.ZxCoinWithdrawAppeal(txid, remark, onechain.CurrentUserId, []byte(pwd))
	return fmt.Sprintf("%d%s%s", ret, cmdSep, txId)
}

/**
 * Description query the withdraw transactions of the current user.
 *
 * @return The return json msg of withdraw query
 */
func OneChainZxCoinWithdrawQuery() string {
	return onechain.ZxCoinWithdrawQuery(onechain.CurrentUserId, onechain.CurrentUserId)
}

/**
 * Description query the withdraw transactions of the userName.
 * if userName is empty,query all the information.
 *
 * @param txid
 * @return The return json msg of withdraw detail query
 */
func OneChainZxCoinWithdrawInfoQuery(txId string) string {
	return onechain.ZxCoinWithdrawInfoQuery(txId, onechain.CurrentUserId)
}

/**
* Description GetTxInfoByTxId
*
* @param txId
* @return ret&X&payload
 */
func OneChainGetTxInfoByTxId(txId string) (retMsg string) {
	_, payload := onechain.GetTxInfoByTxId(txId)
	return payload
}

/**
* Description OneLottery PrizeRule query.
*
* @return retMsg The return json msg of query
 */
func OneChainPrizeRuleQuery() (retMsg string) {
	return onechain.OneChainPrizeRuleQuery(onechain.CurrentUserId)
}

/**
* Description OneLottery activity add
*
* @param name The activity name.
* @param ruleType The rule type. "1":prize rule "2":ball rule
* @param ruleId The rule id.
* @param pictureIndex lottery pic index
* @param createTime
* @param startTime
* @param closeTime
* @param minAttendeeCnt
* @param maxAttendeeCnt  0:no limit
* @param cost
* @param description
* @param pwd
* @return ret&X&txId  ret>=0:success
 */
func OneChainOneLotteryAdd(name string, ruleType string,
	ruleId string, pictureIndex int32, createTime int64, startTime int64,
	closeTime int64, minAttendeeCnt int32, maxAttendeeCnt int32, cost int32,
	total int64, description string, pwd string) (retMsg string) {

	ret, txId := onechain.OneChainOneLotteryAdd(onechain.CurrentUserId, name, ruleType,
		ruleId, uint32(pictureIndex), uint64(createTime), uint64(startTime), uint64(closeTime),
		uint32(minAttendeeCnt), uint32(maxAttendeeCnt),
		uint32(cost), uint64(total), description, []byte(pwd))

	return fmt.Sprintf("%d%s%s", ret, cmdSep, txId)
}

/**
* Description OneLottery activity modify
*
* @param lotteryId The lottery activity id.
* @param name The activity name.
* @param ruleType The rule type. "1":prize rule "2":ball rule
* @param ruleId The rule id.
* @param pictureIndex lottery pic index
* @param updateTime User can modify the activity before it starts.
* @param startTime
* @param closeTime
* @param minAttendeeCnt
* @param maxAttendeeCnt  0:no limit
* @param cost
* @param description
* @param pwd
* @return ret&X&txId  ret>=0:success
 */
func OneChainOneLotteryModify(lotteryId string, name string, ruleType string,
	ruleId string, pictureIndex int32, updateTime int64, startTime int64, closeTime int64,
	minAttendeeCnt int32, maxAttendeeCnt int32, cost int32,
	total int64, description string, pwd string) (retMsg string) {

	ret, txId := onechain.OneChainOneLotteryModify(onechain.CurrentUserId, lotteryId, name, ruleType,
		ruleId, uint32(pictureIndex), uint64(updateTime), uint64(startTime), uint64(closeTime),
		uint32(minAttendeeCnt), uint32(maxAttendeeCnt),
		uint32(cost), uint64(total), description, []byte(pwd))
	return fmt.Sprintf("%d%s%s", ret, cmdSep, txId)
}

/**
* Description OneLottery activity delete
*
* @param lotteryId The lottery activity id.
* @param pwd
* @return ret&X&txId  ret>=0:success
 */
func OneChainOneLotteryDelete(lotteryId string, pwd string) (retMsg string) {
	ret, txId := onechain.OneChainOneLotteryDelete(onechain.CurrentUserId, lotteryId, []byte(pwd))
	return fmt.Sprintf("%d%s%s", ret, cmdSep, txId)
}

/**
* Description OneLottery activity query.
*
* @param lotteryId The lottery activity id. (Query all not closed activity if lottery id is null)
* @return retMsg The return json msg of query
 */
func OneChainOneLotteryQuery(lotteryId string) (retMsg string) {
	return onechain.OneChainOneLotteryQuery(onechain.CurrentUserId, lotteryId)
}

/**
* Description OneLottery history activity query.
*
* @param lotteryId The lottery activity id. (Query all not closed activity if lottery id is null)
* @return retMsg The return json msg of query
 */
func OneChainOneLotteryHistoryQuery(lotteryId string) (retMsg string) {
	return onechain.OneChainOneLotteryHistoryQuery(onechain.CurrentUserId, lotteryId)
}

/**
* Description OneLottery bet.
*
* @param amount
* @param lotteryId
* @param count
* @param createTime
* @param pwd
* @return ret&X&txId  ret>=0:success
 */
func OneChainOneLotteryBet(amount int64, lotteryId string,
	count int32, createTime int64, pwd string) (retMsg string) {

	ret, txId := onechain.OneChainOneLotteryBet(onechain.CurrentUserId, uint64(amount), lotteryId,
		uint32(count), uint64(createTime), []byte(pwd))

	return fmt.Sprintf("%d%s%s", ret, cmdSep, txId)
}

/**
* Description OneLottery bet query.
*
* @param toQueryUser
* @param lotteryId
* @return retMsg The return json msg of query
 */
func OneChainOneLotteryBetQuery(toQueryUser string, lotteryId string) (retMsg string) {
	return onechain.OneChainOneLotteryBetQuery(onechain.CurrentUserId, toQueryUser, lotteryId)
}

/**
* Description OneLottery open reward. Use default admin user to open,and not need pwd.
*
* @param lotteryId
* @return ret&X&txId  ret>=0:success
 */
func OneChainOpenReward(lotteryId string) (retMsg string) {
	time := time.Now().UnixNano() / 1000000
	ret, txId := onechain.OneChainOpenReward(client_sdk.GetRegistrarId(), lotteryId, uint64(time), nil)

	return fmt.Sprintf("%d%s%s", ret, cmdSep, txId)
}

/**
* Description OneChainOneLotteryRefund
*
* @param lotteryId
* @param refundTime
* @param pwd
* @return ret&X&txId  ret>=0:success
 */
func OneChainOneLotteryRefund(lotteryId string, refundTime int64, pwd string) (retMsg string) {
	ret, txId := onechain.OneChainOneLotteryRefund(onechain.CurrentUserId, lotteryId, uint64(refundTime), []byte(pwd))

	return fmt.Sprintf("%d%s%s", ret, cmdSep, txId)
}

/**
* Description Get user hash by user name or get user name by hash.
*
* @param userName The name to query.
* @param userAddress The hash to query.
* @return retMsg The return json msg of query
 */
func OneChainZxCoinGetUserInfo(userName string, userAddress string) (retMsg string) {
	ret, val := onechain.OneChainZxCoinGetUserInfo(onechain.CurrentUserId, userName, userAddress)
	return fmt.Sprintf("%d%s%s", ret, cmdSep, val)
}

/**
* Description Get ticket numbers by ticket id.
*
* @param ticketID The ticket id to query.
* @return retMsg The return json msg of query
 */
func OneChainGetTicketNumbers(ticketID string) (retMsg string) {
	return onechain.OneChainGetTicketNumbers(onechain.CurrentUserId, ticketID)
}

func OneChainSetCurUser(userId string) {
	onechain.CurrentUserId = userId
}

func OneChainGetCurUser() (userId string) {
	return onechain.CurrentUserId
}

func OneChainUserLogin(userId string, pwd string) (retCode int) {
	return onechain.OneChainUserLogin(userId, pwd)
}

func OneChainCheckCurUserPwd(pwd string) (retCode int) {
	return onechain.OneChainUserLogin(onechain.CurrentUserId, pwd)
}

func OneChainCheckUserPwd(userId string, pwd string) (retCode int) {
	return onechain.OneChainCheckUser(userId, pwd)
}

/**
* Description get user balance table by block num
*
* @param blockNumber
* @return ret&X&retValue
 */
func OneChainGetZXCoinStateByBlockID(blockNumber int64) (retMsg string) {
	_, pubHash := onechain.OneChainGetPubkeyHash(onechain.CurrentUserId)
	ret, retValue := onechain.GetStateByBlockID(uint64(blockNumber), pubHash, 0)
	return fmt.Sprintf("%d%s%s", ret, cmdSep, string(retValue))
}

/**
* Description get history lottery table by block num and lottery id
*
* @param blockNumber
* @param lotteryId
* @return ret&X&retValue
 */
func OneChainGetLotteryStateByBlockID(blockNumber int64, lotteryId string) (retMsg string) {
	ret, retValue := onechain.GetStateByBlockID(uint64(blockNumber), lotteryId, 1)
	return fmt.Sprintf("%d%s%s", ret, cmdSep, string(retValue))
}

func OneChainEncrypt(key string, origData []byte) []byte {
	result, err := primitives.AesEncrypt(origData, []byte(key))
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainEncrypt,error:%s", err.Error())
		return nil
	}

	return result
}

func OneChainDecrypt(key string, encryptData []byte) []byte {
	result, err := primitives.AesDecrypt(encryptData, []byte(key))
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainDecrypt,error:%s", err.Error())
		return nil
	}

	return result
}

/**
* Description OneLottery old history activity query.
*
* @param lotteryId The lottery activity id. (Query deleted in db activity)
* @return retMsg The return json msg of query
 */
func OneChainOneLotteryOldHistoryQuery(lotteryId string) (retMsg string) {
	return onechain.OneChainOneLotteryOldHistoryQuery(onechain.CurrentUserId, lotteryId)
}
