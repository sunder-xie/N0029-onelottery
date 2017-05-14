package db

import (
	"encoding/json"
	"errors"
	"os"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/op/go-logging"
)

func init() {
	format := logging.MustStringFormatter("%{time:15:04:05.000} [%{module}] %{level:.4s} : %{message}")
	backend := logging.NewLogBackend(os.Stderr, "", 0)
	backendFormatter := logging.NewBackendFormatter(backend, format)
	logging.SetBackend(backendFormatter).SetLevel(logging.DEBUG, "onechain.db")
}

type OldLotteryHistory struct {
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
	stub            shim.ChaincodeStubInterface
}

func (l *OldLotteryHistory) TableName() string {
	return "OldLotteryHistory"
}

func NewOldLotteryHistory(stub shim.ChaincodeStubInterface) *OldLotteryHistory {
	return &OldLotteryHistory{stub: stub}
}

func (l *OldLotteryHistory) CreateTable() error {
	if table, err := l.stub.GetTable(l.TableName()); table != nil && err == nil {
		logger.Debugf("Exist OldLotteryHistory table\n")
		return nil
	}
	err := l.stub.CreateTable(l.TableName(), []*shim.ColumnDefinition{
		&shim.ColumnDefinition{Name: "TxnID", Type: shim.ColumnDefinition_STRING, Key: true},
		&shim.ColumnDefinition{Name: "NewTxnID", Type: shim.ColumnDefinition_STRING, Key: false},
		&shim.ColumnDefinition{Name: "Version", Type: shim.ColumnDefinition_UINT32, Key: false},
		&shim.ColumnDefinition{Name: "LastCloseTime", Type: shim.ColumnDefinition_UINT64, Key: false},
		&shim.ColumnDefinition{Name: "Numbers", Type: shim.ColumnDefinition_STRING, Key: false},
		&shim.ColumnDefinition{Name: "Balance", Type: shim.ColumnDefinition_UINT64, Key: false},
		&shim.ColumnDefinition{Name: "PrizeTxnID", Type: shim.ColumnDefinition_STRING, Key: false},
		&shim.ColumnDefinition{Name: "CountTotal", Type: shim.ColumnDefinition_UINT32, Key: false},
		&shim.ColumnDefinition{Name: "PictureIndex", Type: shim.ColumnDefinition_UINT32, Key: false},
		&shim.ColumnDefinition{Name: "Status", Type: shim.ColumnDefinition_UINT32, Key: false},
		&shim.ColumnDefinition{Name: "UpdateTime", Type: shim.ColumnDefinition_UINT64, Key: false},
		&shim.ColumnDefinition{Name: "BlockHeight", Type: shim.ColumnDefinition_UINT64, Key: false},
		&shim.ColumnDefinition{Name: "PrevBlockHeight", Type: shim.ColumnDefinition_UINT64, Key: false},
		&shim.ColumnDefinition{Name: "TxnIDs", Type: shim.ColumnDefinition_STRING, Key: false},
		&shim.ColumnDefinition{Name: "CreateTime", Type: shim.ColumnDefinition_UINT64, Key: false},
	})

	if err != nil {
		logger.Errorf("Failed creating LotteryHistory table, %v\n", err.Error())
		return errors.New("Failed creating LotteryHistory table." + err.Error())
	}

	return nil
}

// Add OldLotteryHistory
func (l *OldLotteryHistory) Add() error {
	logger.Debugf("Start Add OldLotteryHistory")

	logger.Debugf("[AddOldLotteryHistory] id=%v\n", l.TxnID)

	logger.Debugf("History = %v\n", *l)

	// check value
	// err := l.checkValue()
	// if err != nil {
	// 	logger.Errorf("[AddLotteryHistory] checkValue error=%v\n", err)
	// 	return err
	// }

	// insert to db
	ok, err := l.stub.InsertRow(l.TableName(), oldHistoryToRow(l))
	if !ok && err == nil {
		logger.Errorf("Add OldLotteryHistory InsertRow error,LotteryHistory was already exist.\n")
		return errors.New("Add OldLotteryHistory was already exist.")
	}
	if err != nil {
		logger.Errorf("Add OldLotteryHistory InsertRow error=%v.\n", err)
		return err
	}
	logger.Debugf("End Add OldLotteryHistory.\n")
	return err
}

// Get one LotteryHistory
func (l *OldLotteryHistory) GetOneByID(id string) (*OldLotteryHistory, error) {
	logger.Debugf("[GetOldLotteryHistory] id=%v\n", id)
	// do get
	row, err := l.stub.GetRow(l.TableName(), []shim.Column{shim.Column{Value: &shim.Column_String_{String_: id}}})
	if err != nil {
		logger.Debugf("[GetOldLotteryHistory] get row err=%v\n", err)
		return nil, errors.New("Failed get OldLotteryHistory row data.")
	}
	if len(row.Columns) == 0 {
		logger.Debug("[GetOldLotteryHistory] get row nil")
		return nil, errors.New("Failed get OldLotteryHistory row not exists.")
	}
	LotteryHistory := rowToOldHistory(row, l.stub)
	return LotteryHistory, nil
}

func getOldHistoryColumn(l *OldLotteryHistory) []*shim.Column {
	return []*shim.Column{
		&shim.Column{Value: &shim.Column_String_{String_: l.TxnID}},
		&shim.Column{Value: &shim.Column_String_{String_: l.NewTxnID}},
		&shim.Column{Value: &shim.Column_Uint32{Uint32: l.Version}},
		&shim.Column{Value: &shim.Column_Uint64{Uint64: l.LastCloseTime}},
		&shim.Column{Value: &shim.Column_String_{String_: l.Numbers}},
		&shim.Column{Value: &shim.Column_Uint64{Uint64: l.Balance}},
		&shim.Column{Value: &shim.Column_String_{String_: l.PrizeTxnID}},
		&shim.Column{Value: &shim.Column_Uint32{Uint32: l.CountTotal}},
		&shim.Column{Value: &shim.Column_Uint32{Uint32: l.PictureIndex}},
		&shim.Column{Value: &shim.Column_Uint32{Uint32: l.Status}},
		&shim.Column{Value: &shim.Column_Uint64{Uint64: l.UpdateTime}},
		&shim.Column{Value: &shim.Column_Uint64{Uint64: l.BlockHeight}},
		&shim.Column{Value: &shim.Column_Uint64{Uint64: l.PrevBlockHeight}},
		&shim.Column{Value: &shim.Column_String_{String_: l.TxnIDs}},
		&shim.Column{Value: &shim.Column_Uint64{Uint64: l.CreateTime}},
	}

}

func rowToOldHistory(row shim.Row, stub shim.ChaincodeStubInterface) *OldLotteryHistory {
	return &OldLotteryHistory{
		TxnID:           row.Columns[0].GetString_(),
		NewTxnID:        row.Columns[1].GetString_(),
		Version:         row.Columns[2].GetUint32(),
		LastCloseTime:   row.Columns[3].GetUint64(),
		Numbers:         row.Columns[4].GetString_(),
		Balance:         row.Columns[5].GetUint64(),
		PrizeTxnID:      row.Columns[6].GetString_(),
		CountTotal:      row.Columns[7].GetUint32(),
		PictureIndex:    row.Columns[8].GetUint32(),
		Status:          row.Columns[9].GetUint32(),
		UpdateTime:      row.Columns[10].GetUint64(),
		BlockHeight:     row.Columns[11].GetUint64(),
		PrevBlockHeight: row.Columns[12].GetUint64(),
		TxnIDs:          row.Columns[13].GetString_(),
		CreateTime:      row.Columns[14].GetUint64(),
		stub:            stub,
	}
}

func oldHistoryToRow(l *OldLotteryHistory) shim.Row {
	return shim.Row{
		Columns: getOldHistoryColumn(l),
	}
}

func hotteryToOldHistory(l *LotteryHistory) *OldLotteryHistory {
	row := historyToRow(l)
	return rowToOldHistory(row, l.stub)
}

// check value of struct RulePrize
func (l *OldLotteryHistory) checkValue() error {
	if l.TxnID == "" ||
		l.LastCloseTime == 0 ||
		l.Numbers == "" ||
		l.PrizeTxnID == "" ||
		l.CountTotal == 0 ||
		l.Status == 0 ||
		l.BlockHeight == 0 ||
		l.PrevBlockHeight == 0 ||
		l.TxnIDs == "" ||
		l.CreateTime == 0 {
		return errors.New("parameter error")
	}
	return nil
}

// unmarshal args to struct RulePrize
func (l *OldLotteryHistory) Unmarshal(args string) error {
	// parse json to struct
	err := json.Unmarshal([]byte(args), l)
	if err != nil {
		return err
	}
	return nil
}
