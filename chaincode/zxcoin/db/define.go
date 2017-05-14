package db

import (
	"github.com/hyperledger/fabric/core/chaincode/shim"
)

type ZxCoin struct {
	stub        shim.ChaincodeStubInterface
	ZxCoinTable ZxCoinTable // zxcoin table
}

type ZxCoinTable struct {
	Owner           string `json:"Owner"`           // hash(public key)
	Name            string `json:"Name"`            // user name
	Balance         uint64 `json:"Balance"`         // user balance
	Reserved        uint64 `json:"Reserved"`        // user reserved (used in user participate in activities)
	TxnIDs          string `json:"TxnIDs"`          // the transation id that modefy this table in current block
	BlockHeight     uint64 `json:"BlockHeight"`     // current block height that modefied this table
	PrevBlockHeight uint64 `json:"PrevBlockHeight"` // last block height that modefied this table
}

type WithdrawHandle struct {
	Stub shim.ChaincodeStubInterface
	User string
	Hash string
}

type WithdrawInfo struct {
	TxId             string `json:"TxId"`             //提现请求对应的交易id
	State            uint32 `json:"State"`            //提现的状态: 1 申请中 2 处理中 3 已打款 4 已确认 5 已撤销 6 提现失败（账户输错等） 7 申诉中
	AccountInfo      string `json:"AccountInfo"`      //账户信息
	Amount           uint64 `json:"Amount"`           //提现对应的众享币金额
	RemitOrderNumber string `json:"RemitOrderNumber"` //Web后台打款的订单号
	Remark           string `json:"Remark"`           //说明，用于提现失败的时候填写
	ModifyTime       uint64 `json:"ModifyTime"`       //交易最后修改时间
	CreateTime       uint64 `json:"CreateTime"`       //交易创建时间
}

type WithdrawDetialInfo struct {
	WithdrawInfo
	WithdrawUserInfo
}

type WithdrawUserInfo struct {
	UserName string `json:"UserName"` //用户名
	UserHash string `json:"UserHash"` //用户Hash
}
