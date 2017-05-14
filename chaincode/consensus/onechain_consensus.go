package consensus

import (
	"errors"
	"fmt"
	"sort"
	"strconv"
	"sync"
	"time"

	"encoding/asn1"
	"golang.org/x/crypto/sha3"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/hyperledger/fabric/core/util"
	"github.com/op/go-logging"
	"peersafe.com/onelottery/sdk/client_sdk"

	"github.com/hyperledger/fabric/core/crypto/primitives"
	membersrvc "github.com/hyperledger/fabric/membersrvc/protos"
)

const MAX_VOTE_SCALE float32 = 0.8

// ECertSubjectRole is the ASN1 object identifier of the subject's role.
var ECertSubjectRole = asn1.ObjectIdentifier{2, 1, 3, 4, 5, 6, 7}

var (
	once                    sync.Once
	pOneChainConsensus      *OneChainConsensus
	mylogger                = logging.MustGetLogger("consensus")
	results                 = make(map[string]int64)
	result_lock             sync.RWMutex
	lotteryCheckUserMap     = make(map[string]*userHashRoundMap)
	lotteryCheckUserMapLock sync.RWMutex
)

type OneChainConsensus struct {
	m           map[string]*voteChanSt
	chainCodeId string
	mapLock     sync.RWMutex
}

type voteSt struct {
	vote  int64
	round int
}

type voteChanSt struct {
	ch         chan *voteSt
	hasStarted bool // true if collect goroutine has started
}

type userHashRoundMap struct {
	m map[string]int
}

type sliceVotes []int64

func NewConsensus() *OneChainConsensus {
	once.Do(func() {
		pOneChainConsensus = &OneChainConsensus{m: make(map[string]*voteChanSt, 256)}
	})
	return pOneChainConsensus
}

//get one lottery consensus result
func (t *OneChainConsensus) OneChainConsensusGetResult(lotteryId string) (int64, error) {

	if lotteryId == "" {
		mylogger.Error("LotteryId is empty.")
		return 0, errors.New("LotteryId is empty.")
	}

	result_lock.RLock()
	defer result_lock.RUnlock()
	if v, ok := results[lotteryId]; ok {
		return v, nil
	} else {
		mylogger.Errorf("Lottery %s consensus has not been over.", lotteryId)
		return 0, errors.New("Lottery " + lotteryId + "consensus has not been over.")
	}

}

func (t *OneChainConsensus) OneChainConsensusStart(chainCodeId string, lotteryId string) error {

	mylogger.Debug("***********************OneChainConsensusStart****************")
	// var err error
	t.chainCodeId = chainCodeId

	now := time.Now().UnixNano() / 1000000 // nanoseconds -> milliseconds
	round := 1
	mylogger.Debugf("now:%d", now)

	sProposal := strconv.FormatInt(now, 10)
	sround := strconv.Itoa(round)

	args := util.ToChaincodeArgs("oneChainConsensusVote", lotteryId, sProposal, sround)

	ret, msg := client_sdk.FabricSdkInvoke(t.chainCodeId, args, "ConsensusVoter", nil)
	if ret != 0 {
		mylogger.Errorf("FabricSdkInvoke failed:%s", msg)
		return errors.New("FabricSdkInvoke failed!")
	} else {
		mylogger.Debug("FabricSdkInvoke over!")
	}

	//add lotteryId into map and start collect votes
	t.mapLock.Lock()
	defer t.mapLock.Unlock()
	_, find := t.m[lotteryId]
	if find {
		mylogger.Errorf("lotteryId %s has in vote map already.", lotteryId)
		return errors.New("lotteryId has in vote map already.")
	} else {
		t.m[lotteryId] = &voteChanSt{make(chan *voteSt, 16), false}
		mylogger.Debugf("LotteryId %s has been created in votesMap.", lotteryId)
	}

	// create user hash map for checking repeated vote
	t.createUserHashMapByLotteryId(lotteryId)

	return nil
}

// args[0] : LotteryId
// args[1] : vote
// args[2] : round
func (t *OneChainConsensus) OneChainConsensusVote(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {

	if len(args) != 3 {
		return nil, errors.New("Invalid arg numbers in Vote, correct is 3")
	}

	bFlag, err := checkVoteRole(stub)
	if err != nil {
		return nil, err
	} else if !bFlag {
		return nil, errors.New("oneChainConsensusVote only can be called by Voter!")
	}

	// parse lotteryId, vote, round
	lotteryId := args[0]
	vote, err := strconv.ParseInt(args[1], 10, 64)
	if err != nil {
		return nil, errors.New("Parse vote failed!")
	}

	round, err := strconv.Atoi(args[2])
	if err != nil {
		return nil, errors.New("Parse round failed!")
	}

	mylogger.Debugf("Received vote from other peers, lotteryId: %s, vote: %d, round:%d", lotteryId, vote, round)

	t.mapLock.Lock()
	defer t.mapLock.Unlock()
	voteChan, find := t.m[lotteryId]
	if find {
		cert, _ := stub.GetCallerCertificate()
		userHash := t.getCertHash(cert)
		if t.checkForRepeatVote(lotteryId, userHash, round) {
			if voteChan.hasStarted {
				voteChan.ch <- &voteSt{vote, round}
				mylogger.Debugf("Vote %d has been written to channel.", vote)
			} else {
				go t.collectVotes(voteChan.ch, lotteryId)
				t.m[lotteryId].hasStarted = true
				voteChan.ch <- &voteSt{vote, round}
			}
		}
	} else {
		mylogger.Errorf("Do not find lottery channel! %s", lotteryId)
		return nil, errors.New("Do not find lottery channel!")
	}

	return nil, nil
}

func (t *OneChainConsensus) DelResult(lotteryId string) {
	result_lock.Lock()
	defer result_lock.Unlock()
	delete(results, lotteryId)
}

func (t *OneChainConsensus) collectVotes(ch chan *voteSt, lotteryId string) {
	mylogger.Debugf("[goroutine %s]Come into collectVotes", lotteryId)

	round := 1
	ticker := time.NewTicker(time.Second * 8)
	votesMap := make(map[int64]int, 1024)
	nextRoundVotesBuf := make([]*voteSt, 0, 128)

	for {
		select {
		case v := <-ch:
			if round >= 4 {
				if v.round == round {
					t.appendToCountMap(votesMap, v.vote)
					mylogger.Debugf("[goroutine %s]Append vote to CountMap %d", lotteryId, v.vote)
				}
			} else {
				if v.round > round {
					// push into buffer
					nextRoundVotesBuf = append(nextRoundVotesBuf, v)
					mylogger.Debugf("[goroutine %s]Push into nextRoundBuffer: cur round:%d, vote round:%d", lotteryId, round, v.round)
				} else if v.round == round {
					t.appendToVotesMap(votesMap, v.vote)
					mylogger.Debugf("[goroutine %s]Get vote from channel: %d", lotteryId, v.vote)
				}
			}
		case <-ticker.C:
			mylogger.Debugf("[goroutine %s]Round %d timeout!", lotteryId, round)
			nextRoundVotesBuf = t.putNextRoundToMap(round, nextRoundVotesBuf, votesMap)
			if round <= 3 {
				proposal, err := t.getProposal(votesMap)
				if err != nil {
					mylogger.Errorf("[goroutine %s]get proposal failed", lotteryId)
					return //
				}
				mylogger.Debugf("[goroutine %s]Round %d proposal:%d", lotteryId, round, proposal)

				round++
				//send out the proposal and start second round
				t.sendProposal(lotteryId, proposal, round)

			} else {
				if len(votesMap) == 0 {
					mylogger.Errorf("[goroutine %s]VotesMap is empty!", lotteryId)
					return
				}

				// clear UserHashMap because this round always be 4 and it will call some
				// bugs in checkForRepeatedVote
				t.clearUserHashMapByLotteryId(lotteryId)
				t.createUserHashMapByLotteryId(lotteryId)

				vote, bFlag := t.hasRightVoteInCountMap(votesMap)
				if bFlag {
					//write to ledger
					result_lock.Lock()
					results[lotteryId] = vote
					result_lock.Unlock()
					mylogger.Debugf("[goroutine %s]Write vote result %d to map", lotteryId, vote)

					t.mapLock.Lock()
					close(ch)
					delete(t.m, lotteryId)
					t.mapLock.Unlock()

					// clear user hash map
					t.clearUserHashMapByLotteryId(lotteryId)

					return
				} else {
					// send another
					t.sendProposal(lotteryId, vote, round)
					mylogger.Debugf("[goroutine %s]no RightVoteInCountMapsend, now send proposal again : %d", lotteryId, vote)
				}
			}
			votesMap = map[int64]int{} // clear votes map
		}
	}
}

func (t *OneChainConsensus) putNextRoundToMap(round int, nextRoundBuf []*voteSt, votesMap map[int64]int) []*voteSt {
	newBuf := make([]*voteSt, 0, 128)

	for _, v := range nextRoundBuf {
		if round == v.round {
			if _, ok := votesMap[v.vote]; ok {
				votesMap[v.vote] += 1
			} else {
				votesMap[v.vote] = 1
			}
		} else if round < v.round {
			newBuf = append(newBuf, v)
		}
	}

	return newBuf
}

func (t *OneChainConsensus) sendProposal(lotteryId string, proposal int64, round int) error {

	sProposal := strconv.FormatInt(proposal, 10)
	sround := strconv.Itoa(round)

	args := util.ToChaincodeArgs("oneChainConsensusVote", lotteryId, sProposal, sround)

	ret, msg := client_sdk.FabricSdkInvoke(t.chainCodeId, args, "ConsensusVoter", nil)
	if ret != 0 {
		mylogger.Errorf("FabricSdkInvoke failed:%s", msg)
		return errors.New("FabricSdkInvoke failed!")
	} else {
		mylogger.Debug("FabricSdkInvoke over!")
	}

	mylogger.Debugf("Send proposal over, lotteryId:%s, proposal:%d, round:%d", lotteryId, proposal, round)
	return nil
}

//get mid one in the votes
func (t *OneChainConsensus) getProposal(m map[int64]int) (int64, error) {
	if len(m) == 0 {
		return 0, errors.New("votes number invalid to 0")
	}

	votes := make([]int64, 0, 1024)

	for k, _ := range m {
		votes = append(votes, k)
	}
	// sort first
	sort.Sort(sliceVotes(votes))
	return votes[len(votes)/2], nil
}

// using map to delete repeat votes
func (t *OneChainConsensus) appendToVotesMap(m map[int64]int, vote int64) {
	_, find := m[vote]
	if !find {
		m[vote] = 0
	}
}

func (t *OneChainConsensus) appendToCountMap(m map[int64]int, vote int64) {
	_, find := m[vote]
	if find {
		m[vote]++
	} else {
		m[vote] = 1
	}
}

func (t *OneChainConsensus) hasRightVoteInCountMap(m map[int64]int) (int64, bool) {
	mapLen := 0
	var maxVoteKey int64 = -1
	var maxVote int = -1
	for k, v := range m {
		if v > maxVote {
			maxVote = v
			maxVoteKey = k
		}
		mapLen += v
	}
	f := float32(maxVote) / float32(mapLen)
	mylogger.Debugf("k:%d, v:%d, f:%f", maxVoteKey, maxVote, f)
	if f > MAX_VOTE_SCALE {
		return maxVoteKey, true
	} else {
		return maxVoteKey, false
	}
}

// return true if this voter has not voted repeated
func (t *OneChainConsensus) checkForRepeatVote(lotteryId string, userHash []byte, round int) bool {
	hashRoundMap, ok := lotteryCheckUserMap[lotteryId]
	if ok {
		s := fmt.Sprintf("%x%d", userHash, round)
		_, ok = hashRoundMap.m[s]
		if ok {
			mylogger.Debugf("For lottery %s, voter %x has voted in round %d", lotteryId[:8], userHash, round)
			return false
		} else {
			mylogger.Debugf("For lottery %s, voter %x has not voted in round %d", lotteryId[:8], userHash, round)
			hashRoundMap.m[s] = 1
			return true
		}
	} else {
		mylogger.Errorf("cannot find %s in lotteryCheckUserMap", lotteryId[:8])
		return false
	}
}

func (t *OneChainConsensus) clearUserHashMapByLotteryId(lotteryId string) {
	lotteryCheckUserMapLock.Lock()
	delete(lotteryCheckUserMap, lotteryId)
	lotteryCheckUserMapLock.Unlock()
}

func (t *OneChainConsensus) createUserHashMapByLotteryId(lotteryId string) {
	lotteryCheckUserMapLock.Lock()
	lotteryCheckUserMap[lotteryId] = &userHashRoundMap{make(map[string]int)}
	lotteryCheckUserMapLock.Unlock()
}

func (t *OneChainConsensus) getCertHash(msg []byte) []byte {
	hash := sha3.New224()
	hash.Write(msg)
	return hash.Sum(nil)
}

// sort interface implements
func (t sliceVotes) Len() int {
	return len(t)
}

func (t sliceVotes) Swap(i, j int) {
	t[i], t[j] = t[j], t[i]
}

func (t sliceVotes) Less(i, j int) bool {
	return t[i] < t[j]
}

func checkVoteRole(stub shim.ChaincodeStubInterface) (bool, error) {

	mylogger.Debug("#########################checkVoteRole##########################")

	cert, err := stub.GetCallerCertificate()
	if err != nil {
		mylogger.Errorf(" [checkVoteRole] GetCallerCertificate failed %s", err)
		return false, err
	}

	x509cert, err := primitives.DERToX509Certificate(cert)
	if err != nil {
		mylogger.Errorf("[checkVoteRole] DERToX509Certificate[%v]\n", err)
		return false, err
	}

	roleRaw, err := primitives.GetCriticalExtension(x509cert, ECertSubjectRole)
	if err != nil {
		mylogger.Errorf("[checkVoteRole] GetCriticalExtension[%v]\n", err)
		return false, err
	}

	role, err := strconv.ParseInt(string(roleRaw), 10, len(roleRaw)*8)
	if err != nil {
		mylogger.Errorf("[checkVoteRole] Failed parsing ECertSubjectRole in enrollment certificate: [%s]", err)
		return false, err
	}

	mylogger.Debugf("[checkVoteRole] role: %d", role)

	return membersrvc.Role(role) == membersrvc.Role_VOTER, nil
}
