package ci;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock notifier for testing. Records all status calls without making HTTP requests.
 */
public class MockNotifier implements Notifier {

    private final List<StatusCall> calls = new ArrayList<>();

    @Override
    public void setStatus(String owner, String repo, String sha, String state, String description) {
        calls.add(new StatusCall(owner, repo, sha, state, description));
    }

    public List<StatusCall> getCalls() {
        return calls;
    }

    public StatusCall getLastCall() {
        return calls.isEmpty() ? null : calls.get(calls.size() - 1);
    }

    public void clear() {
        calls.clear();
    }

    public record StatusCall(String owner, String repo, String sha, String state, String description) {}
}
