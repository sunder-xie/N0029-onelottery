package db

import (
	"encoding/json"
	"errors"
	"fmt"
	"os"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/op/go-logging"
)

var logger = logging.MustGetLogger("onechain.db")

func init() {
	format := logging.MustStringFormatter("%{shortfile} %{time:15:04:05.000} [%{module}] %{level:.4s} : %{message}")
	backend := logging.NewLogBackend(os.Stderr, "", 0)
	backendFormatter := logging.NewBackendFormatter(backend, format)
	logging.SetBackend(backendFormatter).SetLevel(logging.DEBUG, "onechain.db")
}

type Lottery struct {
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

func (l *Lottery) TableName() string {
	return "Lottery"
}

func NewLottery(stub shim.ChaincodeStubInterface) *Lottery {
	lottery := &Lottery{}
	lottery.stub = stub
	return lottery
}

// Create Lottery table
func (l *Lottery) CreateTable() error {
	if table,err := l.stub.GetTable(l.TableName()); table != nil && err == nil {
		logger.Debugf("Exist Lottery table\n")
		return nil
	}
	logger.Debugf("Start creating Lottery table\n")

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
		logger.Errorf("Failed creating Lottery table, %v\n", err.Error())
		return errors.New("Failed creating Lottery table." + err.Error())
	}

	logger.Debugf("End creating Lottery table\n")
	return nil
}

// Add lottery
func (l *Lottery) Add() error {
	logger.Debugf("Start AddLottery\n")
	l.TxnID = l.stub.GetTxID()
	logger.Debugf("[AddLottery] id=%v\n", l.TxnID)
	logger.Debugf("[AddLottery] publisher=%v\n", l.PublisherHash)
	// check value
	err := l.checkValue()
	if err != nil {
		logger.Errorf("[AddLottery] checkValue error=%v\n", err)
		return err
	}
	l.Status = 0
	// insert to db
	ok, err := l.stub.InsertRow(l.TableName(), lotteryToRow(l))
	if !ok && err == nil {
		logger.Errorf("AddLottery InsertRow error,Lottery was already exist.\n")
		return errors.New("Lottery was already exist.")
	}
	if err != nil {
		logger.Errorf("AddLottery InsertRow error=%v.\n", err)
		return err
	}
	// generate lottery numbers
	if l.RuleType == "PrizeRule" {
		if err := GenerateNumbers(l.TxnID, int(l.MaxAttendeeCnt), l.stub); err != nil {
			logger.Errorf("AddLottery Generate lottery numbers failed, %v\n", err)
			return errors.New("Generate lottery numbers failed." + err.Error())
		}
	}
	logger.Debugf("End AddLottery.\n")
	return err
}

// Delete lottery
func (l *Lottery) Delete(flag bool) error {
	logger.Debugf("Start DeleteLottery\n")
	// check id
	if l == nil {
		logger.Errorf("TxnID is nil.\n")
		return errors.New("TxnID is nil.")
	}

	history := NewLotteryHistory(l.stub)
	if !flag {
		// do history add
		history = lotteryToHistory(l, l.stub)
		history.CreateTime = l.CreateTime
	}

	// do delete
	err := l.stub.DeleteRow(
		l.TableName(),
		[]shim.Column{shim.Column{Value: &shim.Column_String_{String_: l.TxnID}}},
	)
	if err != nil {
		logger.Errorf("Lottery delete err: %v\n", err)
		return err
	}

	if !flag {
		err = history.Add()
		if err != nil {
			logger.Errorf("Lottery delete err: %v\n", err)
			return err
		}
	}

	logger.Debugf("End DeleteLottery error=%v\n", err)
	return err
}

func (l *Lottery) Update() error {
	logger.Debugf("Update lottery=%v\n", *l)

	logger.Debugf("lottery TxnID=%v\n", l.TxnID)
	// do update
	ok, err := l.stub.ReplaceRow(l.TableName(), lotteryToRow(l))
	if !ok && err == nil {
		logger.Errorf("UpdateState Lottery was does not exist.\n")
		return errors.New("UpdateState was does not exist.")
	}
	if err != nil {
		return errors.New("UpdateState failed!" + err.Error())
	}
	return nil
}

// Edit lottery
/*
func (l *Lottery) EditLottery(currentLottery *Lottery) error {
	logger.Debugf("Start EditLottery,args\n")
	// check value
	err := l.checkId()
	if err != nil {
		logger.Errorf("[EditLottery] checkId error=%v\n", err)
		return err
	}

	// fliter field need to be update
	if l.Name != "" {
		currentLottery.Name = l.Name
	}
	if l.PublisherName != "" {
		currentLottery.PublisherName = l.PublisherName
	}
	if l.RuleType != "" {
		currentLottery.RuleType = l.RuleType
	}
	if l.RuleID != "" {
		currentLottery.RuleID = l.RuleID
	}
	if l.Counterparty != "" {
		currentLottery.Counterparty = l.Counterparty
	}
	if l.CounterpartySecret != "" {
		currentLottery.CounterpartySecret = l.CounterpartySecret
	}
	if l.StartTime != 0 {
		currentLottery.StartTime = l.StartTime
	}
	if l.CloseTime != 0 {
		currentLottery.CloseTime = l.CloseTime
	}
	if l.MinAttendeeCnt != 0 {
		currentLottery.MinAttendeeCnt = l.MinAttendeeCnt
	}
	if l.MaxAttendeeCnt != 0 {
		currentLottery.MaxAttendeeCnt = l.MaxAttendeeCnt
	}
	if l.Cost != 0 {
		currentLottery.Cost = l.Cost
	}
	if l.Description != "" {
		currentLottery.Description = l.Description
	}
	if l.Balance != 0 {
		currentLottery.Balance = l.Balance
	}
	if l.Total != 0 {
		currentLottery.Total = l.Total
	}
	if l.CountTotal != 0 {
		currentLottery.CountTotal = l.CountTotal
	}
	if l.IsFinished {
		currentLottery.IsFinished = l.IsFinished
	}
	if l.UpdateTime != 0 {
		currentLottery.UpdateTime = l.UpdateTime
	}
	if l.State != 0 {
		currentLottery.State = l.State
	}

	// do update
	ok, err := l.stub.ReplaceRow(l.TableName(), lotteryToRow(currentLottery))
	if !ok && err == nil {
		logger.Errorf("EditLottery Lottery was does not exist.\n")
		return errors.New("Lottery was does not exist.")
	}
	return err
}
*/

// Get one lottery
func (l *Lottery) GetOneByID(id string) (*Lottery, error) {
	logger.Debugf("[GetLottery] id=%v\n", id)
	// do get
	row, err := l.stub.GetRow(l.TableName(), []shim.Column{shim.Column{Value: &shim.Column_String_{String_: id}}})
	if err != nil {
		logger.Debugf("[GetLottery] get row err=%v\n", err)
		return nil, err
	}
	if len(row.Columns) == 0 {
		return nil, nil
	}
	lottery := rowToLottery(row, l.stub)
	return lottery, nil
}

type SimpleLottery struct {
	TxnID         string `json:"txnID"`
	NewTxnID      string `json:"newTxnID"`
	Name          string `json:"name"`           // 活动名称
	Version       uint32 `json:"version"` // 活动的版本号，修改活动、投注及开奖后，值都加1
	PublisherName string `json:"publisherName,omitempty"`
	PublisherHash string `json:"publisherHash,omitempty"`
	CreateTime    uint64 `json:"-"` // 用于标记何时删除历史活动信息
}

// Get all lotterys
func (l *Lottery) GetAll() ([]*SimpleLottery, error) {
	logger.Debugf("Start GetLotterys\n")
	rowChannel, err := l.stub.GetRows(l.TableName(), []shim.Column{})
	if err != nil {
		logger.Debugf("GetLotterys,err = %v\n", err)
		return nil, fmt.Errorf("GetLotterys failed, %s", err)
	}

	var lotterys []*SimpleLottery
	for {
		select {
		case row, ok := <-rowChannel:
			if !ok {
				rowChannel = nil
			} else {
				lottery := rowToLottery(row, l.stub)
				lotterys = append(lotterys, &SimpleLottery{
					TxnID:         lottery.TxnID,
					NewTxnID:      lottery.NewTxnID,
					Name: 	       lottery.Name,
					Version:       lottery.Version,
					PublisherName: lottery.PublisherName,
					PublisherHash: lottery.PublisherHash,
					CreateTime:    lottery.UpdateTime,
				})
				logger.Debugf("GetLotterys range row,lottery = %v\n", *lottery)
			}
		}
		if rowChannel == nil {
			break
		}
	}

	if len(lotterys) == 0 {
		return nil, nil
	}

	logger.Debugf("End GetLotterys\n")
	return lotterys, nil
}

// Get no finished lotterys
func (l *Lottery) GetNofinishedLotterys() ([]*Lottery, error) {
	rowChannel, err := l.stub.GetRows(l.TableName(), []shim.Column{})
	if err != nil {
		logger.Debugf("GetLotterys,err = %v\n", err)
		return nil, fmt.Errorf("GetLotterys failed, %s", err)
	}

	var lotterys []*Lottery
	for {
		select {
		case row, ok := <-rowChannel:
			if !ok {
				rowChannel = nil
			} else {
				lottery := rowToLottery(row, l.stub)
				if lottery.CountTotal != lottery.MaxAttendeeCnt {
					lotterys = append(lotterys, lottery)
				}
				logger.Debugf("GetLotterys range row,lottery = %v\n", *lottery)
			}
		}
		if rowChannel == nil {
			break
		}
	}
	return lotterys, nil
}

func rowToLottery(row shim.Row, stub shim.ChaincodeStubInterface) *Lottery {
	lottery := &Lottery{}
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

func getLotteryColumn(l *Lottery) []*shim.Column {
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

func lotteryToRow(l *Lottery) shim.Row {
	return shim.Row{
		Columns: getLotteryColumn(l),
	}
}

// check value of struct RulePrize
func (l *Lottery) checkValue() error {
	if l.TxnID == "" ||
		l.Name == "" ||
		l.RuleID == "" ||
		l.RuleType == "" ||
		l.PublisherHash == "" ||
		l.PublisherName == "" ||
		l.CreateTime == 0 ||
		l.StartTime == 0 ||
		l.CloseTime == 0 ||
		l.MinAttendeeCnt == 0 ||
		l.MaxAttendeeCnt == 0 ||
		l.Cost == 0 {
		return errors.New("parameter error")
	}

	if l.RuleType != "PrizeRule" && l.RuleType != "BallRule" {
		return errors.New("Undefined rule type.")
	}
	if l.StartTime >= l.CloseTime {
		return errors.New("Start time can not larger than close time.")
	}
	if l.MinAttendeeCnt > l.MaxAttendeeCnt {
		return errors.New("MinAttendeeCnt can not larger than maxAttendeeCnt.")
	}
	return nil
}

// unmarshal args to struct RulePrize
func (l *Lottery) Unmarshal(args string) error {
	// parse json to struct
	err := json.Unmarshal([]byte(args), l)
	if err != nil {
		return err
	}
	return nil
}
