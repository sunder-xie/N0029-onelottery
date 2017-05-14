package db

import (
	"encoding/json"
	"errors"

	"github.com/hyperledger/fabric/core/chaincode/shim"
)

type Ticket struct {
	TxnID        string `json:"txnID"`        // 投注的交易ID
	Attendee     string `json:"attendee"`     // 投注人
	AttendeeName string `json:"attendeeName"` // 投注人姓名
	Numbers      string `json:"numbers"`      // 投注号码
	Amount       uint64 `json:"amount"`       // 投注金额
	CreateTime   uint64 `json:"createTime'`   // 投注时间
	LotteryID    string `json:"lotteryID"`    // 活动ID
	stub         shim.ChaincodeStubInterface
}

func NewTicket(stub shim.ChaincodeStubInterface) *Ticket {
	return &Ticket{stub: stub}
}

// Get tablename
func (t *Ticket) TableName() string {
	return "Ticket_" + t.LotteryID
}

// Create ticket table
func (t *Ticket) CreateTable(lotteryID string) error {
	// lottery had exist
	if t.TableExist(t.TableName()) {
		return nil
	}

	t.LotteryID = lotteryID

	err := t.stub.CreateTable(t.TableName(), []*shim.ColumnDefinition{
		&shim.ColumnDefinition{Name: "TxnID", Type: shim.ColumnDefinition_STRING, Key: true},
		&shim.ColumnDefinition{Name: "Attendee", Type: shim.ColumnDefinition_STRING, Key: false},
		&shim.ColumnDefinition{Name: "AttendeeName", Type: shim.ColumnDefinition_STRING, Key: false},
		&shim.ColumnDefinition{Name: "Numbers", Type: shim.ColumnDefinition_STRING, Key: false},
		&shim.ColumnDefinition{Name: "Amount", Type: shim.ColumnDefinition_UINT64, Key: false},
		&shim.ColumnDefinition{Name: "CreateTime", Type: shim.ColumnDefinition_UINT64, Key: false},
	})
	if err != nil {
		return errors.New("Failed creating Ticket table." + err.Error())
	}

	return nil
}

// Delete ticket table
func (t *Ticket) DeleteTable(lotteryID string) error {
	if !t.TableExist(lotteryID) {
		//		return errors.New("Table not exist!")
		return nil
	}

	t.LotteryID = lotteryID

	// do detele table
	err := t.stub.DeleteTable(t.TableName())
	if err != nil {
		logger.Errorf("Delete ticket table failed,err:%v\n", err)
		return err
	}

	return err
}

// Add ticket
func (t *Ticket) Add(lotteryID string) error {
	var err error

	t.LotteryID = lotteryID
	t.CreateTable(lotteryID)

	// insert to db
	ok, err := t.stub.InsertRow(t.TableName(), ticketToRaw(t))
	if !ok && err == nil {
		return errors.New("Ticket was already exist.")
	}
	return err
}

// func (t *Ticket) EditTicket() error {
// 	var err error
// 	// update to db
// 	ok, err := t.stub.ReplaceRow(t.TableName(), ticketToRaw(t))
// 	if !ok && err == nil {
// 		return errors.New("Ticket was already exist.")
// 	}
// 	return err
// }

// Get one ticket
func (t *Ticket) GetOneByID(lotteryID, id string) (*Ticket, error) {
	t.LotteryID = lotteryID
	// do get
	row, err := t.stub.GetRow(t.TableName(), []shim.Column{shim.Column{Value: &shim.Column_String_{String_: id}}})
	if err != nil {
		return nil, err
	}
	if len(row.Columns) == 0 {
		return nil, nil
	}
	return rowToTicket(row, t.stub), nil
}

// Get all tickets
func (t *Ticket) GetTickets(lotteryID, attendee string) ([]*Ticket, error) {
	logger.Debugf("[GetTickets] lotteryId=%v,attendee=%v\n", lotteryID, attendee)

	if !t.TableExist(lotteryID) {
		//		return errors.New("Table not exist!")
		return nil, nil
	}

	t.LotteryID = lotteryID
	logger.Debugf("ticket.TableName=%v\n", t.TableName())

	rowChannel, err := t.stub.GetRows(t.TableName(), []shim.Column{})
	if err != nil {
		return nil, err
	}

	var lotteryTicket, attendeeTicket []*Ticket

	for {
		select {
		case row, ok := <-rowChannel:
			if !ok {
				rowChannel = nil
			} else {
				ticket := rowToTicket(row, t.stub)
				// get ticket for attendee
				if ticket.Attendee == attendee {
					attendeeTicket = append(attendeeTicket, ticket)
				}
				// get ticket for attendee
				lotteryTicket = append(lotteryTicket, ticket)
				logger.Debugf("GetTickets range row,ticket = %v\n", *ticket)
			}
		}
		if rowChannel == nil {
			break
		}
	}

	if attendee != "" {
		return attendeeTicket, nil
	}

	return lotteryTicket, nil
}

func rowToTicket(row shim.Row, stub shim.ChaincodeStubInterface) *Ticket {
	return &Ticket{
		TxnID:        row.Columns[0].GetString_(),
		Attendee:     row.Columns[1].GetString_(),
		AttendeeName: row.Columns[2].GetString_(),
		Numbers:      row.Columns[3].GetString_(),
		Amount:       row.Columns[4].GetUint64(),
		CreateTime:   row.Columns[5].GetUint64(),
		stub:         stub,
	}
}

func ticketToRaw(t *Ticket) shim.Row {
	return shim.Row{
		Columns: []*shim.Column{
			&shim.Column{Value: &shim.Column_String_{String_: t.TxnID}},
			&shim.Column{Value: &shim.Column_String_{String_: t.Attendee}},
			&shim.Column{Value: &shim.Column_String_{String_: t.AttendeeName}},
			&shim.Column{Value: &shim.Column_String_{String_: t.Numbers}},
			&shim.Column{Value: &shim.Column_Uint64{Uint64: t.Amount}},
			&shim.Column{Value: &shim.Column_Uint64{Uint64: t.CreateTime}},
		},
	}
}

// check value of struct GetTickets
func (t *Ticket) checkValue() error {
	if t.TxnID == "" || t.Attendee == "" || t.AttendeeName == "" || t.Numbers == "" || t.CreateTime == 0 {
		return errors.New("parameter error")
	}
	return nil
}

// unmarshal args to struct GetTickets
func (t *Ticket) Unmarshal(args string) error {
	// parse json to struct
	err := json.Unmarshal([]byte(args), t)
	if err != nil {
		return err
	}
	return nil
}

// Lottery tickets table exist or not
func (t *Ticket) TableExist(lotteryID string) bool {
	t.LotteryID = lotteryID
	table, err := t.stub.GetTable(t.TableName())
	if err == shim.ErrTableNotFound || table == nil{
		return false
	}
	return true
}
