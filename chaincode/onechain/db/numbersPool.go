package db

import (
	"encoding/json"
	"errors"
	"strconv"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"peersafe.com/onelottery/chaincode/onechain/util"
)

var base int = 10000000
var NumberState string = "number_state"

type NumberPool struct {
	PoolNumber []int `json:"poolNumber"`
	Total      int   `json:"total"`
}

func GenerateNumbers(lotteryID string, total int, stub shim.ChaincodeStubInterface) error {
	pool := &NumberPool{}

	var numbers []int

	pool.PoolNumber = numbers
	pool.Total = total
	// json marshal
	data, err := json.Marshal(pool)
	if err != nil {
		logger.Errorf("[GenerateNumbers] json marshal err %v\n", err)
		return err
	}

	// put to db
	return stub.PutState(lotteryID+NumberState, data)
}

func GetNumber(lotteryID string, batch int, stub shim.ChaincodeStubInterface, time uint64) ([]int, *NumberPool, error) {
	data, err := stub.GetState(lotteryID + NumberState)
	if err != nil {
		return nil, nil, err
	}
	// json unmarshal
	pool := &NumberPool{}
	err = json.Unmarshal(data, pool)
	if err != nil {
		return nil, nil, err
	}
	if len(pool.PoolNumber) >= pool.Total {
		return nil, nil, errors.New("Can not generate number. Because pool had full.")
	}
	if len(pool.PoolNumber)+batch > pool.Total {
		return nil, nil, errors.New("Too much number to be generated.")
	}

	var result []int
	var i int = 1
	for {
		number := generateNumber(pool.Total, strconv.FormatUint(time, 10)+strconv.Itoa(i+1))
		i += len(result)

		if isExist(number, pool.PoolNumber) {
			i += 1
			continue
		}
		// number can be use
		pool.PoolNumber = append(pool.PoolNumber, number)
		result = append(result, number)

		// enough number
		if len(result) == batch {
			break
		}
	}
	return result, pool, nil
}

func SyncPool(stub shim.ChaincodeStubInterface, lotteryID string, pool *NumberPool) error {
	// json marshal
	data, err := json.Marshal(pool)
	if err != nil {
		logger.Errorf("[GetNumber] json marshal err %v\n", err)
		return err
	}
	// put to db
	return stub.PutState(lotteryID+NumberState, data)
}

func isExist(a int, array []int) bool {
	for _, v := range array {
		if a == v {
			return true
		}
	}
	return false
}

func generateNumber(total int, time string) int {
	newTime, _ := strconv.ParseInt(util.Hash(time)[0:7], 16, 64)
	number := int(int(newTime)%total) + base
	logger.Debugf("generate bet number %v\n", number)
	return number
}
