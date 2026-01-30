package ci;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppTest {
  @Test
  void add_returnsSum() {
    assertEquals(5, App.add(2, 3));
  }
}
