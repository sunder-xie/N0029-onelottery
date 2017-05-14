/*
Copyright IBM Corp. 2016 All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

		 http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package onechain

/*************************** zxcoin table ***************************/
type ZxCoinTable struct {
	Owner           string `json:"Owner"`           // hash(public key)
	Name            string `json:"Name"`            // user name
	Balance         uint64 `json:"Balance"`         // user balance
	Reserved        uint64 `json:"Reserved"`        // user reserved (used in user participate in activities)
	TxnIDs          string `json:"TxnIDs"`          // the transation id that modefy this table in current block
	BlockHeight     uint64 `json:"BlockHeight"`     // current block height that modefied this table
	PrevBlockHeight uint64 `json:"PrevBlockHeight"` // last block height that modefied this table
}

/*************************** history lottery table ***************************/
type LotteryHistory struct {
	TxnID           string `json:"txnID"`          // 创建该活交易id，主键
	NewTxnID        string `json:"newTxnID"`       // 修改此活动的交易id，默认为空
	Version         uint32 `json:"version"`        // 活动的版本号，修改活动、投注及开奖后，值都加1
	LastCloseTime   uint64 `json:"lastCloseTime"`  // 活动最后的共识关闭时间
	Numbers         string `json:"numbers"`        // 活动中奖号码
	Balance         uint64 `json:"balance"`        // 活动募集到的总金额
	PrizeTxnID      string `json:"prizeTxnID"`     // 中奖的Ticket.TxnID
	CountTotal      uint32 `json:"countTotal"`     // 活动当前已总注数
	PictureIndex    uint32 `json:"pictureIndex"`   // 活动所使用的图片编号
	Status          uint32 `json:"status"`         // 0未开始 1进行中 2可开奖 3开奖中 4已开奖 5退款中 6已退款 7 失败(无人投注，到期关闭)
	UpdateTime      uint64 `json:"updateTime"`     // 活动被修改时间（活动创建、修改、投注、开奖、退款时都更新此字段值为当前时间（每次开奖/退款后此记录移动至历史表））
	BlockHeight     uint64 `json:"blockHeight"`    // 当前交易集合所在的区块序号，默认0，当交易进来时的实际BlockHeight和当前行的BlockHeight不一样是，设置curRow. PrevBlockHeight=curRow. BlockHeight, curRow. BlockHeight=Txn.BlockHeight
	PrevBlockHeight uint64 `json:"preBlockHeight"` // 上次交易集合所在的区块序号，默认0
	TxnIDs          string `json:"txnIDs"`         // 当前区块中投注的交易ID集合，默认空，当交易进来时的实际BlockHeight和当前行的BlockHeight不一样是，先置空TxnIDs
	CreateTime      uint64 `json:"createTime"`     // 活动移至历史活动表的时间
}

/*************************** normal fee struct ***************************/
type NormalFee struct {
	CreateLotteryFee uint64
	ModefyLotteryFee uint64
	TransferFee      uint64
}

/*************************** request struct ***************************/
type ZxCoinQueryUserInfoResponse struct {
	Code        int                       `json:"code"`
	CodeMessage string                    `json:"message"`
	Data        ZxCoinGetUserInfoResponse `json:"data"`
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
	UserId     string `json:"userID"` // user id
	NameTo     string `json:"nameTo"`
	UserCertTo string `json:"userCertTo"`
	Extras     string `json:"Extras"`
	Amount     uint64 `json:"amount"`
	Fee        uint64 `json:"fee"`
	Type       uint32 `json:"type"` // 0:normal transfer and need minus fee towards， 1:consume
	Remark     string `json:"remark"`
	Time       uint64 `json:"time"`
}

type ZxCoinTransferRespData struct {
	Owner    string `json:"owner"`
	Oppisite string `json:"oppisite"`
	Amount   uint64 `json:"amount"`
	Fee      uint64 `json:"fee"`
}

type ZxCoinTransferResponse struct {
	Code        int                    `json:"code"`
	CodeMessage string                 `json:"message"`
	Data        ZxCoinTransferRespData `json:"data"`
}

/*************************** query balance ***************************/
type ZxCoinQueryBalanceRequest struct {
	UserCert string `json:"userCert"`
}

type ZxCoinQueryBalanceResponse struct {
	Balance         uint64 `json:"Balance"`
	Reserved        uint64 `json:"Reserved"`
	UserId          string `json:"Name"`
	BlockHeight     uint64 `json:"BlockHeight"`
	TxnIDs          string `json:"TxnIDs"`
	PrevBlockHeight uint64 `json:"PrevBlockHeight"`
	Owner           string `json:"Owner"`
}

/*************************** frezon ***************************/
type ZxCoinFrezonRequest struct {
	Amount int64  `json:"amount"`
	Time   uint64 `json:"time"`
	Type   uint64 `json:"type"`
	Remark string `json:"remark"`
	UserId string `json:"userID"`
}

type ZxCoinFrezonResponse struct {
	Amount int64  `json:"amount"`
	Type   uint64 `json:"type"`
}

/*************************** get user hash or user id ***************************/
type ZxCoinGetUserInfoRequest struct {
	Owner  string `json:"owner"`
	UserId string `json:"userId"`
}

/************************** the Withdraw requst****************************************/
type AccountInfo struct {
	BankName    string `json:"BankName"`
	AccountName string `json:"AccountName"`
	AccountId   string `json:"AccountId"`
}

type WithdrawApply struct {
	AccountInfo string `json:"AccountInfo"` //账户信息
	UserId      string `json:"UserId"`      //要提现的用户在一元链上的用户名
	Amount      uint64 `json:"Amount"`      //提现对应的众享币金额
	ModifyTime  uint64 `json:"ModifyTime"`  //修改时间
}

type WithdrawTxInfo struct {
	TxId       string `json:"TxId"`       //初始发起提现请求对应的交易id
	ModifyTime uint64 `json:"ModifyTime"` //修改时间
	Extras     string `json:"Extras"` //附加信息(操作员等)
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

type ZxCoinResponse struct {
	Code        int         `json:"code"`
	CodeMessage string      `json:"message"`
	Data        interface{} `json:"data"`
}

type WithdrawUserInfo struct {
	UserName string `json:"UserName"`
	UserHash string `json:"UserHash"`
}

type ConfigPair struct {
	Key string
	Val uint64
}

/******************************************************************/

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

/*************************** rulePrize request ***************************/
type RulePrizeAddRequest struct {
	Percentage uint64 `json:"percentage"` // 中奖者所占比例
	Hide       bool   `json:"hide"`       // 是否隐藏
	CreateTime uint64 `json:"createTime"` // create time
}

type RulePrizeModifyRequest struct {
	ID         string `json:"ruleID"`     // 抽奖类规则id
	Percentage uint64 `json:"percentage"` // 中奖者所占比例
	Hide       bool   `json:"hide"`       // 是否隐藏
}

type RulePrizeDeleteRequest struct {
	ID string `json:"ruleId"` // 抽奖类规则id
}

/*************************** lottery request ***************************/
type OnelotteryAddRequest struct {
	Name           string `json:"name"`           // 活动名称
	Fee            uint64 `json:"fee"`            // 手续费
	PublisherName  string `json:"publisherName"`  // 活动发布者名字
	RuleType       string `json:"ruleType"`       // 活动对应的规则类型
	RuleID         string `json:"ruleId"`         // 活动对应的规则id
	PictureIndex   uint32 `json:"pictureIndex"`   // 活动所使用的图片编号
	CreateTime     uint64 `json:"createTime"`     // 活动创建时间
	StartTime      uint64 `json:"startTime"`      // 活动开始时间
	CloseTime      uint64 `json:"closeTime"`      // 活动关闭时间
	MinAttendeeCnt uint32 `json:"minAttendeeCnt"` // 活动最少参与人数（必填且大于0）
	MaxAttendeeCnt uint32 `json:"maxAttendeeCnt"` // 活动最大参与人数（必须大于0）
	Cost           uint32 `json:"cost"`           // 参与活动的单次费用或单次最小金额
	Total          uint64 `json:total`            //活动要募集的总金额
	Description    string `json:"description"`    // 活动介绍
}

type OnelotteryModifyRequest struct {
	ID             string `json:"txnID"`          // 活动id
	Name           string `json:"name"`           // 活动名称
	Fee            uint64 `json:"fee"`            // 手续费
	PublisherName  string `json:"publisherName"`  // 活动发布者名字
	RuleType       string `json:"ruleType"`       // 活动对应的规则类型
	PictureIndex   uint32 `json:"pictureIndex"`   // 活动所使用的图片编号
	RuleID         string `json:"ruleId"`         // 活动对应的规则id
	UpdateTime     uint64 `json:"updateTime"`     // 活动创建时间
	StartTime      uint64 `json:"startTime"`      // 活动开始时间
	CloseTime      uint64 `json:"closeTime"`      // 活动关闭时间
	MinAttendeeCnt uint32 `json:"minAttendeeCnt"` // 活动最少参与人数（必填且大于0）
	MaxAttendeeCnt uint32 `json:"maxAttendeeCnt"` // 活动最大参与人数（必须大于0）
	Cost           uint32 `json:"cost"`           // 参与活动的单次费用或单次最小金额
	Total          uint64 `json:total`            //活动要募集的总金额
	Description    string `json:"description"`    // 活动介绍
}

type OnelotteryQueryRequest struct {
	ID string `json:"txnID"` // 活动id
}

type OneLotteryHistoryQueryRequest struct {
	ID string `json:"txnID"` // 活动id
}

type OnelotteryDeleteRequest struct {
	ID         string `json:"txnID"`      // 活动id
	UpdateTime uint64 `json:"updateTime"` // 删除活动时间
}

/*************************** lottery bet request ***************************/
type OnelotteryBetRequest struct {
	LotteryID  string `json:"lotteryID"` // 对应的活动id
	Amount     uint64 `json:"amount"`    // 投注众享币的数量，真实金额*10000
	Count      uint32 `json:"count"`
	UserId     string `json:"userID"` //投注者的名字
	CreateTime uint64 `json:"createTime'`
}

type onelotteryBetQueryRequest struct {
	Attendee  string `json:"attendee"`
	LotteryId string `json:"lotteryID"`
}

type OnelotteryGetTicketNumbersRequest struct {
	LotteryId string `json:"lotteryID"`
}

type OnelotteryOpenRewardRequest struct {
	LotteryId   string `json:"lotteryID"`
	CurrentTime uint64 `json:"currentTime"`
}

type OnelotteryRefundRequest struct {
	LotteryID   string `json:"lotteryID"`
	CurrentTime uint64 `json:"currentTime"`
}

type OnelotteryBetOverRequest struct {
	LotteryID   string `json:"lotteryID"`
	CurrentTime uint64 `json:"currentTime"`
}

type OneLotteryRefundRequest struct {
	LotteryID   string `json:"lotteryID"`
	CurrentTime uint64 `json:"currentTime"`
}

/*************************** ruleBall request ***************************/
type ruleBallAddReq struct {
	Name        string `json:"name"`        // 规则名称
	Percentage1 uint32 `json:"percentage1"` // 一等奖所占比例
	Percentage2 uint32 `json:"percentage2"` // 二等奖所占比例
	Percentage3 uint32 `json:"percentage3"` // 三等奖所占比例
}

type ruleBallModifyReq struct {
	ID          string `json:"ruleId"`      // 球类规则id
	Name        string `json:"name"`        // 规则名称
	Percentage1 uint32 `json:"percentage1"` // 一等奖所占比例
	Percentage2 uint32 `json:"percentage2"` // 二等奖所占比例
	Percentage3 uint32 `json:"percentage3"` // 三等奖所占比例
}

type ruleBallDeleteReq struct {
	ID string `json:"ruleId"` // 球类规则id
}
