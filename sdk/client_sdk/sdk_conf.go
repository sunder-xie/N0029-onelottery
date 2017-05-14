package client_sdk

import (
	"encoding/base64"
	"fmt"
	"io/ioutil"
	"path/filepath"

	"github.com/hyperledger/fabric/core/crypto/primitives"
	"github.com/spf13/viper"
	"golang.org/x/crypto/sha3"
)

func getECAAAddr() string {
	return viper.GetString("peer.pki.eca.paddr")
}

func isTLSEnabled() bool {
	return viper.GetBool("peer.tls.enabled")
}

func getClientCert() string {
	return viper.GetString("peer.tls.cert.file")
}

func getServerName() string {
	return viper.GetString("peer.tls.serverhostoverride")
}

func isPkiTLSEnabled() bool {
	return viper.GetBool("peer.pki.tls.enabled")
}

func getPkiClientCert() string {
	return viper.GetString("peer.pki.tls.rootcert.file")
}

func getPkiServerName() string {
	return viper.GetString("peer.pki.tls.serverhostoverride")
}

func getRawsPath(name string) string {
	configurationPath := viper.GetString("peer.fileSystemPath")

	// Set configuration path
	configurationPath = filepath.Join(
		configurationPath,
		"crypto", "client", name,
	)

	keystorePath := filepath.Join(configurationPath, "ks")

	rawsPath := filepath.Join(keystorePath, "raw")
	return rawsPath
}

func GetUserDir(name string) string {
	configurationPath := viper.GetString("peer.fileSystemPath")

	// Set configuration path
	configurationPath = filepath.Join(
		configurationPath,
		"crypto", "client", name,
	)

	return configurationPath
}

func GetPathForAlias(enrollId string, alias string) string {
	return filepath.Join(getRawsPath(enrollId), alias)
}

func GetEnrollmentKeyFilename() string {
	return "enrollment.key"
}

func GetEnrollmentCertFilename() string {
	return "enrollment.cert"
}

func getRegisterIdFileName() string {
	return "register.id"
}

func getEnrollIdFileName() string {
	return "enrollment.id"
}

func isSecurityEnabled() bool {
	return viper.GetBool("security.enabled")
}

func getPeerAddress() string {
	return viper.GetString("peer.address")
}

func getDevMode() string {
	return viper.GetString("chaincode.mode")
}

func GetListenEventAddress() string {
	return viper.GetString("event.listenAddress")
}

func GetRegistrarId() string {
	return viper.GetString("eca.registrarId")
}

func getRegistrarSecret() string {
	return viper.GetString("eca.registrarSecret")
}

func GetChainCodeIdName() string {
	return viper.GetString("chaincode.id.name")
}

func GetDBChainCodeIdName() string {
	return viper.GetString("chaincode.id.db_cc_id")
}

func LoadCertX509CertRaw(enrollId string) ([]byte, error) {
	path := GetPathForAlias(enrollId, GetEnrollmentCertFilename())
	ClientSdkLogger.Debugf("Loading certificate [%s]...", path)

	raw, err := ioutil.ReadFile(path)

	if err != nil {
		ClientSdkLogger.Errorf("Failed loading certificate [%s].", err.Error())

		return nil, err
	}

	cert, _, err := primitives.PEMtoCertificateAndDER(raw)

	return cert.Raw, nil
}

func LoadCertX509AndDer(enrollId string) (string, error) {
	path := GetPathForAlias(enrollId, GetEnrollmentCertFilename())
	ClientSdkLogger.Debugf("Loading certificate [%s]...", path)

	raw, err := ioutil.ReadFile(path)
	if err != nil {
		ClientSdkLogger.Errorf("Failed loading certificate [%s].", err.Error())

		return "", err
	}

	cert, _, err := primitives.PEMtoCertificateAndDER(raw)

	pubKey := base64.StdEncoding.EncodeToString(cert.Raw)
	ClientSdkLogger.Debugf("loadCertX509AndDer,pubkey:%s", pubKey)
	return pubKey, nil
}

func getPubkeyHashByCert(userCert string) (pubHash string) {
	if userCert == "" {
		ClientSdkLogger.Error("GetPubkeyHashByCert,userCert may empty!")
		return ""
	}

	hash := sha3.New224()

	hash.Write([]byte(userCert))
	hash_context := hash.Sum(nil)
	return fmt.Sprintf("%x", hash_context)
}

func IsSignEnabled() bool {
	return viper.GetBool("chaincode.signEnabled")
}
