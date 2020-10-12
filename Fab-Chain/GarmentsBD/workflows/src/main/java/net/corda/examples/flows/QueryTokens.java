package net.corda.examples.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.tokens.contracts.types.TokenPointer;
import com.r3.corda.lib.tokens.contracts.types.TokenType;
import com.r3.corda.lib.tokens.workflows.utilities.QueryUtilities;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.utilities.ProgressTracker;
import net.corda.examples.states.FungibleTokenState;

public class QueryTokens {

    @StartableByRPC
    @InitiatingFlow
    public static class GetTokenBalance extends FlowLogic<String> {
        private final ProgressTracker progressTracker = new ProgressTracker();
        private final String currency;


        public GetTokenBalance(String currency) {
            this.currency = currency;
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Override
        @Suspendable
        public String call() throws FlowException {
            //get house states on ledger with uuid as input tokenId
            StateAndRef<FungibleTokenState> stateAndRef = getServiceHub().getVaultService().
                    queryBy(FungibleTokenState.class).getStates().stream()
                    .filter(sf -> sf.getState().getData().getSymbol().equals(currency)).findAny()
                    .orElseThrow(() -> new IllegalArgumentException("FungibleHouseTokenState symbol=\"" + currency + "\" not found from vault"));

            //get the RealEstateEvolvableTokenType object
            FungibleTokenState evolvableTokenType = stateAndRef.getState().getData();

            //get the pointer pointer to the house
            TokenPointer<FungibleTokenState> tokenPointer = evolvableTokenType.toPointer(FungibleTokenState.class);

            Amount<TokenType> amount = QueryUtilities.tokenBalance(getServiceHub().getVaultService(), tokenPointer);
            return "\n You currently have " + amount.getQuantity() + " " + currency + " Tokens\n";
        }
    }

}