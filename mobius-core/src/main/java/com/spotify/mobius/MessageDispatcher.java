/*
 * -\-\-
 * Mobius
 * --
 * Copyright (c) 2017-2020 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 */
package com.spotify.mobius;

import static com.spotify.mobius.internal_util.Preconditions.checkNotNull;

import com.spotify.mobius.disposables.Disposable;
import com.spotify.mobius.functions.Consumer;
import com.spotify.mobius.runners.WorkRunner;
import javax.annotation.Nonnull;

/**
 * Dispatches messages to a given runner.
 *
 * @param <M> message type (typically a model, event, or effect descriptor type)
 */
class MessageDispatcher<M> implements Consumer<M>, Disposable {

  @Nonnull private final WorkRunner runner;
  @Nonnull private final Consumer<M> consumer;

  private volatile boolean disposed = false;

  MessageDispatcher(WorkRunner runner, Consumer<M> consumer) {
    this.runner = checkNotNull(runner);
    this.consumer = checkNotNull(consumer);
  }

  @Override
  public void accept(final M message) {
    if (disposed) {
      return;
    }

    runner.post(
        () -> {
          try {
            consumer.accept(message);
          } catch (Throwable throwable) {
            MobiusHooks.handleError(
                new RuntimeException(
                    "Consumer threw an exception when accepting message: " + message, throwable));
          }
        });
  }

  @Override
  public void dispose() {
    disposed = true;
    runner.dispose();
  }
}
