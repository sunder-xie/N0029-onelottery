package db

import (
	"encoding/json"
	"errors"

	"github.com/hyperledger/fabric/core/chaincode/shim"
)

type RulePrize struct {
	RuleID     string `json:"ruleID"`     // 抽奖类规则id
	Percentage uint32 `json:"percentage"` // 中奖者所占比例
	Hide       bool   `json:"hide"`       // 该规则是否隐藏
	CreateTime uint64 `json:"createTime"` // 创建时间
	IsUsed     bool   `json:"-"`          // 该规则是否被使用
	stub       shim.ChaincodeStubInterface
}

// get tablename
func (r *RulePrize) TableName() string {
	return "PrizeRule"
}

func NewPrizeRule(stub shim.ChaincodeStubInterface) *RulePrize {
	return &RulePrize{stub: stub}
}

// Create PrizeRule table
func (r *RulePrize) CreateTable() error {
	if table,err := r.stub.GetTable(r.TableName()); table != nil && err == nil {
		logger.Debugf("Exist RulePrize table\n")
		return nil
	}
	logger.Debugf("Start creating RulePrize table\n")
	err := r.stub.CreateTable(r.TableName(), []*shim.ColumnDefinition{
		&shim.ColumnDefinition{Name: "RuleID", Type: shim.ColumnDefinition_STRING, Key: true},
		&shim.ColumnDefinition{Name: "Percentage", Type: shim.ColumnDefinition_UINT32, Key: false},
		&shim.ColumnDefinition{Name: "Hide", Type: shim.ColumnDefinition_BOOL, Key: false},
		&shim.ColumnDefinition{Name: "CreateTime", Type: shim.ColumnDefinition_UINT64, Key: false},
		&shim.ColumnDefinition{Name: "IsUsed", Type: shim.ColumnDefinition_BOOL, Key: false},
	})
	if err != nil {
		logger.Debugf("Failed creating PrizeRule table.%v\n", err)
		return errors.New("Failed creating PrizeRule table." + err.Error())
	}
	logger.Debugf("End creating RulePrize table\n")
	return nil
}

// Add rule
func (r *RulePrize) Add() error {
	// generate id
	r.RuleID = r.stub.GetTxID()
	logger.Debugf("[AddRule] generate id=%v\n", r.RuleID)

	// check value
	err := r.checkValue()
	if err != nil {
		logger.Errorf("RulePrize AddRule checkValue error=%v\n", err)
		return err
	}
	// insert to db
	ok, err := r.stub.InsertRow(r.TableName(), prizeToRow(r))
	if !ok && err == nil {
		logger.Errorf("RulePrize AddRule InsertRow error,Lottery was already exist.\n")
		return errors.New("Rule was already exist.")
	}
	logger.Debugf("End RulePrize AddRule.\n")
	return err
}

// Delete rule
func (r *RulePrize) Delete() error {
	logger.Debugf("Start RulePrize DeleteRule\n")
	// check id
	err := r.checkId()
	if err != nil {
		logger.Errorf("RulePrize DeleteRule checkId error=%v\n", err)
		return err
	}

	// get current rule
	currentRule, err := r.GetOneByID(r.RuleID)
	if err != nil {
		return err
	}
	if currentRule == nil {
		return nil
	}
	if currentRule.IsUsed {
		return errors.New("rule had been used, can not be edit.")
	}

	// do delete
	err = r.stub.DeleteRow(
		r.TableName(),
		[]shim.Column{shim.Column{Value: &shim.Column_String_{String_: r.RuleID}}},
	)
	logger.Debugf("End RulePrize DeleteRule,err = %v\n", err)
	return err
}

// Edit rule
func (r *RulePrize) Update() error {
	logger.Debugf("Start RulePrize EditRule\n")
	// check value
	err := r.checkId()
	if err != nil {
		logger.Errorf("RulePrize EditRule checkId error=%v\n", err)
		return err
	}

	// get current rule
	currentRule, err := r.GetOneByID(r.RuleID)
	logger.Debugf("row : %v\n", currentRule)
	if err != nil {
		return err
	}
	if currentRule == nil {
		return errors.New("rule not exist.")
	}

	currentRule.Hide = r.Hide
	logger.Debugf("row used flag : %v\n", r.IsUsed)
	if !currentRule.IsUsed {
		currentRule.IsUsed = r.IsUsed
		if r.Percentage != 0 {
			currentRule.Percentage = r.Percentage
		}
	}

	// do update
	ok, err := r.stub.ReplaceRow(r.TableName(), prizeToRow(currentRule))
	if !ok && err == nil {
		logger.Errorf("RulePrize EditRule Rule was does not exist\n")
		return errors.New("Rule was does not exist.")
	}
	return err
}

// Get one rule
func (r *RulePrize) GetOneByID(ruleId string) (*RulePrize, error) {
	logger.Debugf("========== rule.ID=%v\n", ruleId)
	// do get
	row, err := r.stub.GetRow(r.TableName(), []shim.Column{shim.Column{Value: &shim.Column_String_{String_: ruleId}}})
	if err != nil {
		logger.Debugf("=========no data!!! %v\n", err)
		return nil, err
	}
	if len(row.Columns) == 0 {
		return nil, nil
	}
	logger.Debugf("============ get row = %v\n", row)
	return rowToPrize(row, r.stub), nil
}

// Get all rules
func (r *RulePrize) GetAll() ([]*RulePrize, error) {
	logger.Debugf("Start RulePrize GetRules\n")
	rowChannel, err := r.stub.GetRows(r.TableName(), []shim.Column{})
	if err != nil {
		logger.Debugf("RulePrize GetRows,err = %v\n", err)
		return nil, errors.New("RulePrize getrules." + err.Error())
	}

	var rules []*RulePrize
	for {
		select {
		case row, ok := <-rowChannel:
			if !ok {
				rowChannel = nil
			} else {
				rule := rowToPrize(row, r.stub)
				rules = append(rules, rule)
				logger.Debugf("RulePrize range row,rule = %v\n", *rule)
			}
		}
		if rowChannel == nil {
			break
		}
	}

	if len(rules) == 0 {
		return nil, nil
	}

	logger.Debugf("End RulePrize GetRules\n")
	return rules, nil
}

func rowToPrize(row shim.Row, stub shim.ChaincodeStubInterface) *RulePrize {
	return &RulePrize{
		RuleID:     row.Columns[0].GetString_(),
		Percentage: row.Columns[1].GetUint32(),
		Hide:       row.Columns[2].GetBool(),
		CreateTime: row.Columns[3].GetUint64(),
		IsUsed:     row.Columns[4].GetBool(),
		stub:       stub,
	}
}

func prizeToRow(p *RulePrize) shim.Row {
	return shim.Row{
		Columns: []*shim.Column{
			&shim.Column{Value: &shim.Column_String_{String_: p.RuleID}},
			&shim.Column{Value: &shim.Column_Uint32{Uint32: p.Percentage}},
			&shim.Column{Value: &shim.Column_Bool{Bool: p.Hide}},
			&shim.Column{Value: &shim.Column_Uint64{Uint64: p.CreateTime}},
			&shim.Column{Value: &shim.Column_Bool{Bool: p.IsUsed}},
		},
	}
}

// check id is nil or not
func (r *RulePrize) checkId() error {
	if r.RuleID == "" {
		return errors.New("parameter error")
	}
	return nil
}

// check value of struct RulePrize
func (r *RulePrize) checkValue() error {
	if r.RuleID == "" || r.Percentage == 0 || r.Percentage > 100 || r.CreateTime == 0 {
		return errors.New("parameter error")
	}
	return nil
}

// unmarshal args to struct RulePrize
func (r *RulePrize) Unmarshal(args string) error {
	// parse json to struct
	err := json.Unmarshal([]byte(args), r)
	if err != nil {
		return err
	}
	return nil
}
