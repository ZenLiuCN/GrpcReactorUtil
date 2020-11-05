package cn.zenliu.java.grpc.util;

import io.grpc.stub.StreamObserver;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author Zen.Liu
 * @apiNote ClientUtil use to warp a Grpc client to Reactor style
 * @since 2020-11-05
 */
public interface ClientUtil {
    /**
     * use to wrap a One to One Call
     *
     * @param req     Mono of request object
     * @param consumer may the stub method
     * @param <REQ>    request type
     * @param <RES>    response type
     * @return Mono of response
     */
    static <REQ, RES> Mono<RES> oneToOne(
            Mono<REQ> req,
            BiConsumer<REQ, StreamObserver<RES>> consumer
    ) {
        return req.flatMap(x -> {
            Components.OneStreamObserver<RES> one = Components.one();
            consumer.accept(x, one);
            return one.asMono();
        });
    }

    /**
     *  use to warp a one to many request
     * @param req     Mono of request object
     * @param consumer may the stub method
     * @param <REQ>    request type
     * @param <RES>    response type
     * @return Flux of response
     */
    static <REQ, RES> Flux<RES> oneToMany(
            Mono<REQ> req,
            BiConsumer<REQ, StreamObserver<RES>> consumer

    ) {
        return req.flatMapMany(x -> {
            Components.ManyStreamObserver<RES> one = Components.many();
            consumer.accept(x, one);
            return one.asFlux();
        });
    }

    /**
     * use to wrap a Many to one request
     *
     * @param req       Flux of request objects
     * @param processor may the stub method
     * @param <REQ>     request type
     * @param <RES>     response type
     * @return Flux of response
     */
    static <REQ, RES> Mono<RES> manyToOne(
            Flux<REQ> req,
            Function<StreamObserver<RES>, StreamObserver<REQ>> processor
    ) {
        Components.OneStreamObserver<RES> one = Components.one();
        StreamObserver<REQ> res = processor.apply(one);
        try {
            req
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe(
                            v -> Components.whenNotCancel(res, o -> o.onNext(v)),
                            res::onError,
                            res::onCompleted);
        } catch (Throwable t) {
            res.onError(t);
        }
        return one.asMono();

    }

    /**
     *  use to wrap a many to many request
     * @param req Flux of request objects
     * @param processor may the stub method
     * @param <REQ>    request type
     * @param <RES>    response type
     * @return Mono of response
     */
    static <REQ, RES> Flux<RES> manyToMany(
            Flux<REQ> req,
            Function<StreamObserver<RES>, StreamObserver<REQ>> processor
    ) {
        Components.ManyStreamObserver<RES> many = Components.many();
        StreamObserver<REQ> res = processor.apply(many);
        try {
            req
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe(
                            v -> Components.whenNotCancel(res, o -> o.onNext(v)),
                            res::onError,
                            res::onCompleted);
        } catch (Throwable t) {
            res.onError(t);
        }
        return many.asFlux();
    }


}
