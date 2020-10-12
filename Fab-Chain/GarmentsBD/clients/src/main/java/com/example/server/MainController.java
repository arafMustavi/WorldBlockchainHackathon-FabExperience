package com.example.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.corda.client.jackson.JacksonSupport;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.transactions.SignedTransaction;
import net.corda.examples.flows.EvolvableFungibleTokenFlow;
import net.corda.examples.flows.QueryTokens;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/api/example/") // The paths for HTTP requests are relative to this base path.
public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(RestController.class);
    private final CordaRPCOps proxy;
    private final CordaX500Name me;

    public MainController(NodeRPCConnection rpc) {
        this.proxy = rpc.getProxy();
        this.me = proxy.nodeInfo().getLegalIdentities().get(0).getName();

    }

    /**
     * Helpers for filtering the network map cache.
     */
    public String toDisplayString(X500Name name) {
        return BCStyle.INSTANCE.toString(name);
    }

    private boolean isNotary(NodeInfo nodeInfo) {
        return !proxy.notaryIdentities()
                .stream().filter(el -> nodeInfo.isLegalIdentity(el))
                .collect(Collectors.toList()).isEmpty();
    }

    private boolean isMe(NodeInfo nodeInfo) {
        return nodeInfo.getLegalIdentities().get(0).getName().equals(me);
    }

    private boolean isNetworkMap(NodeInfo nodeInfo) {
        return nodeInfo.getLegalIdentities().get(0).getName().getOrganisation().equals("Network Map Service");
    }

    @GetMapping(value = "/status", produces = TEXT_PLAIN_VALUE)
    private String status() {
        return "200";
    }

    @GetMapping(value = "/servertime", produces = TEXT_PLAIN_VALUE)
    private String serverTime() {
        return (LocalDateTime.ofInstant(proxy.currentNodeTime(), ZoneId.of("UTC"))).toString();
    }

    @GetMapping(value = "/addresses", produces = TEXT_PLAIN_VALUE)
    private String addresses() {
        return proxy.nodeInfo().getAddresses().toString();
    }

    @GetMapping(value = "/identities", produces = TEXT_PLAIN_VALUE)
    private String identities() {
        return proxy.nodeInfo().getLegalIdentities().toString();
    }

    @GetMapping(value = "/platformversion", produces = TEXT_PLAIN_VALUE)
    private String platformVersion() {
        return Integer.toString(proxy.nodeInfo().getPlatformVersion());
    }

    @GetMapping(value = "/peers", produces = APPLICATION_JSON_VALUE)
    public HashMap<String, List<String>> getPeers() {
        HashMap<String, List<String>> myMap = new HashMap<>();

        // Find all nodes that are not notaries, ourself, or the network map.
        Stream<NodeInfo> filteredNodes = proxy.networkMapSnapshot().stream()
                .filter(el -> !isNotary(el) && !isMe(el) && !isNetworkMap(el));
        // Get their names as strings
        List<String> nodeNames = filteredNodes.map(el -> el.getLegalIdentities().get(0).getName().toString())
                .collect(Collectors.toList());

        myMap.put("peers", nodeNames);
        return myMap;
    }

    @GetMapping(value = "/notaries", produces = TEXT_PLAIN_VALUE)
    private String notaries() {
        return proxy.notaryIdentities().toString();
    }

    @GetMapping(value = "/flows", produces = TEXT_PLAIN_VALUE)
    private String flows() {
        return proxy.registeredFlows().toString();
    }

    @GetMapping(value = "/states", produces = TEXT_PLAIN_VALUE)
    private String states() {
        return proxy.vaultQuery(ContractState.class).getStates().toString();
    }

    @GetMapping(value = "/me", produces = APPLICATION_JSON_VALUE)
    private HashMap<String, String> whoami() {
        HashMap<String, String> myMap = new HashMap<>();
        myMap.put("me", me.toString());
        return myMap;
    }

    @PostMapping("/create-token")
    @ResponseBody
    public ResponseEntity createBankToken(@RequestBody HashMap<String, Object> form) throws IllegalArgumentException {

//         ---------------------------------------------------------
//         {
//           "currencyName": "taka",
//           "valuation": 80
//         }
//         ---------------------------------------------------------

        try {
            String symbol = (String) form.get("currencyName");
            String valuation = (String) form.get("valuation");
//            BigDecimal valuation = BigDecimal.valueOf(Long.parseLong(String.valueOf((Integer) form.get("valuation"))));

            SignedTransaction result = proxy.startFlowDynamic(
                    EvolvableFungibleTokenFlow.CreateToken.class,
                    symbol,
                    valuation
            ).getReturnValue().get();
            // Return the response.
            final HashMap<String, Object> response = new HashMap<>();
            response.put("message", "CreateToken Flow completed successfully!");

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);
            // For the purposes of this demo app, we do not differentiate by exception type.
        } catch (Exception ex) {
            final HashMap<String, Object> response = new HashMap<>();
            response.put("message", "Failed to complete flow. " + ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
    }

    // ----------------------------- App Specific API Endpoints ---------------------------------
    //-------------------
    //
    //         CREATE TOKEN
    //
    //-------------------

    @PostMapping("/issue-token")
    @ResponseBody
    public ResponseEntity issueBankToken(@RequestBody HashMap<String, Object> form) throws IllegalArgumentException {

        // ---------------------------------------------------------
//         {
//           "receiver": "<party_identity>",
//           "currency": "taka",
//           "quantity": 120
//         }
        // ---------------------------------------------------------

        try {

            Party receiverParty = proxy.wellKnownPartyFromX500Name(
                    CordaX500Name.parse(String.valueOf(form.get("receiver")))
            );

            SignedTransaction result = proxy.startFlowDynamic(
                    EvolvableFungibleTokenFlow.IssueToken.class,
                    form.get("currency"),
                    Integer.parseInt((String) form.get("quantity")),
                    receiverParty
            ).getReturnValue().get();
            // Return the response.
            final HashMap<String, Object> response = new HashMap<>();
            response.put("message", "IssueToken Flow completed successfully!");

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(response);
            // For the purposes of this demo app, we do not differentiate by exception type.
        } catch (Exception ex) {
            final HashMap<String, Object> response = new HashMap<>();
            response.put("message", "Failed to complete flow. " + ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
    }
    //-------------------
    //
    //         ISSUE TOKEN
    //
    //-------------------

    @PostMapping("/move-token")
    @ResponseBody
    public ResponseEntity moveBankToken(@RequestBody HashMap<String, Object> form) throws IllegalArgumentException {

        // ---------------------------------------------------------
//         {
//           "receiver": "<party_identity>",
//           "currency": "taka",
//           "quantity": 120
//         }
        // ---------------------------------------------------------

        try {

            Party receiverParty = proxy.wellKnownPartyFromX500Name(
                    CordaX500Name.parse(String.valueOf(form.get("receiver")))
            );

            SignedTransaction result = proxy.startFlowDynamic(
                    EvolvableFungibleTokenFlow.MoveToken.class,
                    form.get("currency"),
                    receiverParty,
                    Integer.parseInt((String) form.get("quantity"))
            ).getReturnValue().get();
            // Return the response.
            final HashMap<String, Object> response = new HashMap<>();
            response.put("message", "MoveToken Flow completed successfully!");

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(response);
            // For the purposes of this demo app, we do not differentiate by exception type.
        } catch (Exception ex) {
            final HashMap<String, Object> response = new HashMap<>();
            response.put("message", "Failed to complete flow. " + ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
    }
    //-------------------
    //
    //         MOVE TOKEN
    //
    //-------------------

    @PostMapping("/get-token")
    @ResponseBody
    public ResponseEntity getToken(@RequestBody HashMap<String, Object> form) throws IllegalArgumentException {
        try {
            String currency = (String) form.get("currency");
            // String valuation = (String) form.get("valuation");

            String result = proxy.startFlowDynamic(
                    QueryTokens.GetTokenBalance.class,
                    currency
            ).getReturnValue().get();

            System.out.println("The Result is :" + result);
            // Return the response.
            final HashMap<String, Object> response = new HashMap<>();
            response.put("message", "Get token Flow completed successfully!" + result);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);
            // For the purposes of this demo app, we do not differentiate by exception type.
        } catch (Exception ex) {
            final HashMap<String, Object> response = new HashMap<>();
            response.put("message", "Failed to complete flow. " + ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }
        //         ---------------------------------------------------------
//         {
//           "currency": "taka",
//         }
//         ---------------------------------------------------------

    }
    //-------------------
    //
    //         GET TOKEN
    //
    //-------------------

    @Configuration
    class Plugin {
        @Bean
        public ObjectMapper registerModule() {
            return JacksonSupport.createNonRpcMapper();
        }
    }

}

