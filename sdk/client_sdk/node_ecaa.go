package client_sdk

import (
	"google.golang.org/grpc"

	"github.com/hyperledger/fabric/core/comm"

	membersrvc "github.com/hyperledger/fabric/membersrvc/protos"
	"google.golang.org/grpc/credentials"
	"path/filepath"
	"log"
)

func getECAAClient() (*grpc.ClientConn, membersrvc.ECAAClient, error) {
	ClientSdkLogger.Debug("Getting ECAA client...")

	conn, err := getClientConn(getECAAAddr(), "")
	if err != nil {
		ClientSdkLogger.Errorf("Failed getting client connection: [%s]", err)
	}

	client := membersrvc.NewECAAClient(conn)

	ClientSdkLogger.Debug("Getting ECAA client...done")

	return conn, client, nil
}

func getClientConn(address string, serverName string) (*grpc.ClientConn, error) {
	ClientSdkLogger.Debugf("Dial to addr:[%s], with serverName:[%s]...", address, serverName)

	 if isPkiTLSEnabled() {
	 	ClientSdkLogger.Debug("TLS enabled...")

		 pem, err := filepath.Abs(getPkiClientCert())
		 if err != nil {
			 log.Fatalf("Failed to get pem file %v", err)
		 }
		 creds, err := credentials.NewClientTLSFromFile(pem, getPkiServerName())
		 if err != nil {
			 log.Fatalf("Failed to create TLS credentials %v", err)
		 }

	 	return comm.NewClientConnectionWithAddress(address, false, true, creds)
	 }

	ClientSdkLogger.Debug("TLS disabled...")
	return comm.NewClientConnectionWithAddress(address, false, false, nil)
}
