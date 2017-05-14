package handler

const (
	TRANSFER_COST             = "transfer_cost"
	LOTTERY_CREATE_COST       = "lottery_create_cost"
	LOTTERY_MODIFY_COST       = "lottery_modify_cost"
	HISTORY_LOTTERY_KEEP_TIME = "history_lottery_keep_time"
)

/*************************** keep fee struct ***************************/
type NormalFee struct {
	CreateLotteryFee uint64
	ModefyLotteryFee uint64
	TransferFee      uint64
}

/*************************** request struct ***************************/
type ZxCoinResponse struct {
	Code        int         `json:"code"`
	CodeMessage string      `json:"message"`
	Data        interface{} `json:"data"`
}

/*************************** init ***************************/
type ZxCoinInitRequest struct {
	InitAmount             uint64 `json:"initAmount"`
	AdminUserId            string `json:"adminUserId"`
	Time                   uint64 `json:"time"`
	TransferCost           uint64 `json:"transferCost"`
	DeployLotteryCost      uint64 `json:"deployLotteryCost"`
	ModifyLotteryCost      uint64 `json:"modifyLotteryCost"`
	HistoryLotteryKeepTime uint64 `json:"historyLotteryKeepTime"`
}

/*************************** tansfer ***************************/
type ZxCoinTransferRequest struct {
	UserId     string `json:"userID"`
	NameTo     string `json:"nameTo"`
	UserCertTo string `json:"userCertTo"`
	Extras     string `json:"Extras"`
	Amount     uint64 `json:"amount"`
	Fee        uint64 `json:"fee"`
	Type       uint32 `json:"type"` // 0:normal transfer and need minus fee towards， 1:consume
	Remark     string `json:"remark"`
	Time       uint64 `json:"time"`
}

type ZxCoinTransferResponse struct {
	Owner          string `json:"owner"`
	OwnUserId      string `json:"ownUserId"`
	Oppisite       string `json:"oppisite"`
	OppisiteUserId string `json:"oppisiteUserId"`
	Extras         string `json:"Extras"`
	Amount         uint64 `json:"amount"`
	Fee            uint64 `json:"fee"`
}

/*************************** query balance ***************************/
type ZxCoinQueryBalanceRequest struct {
	UserCert string `json:"userCert"`
}

type ZxCoinQueryBalanceResponse struct {
	Balance         uint64 `json:"balance"`
	Reserved        uint64 `json:"reserved"`
	UserId          string `json:"userId"`
	BlockHeight     uint64 `json:"blockHeight"`
	TxnIDs          string `json:"txnIDs"`
	PrevBlockHeight uint64 `json:"prevBlockHeight"`
}

/*************************** frezon ***************************/
type ZxCoinFrezonRequest struct {
	UserId string `json:"userID"`
	Amount int64  `json:"amount"`
	Time   uint64 `json:"time"`
	Type   uint64 `json:"type"`
	Remark string `json:"remark"`
}

type ZxCoinFrezonResponse struct {
	Amount int64  `json:"amount"`
	Type   uint64 `json:"type"`
}

/*************************** refund ***************************/
type ZxCoinLotteryRefundRequest struct {
	TransactionID []string `json:"transactionID"`
	Amount        []uint64 `json:"amount"`
	Attendee      []string `json:"attendee"`
	Publisher     string   `json:"publisher"`
	Remark        string   `json:"remark"`
	Time          uint64   `json:"time"`
	LotteryID     string   `json:"lotteryID"`
}

type ZxCoinBetRequest struct {
	Amount uint64 `json:"amount"`
}

type ZxCoinLotteryRefundResponse struct {
	Array       []*RefundObj `json:"array"`
	LotteryID   string       `json:"lotteryID"`
	CurrentTime uint64       `json:"currentTime"`
}

type RefundObj struct {
	Owner    string `json:"owner"`
	Oppisite string `json:"oppisite"`
	Amount   uint64 `json:"amount"`
}

/*************************** get user hash or user id ***************************/
type ZxCoinGetUserInfoRequest struct {
	Owner  string `json:"owner"`
	UserId string `json:"userId"`
}

type ZxCoinGetUserInfoResponse struct {
	Owner  string `json:"owner"`
	UserId string `json:"userId"`
}

/*************************** set user name ***************************/
type ZxCoinSetUserNameRequest struct {
	Name string `json:"name"`
}

/*************************** request payload ***************************/
type Payload struct {
	args []string `json:"args"`
}

//>>>>>>>>>>>>>>>>>>>>>About Withdraw<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

type WithdrawApply struct {
	AccountInfo string `json:"AccountInfo"` //账户信息
	UserId      string `json:"UserId"`      //要提现的用户在一元链上的用户名
	Amount      uint64 `json:"Amount"`      //提现对应的众享币金额
	ModifyTime  uint64 `json:"ModifyTime"`  //修改时间
}

type WithdrawUserInfo struct {
	UserName string `json:"UserName"`
	UserHash string `json:"UserHash"`
}

type WithdrawTxInfo struct {
	TxId       string `json:"TxId"`       //初始发起提现请求对应的交易id
	ModifyTime uint64 `json:"ModifyTime"` //修改时间
	WithdrawUserInfo
	Extras string `json:"Extras"` //附加信息(操作员等)
}

type WithdrawRemark struct {
	WithdrawTxInfo
	Remark string `json:"Remark"` //提现失败的原因
}

type WithdrawSuccess struct {
	WithdrawTxInfo
	RemitOrderNumber string `json:"RemitOrderNumber"` //Web后台打款的订单号
}

type WithdrawAppealDone struct {
	WithdrawTxInfo
	Remark string `json:"Remark"` //提现失败的原因
	Result uint32 `json:"Result"` //申诉处理结果：1 申诉驳回 2 申诉接受
}

type WithdrawRecordInfo struct {
	TxId  string `json:"TxId"`  //初始发起提现请求对应的交易id
	State uint32 `json:"State"` //提现的状态: 1 申请中 2 处理中 3 已打款 4 已确认 5 已撤销 6 提现失败（账户输错等） 7 申诉中
}

const (
	Withdraw_pending    = iota + 1 //申请中
	Withdraw_hasPaid               //已打款
	Withdraw_hasConfirm            //已确认
	Withdraw_Recall                //申请撤回
	Withdraw_failed                //提现失败
	Withdraw_appeal                //申诉中
)

type ConfigPair struct {
	Key string
	Val uint64
}
