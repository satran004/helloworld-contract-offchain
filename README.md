## Overview
This repo contains offchain Java Code to interact with [Aiken HelloWorld Contract](https://aiken-lang.org/example--hello-world).

### Aiken Contract Code

```shell
use aiken/collection/list
use aiken/crypto.{VerificationKeyHash}
use cardano/transaction.{OutputReference, Transaction}

pub type Datum {
  owner: VerificationKeyHash,
}

pub type Redeemer {
  msg: ByteArray,
}

validator hello_world {
  spend(
    datum: Option<Datum>,
    redeemer: Redeemer,
    _own_ref: OutputReference,
    self: Transaction,
  ) {
    expect Some(Datum { owner }) = datum
    let must_say_hello = redeemer.msg == "Hello, World!"
    let must_be_signed = list.has(self.extra_signatories, owner)
    must_say_hello && must_be_signed
  }

  else(_) {
    fail
  }

}
```

### Configuration

- Provide "Blockfrost ProjectId" for PREPROD in ``HelloContractOffchain.java`` to create BackendService
- To lock, uncomment lock() method call in main method
- To unlock, uncomment unlock() method call in main method.

### Build

```
./mvnw clean package
```

### Run

```
./mvnw exec:java -Dexec.mainClass="com.test.HelloContractOffchain"
```
