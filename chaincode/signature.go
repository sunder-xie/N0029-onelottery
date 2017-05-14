package main

import (
	"crypto/ecdsa"
	"crypto/sha256"
	"crypto/sha512"
	"encoding/asn1"
	"encoding/base64"
	"errors"
	"fmt"
	"golang.org/x/crypto/sha3"
	"hash"
	"math/big"

	"github.com/hyperledger/fabric/core/chaincode/shim"
)

// ECDSASignature represents an ECDSA signature
type ECDSASignature struct {
	R, S *big.Int
}

func getHashSHA2(bitsize int) (hash.Hash, error) {
	switch bitsize {
	case 224:
		return sha256.New224(), nil
	case 256:
		return sha256.New(), nil
	case 384:
		return sha512.New384(), nil
	case 512:
		return sha512.New(), nil
	case 521:
		return sha512.New(), nil
	default:
		return nil, fmt.Errorf("Invalid bitsize. It was [%d]. Expected [224, 256, 384, 512, 521]", bitsize)
	}
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
	// var hash hash.Hash
	// var err error
	// switch primitives.GetHashAlgorithm() {
	// case "SHA2":
	// 	hash, err = getHashSHA2(bitsize)
	// case "SHA3":
	// hash, err = getHashSHA3(bitsize)
	// default:
	// 	return nil, fmt.Errorf("Invalid hash algorithm " + primitives.GetHashAlgorithm())
	// }
	hash, err := getHashSHA3(bitsize)
	if err != nil {
		return nil, err
	}

	hash.Write(msg)
	return hash.Sum(nil), nil
}

func verifyImpl(vk *ecdsa.PublicKey, signature, message []byte) (bool, error) {
	ecdsaSignature := new(ECDSASignature)
	_, err := asn1.Unmarshal(signature, ecdsaSignature)
	if err != nil {
		return false, err
	}

	h, err := computeHash(message, vk.Params().BitSize)
	if err != nil {
		return false, err
	}

	return ecdsa.Verify(vk, h, ecdsaSignature.R, ecdsaSignature.S), nil
}

func checkSigma(stub shim.ChaincodeStubInterface, pubKey string) (bool, error) {
	myLogger.Debug("Check caller...")

	// decode base64 string
	certificate, err := base64.StdEncoding.DecodeString(pubKey)
	if err != nil {
		myLogger.Errorf("[%v]\n", err)
		return false, errors.New("Failed decode base64 string.")
	}

	sigma, err := stub.GetCallerMetadata()
	if err != nil {
		return false, errors.New("Failed getting metadata")
	}
	payload, err := stub.GetPayload()
	if err != nil {
		return false, errors.New("Failed getting payload")
	}
	binding, err := stub.GetBinding()
	if err != nil {
		return false, errors.New("Failed getting binding")
	}

	myLogger.Debugf("passed certificate [% x]", certificate)
	myLogger.Debugf("passed sigma [% x]", sigma)
	myLogger.Debugf("passed payload [% x]", payload)
	myLogger.Debugf("passed binding [% x]", binding)

	ok, err := stub.VerifySignature(
		certificate,
		sigma,
		append(payload, binding...),
	)
	if err != nil {
		myLogger.Errorf("Failed checking signature [%s]", err)
		return ok, err
	}
	if !ok {
		myLogger.Error("Invalid signature")
	}

	myLogger.Debug("Check caller...Verified!")

	return ok, err
}

/*
func checkSigma(stub shim.ChaincodeStubInterface, pubKey string) (bool, error) {
	myLogger.Debug("Check Sigma...")

	certbuf, err := base64.StdEncoding.DecodeString(pubKey)
	if err != nil {
		myLogger.Errorf("[%v]\n", err)
		//return nil, err
	}

	key, err := x509.ParsePKIXPublicKey(certbuf)
	if err != nil {
		myLogger.Errorf("ParsePKIXPublicKey error[%v]\n", err)
		//return nil, err
	}

	ecdsaPub, ok := key.(*ecdsa.PublicKey)
	if !ok {
		myLogger.Errorf("To ecdsa.PublicKey error[%v]\n", !ok)
		//return nil, errors.New("not an RSA public key")
	}

	sigma, err := stub.GetCallerMetadata()
	if err != nil {
		return false, errors.New("Failed getting metadata")
	}
	payload, err := stub.GetPayload()
	if err != nil {
		return false, errors.New("Failed getting payload")
	}

	myLogger.Debugf("passed sigma [% x]", sigma)
	myLogger.Debugf("passed payload [% x]", payload)

	//ok, err := stub.VerifySignature(
	ok, err = verifyImpl(
		ecdsaPub,
		sigma,
		payload,
	)
	if err != nil {
		myLogger.Errorf("Failed checking signature [%s]", err)
		return ok, err
	}
	if !ok {
		myLogger.Error("Invalid signature")
	}

	myLogger.Debug("Check caller...Verified!")

	return ok, err
}
*/

func parameterConvert(args []string) []string {
	hashPub, err := computeHash([]byte(args[0]), 224)
	if err != nil {
		myLogger.Error("Invalid signature")
	}

	args[0] = fmt.Sprintf("%x", hashPub)
	myLogger.Debug(args)

	return args
}
