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
	"testing"
)

func TestDaemon_Start(t *testing.T) {
	eventCall := func(eventType EventType, event interface{}) {
		switch eventType {
		case Event_Type_Block:
			ClientSdkLogger.Infof("TestDaemon_Start,recieve Event_Type_Block")
			t.Log("TestDaemon_Start,recieve Event_Type_Block")
		case Event_Type_Rejection:
			t.Log("TestDaemon_Start,recieve Event_Type_Rejection")
			ClientSdkLogger.Infof("TestDaemon_Start,recieve Event_Type_Rejection")
		case Event_Type_ChainCode:
			t.Log("TestDaemon_Start,recieve Event_Type_ChainCode")
			ClientSdkLogger.Infof("TestDaemon_Start,recieve Event_Type_ChainCode")
		}
	}

	// ret := startDaemon("172.17.0.2:7053", true, "", eventCall)
	ret := startDaemon("127.0.0.2:7053", true, "", eventCall)
	if ret != SUCCESS {
		t.Log("Start daemon error!")
		t.Fail()
	}
}
