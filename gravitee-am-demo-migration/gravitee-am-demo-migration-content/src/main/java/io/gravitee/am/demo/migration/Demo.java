package io.gravitee.am.demo.migration;

import io.reactivex.Single;

public class Demo {
  public void main() {
    String key = generateKey("foo").blockingGet();
  }

  private Single<String> generateKey(String foo) {
    if (foo != null) {
      return Single.just(foo);
    }
    return Single.never();
  }
}
