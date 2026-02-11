package ci;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Notifier factory pattern.
 */
public class NotifierTest {

    @Test
    void mockNotifier_recordsCalls() {
        MockNotifier mock = new MockNotifier();

        mock.setStatus("mockowner", "mock", "mocksha1", "pending", "Build started");
        mock.setStatus("mockowner", "mock", "mocksha1", "success", "Build passed");

        assertEquals(2, mock.getCalls().size());

        MockNotifier.StatusCall last = mock.getLastCall();
        assertEquals("mockowner", last.owner());
        assertEquals("mock", last.repo());
        assertEquals("mocksha1", last.sha());
        assertEquals("success", last.state());
        assertEquals("Build passed", last.description());
    }

    @Test
    void mockNotifier_clear_removesCalls() {
        MockNotifier mock = new MockNotifier();
        mock.setStatus("mockowner", "mock", "mocksha1", "pending", "Build started");

        mock.clear();

        assertTrue(mock.getCalls().isEmpty());
        assertNull(mock.getLastCall());
    }

    @Test
    void notifierFactory_givenToken_returnsGitHubNotifier() {
        Notifier notifier = NotifierFactory.create("valid-token");
        assertInstanceOf(GitHubNotifier.class, notifier);
    }

    @Test
    void notifierFactory_noToken_returnsMockNotifier() {
        Notifier notifier = NotifierFactory.create(null);
        assertInstanceOf(MockNotifier.class, notifier);

        Notifier notifier2 = NotifierFactory.create("");
        assertInstanceOf(MockNotifier.class, notifier2);
    }

    @Test
    void gitHubNotifier_nullToken_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new GitHubNotifier(null));
        assertThrows(IllegalArgumentException.class, () -> new GitHubNotifier(""));
        assertThrows(IllegalArgumentException.class, () -> new GitHubNotifier("   "));
    }
}
