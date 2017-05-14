var dockerFileContents = "from hyperledger/fabric-ccenv" + "\n" +
	"COPY . $GOPATH/src/peersafe.com/onelottery/" + "\n" +
	"WORKDIR $GOPATH" + "\n\n" +
	"RUN go install peersafe.com/onelottery && cp $GOPATH/src/peersafe.com/onelottery/core.yaml $GOPATH/bin && mv $GOPATH/bin/onelottery $GOPATH/bin/%s";


var keep = [
	".go",
	".yaml",
	".json",
	".c",
	".h",
	".pem",
	".key",
	".chain",
	".cert.chain",
	".cert",
	".id"
];