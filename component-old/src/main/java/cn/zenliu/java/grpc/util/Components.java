package cn.zenliu.java.grpc.util;

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import reactor.core.publisher.*;

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

    static <T> OneStreamObserver<T> one() {
        return new OneStreamObserver<>();
    }

    static <T> ManyStreamObserver<T> many() {
        return new ManyStreamObserver<>();
    }

    final class OneStreamObserver<T> implements StreamObserver<T> {
        final DirectProcessor<T> processor = DirectProcessor.create();
        final FluxSink<T> sink = processor.sink();
        final Mono<T> one = processor.last();

        OneStreamObserver() {
        }

        public Mono<T> asMono() {
            return one;
        }

        @Override
        public void onNext(T value) {
            sink.next(value);
            sink.complete();
        }

        @Override
        public void onError(Throwable t) {
            sink.error(t);
            sink.complete();
        }

        @Override
        public void onCompleted() {
            //  sink.tryEmitEmpty();
        }
    }

    final class ManyStreamObserver<T> implements StreamObserver<T> {
        final EmitterProcessor<T> processor = EmitterProcessor.create();
        final FluxSink<T> sink = processor.sink();
        final Flux<T> flux = processor.onBackpressureBuffer();

        ManyStreamObserver() {
        }

        public Flux<T> asFlux() {
            return flux;
        }

        @Override
        public void onNext(T value) {
            sink.next(value);
        }

        @Override
        public void onError(Throwable t) {
            sink.error(t);
        }

        @Override
        public void onCompleted() {
            sink.complete();
        }
    }
}
