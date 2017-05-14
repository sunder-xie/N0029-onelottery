package db

import (
	"github.com/hyperledger/fabric/core/chaincode/shim"
	"errors"
	"fmt"
	"encoding/json"
)

const WITHDRAW_RECORD_LIST = "WithdrawRecordList"

func (handle *WithdrawHandle) getTxUserInfo(txid string) (*WithdrawUserInfo, error) {
	data, err := handle.Stub.GetState("WithdrawHash_" + txid)
	if err != nil {
		return nil, err
	}
	if data == nil {
		err = errors.New("The transaction is not exist")
		return nil, err
	}
	var userInfo WithdrawUserInfo
	err = json.Unmarshal(data, &userInfo)
	if err != nil {
		return nil, err
	}
	return &userInfo, err
}

func (handle *WithdrawHandle) setTxUserInfo(txid, name, hash string) error {
	if txid == "" || name == "" || hash == "" {
		return errors.New(fmt.Sprintf("The parameter of setTxUserInfo is empty.txid:%s, name:%s, hash:%s",txid,name,hash))
	}
	data, err := json.Marshal(WithdrawUserInfo{UserName: name, UserHash: hash})
	if err != nil {
		return err
	}
	return handle.Stub.PutState("WithdrawHash_" + txid, data)
}

//创建句柄同时把表也创建了
func GetHandle(stub shim.ChaincodeStubInterface, name, hash string) (*WithdrawHandle, error) {
	if name == "" {
		return nil, errors.New("The table name is empty!")
	}
	handle := &WithdrawHandle{Stub: stub, User:name, Hash:hash}
	if !handle.IsTableExist() {
		//创建表
		err := handle.createTable()
		if err != nil {
			return nil, err
		}
		//查询并插入
		err = handle.addToUserList(name)
		if err != nil {
			return nil, err
		}
	}
	return handle, nil
}

func FindHandleByTxid(stub shim.ChaincodeStubInterface, txid string) (*WithdrawHandle, error) {
	handle := &WithdrawHandle{Stub: stub}
	userInfo, err := handle.getTxUserInfo(txid)
	if err != nil {
		return nil, err
	}
	handle.User = userInfo.UserName
	handle.Hash = userInfo.UserHash
	if !handle.IsTableExist() {
		return nil, errors.New("The table of user " + userInfo.UserName + " is not exist!")
	}
	return handle, nil
}

//插入表名列表
func (handle *WithdrawHandle) addToUserList(name string) error {
	userList, err := handle.GetUserList()
	if err == nil {
		userList, err = AddStr(userList, name)
		if err == nil {
			return handle.setUserList(userList)
		}
	}
	return err
}

//获取表
func (handle *WithdrawHandle) GetUserList() ([]string, error) {
	data, err := handle.Stub.GetState(WITHDRAW_RECORD_LIST)
	if err == nil {
		return JsonToStrList(string(data))
	}
	return nil, err
}

//设置表
func (handle *WithdrawHandle) setUserList(userList []string) error {
	data, err := StrListToJson(userList)
	if err == nil {
		return handle.Stub.PutState(WITHDRAW_RECORD_LIST, []byte(data))
	}
	return err
}

func (handle *WithdrawHandle) IsTableExist() bool {
	table, err := handle.Stub.GetTable(handle.TableName())
	if table != nil && err == nil {
		return true
	}
	return false
}

func (handle *WithdrawHandle) TableName() string {
	return "Withdraw_" + handle.User
}

//Create table withdraw
func (handle *WithdrawHandle) createTable() error {
	err := handle.Stub.CreateTable(handle.TableName(), withdrawTableDefine())
	if err != nil {
		return errors.New("Failed creating Withdraw table with name:" + handle.TableName() + "." + err.Error())
	}
	return nil
}

func (handle *WithdrawHandle) AddInfo(info *WithdrawInfo) error {
	ok, err := handle.Stub.InsertRow(handle.TableName(), withdrawInfoToRow(info))

	if err != nil {
		return errors.New("Insert to Withdraw failed." + err.Error())
	} else if !ok {
		return errors.New("Record was already exist.")
	}

	err = handle.setTxUserInfo(info.TxId, handle.User, handle.Hash)

	if err != nil {
		return errors.New("setTxUser failed!" + err.Error())
	}

	return nil
}

func (handle *WithdrawHandle) UpdateInfo(info *WithdrawInfo) error {
	ok, err := handle.Stub.ReplaceRow(handle.TableName(), withdrawInfoToRow(info))

	if !ok && err == nil {
		return errors.New("WithdrawRecord does not exist.")
	}

	if err != nil {
		fmt.Sprintf("Update WithdrawRecord failed. err : %s", err.Error())
		return errors.New(fmt.Sprintf("Update WithdrawRecord failed. err : %s", err.Error()))
	}

	return nil
}

func (handle *WithdrawHandle) GetInfo(txid string) (*WithdrawInfo, error) {
	var columns1 []shim.Column
	col1 := shim.Column{Value: &shim.Column_String_{String_: txid}}
	columns1 = append(columns1, col1)
	row, err := handle.Stub.GetRow(handle.TableName(), columns1)
	myLogger.Debugf("row : %v", row)
	if err != nil {
		return nil, errors.New(fmt.Sprintf("Failed GetFeild : %s", err.Error()))
	} else if len(row.Columns) == 0 {
		return nil, errors.New("Txid of Withdraw not exists")
	}
	return rowToWithdrawInfo(row), nil
}

func (handle *WithdrawHandle) CheckOwnerUser() error {
	//获取所有者Hash
	ownerHash, err := GetOwnerHash(handle.Stub)
	if err != nil {
		return err
	}

	//检查是否为当前用户交易
	if ownerHash != handle.Hash {
		return errors.New("The tx is not belong to user")
	}
	return nil
}

func (handle *WithdrawHandle) GetInfos() (chan *WithdrawInfo, error) {
	rowChannel, err := handle.Stub.GetRows(handle.TableName(), []shim.Column{})
	if err != nil {
		return nil, err
	}
	chanInfo := make(chan *WithdrawInfo)
	go func() {
		for {
			row, ok := <-rowChannel
			if !ok {
				rowChannel = nil
				break
			}
			chanInfo <- rowToWithdrawInfo(row)
		}
		close(chanInfo)
	}()
	return chanInfo, nil
}

func withdrawTableDefine() []*shim.ColumnDefinition {
	return []*shim.ColumnDefinition{
		&shim.ColumnDefinition{Name: "TxId", Type: shim.ColumnDefinition_STRING, Key: true},
		&shim.ColumnDefinition{Name: "State", Type: shim.ColumnDefinition_UINT32, Key: false},
		&shim.ColumnDefinition{Name: "AccountInfo", Type: shim.ColumnDefinition_STRING, Key: false},
		&shim.ColumnDefinition{Name: "Amount", Type: shim.ColumnDefinition_UINT64, Key: false},
		&shim.ColumnDefinition{Name: "RemitOrderNumber", Type: shim.ColumnDefinition_STRING, Key: false},
		&shim.ColumnDefinition{Name: "Remark", Type: shim.ColumnDefinition_STRING, Key: false},
		&shim.ColumnDefinition{Name: "ModifyTime", Type: shim.ColumnDefinition_UINT64, Key: false},
		&shim.ColumnDefinition{Name: "CreateTime", Type: shim.ColumnDefinition_UINT64, Key: false},
	}
}

func rowToWithdrawInfo(row shim.Row) *WithdrawInfo {
	return &WithdrawInfo{
		TxId:             row.Columns[0].GetString_(),
		State:            row.Columns[1].GetUint32(),
		AccountInfo:      row.Columns[2].GetString_(),
		Amount:           row.Columns[3].GetUint64(),
		RemitOrderNumber: row.Columns[4].GetString_(),
		Remark:           row.Columns[5].GetString_(),
		ModifyTime:       row.Columns[6].GetUint64(),
		CreateTime:       row.Columns[7].GetUint64(),
	}
}

func withdrawInfoToRow(info *WithdrawInfo) shim.Row {
	return shim.Row{
		Columns: []*shim.Column{
			&shim.Column{Value: &shim.Column_String_{String_: info.TxId}},
			&shim.Column{Value: &shim.Column_Uint32{Uint32: info.State}},
			&shim.Column{Value: &shim.Column_String_{String_: info.AccountInfo}},
			&shim.Column{Value: &shim.Column_Uint64{Uint64: info.Amount}},
			&shim.Column{Value: &shim.Column_String_{String_: info.RemitOrderNumber}},
			&shim.Column{Value: &shim.Column_String_{String_: info.Remark}},
			&shim.Column{Value: &shim.Column_Uint64{Uint64: info.ModifyTime}},
			&shim.Column{Value: &shim.Column_Uint64{Uint64: info.CreateTime}}},
	}
}