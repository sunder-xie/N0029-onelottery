package db

import (
	"encoding/json"
	"errors"
	"fmt"

	"github.com/hyperledger/fabric/core/chaincode/shim"
)

type RuleBall struct {
	RuleID      string `json:"ruleID"`      // 球类规则id
	Percentage1 uint32 `json:"percentage1"` // 一等奖所占比例
	Percentage2 uint32 `json:"percentage2"` // 二等奖所占比例
	Percentage3 uint32 `json:"percentage3"` // 三等奖所占比例
	IsUsed      bool   `json:"-"`           // 该规则是否被使用
	stub        shim.ChaincodeStubInterface
}

// get tablename
func (r *RuleBall) TableName() string {
	return "BallRule"
}

func NewBallRule(stub shim.ChaincodeStubInterface) *RuleBall {
	return &RuleBall{stub: stub}
}

// Create BallRule table
func (r *RuleBall) CreateTable() error {
	if table,err := r.stub.GetTable(r.TableName()); table != nil && err == nil {
		logger.Debugf("Exist RuleBall table\n")
		return nil
	}
	err := r.stub.CreateTable(r.TableName(), []*shim.ColumnDefinition{
		&shim.ColumnDefinition{Name: "RuleID", Type: shim.ColumnDefinition_STRING, Key: true},
		&shim.ColumnDefinition{Name: "Percentage1", Type: shim.ColumnDefinition_UINT32, Key: false},
		&shim.ColumnDefinition{Name: "Percentage2", Type: shim.ColumnDefinition_UINT32, Key: false},
		&shim.ColumnDefinition{Name: "Percentage3", Type: shim.ColumnDefinition_UINT32, Key: false},
		&shim.ColumnDefinition{Name: "IsUsed", Type: shim.ColumnDefinition_BOOL, Key: false},
	})
	if err != nil {
		return errors.New("Failed creating BallRule table." + err.Error())
	}
	return nil
}

// Add rule
func (r *RuleBall) Add(args string) error {
	// parse json
	err := r.Unmarshal(args)
	if err != nil {
		return err
	}
	// check value
	err = r.checkValue()
	if err != nil {
		return err
	}
	// generate id
	r.RuleID = r.stub.GetTxID()
	logger.Debugf("[AddRule] generate id=%v\n", r.RuleID)

	// insert to db
	ok, err := r.stub.InsertRow(r.TableName(), ballToRow(r))
	if !ok && err == nil {
		return errors.New("Rule was already exist.")
	}
	if err != nil {
		return errors.New("Addrule failed!" + err.Error())
	}
	return nil
}

// Delete rule
func (r *RuleBall) Delete() error {
	// check id
	err := r.checkId()
	if err != nil {
		return err
	}
	// get current rule
	currentRule, err := r.GetOneByID(r.RuleID)
	if err != nil {
		return err
	}
	if currentRule.IsUsed {
		return errors.New("rule had been used, can not be edit.")
	}
	// do delete
	err = r.stub.DeleteRow(
		r.TableName(),
		[]shim.Column{shim.Column{Value: &shim.Column_String_{String_: r.RuleID}}},
	)
	return err
}

// Edit rule
func (r *RuleBall) Update() error {
	// check value
	err := r.checkId()
	if err != nil {
		return err
	}
	// get current rule
	currentRule, err := r.GetOneByID(r.RuleID)
	if err != nil {
		return err
	}
	if currentRule.IsUsed {
		return errors.New("rule had been used, can not be edit.")
	}

	if r.Percentage1 != 0 {
		currentRule.Percentage1 = r.Percentage1
	}
	if r.Percentage2 != 0 {
		currentRule.Percentage2 = r.Percentage2
	}
	if r.Percentage3 != 0 {
		currentRule.Percentage3 = r.Percentage3
	}

	currentRule.IsUsed = r.IsUsed

	// do update
	ok, err := r.stub.ReplaceRow(r.TableName(), ballToRow(currentRule))
	if !ok && err == nil {
		return errors.New("Rule was does not exist.")
	}
	return err
}

// Get one rule
func (r *RuleBall) GetOneByID(id string) (*RuleBall, error) {
	// do get
	row, err := r.stub.GetRow(r.TableName(), []shim.Column{shim.Column{Value: &shim.Column_String_{String_: id}}})
	if err != nil {
		logger.Debugf("=========no data!!! %v\n", err)
		return nil, errors.New("Failed get rule row data.")
	}
	if len(row.Columns) == 0 {
		return nil, errors.New("rule not exist!")
	}

	rule := rowToBall(row, r.stub)
	logger.Debug("RuleBall GetRule rule=%v\n", rule)

	return rule, nil
}

// Get all rules
func (r *RuleBall) GetAll() ([]*RuleBall, error) {
	logger.Debugf("Start RuleBall \n")
	rowChannel, err := r.stub.GetRows(r.TableName(), []shim.Column{})
	if err != nil {
		logger.Debugf("RuleBall GetRows,err = %v\n", err)
		return nil, fmt.Errorf("RuleBall getrules. %s", err)
	}

	var rules []*RuleBall
	for {
		select {
		case row, ok := <-rowChannel:
			if !ok {
				rowChannel = nil
			} else {
				rule := rowToBall(row, r.stub)
				rules = append(rules, rule)
				logger.Debugf("RuleBall range row,rule = %v\n", *rule)
			}
		}
		if rowChannel == nil {
			break
		}
	}

	if len(rules) == 0 {
		return nil, nil
	}

	logger.Debugf("End RuleBall GetRules\n")
	return rules, nil
}

func rowToBall(row shim.Row, stub shim.ChaincodeStubInterface) *RuleBall {
	return &RuleBall{
		RuleID:      row.Columns[0].GetString_(),
		Percentage1: row.Columns[1].GetUint32(),
		Percentage2: row.Columns[2].GetUint32(),
		Percentage3: row.Columns[3].GetUint32(),
		IsUsed:      row.Columns[4].GetBool(),
		stub:        stub,
	}
}

func ballToRow(b *RuleBall) shim.Row {
	return shim.Row{
		Columns: []*shim.Column{
			&shim.Column{Value: &shim.Column_String_{String_: b.RuleID}},
			&shim.Column{Value: &shim.Column_Uint32{Uint32: b.Percentage1}},
			&shim.Column{Value: &shim.Column_Uint32{Uint32: b.Percentage2}},
			&shim.Column{Value: &shim.Column_Uint32{Uint32: b.Percentage3}},
			&shim.Column{Value: &shim.Column_Bool{Bool: b.IsUsed}},
		},
	}
}

// check id is nil or not
func (r *RuleBall) checkId() error {
	if r.RuleID == "" {
		return errors.New("parameter error")
	}
	return nil
}

// check value of struct RuleBall
func (r *RuleBall) checkValue() error {
	if r.RuleID == "" || r.Percentage1 == 0 || r.Percentage2 == 0 || r.Percentage3 == 0 || r.Percentage1 > 100 || r.Percentage2 > 100 || r.Percentage3 > 100 {
		return errors.New("parameter error")
	}
	return nil
}

// unmarshal args to struct RuleBall
func (r *RuleBall) Unmarshal(args string) error {
	// parse json to struct
	err := json.Unmarshal([]byte(args), r)
	if err != nil {
		return err
	}
	return nil
}
