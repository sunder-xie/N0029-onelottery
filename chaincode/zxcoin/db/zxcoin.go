package db

import (
	"errors"
	"fmt"
	"os"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/op/go-logging"
)

var ZxCoinTableName = "ZxCoin"

var LOG_LEVEL = false

var myLogger = logging.MustGetLogger("zxcoin.db")

func init() {
	if LOG_LEVEL {
		format := logging.MustStringFormatter("%{shortfile} %{time:15:04:05.000} [%{module}] %{level:.4s} : %{message}")
		backend := logging.NewLogBackend(os.Stderr, "", 0)
		backendFormatter := logging.NewBackendFormatter(backend, format)
		logging.SetBackend(backendFormatter).SetLevel(logging.DEBUG, "zxcoin.db")
	}
}

func NewZxCoin(stub shim.ChaincodeStubInterface) *ZxCoin {
	return &ZxCoin{stub: stub}
}

// Create table ZxCoin
func (this *ZxCoin) CreateTable() error {
	err := this.stub.CreateTable(ZxCoinTableName, []*shim.ColumnDefinition{
		&shim.ColumnDefinition{Name: "Owner", Type: shim.ColumnDefinition_STRING, Key: true},
		&shim.ColumnDefinition{Name: "Name", Type: shim.ColumnDefinition_STRING, Key: false},
		&shim.ColumnDefinition{Name: "Balance", Type: shim.ColumnDefinition_UINT64, Key: false},
		&shim.ColumnDefinition{Name: "Reserved", Type: shim.ColumnDefinition_UINT64, Key: false},
		&shim.ColumnDefinition{Name: "TxnIDs", Type: shim.ColumnDefinition_STRING, Key: false},
		&shim.ColumnDefinition{Name: "BlockHeight", Type: shim.ColumnDefinition_UINT64, Key: false},
		&shim.ColumnDefinition{Name: "PrevBlockHeight", Type: shim.ColumnDefinition_UINT64, Key: false},
	})
	if err != nil {
		return errors.New("Failed creating ZxCoin table." + err.Error())
	}
	return nil
}

// Add ZxCoin user info
func (this *ZxCoin) AddZxCoinUser(table ZxCoinTable) error {
	ok, err := this.stub.InsertRow(ZxCoinTableName, shim.Row{
		Columns: []*shim.Column{
			&shim.Column{Value: &shim.Column_String_{String_: table.Owner}},
			&shim.Column{Value: &shim.Column_String_{String_: table.Name}},
			&shim.Column{Value: &shim.Column_Uint64{Uint64: table.Balance}},
			&shim.Column{Value: &shim.Column_Uint64{Uint64: table.Reserved}},
			&shim.Column{Value: &shim.Column_String_{String_: table.TxnIDs}},
			&shim.Column{Value: &shim.Column_Uint64{Uint64: table.BlockHeight}},
			&shim.Column{Value: &shim.Column_Uint64{Uint64: table.PrevBlockHeight}}},
	})

	if !ok && err == nil {
		return errors.New("Record was already exist.")
	}

	if err != nil {
		return errors.New("Insert to ZxCoin failed.")
	}

	return nil
}

// Edit ZxCoin user info
func (this *ZxCoin) EditZxCoinUser(table ZxCoinTable) error {
	// do update
	ok, err := this.stub.ReplaceRow(ZxCoinTableName, shim.Row{
		Columns: []*shim.Column{
			&shim.Column{Value: &shim.Column_String_{String_: table.Owner}},
			&shim.Column{Value: &shim.Column_String_{String_: table.Name}},
			&shim.Column{Value: &shim.Column_Uint64{Uint64: table.Balance}},
			&shim.Column{Value: &shim.Column_Uint64{Uint64: table.Reserved}},
			&shim.Column{Value: &shim.Column_String_{String_: table.TxnIDs}},
			&shim.Column{Value: &shim.Column_Uint64{Uint64: table.BlockHeight}},
			&shim.Column{Value: &shim.Column_Uint64{Uint64: table.PrevBlockHeight}},
		},
	})
	if !ok && err == nil {
		return errors.New("Record does not exists.")
	}

	if err != nil {
		fmt.Sprintf("Update ZxCoin failed. err : %s", err.Error())
		return errors.New(fmt.Sprintf("Update ZxCoin failed. err : %s", err.Error()))
	}

	return nil
}

// Get ZxCoin user info by hash
func (this *ZxCoin) GetZxCoinUserByHash(owner string) (*ZxCoinTable, error) {
	var columns1 []shim.Column
	col1 := shim.Column{Value: &shim.Column_String_{String_: owner}}
	columns1 = append(columns1, col1)
	row, err := this.stub.GetRow(ZxCoinTableName, columns1)
	myLogger.Debugf("row : %v", row)
	if err != nil || len(row.Columns) == 0 {
		if err == nil {
			err = errors.New("user not exists")
		}
		return nil, errors.New(fmt.Sprintf("Failed retriving user info : %s", err.Error()))
	}

	result := &ZxCoinTable{
		Owner:           row.Columns[0].GetString_(),
		Name:            row.Columns[1].GetString_(),
		Balance:         row.Columns[2].GetUint64(),
		Reserved:        row.Columns[3].GetUint64(),
		TxnIDs:          row.Columns[4].GetString_(),
		BlockHeight:     row.Columns[5].GetUint64(),
		PrevBlockHeight: row.Columns[6].GetUint64(),
	}

	return result, nil
}

// Get ZxCoin user info by name
func (this *ZxCoin) GetZxCoinUserByName(name string) (*ZxCoinTable, error) {
	var result *ZxCoinTable
	var columns1 []shim.Column
	//	col1 := shim.Column{Value: &shim.Column_String_{String_: owner}}
	//	columns1 = append(columns1, col1)
	rowChannel, err := this.stub.GetRows(ZxCoinTableName, columns1)
	if err != nil {
		return result, errors.New(fmt.Sprintf("Failed retriving user info : %s", err.Error()))
	}

	flag := false
	for {
		select {
		case row, ok := <-rowChannel:
			if !ok {
				rowChannel = nil
				myLogger.Debug("query over in get GetZxCoinUserByName!")
			} else {
				tmp := ZxCoinTable{
					Owner:           row.Columns[0].GetString_(),
					Name:            row.Columns[1].GetString_(),
					Balance:         row.Columns[2].GetUint64(),
					Reserved:        row.Columns[3].GetUint64(),
					TxnIDs:          row.Columns[4].GetString_(),
					BlockHeight:     row.Columns[5].GetUint64(),
					PrevBlockHeight: row.Columns[5].GetUint64(),
				}
				myLogger.Debug(tmp)
				if tmp.Name == name {
					result = &tmp
					flag = true
					break
				}
			}
		}
		if rowChannel == nil {
			break
		}
	}

	if flag == false {
		err = errors.New("Failed retriving userCert in GetZxCoinUserByName")
		myLogger.Debug(err.Error())
		return result, err
	}

	return result, nil
}
