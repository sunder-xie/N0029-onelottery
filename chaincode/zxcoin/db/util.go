package db

import (
	"bytes"
	"encoding/base64"
	"encoding/json"
	"errors"
	"fmt"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	"golang.org/x/crypto/sha3"
	"hash"
	"strconv"
)

const (
	// zxcoin admin Key
	ZXCOIN_ADMIN = "zxcoin_admin"
)

var autoConfirmTime uint64

//从json中解析出成员列表
func JsonToStrList(jsStr string) ([]string, error) {
	//若为空字符串
	if jsStr == "" {
		return nil, nil
	}

	var strList []string
	err := json.Unmarshal([]byte(jsStr), &strList)
	if err != nil {
		return nil, err
	}

	return strList, nil
}

//将字符串数组转换为关键字为key的json对象
func StrListToJson(strList []string) (string, error) {
	jsVal, err := json.Marshal(strList)
	if err != nil {
		return "", err
	}
	return string(jsVal), nil
}

//添加到字符串数组，不可重复添加
func AddStr(strList []string, str string) ([]string, error) {
	if ContainsStr(strList, str) {
		return strList, errors.New("The member exist in the list")
	}
	return append(strList, str), nil
}

//从字符串数组中移除，必须在数组中包含
func RemoveStr(strList []string, str string) ([]string, error) {
	myLogger.Debugf("removeStr  %s from %v \n", str, strList)
	var index = int(-1)
	for i, v := range strList {
		if str == v {
			index = i
			break
		}
	}
	if index < 0 {
		return strList, errors.New("There's no exist the member:" + str)
	}

	return append(strList[:index], strList[index+1:]...), nil
}

//字符串数组中包含对应字符串
func ContainsStr(strList []string, str string) bool {
	for _, v := range strList {
		if v == str {
			return true
		}
	}
	return false
}

func getHashSHA3(bitsize int) (hash.Hash, error) {
	switch bitsize {
	case 224:
		return sha3.New224(), nil
	case 256:
		return sha3.New256(), nil
	case 384:
		return sha3.New384(), nil
	case 512:
		return sha3.New512(), nil
	case 521:
		return sha3.New512(), nil
	default:
		return nil, fmt.Errorf("Invalid bitsize. It was [%d]. Expected [224, 256, 384, 512, 521]", bitsize)
	}
}

func computeHash(msg []byte, bitsize int) ([]byte, error) {
	hash, err := getHashSHA3(bitsize)
	if err != nil {
		return nil, err
	}

	hash.Write(msg)
	return hash.Sum(nil), nil
}

func GetCertificate(stub shim.ChaincodeStubInterface) (string, error) {
	if Raw, err := stub.GetCallerCertificate(); err != nil {
		err = fmt.Errorf("getOwnerHash error in GetCallerCertificate :%s!", err.Error())
		return "", err
	} else {
		myLogger.Debug("***GetCertificate1", string(Raw))
		cert := base64.StdEncoding.EncodeToString(Raw)
		myLogger.Debug("***GetCertificate2", cert)
		return cert, nil
	}
}

func GetOwnerHash(stub shim.ChaincodeStubInterface) (string, error) {
	// get caller public key
	str, err := GetCertificate(stub)
	if err != nil {
		return "", err
	}
	pubHash := getPublicKeyHash([]byte(str))
	myLogger.Debug("***GetOwnerHash", pubHash)
	return pubHash, nil
}

func GetAdminHash(stub shim.ChaincodeStubInterface) (string, error) {
	// get admin cert form db
	adminRaw, err := stub.GetState(ZXCOIN_ADMIN)
	var adminCert string
	if err != nil {
		myLogger.Errorf("Get admin cert err:%v\n", err)
		return "", err
	} else {
		adminCert = base64.StdEncoding.EncodeToString(adminRaw)
	}
	if err != nil {
		return "", err
	}
	pubHash := getPublicKeyHash([]byte(adminCert))
	myLogger.Debug("***GetAdminHash", pubHash)
	return pubHash, nil
}

func getPublicKeyHash(pubKey []byte) string {
	hashPub, err := computeHash([]byte(pubKey), 224)
	if err != nil {
		myLogger.Error("Invalid signature")
	}

	tmp := fmt.Sprintf("%x", hashPub)
	myLogger.Debug(tmp)

	return tmp
}

func CheckAdmin(stub shim.ChaincodeStubInterface) error {
	// get admin cert form db
	data, err := stub.GetState(ZXCOIN_ADMIN)
	if err != nil {
		myLogger.Errorf("Get admin cert err:%v\n", err)
		return err
	}

	// get admin cert form request
	adminCert, err := stub.GetCallerCertificate()
	if err != nil {
		myLogger.Debug("Failed getting Certificate")
		return errors.New("Failed getting Certificate.")
	}
	if len(adminCert) == 0 {
		myLogger.Debug("Invalid admin certificate. Empty.")
		return errors.New("Invalid admin certificate. Empty.")
	}

	if !bytes.Equal(data, adminCert) {
		return errors.New("Permission denied.")
	}
	return nil
}

func AutoConfirmTime(stub shim.ChaincodeStubInterface) uint64 {
	if autoConfirmTime <= 0 {
		val, err := stub.GetState("AutoConfirmTime")
		if err != nil {
			myLogger.Debug("Get AutoConfirmTime failed")
			return 432000000 //5 days
		}
		autoConfirmTime, err = strconv.ParseUint(string(val), 10, 0)
		if err != nil {
			myLogger.Debug("ParseUint AutoConfirmTime failed")
			return 432000000 //5 days
		}
	}
	return autoConfirmTime
}

func SetAutoConfirmTime(stub shim.ChaincodeStubInterface, time uint64) error {
	autoConfirmTime = time
	return stub.PutState("AutoConfirmTime", []byte(strconv.FormatUint(time, 10)))
}
