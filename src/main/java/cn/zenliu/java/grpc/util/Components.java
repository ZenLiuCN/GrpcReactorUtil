package cn.zenliu.java.grpc.util;

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.function.Consumer;

/**
 * @author Zen.Liu
 * @apiNote
 * @since 2020-11-05
 */
interface Components {
    static <T> void whenNotCancel(StreamObserver<T> observer, Consumer<StreamObserver<T>> consumer) {
        if (isCancelled(observer)) return;
        consumer.accept(observer);
    }

    static <T> void whenNotCancelElse(
            StreamObserver<T> observer,
            Consumer<StreamObserver<T>> consumer,
            Runnable onCancel
    ) {
        if (isCancelled(observer)) onCancel.run();
        else
            consumer.accept(observer);
    }

    static boolean isCancelled(StreamObserver<?> observer) {
        return observer instanceof ServerCallStreamObserver && ((ServerCallStreamObserver<?>) observer).isCancelled();
    }
    static <T> OneStreamObserver<T> one(){
        return new OneStreamObserver<>();
    }
    static <T> ManyStreamObserver<T> many(){
        return new ManyStreamObserver<>();
    }
    final class OneStreamObserver<T> implements StreamObserver<T> {
        final Sinks.One<T> sink = Sinks.one();

         OneStreamObserver() {
        }

        public Mono<T> asMono() {
            return sink.asMono();
        }

        @Override
        public void onNext(T value) {
            sink.tryEmitValue(value);
        }

        @Override
        public void onError(Throwable t) {
            sink.tryEmitError(t);
        }

        @Override
        public void onCompleted() {
            //  sink.tryEmitEmpty();
        }
    }
    final class ManyStreamObserver<T> implements StreamObserver<T> {
        final Sinks.Many<T> sink = Sinks.many().multicast().onBackpressureBuffer();
        ManyStreamObserver() {
        }
        public Flux<T> asFlux() {
            return sink.asFlux();
        }

        @Override
        public void onNext(T value) {
            sink.tryEmitNext(value);
        }

        @Override
        public void onError(Throwable t) {
            sink.tryEmitError(t);
        }

        @Override
        public void onCompleted() {
            sink.tryEmitComplete();
        }
    }
}
