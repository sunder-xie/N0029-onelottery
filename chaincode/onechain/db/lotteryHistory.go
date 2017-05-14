package db

import (
	"encoding/json"
	"errors"
	"fmt"
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

type LotteryHistory struct {
	OldLotteryHistory
	Name           string `json:"name"`           // 活动名称
	RuleType       string `json:"ruleType"`       // 规则类型，对应具体规则的表名
	RuleID         string `json:"ruleID"`         // 具体规则表的ID
	PublisherHash  string `json:"publisherHash"`  // 活动的发布者的公钥hash
	PublisherName  string `json:"publisherName"`  // 活动的发布者的用户名
	StartTime      uint64 `json:"startTime"`      // 开始时间
	CloseTime      uint64 `json:"closeTime"`      // 结束时间
	MinAttendeeCnt uint32 `json:"minAttendeeCnt"` // 最小参与人数
	MaxAttendeeCnt uint32 `json:"maxAttendeeCnt"` // 最大参与人数
	Cost           uint64 `json:"cost"`           // 参与活动的单次费用或单次最小金额
	Description    string `json:"description"`    // 活动描述
	Fee            uint64 `json:"fee"`            // 发布活动手续费（需判断是否小于增加活动的最小手续费金额）
}

func (l *LotteryHistory) TableName() string {
	return "LotteryHistory"
}

func NewLotteryHistory(stub shim.ChaincodeStubInterface) *LotteryHistory {
	lottery := &LotteryHistory{}
	lottery.stub = stub
	return lottery
}

func (l *LotteryHistory) CreateTable() error {
	if table, err := l.stub.GetTable(l.TableName()); table != nil && err == nil {
		logger.Debugf("Exist LotteryHistory table\n")
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
		&shim.ColumnDefinition{Name: "Name", Type: shim.ColumnDefinition_STRING, Key: false},
		&shim.ColumnDefinition{Name: "RuleType", Type: shim.ColumnDefinition_STRING, Key: false},
		&shim.ColumnDefinition{Name: "RuleID", Type: shim.ColumnDefinition_STRING, Key: false},
		&shim.ColumnDefinition{Name: "PublisherHash", Type: shim.ColumnDefinition_STRING, Key: false},
		&shim.ColumnDefinition{Name: "PublisherName", Type: shim.ColumnDefinition_STRING, Key: false},
		&shim.ColumnDefinition{Name: "StartTime", Type: shim.ColumnDefinition_UINT64, Key: false},
		&shim.ColumnDefinition{Name: "CloseTime", Type: shim.ColumnDefinition_UINT64, Key: false},
		&shim.ColumnDefinition{Name: "MinAttendeeCnt", Type: shim.ColumnDefinition_UINT32, Key: false},
		&shim.ColumnDefinition{Name: "MaxAttendeeCnt", Type: shim.ColumnDefinition_UINT32, Key: false},
		&shim.ColumnDefinition{Name: "Cost", Type: shim.ColumnDefinition_UINT64, Key: false},
		&shim.ColumnDefinition{Name: "Description", Type: shim.ColumnDefinition_STRING, Key: false},
	})

	if err != nil {
		logger.Errorf("Failed creating LotteryHistory table, %v\n", err.Error())
		return errors.New("Failed creating LotteryHistory table." + err.Error())
	}

	return nil
}

// Add LotteryHistory
func (l *LotteryHistory) Add() error {
	logger.Debugf("Start AddLotteryHistory\n")

	logger.Debugf("[AddLotteryHistory] id=%v\n", l.TxnID)

	logger.Debugf("History = %v\n", *l)

	// check value
	// err := l.checkValue()
	// if err != nil {
	// 	logger.Errorf("[AddLotteryHistory] checkValue error=%v\n", err)
	// 	return err
	// }

	// insert to db
	ok, err := l.stub.InsertRow(l.TableName(), historyToRow(l))
	if !ok && err == nil {
		logger.Errorf("AddLotteryHistory InsertRow error,LotteryHistory was already exist.\n")
		return errors.New("LotteryHistory was already exist.")
	}
	if err != nil {
		logger.Errorf("AddLotteryHistory InsertRow error=%v.\n", err)
		return err
	}
	logger.Debugf("End AddLotteryHistory.\n")
	return err
}

// Delete LotteryHistory
func (l *LotteryHistory) Delete() error {
	logger.Debugf("Start DeleteLotteryHistory\n")
	// check id
	if l.TxnID == "" {
		logger.Errorf("TxnID is nil.\n")
		return errors.New("TxnID is nil.")
	}
	// do delete
	err := l.stub.DeleteRow(
		l.TableName(),
		[]shim.Column{shim.Column{Value: &shim.Column_String_{String_: l.TxnID}}},
	)

	// do old history add
	history := hotteryToOldHistory(l)
	history.CreateTime = l.UpdateTime

	err = history.Add()
	if err != nil {
		logger.Errorf("DeleteLotteryHistory err: %v\n", err)
		return err
	}

	ticket := NewTicket(l.stub)
	if err = ticket.DeleteTable(l.TxnID); err != nil {
		logger.Errorf("DeleteTiecktHistory err: %v\n", err)
		return err
	}

	logger.Debugf("End DeleteLotteryHistory error=%v\n", err)
	return err
}

func (l *LotteryHistory) Update() error {
	// do update
	ok, err := l.stub.ReplaceRow(l.TableName(), historyToRow(l))
	if !ok && err == nil {
		logger.Errorf("UpdateState LotteryHistory was does not exist.\n")
	}

	if err != nil {
		return errors.New("UpdateState failed!" + err.Error())
	}
	return nil
}

// Get one LotteryHistory
func (l *LotteryHistory) GetOneByID(id string) (*LotteryHistory, error) {
	logger.Debugf("[GetLotteryHistory] id=%v\n", id)
	// do get
	row, err := l.stub.GetRow(l.TableName(), []shim.Column{shim.Column{Value: &shim.Column_String_{String_: id}}})
	if err != nil || len(row.Columns) == 0 {
		logger.Debugf("[GetLotteryHistory] get row err=%v\n", err)
		return nil, errors.New("Failed get LotteryHistory row data.")
	}
	LotteryHistory := rowToHistory(row, l.stub)
	return LotteryHistory, nil
}

// Get all LotteryHistorys
func (l *LotteryHistory) GetAll() ([]*SimpleLottery, error) {
	logger.Debugf("Start GetLotteryHistorys\n")
	rowChannel, err := l.stub.GetRows(l.TableName(), []shim.Column{})
	if err != nil {
		logger.Debugf("GetLotteryHistorys,err = %v\n", err)
		return nil, fmt.Errorf("GetLotteryHistorys failed, %s", err)
	}

	var historys []*SimpleLottery
	for {
		select {
		case row, ok := <-rowChannel:
			if !ok {
				rowChannel = nil
			} else {
				history := rowToHistory(row, l.stub)
				historys = append(historys, &SimpleLottery{
					TxnID:         history.TxnID,
					NewTxnID:      history.NewTxnID,
					Name:          history.Name,
					Version:       history.Version,
					PublisherHash: history.PublisherHash,
					PublisherName: history.PublisherName,
				})
				logger.Debugf("Gethistorys range row,history = %v\n", *history)
			}
		}
		if rowChannel == nil {
			break
		}
	}

	if len(historys) == 0 {
		return nil, nil
	}

	logger.Debugf("End GetLotteryHistorys\n")
	return historys, nil
}

// Get no finished lotterys
func (l *LotteryHistory) GetNoDeleteLotterys() ([]*LotteryHistory, error) {
	rowChannel, err := l.stub.GetRows(l.TableName(), []shim.Column{})
	if err != nil {
		logger.Debugf("GetLotterys,err = %v\n", err)
		return nil, fmt.Errorf("GetLotterys failed, %s", err)
	}

	var lotterys []*LotteryHistory
	for {
		select {
		case row, ok := <-rowChannel:
			if !ok {
				rowChannel = nil
			} else {
				lottery := rowToHistory(row, l.stub)
				lotterys = append(lotterys, lottery)
				logger.Debugf("GetNoDeleteLotterys range row,lottery = %v\n", *lottery)
			}
		}
		if rowChannel == nil {
			break
		}
	}
	return lotterys, nil
}

func getHistoryColumn(l *LotteryHistory) []*shim.Column {
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
		&shim.Column{Value: &shim.Column_String_{String_: l.Name}},
		&shim.Column{Value: &shim.Column_String_{String_: l.RuleType}},
		&shim.Column{Value: &shim.Column_String_{String_: l.RuleID}},
		&shim.Column{Value: &shim.Column_String_{String_: l.PublisherHash}},
		&shim.Column{Value: &shim.Column_String_{String_: l.PublisherName}},
		&shim.Column{Value: &shim.Column_Uint64{Uint64: l.StartTime}},
		&shim.Column{Value: &shim.Column_Uint64{Uint64: l.CloseTime}},
		&shim.Column{Value: &shim.Column_Uint32{Uint32: l.MinAttendeeCnt}},
		&shim.Column{Value: &shim.Column_Uint32{Uint32: l.MaxAttendeeCnt}},
		&shim.Column{Value: &shim.Column_Uint64{Uint64: l.Cost}},
		&shim.Column{Value: &shim.Column_String_{String_: l.Description}},
	}

}

func rowToHistory(row shim.Row, stub shim.ChaincodeStubInterface) *LotteryHistory {
	lottery := &LotteryHistory{}
	lottery.TxnID = row.Columns[0].GetString_()
	lottery.NewTxnID = row.Columns[1].GetString_()
	lottery.Version = row.Columns[2].GetUint32()
	lottery.LastCloseTime = row.Columns[3].GetUint64()
	lottery.Numbers = row.Columns[4].GetString_()
	lottery.Balance = row.Columns[5].GetUint64()
	lottery.PrizeTxnID = row.Columns[6].GetString_()
	lottery.CountTotal = row.Columns[7].GetUint32()
	lottery.PictureIndex = row.Columns[8].GetUint32()
	lottery.Status = row.Columns[9].GetUint32()
	lottery.UpdateTime = row.Columns[10].GetUint64()
	lottery.BlockHeight = row.Columns[11].GetUint64()
	lottery.PrevBlockHeight = row.Columns[12].GetUint64()
	lottery.TxnIDs = row.Columns[13].GetString_()
	lottery.CreateTime = row.Columns[14].GetUint64()
	lottery.Name = row.Columns[15].GetString_()
	lottery.RuleType = row.Columns[16].GetString_()
	lottery.RuleID = row.Columns[17].GetString_()
	lottery.PublisherHash = row.Columns[18].GetString_()
	lottery.PublisherName = row.Columns[19].GetString_()
	lottery.StartTime = row.Columns[20].GetUint64()
	lottery.CloseTime = row.Columns[21].GetUint64()
	lottery.MinAttendeeCnt = row.Columns[22].GetUint32()
	lottery.MaxAttendeeCnt = row.Columns[23].GetUint32()
	lottery.Cost = row.Columns[24].GetUint64()
	lottery.Description = row.Columns[25].GetString_()
	lottery.stub = stub
	return lottery
}

func historyToRow(l *LotteryHistory) shim.Row {
	return shim.Row{
		Columns: getHistoryColumn(l),
	}
}

func lotteryToHistory(l *Lottery, stub shim.ChaincodeStubInterface) *LotteryHistory {
	row := lotteryToRow(l)
	lottery := rowToHistory(row, stub)
	return lottery
}

// check value of struct RulePrize
func (l *LotteryHistory) checkValue() error {
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
func (l *LotteryHistory) Unmarshal(args string) error {
	// parse json to struct
	err := json.Unmarshal([]byte(args), l)
	if err != nil {
		return err
	}
	if l == nil {
		return errors.New("param error")
	}
	if l.TxnID == "" {
		errors.New("lottery id nil in delete history lottery.")
	}
	return nil
}
