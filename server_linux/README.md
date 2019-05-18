
# Instructions on how to setup the server

this document guides you thru the process of 
- setting up the environment
	- installing go
	- installing python3
	- installing required python3 libraries
	- installing solc for deployment of contracts

- setting up a new validator node
	- creating keypair for you
- starting the new node
- configuring the server
- running the server

for instructions on how to setup the app please see `[~repo/android_app/README.md]`

### requirements
- ubuntu 18.04 LTS
- solc

#### setting up the environment
install go
install python3
if you would like to recompile burrow or the contract complete the section "recompile from scratch" now

go to the burrow directory:
```
cd burrow/
```

#### create new validation node
see https://github.com/hyperledger/burrow/blob/develop/docs/quickstart/single-full-node.md
```
./burrow spec -f1 | ./burrow configure -s- > burrow.toml
```
creates a single full (-f1) account and no participants (missing -p1)
this also creates the `.keys/data/` directory
if you want to find the address just
```
cat .keys/data/*
```
or
```
cat burrow.toml
```
and take the address from the output

#### starting the new node
see https://github.com/hyperledger/burrow/blob/develop/docs/quickstart/single-full-node.md
- install solc 
```
sudo snap install solc
```
- start the node:
```
./burrow start
```
- deploy the contract:
```
./burrow deploy --address put_address_here -f deploy/deploy_contract.yaml
```
*now copy the address of the deployed contract*


#### configuring the server
- open the file `/server_linux/settings.py` 
- insert a `password` of your choice
- insert your `address`
- insert your `contract_address`
*if you recompiled burrow you have to specify the location of your new burrow executable under `burrow_location`, you can also replace the existing burrow executable in `burrow` with the new one*

#### starting the server
```
python3 server.py
```

#### recompile from scratch

install burrow
```
go get github.com/hyperledger/burrow
cd $GOPATH/src/github.com/hyperledger/burrow
export GO111MODULE=on
make build
```


