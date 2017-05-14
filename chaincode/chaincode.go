package main

import (
	"errors"
	"fmt"
	"os"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/hyperledger/fabric/core/crypto/primitives"
	"github.com/op/go-logging"
	"peersafe.com/onelottery/sdk/client_sdk"

	"encoding/base64"
	occ "peersafe.com/onelottery/chaincode/consensus"
	"peersafe.com/onelottery/chaincode/invokeCCDB"
	"peersafe.com/onelottery/chaincode/onechain"
	"peersafe.com/onelottery/chaincode/zxcoin"
)

var myLogger = logging.MustGetLogger("onelotteryChaincode")

var zxCoin = zxcoin.NewZxCoin()
var oneChain = onechain.NewOnechain()
var consensus = occ.NewConsensus()

const DB_CHAINDOCE_ID = "DATABASE_CHAINCODE_ID"

var dbChaincodeId string

type handlerFunc func(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error)

var handler = map[string]handlerFunc{
	// zxCoin chaincode handler
	"zxCoinInit":                zxCoin.ZxCoinInit,
	"zxCoinTransfer":            zxCoin.ZxCoinTransfer,
	"zxCoinAccountBalance":      zxCoin.ZxCoinAccountBalance,
	"zxCoinFrezon":              zxCoin.ZxCoinFrezon,
	"zxCoinGetUserInfo":         zxCoin.ZxCoinGetUserInfo,
	"zxCoinWithdraw":            zxCoin.ZxCoinWithdraw,
	"zxCoinWithdrawRecall":      zxCoin.ZxCoinWithdrawRecall,
	"zxCoinWithdrawFail":        zxCoin.ZxCoinWithdrawFail,
	"zxCoinWithdrawRemitSucces": zxCoin.ZxCoinWithdrawRemitSuccess,
	"zxCoinWithdrawConfirm":     zxCoin.ZxCoinWithdrawConfirm,
	"zxCoinWithdrawAppeal":      zxCoin.ZxCoinWithdrawAppeal,
	"zxCoinWithdrawAppealDone":  zxCoin.ZxCoinWithdrawAppealDone,
	"zxCoinWithdrawQuery":       zxCoin.ZxCoinWithdrawQuery,
	"zxCoinWithdrawInfoQuery":   zxCoin.ZxCoinWithdrawInfoQuery,
	"zxCoinSetConfig":           zxCoin.ZxCoinSetConfig,
	// oneChain chaincode handler
	"oneChainInit":               oneChain.OneChainInit,
	"oneLotteryPrizeRuleAdd":     oneChain.OneLotteryPrizeRuleAdd,
	"oneLotteryPrizeRuleDelete":  oneChain.OneLotteryPrizeRuleDelete,
	"oneLotteryPrizeRuleEdit":    oneChain.OneLotteryPrizeRuleEdit,
	"oneLotteryPrizeRuleQuery":   oneChain.OneLotteryPrizeRuleQuery,
	"oneLotteryAdd":              oneChain.OneLotteryAdd,
	"oneLotteryEdit":             oneChain.OneLotteryEdit,
	"oneLotteryDelete":           oneChain.OneLotteryDelete,
	"oneLotteryQuery":            oneChain.OneLotteryQuery,
	"oneLotteryHistoryQuery":     oneChain.OneLotteryHistoryQuery,
	"oneLotteryHistoryDelete":    oneChain.OneLotteryHistoryDelete,
	"oneLotteryOldHistoryQuery":  oneChain.OneLotteryOldHistoryQuery,
	"oneLotteryBet":              oneChain.OneLotteryBet,
	"oneLotteryBetQuery":         oneChain.OneLotteryBetQuery,
	"oneLotteryBetOver":          oneChain.OneLotteryBetOver,
	"oneLotteryGetTicketNumbers": oneChain.OneLotteryGetTicketNumbers,
	"oneLotteryRefund":           oneChain.OneLotteryRefund, // inner interface
	"oneLotteryStart":            oneChain.OneLotteryStart,  // inner interface
	//oneChainConsensus chaincode hander
	"oneChainConsensusVote": consensus.OneChainConsensusVote, // inner interface
	"oneLotteryTest":        oneChain.OneLotteryTest,
}

type OneLotteryChaincode struct {
}

func init() {
	format := logging.MustStringFormatter("%{shortfile} %{time:15:04:05.000} [%{module}] %{level:.4s} : %{message}")
	backend := logging.NewLogBackend(os.Stderr, "", 0)
	backendFormatter := logging.NewBackendFormatter(backend, format)

	logging.SetBackend(backendFormatter).SetLevel(logging.DEBUG, "onelotteryChaincode")
}

func checkDbChaincodeId(stub shim.ChaincodeStubInterface) error {
	if dbChaincodeId == "" {
		CCId, err := stub.GetState(DB_CHAINDOCE_ID)
		if err != nil {
			return errors.New("Failed to get " + DB_CHAINDOCE_ID + err.Error())
		}
		dbChaincodeId = string(CCId)
	}
	return nil
}

func setDbChaincodeId(stub shim.ChaincodeStubInterface, ccid string) error {
	//init rpc invoke dbchaincode
	dbChaincodeId = ccid
	err := stub.PutState(DB_CHAINDOCE_ID, []byte(dbChaincodeId))
	if err != nil {
		return errors.New("Failed to save " + DB_CHAINDOCE_ID)
	}
	args := [][]byte{[]byte("SetTrustCCid"), []byte(stub.GetTxID())}
	_, err = stub.InvokeChaincode(dbChaincodeId, args)
	if err != nil {
		err = errors.New("SetTrustCCid failed!" + err.Error())
	}
	return err
}

// Init method will be called during deployment.
// The deploy transaction metadata is supposed to contain the administrator cert
func (t *OneLotteryChaincode) Init(stub shim.ChaincodeStubInterface, function string, args []string) (data []byte, err error) {
	myLogger.Debug("Init Chaincode...")
	myLogger.Debugf("function=%v,args=%v\n", function, args)

	// parameter check
	if len(args) != 2 {
		myLogger.Debugf("arguments number %v\n", len(args))
		return nil, errors.New("Incorrect number of arguments. Expecting 2")
	}

	setDbChaincodeId(stub, args[1])
	myStub := invokeCCDB.NewInvokeCCDB(stub, dbChaincodeId, true)

	cert, err := stub.GetCallerCertificate()
	if err != nil {
		return nil, errors.New("Get CallerCertificate failed:" + err.Error())
	}
	args = append([]string{base64.StdEncoding.EncodeToString(cert)}, args[:1]...)

	// parameter convert
	args = parameterConvert(args)

	h := handler["oneChainInit"]
	if h == nil {
		myLogger.Error("OneChain init function no set")
		return nil, errors.New("OneChain init function no set")
	}
	data, err = h(myStub, function, args)
	if err != nil {
		return data, err
	}

	// zxcoin init
	h = handler["zxCoinInit"]
	if h == nil {
		myLogger.Error("ZxcoinChain init function no set")
		return nil, errors.New("ZxcoinChain init function no set")
	}
	return h(myStub, function, args)
}

func (t *OneLotteryChaincode) Invoke(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	myLogger.Debugf("function=%v,args=%v\n", function, args)

	err := checkDbChaincodeId(stub)
	if err != nil {
		return nil, err
	}
	myStub := invokeCCDB.NewInvokeCCDB(stub, dbChaincodeId, true)

	oneChain.OneChainInitArgs(myStub)
	oneChain.OneChainCheckHistoryLottery()

	isFunctionWithdraw := len(function) >= 14 && function[:14] == "zxCoinWithdraw"
	if !isFunctionWithdraw && function != "oneChainConsensusVote" {
		cert, err := stub.GetCallerCertificate()
		if err != nil {
			return nil, errors.New("Get CallerCertificate failed:" + err.Error())
		}
		args = append([]string{base64.StdEncoding.EncodeToString(cert)}, args...)
		// parameter convert
		args = parameterConvert(args)
	}

	h := handler[function]
	if h == nil {
		myLogger.Error("function not found")
		return nil, errors.New("function not found")
	}
	return h(myStub, function, args)
}

func (t *OneLotteryChaincode) Query(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	myLogger.Debugf("Query [%s]:", function, args)

	err := checkDbChaincodeId(stub)
	if err != nil {
		return nil, err
	}
	myStub := invokeCCDB.NewInvokeCCDB(stub, dbChaincodeId, false)

	isFunctionWithdraw := len(function) >= 14 && function[:14] == "zxCoinWithdraw"
	if !isFunctionWithdraw {
		args = append([]string{""}, args...)
	}

	h := handler[function]
	if h == nil {
		myLogger.Error("function not found")
		return nil, errors.New("function not found")
	}
	return h(myStub, function, args)
}

func main() {
	// init cert path
	ret := client_sdk.FabricSdkInit("/opt/gopath/src/peersafe.com/onelottery/chaincode/", "/opt/gopath/src/peersafe.com/onelottery/chaincode/", "core", false)
	if ret != 0 {
		myLogger.Debugf("FabricSdkInit failed \n")
		return
	}

	primitives.SetSecurityLevel("SHA3", 256)
	err := shim.Start(new(OneLotteryChaincode))
	if err != nil {
		fmt.Printf("Error starting OneLotteryChaincode: %s", err)
	}
}
