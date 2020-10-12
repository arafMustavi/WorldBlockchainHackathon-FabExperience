package net.corda.examples;

import com.google.common.collect.ImmutableList;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.StartedMockNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FlowTests {
    private final MockNetwork network = new MockNetwork(ImmutableList.of("com.tokenizedhouse"));
    private final StartedMockNode a = network.createNode();
    private final StartedMockNode b = network.createNode();

    @Before
    public void setup() {
        network.runNetwork();
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }

    @Test
    public void dummyTest() {

    }
}
