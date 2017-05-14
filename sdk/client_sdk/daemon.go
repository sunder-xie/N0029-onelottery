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
	"fmt"
	"time"

	"github.com/hyperledger/fabric/events/consumer"
	pb "github.com/hyperledger/fabric/protos"
)

var (
	daemonShutDownNotify chan int
	obcEHClient          *consumer.EventsClient
)

type EventType int

const (
	Event_Type_Block             EventType = 0
	Event_Type_Rejection         EventType = 1
	Event_Type_ChainCode         EventType = 2
	Event_Type_Daemon_Start_Suc  EventType = 3
	Event_Type_Daemon_Start_Fail EventType = 4
	Event_Type_Daemon_Shut_down  EventType = 5
)

type EventCallBack func(EventType, interface{})

/**
* Description start a damemon to recieve the event
*
* @param eventAddress
* @param isListenToRejections
* @param chainCodeId
* @param callback
* @return ret
 */
func startDaemon(eventAddress string, isListenToRejections bool, chainCodeId string, callback EventCallBack) (ret int) {
	daemonShutDownNotify = make(chan int, 10)
	a := createEventClient(eventAddress, isListenToRejections, chainCodeId)
	if a == nil {
		ClientSdkLogger.Error("Error creating event client")
		callback(Event_Type_Daemon_Start_Fail, nil)
		return INTERNAL_ERR
	}

	callback(Event_Type_Daemon_Start_Suc, nil)

	for {
		select {
		case <-daemonShutDownNotify:
			ClientSdkLogger.Info("Recieve daemon shutdown notify!")
			goto ForEnd
		case b := <-a.notfy:
			fmt.Printf("\n")
			fmt.Printf("\n")
			fmt.Printf("Received block\n")
			fmt.Printf("--------------\n")
			for _, r := range b.Block.Transactions {
				fmt.Printf("Transaction:\n\t[%v]\n", r)
			}
			callback(Event_Type_Block, *b.Block)
		case r := <-a.rejected:
			fmt.Printf("\n")
			fmt.Printf("\n")
			fmt.Printf("Received rejected transaction\n")
			fmt.Printf("--------------\n")
			fmt.Printf("Transaction error:\n%s\t%s\n", r.Rejection.Tx.Txid, r.Rejection.ErrorMsg)
			callback(Event_Type_Rejection, *r.Rejection)
		case ce := <-a.cEvent:
			fmt.Printf("\n")
			fmt.Printf("\n")
			fmt.Printf("Received chaincode event\n")
			fmt.Printf("------------------------\n")
			fmt.Printf("Chaincode Event:%v\n", ce)
			callback(Event_Type_ChainCode, *ce.ChaincodeEvent)
		}
	}

ForEnd:

	callback(Event_Type_Daemon_Shut_down, nil)

	return SUCCESS
}

/**
* Description stop the damemon to recieve the event
*
* @return
 */
func stopDaemon() {
	if nil != obcEHClient {
		ClientSdkLogger.Info("Call obcEHClient.Stop()")
		obcEHClient.Stop()
		obcEHClient = nil
		daemonShutDownNotify <- 1
	}
}

type adapter struct {
	notfy              chan *pb.Event_Block
	rejected           chan *pb.Event_Rejection
	cEvent             chan *pb.Event_ChaincodeEvent
	listenToRejections bool
	chaincodeID        string
}

//GetInterestedEvents implements consumer.EventAdapter interface for registering interested events
func (a *adapter) GetInterestedEvents() ([]*pb.Interest, error) {
	if a.chaincodeID != "" {
		return []*pb.Interest{
			//不监听block event
			{EventType: pb.EventType_REJECTION},
			{EventType: pb.EventType_CHAINCODE,
				RegInfo: &pb.Interest_ChaincodeRegInfo{
					ChaincodeRegInfo: &pb.ChaincodeReg{
						ChaincodeID: a.chaincodeID,
						EventName:   ""}}}}, nil
	}
	return []*pb.Interest{{EventType: pb.EventType_BLOCK}, {EventType: pb.EventType_REJECTION}}, nil
}

//Recv implements consumer.EventAdapter interface for receiving events
func (a *adapter) Recv(msg *pb.Event) (bool, error) {
	if o, e := msg.Event.(*pb.Event_ChaincodeEvent); e {
		a.cEvent <- o
		return true, nil
	}
	if o, e := msg.Event.(*pb.Event_Block); e {
		a.notfy <- o
		return true, nil
	}
	if o, e := msg.Event.(*pb.Event_Rejection); e {
		if a.listenToRejections {
			a.rejected <- o
		}
		return true, nil
	}

	return false, fmt.Errorf("Receive unkown type event: %v", msg)
}

//Disconnected implements consumer.EventAdapter interface for disconnecting
func (a *adapter) Disconnected(err error) {
	ClientSdkLogger.Errorf("Event listen disconnected:%s", err.Error())
	stopDaemon()
}

func createEventClient(eventAddress string, listenToRejections bool, cid string) *adapter {
	done := make(chan *pb.Event_Block)
	reject := make(chan *pb.Event_Rejection)
	adapter := &adapter{notfy: done, rejected: reject, listenToRejections: listenToRejections, chaincodeID: cid, cEvent: make(chan *pb.Event_ChaincodeEvent)}
	obcEHClient, _ = consumer.NewEventsClient(eventAddress, 30*time.Second, adapter)
	if err := obcEHClient.Start(); err != nil {
		fmt.Printf("could not start chat %s\n", err)
		obcEHClient.Stop()
		obcEHClient = nil
		return nil
	}

	return adapter
}
