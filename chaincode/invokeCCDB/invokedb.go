package invokeCCDB

import (
	"bytes"
	"encoding/gob"
	"errors"
	"github.com/golang/protobuf/proto"
	"github.com/hyperledger/fabric/core/chaincode/shim"
)

type InvokeCCDB struct {
	shim.ChaincodeStubInterface
	dbChaincodeId string
	bInvoke bool
}

func NewInvokeCCDB(stub shim.ChaincodeStubInterface, dbChaincodeId string,bInvoke bool) *InvokeCCDB {
	cs := new(InvokeCCDB)
	cs.ChaincodeStubInterface = stub
	cs.dbChaincodeId = dbChaincodeId
	cs.bInvoke = bInvoke
	return cs
}

func Encode(data interface{}) ([]byte, error) {
	buf := bytes.NewBuffer(nil)
	enc := gob.NewEncoder(buf)
	err := enc.Encode(data)
	if err != nil {
		return nil, errors.New("DbInvoke Encode " + err.Error())
	}
	return buf.Bytes(), nil
}

func Decode(data []byte, to interface{}) error {
	buf := bytes.NewBuffer(data)
	dec := gob.NewDecoder(buf)
	err := dec.Decode(to)
	if err != nil {
		return errors.New("DbInvoke Decode " + err.Error())
	}
	return nil
}

func ColumnsToRow(key []shim.Column) *shim.Row {
	row := new(shim.Row)
	for _, v := range key {
		row.Columns = append(row.Columns, &v)
	}
	return row
}

func (stub *InvokeCCDB) PutState(key string, value []byte) error {
	args := [][]byte{[]byte("PutState"), []byte(key), value}
	_, err := stub.ChaincodeStubInterface.InvokeChaincode(stub.dbChaincodeId, args)
	return err
}

func (stub *InvokeCCDB) GetState(key string) ([]byte, error) {
	args := [][]byte{[]byte("GetState"), []byte(key)}
	if stub.bInvoke {
		return stub.ChaincodeStubInterface.InvokeChaincode(stub.dbChaincodeId, args)
	}
	return stub.ChaincodeStubInterface.QueryChaincode(stub.dbChaincodeId, args)
}

func (stub *InvokeCCDB) DelState(key string) error {
	args := [][]byte{[]byte("DelState"), []byte(key)}
	_, err := stub.ChaincodeStubInterface.InvokeChaincode(stub.dbChaincodeId, args)
	return err
}

func (stub *InvokeCCDB) CreateTable(name string, columnDefinitions []*shim.ColumnDefinition) error {
	table := &shim.Table{name, columnDefinitions}
	value, err := proto.Marshal(table)
	if err != nil {
		return err
	}
	args := [][]byte{[]byte("CreateTable"), value}
	_, err = stub.ChaincodeStubInterface.InvokeChaincode(stub.dbChaincodeId, args)
	return err
}

func (stub *InvokeCCDB) GetTable(tableName string) (*shim.Table, error) {
	args := [][]byte{[]byte("GetTable"), []byte(tableName)}
	var ret []byte
	var err error
	if stub.bInvoke {
		ret, err = stub.ChaincodeStubInterface.InvokeChaincode(stub.dbChaincodeId, args)
	} else {
		ret, err = stub.ChaincodeStubInterface.QueryChaincode(stub.dbChaincodeId, args)
	}
	if err != nil {
		return nil, err
	}
	table := new(shim.Table)
	err = proto.Unmarshal(ret, table)
	if err != nil {
		return nil, err
	}
	return table, err
}

func (stub *InvokeCCDB) DeleteTable(tableName string) error {
	args := [][]byte{[]byte("DeleteTable"), []byte(tableName)}
	_, err := stub.ChaincodeStubInterface.InvokeChaincode(stub.dbChaincodeId, args)
	return err
}

func (stub *InvokeCCDB) InsertRow(tableName string, row shim.Row) (bool, error) {
	r, err := proto.Marshal(&row)
	if err != nil {
		return false, err
	}
	args := [][]byte{[]byte("InsertRow"), []byte(tableName), r}
	ret, err := stub.ChaincodeStubInterface.InvokeChaincode(stub.dbChaincodeId, args)
	if err != nil {
		return false, err
	}
	return string(ret) == "true", nil
}

func (stub *InvokeCCDB) ReplaceRow(tableName string, row shim.Row) (bool, error) {
	r, err := proto.Marshal(&row)
	if err != nil {
		return false, err
	}
	args := [][]byte{[]byte("ReplaceRow"), []byte(tableName), r}
	ret, err := stub.ChaincodeStubInterface.InvokeChaincode(stub.dbChaincodeId, args)
	if err != nil {
		return false, err
	}
	return string(ret) == "true", nil
}

func (stub *InvokeCCDB) GetRow(tableName string, key []shim.Column) (shim.Row, error) {
	var row shim.Row
	k, err := proto.Marshal(ColumnsToRow(key))
	if err != nil {
		return row, err
	}
	args := [][]byte{[]byte("GetRow"), []byte(tableName), k}
	var ret []byte
	if stub.bInvoke {
		ret, err = stub.ChaincodeStubInterface.InvokeChaincode(stub.dbChaincodeId, args)
	} else {
		ret, err = stub.ChaincodeStubInterface.QueryChaincode(stub.dbChaincodeId, args)
	}
	if err != nil {
		return row, err
	}
	err = proto.Unmarshal(ret, &row)
	return row, err
}

func (stub *InvokeCCDB) DeleteRow(tableName string, key []shim.Column) error {
	k, err := proto.Marshal(ColumnsToRow(key))
	if err != nil {
		return err
	}
	args := [][]byte{[]byte("DeleteRow"), []byte(tableName), k}
	_, err = stub.ChaincodeStubInterface.InvokeChaincode(stub.dbChaincodeId, args)
	return err
}

func (stub *InvokeCCDB) GetRows(tableName string, key []shim.Column) (<-chan shim.Row, error) {
	k, err := proto.Marshal(ColumnsToRow(key))
	if err != nil {
		return nil, err
	}
	args := [][]byte{[]byte("GetRows"), []byte(tableName), k}
	var ret []byte
	if stub.bInvoke {
		ret, err = stub.ChaincodeStubInterface.InvokeChaincode(stub.dbChaincodeId, args)
	} else {
		ret, err = stub.ChaincodeStubInterface.QueryChaincode(stub.dbChaincodeId, args)
	}

	bb := [][]byte{}
	Decode(ret, &bb)
	chan_rows := make(chan shim.Row)
	go func() {
		for _, b := range bb {
			row := new(shim.Row)
			proto.Unmarshal(b, row)
			chan_rows <- *row
		}
		close(chan_rows)
	}()

	return chan_rows, nil
}
