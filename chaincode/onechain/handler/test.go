package handler

import (
	"encoding/json"

	"github.com/hyperledger/fabric/core/chaincode/shim"
)

type Test struct {
}

func NewTest() *Test {
	return &Test{}
}

type testBody struct {
	PutCount int `json:"putCount"`
	GetCount int `json:"getCount"`
}

func (t *Test) Test(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	logger.Debugf("Start Test...\n")

	if args[1] != "" {
		tb := &testBody{}
		json.Unmarshal([]byte(args[1]), tb)

		// put count
		for i := 0; i < tb.PutCount; i++ {
			stub.PutState("testKey", []byte("1"))
		}

		// get count
		for i := 0; i < tb.GetCount; i++ {
			stub.GetState("testKey")
		}
	}

	logger.Debugf("End Test...\n")
	return writeMessage(function, stub, SUCCESS, "", nil)
}
