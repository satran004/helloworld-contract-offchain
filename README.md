## Overview
This repo contains offchain Java Code to interact with [Aiken HelloWorld Contract](https://aiken-lang.org/example--hello-world).

### Aiken Contract Code

```shell
use aiken/hash.{Blake2b_224, Hash}
use aiken/list
use aiken/transaction.{ScriptContext}
use aiken/transaction/credential.{VerificationKey}
 
type Datum {
  owner: Hash<Blake2b_224, VerificationKey>,
}
 
type Redeemer {
  msg: ByteArray,
}
 
validator {
  fn hello_world(datum: Datum, redeemer: Redeemer, context: ScriptContext) -> Bool {
    let must_say_hello =
      redeemer.msg == "Hello, World!"
 
    let must_be_signed =
      list.has(context.transaction.extra_signatories, datum.owner)
 
    must_say_hello && must_be_signed
  }
}
```
