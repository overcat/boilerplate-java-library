package io.github.overcat;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UtilsTest {

  @Test
  void add() {
    assert Utils.add(1, 2) == 3;
  }
}
