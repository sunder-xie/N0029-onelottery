package util

import (
	"fmt"

	"github.com/hyperledger/fabric/core/chaincode/shim"
)

func GetTxTime(stub shim.ChaincodeStubInterface) (uint64, error) {
	timestamp, err := stub.GetTxTimestamp()
	if err != nil {
		fmt.Printf("get tx timestamp error %v\n", err)
		return 0, err
	}
	return uint64(timestamp.Seconds), nil
}
