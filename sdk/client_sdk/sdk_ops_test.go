/*
Copyright IBM Corp. 2016 All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

		 http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package client_sdk

import (
	"github.com/hyperledger/fabric/core/util"
	membersrvc "github.com/hyperledger/fabric/membersrvc/protos"
	pb "github.com/hyperledger/fabric/protos"
	"testing"
)

func TestSdkOps_InitSdk(t *testing.T) {
	err := initClientSdk("./", "./")
	if err != nil {
		t.Logf("Error Init client sdk ops: %s", err)
		t.Fail()
	}
}

func TestSdkOps_Register(t *testing.T) {
	registerUserReq := new(membersrvc.RegisterUserReq)
	identity := new(membersrvc.Identity)
	identity.Id = "client_sdk_test1"
	registerUserReq.Id = identity
	registerUserReq.Role = membersrvc.Role_CLIENT
	registerUserReq.Attributes = nil
	registerUserReq.Affiliation = "institutions"
	registerUserReq.Registrar = nil
	registerUserReq.Sig = nil
	_, err := register(registerUserReq)
	if err != nil {
		t.Logf("Error Register ops: %s", err)
		t.Fail()
	}
}

// func TestSdkOps_Enroll(t *testing.T) {
// 	//err := enroll("client_sdk_test1", "HHxTqtJHjAdG")
// 	err := Enroll("jim", "6avZQLwcUe9b")
// 	if err != nil {
// 		t.Logf("Error Enroll ops: %s", err)
// 		t.Fail()
// 	}
// }

func TestSdkOps_RegisterAndEnroll(t *testing.T) {
	registerUserReq := new(membersrvc.RegisterUserReq)
	identity := new(membersrvc.Identity)
	identity.Id = "client_sdk_test2"
	registerUserReq.Id = identity
	registerUserReq.Role = membersrvc.Role_CLIENT
	registerUserReq.Attributes = nil
	registerUserReq.Affiliation = "institutions"
	registerUserReq.Registrar = nil
	registerUserReq.Sig = nil
	err := registerAndEnroll(registerUserReq)
	if err != nil {
		t.Logf("Error RegisterAndEnroll ops: %s", err)
		t.Fail()
	}
}

func TestSdkOps_Deploy(t *testing.T) {
	// Build the spec
	//chaincodePath := "github.com/hyperledger/fabric/examples/chaincode/go/chaincode_example02"
	f := "init"
	args := util.ToChaincodeArgs(f, "a", "100", "b", "200")
	spec := &pb.ChaincodeSpec{Type: pb.ChaincodeSpec_GOLANG, ChaincodeID: &pb.ChaincodeID{Name: "mycc"},
		CtorMsg: &pb.ChaincodeInput{args}, SecureContext: "client_sdk_test2"}

	result, err := deploy(spec)
	if err != nil {
		t.Fail()
		t.Logf("Error in Testsdk_ops deploy call: %s", err)
	}

	if result != nil {
		t.Logf("Deploy result = %s", result.ChaincodeSpec)
	}
}

func TestSdkOps_Invoke(t *testing.T) {
	f := "invoke"
	argsInvoke := util.ToChaincodeArgs(f, "a", "b", "10")
	chainCodeId := "mycc" //buildResult.ChaincodeSpec.GetChaincodeID().Name
	invokeSpec := &pb.ChaincodeSpec{Type: pb.ChaincodeSpec_GOLANG, ChaincodeID: &pb.ChaincodeID{Name: chainCodeId},
		CtorMsg: &pb.ChaincodeInput{argsInvoke}, SecureContext: "jim"}

	invokequeryPayload := &pb.ChaincodeInvocationSpec{ChaincodeSpec: invokeSpec}
	pbResponse, err := invoke(invokequeryPayload)
	if err != nil {
		t.Fail()
		t.Logf("Error in Testsdk_ops invoke call: %s", err)
	} else {
		t.Logf("invoke resultCode = %d, msg = %s", pbResponse.Status, pbResponse.Msg)
	}
}

func TestSdkOps_Query(t *testing.T) {
	f := "query"
	argsQuery := util.ToChaincodeArgs(f, "a")
	chainCodeId := "mycc"
	querySpec := &pb.ChaincodeSpec{Type: pb.ChaincodeSpec_GOLANG, ChaincodeID: &pb.ChaincodeID{Name: chainCodeId},
		CtorMsg: &pb.ChaincodeInput{argsQuery}, SecureContext: "jim"}

	queryPayload := &pb.ChaincodeInvocationSpec{ChaincodeSpec: querySpec}
	pbResponse, err := query(queryPayload)
	if err != nil {
		t.Fail()
		t.Logf("Error in Testsdk_ops query call: %s", err)
	} else {
		t.Logf("query resultCode = %d, msg = %s", pbResponse.Status, pbResponse.Msg)
	}
}
