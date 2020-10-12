# Fab-Chain Prototype Demonstration

This sample will be a simple walk though of the creation, issuance, and transfer of the Fab-Tokens.

## Pre-Requisites

See https://docs.corda.net/getting-set-up.html.

For a brief introduction to Token SDK in Corda, see https://medium.com/corda/introduction-to-token-sdk-in-corda-9b4dbcf71025

## Usage

### Running the CorDapp

Open a terminal and go to the project root directory and type: (to deploy the nodes using bootstrapper)
```
./gradlew clean deployNodes
```
Then type: (to run the nodes)
```
./build/nodes/runnodes
```

### Interacting with the nodes

#### Shell

When started via the command line, each node will display an interactive shell:

    Welcome to the Corda interactive shell.
    Useful commands include 'help' to see what is available, and 'bye' to shut down the node.

    Tue July 09 11:58:13 GMT 2019>>>

You can use this shell to interact with your node.


Create FabTokens on the ledger using BGMEA's terminal
```
flow start CreateToken symbol: FabToken, valuation: 80
```
This will create a linear state of type FabTokens in BGMEA's vault. We will use the FabTokens to tokenise our Garment Assets

BGMEA will now issue some FabToken to VendorA and VendorB as a part of their Asset Tokenization. 
Run below command via BGMEA's terminal.

```
flow start IssueToken currency: FabToken, quantity: 300, receiver: VendorA
flow start IssueToken currency: FabToken, quantity: 300, receiver: VendorB
flow start IssueToken currency: FabToken, quantity: 0, receiver: Manufacturer
```
Now at VendorA's terminal, we can check the tokens by running:
```
flow start GetTokenBalance currency: FabToken
```
Since FactoryA now has 300 tokens,we now move tokens from VendorA's to VendorB

```
flow start MoveToken currency: FabToken, receiver: VendorB, quantity: 170
flow start MoveToken currency: FabToken, receiver: VendorB, quantity: 100
```
To view the number of Tokens held by both VendorA and VendorB by executing the following Query flow in their respective terminals.
```
flow start GetTokenBalance currency: FabToken
```

To start a Spring Boot server for each node ,opena terminal/command prompt for each node.
Then enter the following command, replacing X with A, B, C and D:
```
.\gradlew.bat runPartyXServer
```
To invoke a server,go to webbrowser and in the following links:
```
BGMEA : http://localhost:50005/
VendorA: http://localhost:50006/
VendorB : http://localhost:50007/
Manufacturer : http://localhost:50008/

```