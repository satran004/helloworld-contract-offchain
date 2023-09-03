package com.test;

import com.bloxbean.cardano.aiken.AikenTransactionEvaluator;
import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.client.api.UtxoSupplier;
import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.api.DefaultUtxoSupplier;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.function.helper.ScriptUtxoFinders;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.plutus.blueprint.PlutusBlueprintUtil;
import com.bloxbean.cardano.client.plutus.blueprint.model.PlutusVersion;
import com.bloxbean.cardano.client.plutus.spec.BytesPlutusData;
import com.bloxbean.cardano.client.plutus.spec.ConstrPlutusData;
import com.bloxbean.cardano.client.plutus.spec.PlutusData;
import com.bloxbean.cardano.client.plutus.spec.PlutusScript;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.ScriptTx;
import com.bloxbean.cardano.client.quicktx.Tx;

public class HelloContractOffchain {
    String senderMnemonic = "busy scare swamp save green sad comic embark wing idle phrase kick monster protect collect napkin exchange stove drill guide napkin sting crane educate";
    Account sender = new Account(Networks.testnet(), senderMnemonic);
    String receiver = "addr_test1qz3s0c370u8zzqn302nppuxl840gm6qdmjwqnxmqxme657ze964mar2m3r5jjv4qrsf62yduqns0tsw0hvzwar07qasqeamp0c";
    String compiledCode = "59010d010000323232323232323232322223232533300a3232533300c002100114a06644646600200200644a66602400229404c8c94ccc044cdc78010028a511330040040013015002375c60260026eb0cc01cc024cc01cc024011200048040dd71980398048032400066e3cdd71980318040022400091010d48656c6c6f2c20576f726c642100149858c94ccc028cdc3a400000226464a66601e60220042930b1bae300f00130080041630080033253330093370e900000089919299980718080010a4c2c6eb8c038004c01c01058c01c00ccc0040052000222233330073370e0020060164666600a00a66e000112002300d001002002230053754002460066ea80055cd2ab9d5573caae7d5d0aba21";

    BackendService backendService = new BFBackendService("http://localhost:8080/api/v1/", "dummy_key");
    PlutusScript plutusScript = PlutusBlueprintUtil.getPlutusScriptFromCompiledCode(compiledCode, PlutusVersion.v2);
    String scrtiptAddr = AddressProvider.getEntAddress(plutusScript, Networks.testnet()).toBech32();

    public void lock() {
        //create datum
        PlutusData datum = ConstrPlutusData.of(0, BytesPlutusData.of(sender.getBaseAddress().getPaymentCredentialHash().get()));

        Tx tx = new Tx()
                .payToContract(scrtiptAddr, Amount.ada(10), datum)
                .from(sender.baseAddress());

        QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);
        Result<String> result = quickTxBuilder.compose(tx)
                .withSigner(SignerProviders.signerFrom(sender))
                .completeAndWait(System.out::println);

        System.out.println(result);
    }

    public void unlock() throws ApiException {
        PlutusData datum = ConstrPlutusData.of(0, BytesPlutusData.of(sender.getBaseAddress().getPaymentCredentialHash().get()));

        UtxoSupplier utxoSupplier = new DefaultUtxoSupplier(backendService.getUtxoService());
        Utxo scriptUtxo = ScriptUtxoFinders.findFirstByInlineDatum(utxoSupplier, scrtiptAddr, datum).orElseThrow();

        PlutusData redeemer = ConstrPlutusData.of(0, BytesPlutusData.of("Hello, World!"));

        ScriptTx sctipTx = new ScriptTx()
                .collectFrom(scriptUtxo, redeemer)
                .payToAddress(receiver, Amount.ada(10))
                .attachSpendingValidator(plutusScript);

        QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);
        Result<String> result = quickTxBuilder.compose(sctipTx)
                .feePayer(receiver)
                .collateralPayer(sender.baseAddress())
                .withSigner(SignerProviders.signerFrom(sender))
                .withRequiredSigners(sender.getBaseAddress())
                .withTxEvaluator(new AikenTransactionEvaluator(backendService))
                .completeAndWait(System.out::println);

        System.out.println(result);
    }


    public static void main(String[] args) throws ApiException {
        HelloContractOffchain helloContractOffchain = new HelloContractOffchain();
        helloContractOffchain.lock();
//        helloContractOffchain.unlock();
    }
}
