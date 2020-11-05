package cn.zenliu.java.grpc.util;

import io.grpc.stub.StreamObserver;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static cn.zenliu.java.grpc.util.Components.whenNotCancel;

/**
 * @author Zen.Liu
 * @apiNote ServerUtil use to warp a Grpc server service to Reactor style
 * @since 2020-11-05
 */
public interface ServerUtil {
    /**
     * use to wrap a one to one service ,implement should be Mono<REQ> -> Mono<RES>
     *
     * @param req       the request object
     * @param observer  the request observer
     * @param processor may the implement function or method
     * @param <REQ>     request type
     * @param <RES>     response type
     */
    static <REQ, RES> void oneToOne(
            REQ req,
            StreamObserver<RES> observer,
            Function<Mono<REQ>, Mono<RES>> processor
    ) {
        try {
            processor.apply(Mono.just(req))
                    .subscribe(
                            value -> whenNotCancel(observer, o -> o.onNext(value)),
                            observer::onError,
                            observer::onCompleted);
        } catch (Throwable throwable) {
            observer.onError(throwable);
        }

    }

    /**
     * use to wrap a one to many service,implement should be Mono<REQ> -> Flux<RES>
     *
     * @param req       the request object
     * @param observer  the request observer
     * @param processor may the implement function or method
     * @param <REQ>     request type
     * @param <RES>     response type
     */
    static <REQ, RES> void oneToMany(
            REQ req,
            StreamObserver<RES> observer,
            Function<Mono<REQ>, Flux<RES>> processor
    ) {
        try {
            processor.apply(Mono.just(req))
                    .subscribe(
                            value -> whenNotCancel(observer, o -> o.onNext(value)),
                            observer::onError,
                            observer::onCompleted);
        } catch (Throwable throwable) {
            observer.onError(throwable);
        }
    }

    /**
     * use to wrap a many to one service,implement should be Flux<REQ> -> Mono<RES>
     *
     * @param observer  the request observer
     * @param processor may the implement function or method
     * @param <REQ>     request type
     * @param <RES>     response type
     * @return the StreamObserver should to return
     */
    static <REQ, RES> StreamObserver<REQ> manyToOne(
            StreamObserver<RES> observer,
            Function<Flux<REQ>, Mono<RES>> processor
    ) {
        Components.ManyStreamObserver<REQ> reqObserver = Components.many();
        try {
            processor.apply(reqObserver.asFlux())
                    .subscribe(
                            value -> whenNotCancel(observer, o -> o.onNext(value)),
                            observer::onError,
                            observer::onCompleted);
        } catch (Throwable throwable) {
            observer.onError(throwable);
        }
        return reqObserver;
    }

    /**
     * use to wrap a many to many service ,implement should be Flux<REQ> -> Flux<RES>
     *
     * @param observer  the request observer
     * @param processor may the implement function or method
     * @param <REQ>     request type
     * @param <RES>     response type
     * @return the StreamObserver should to return
     */
    static <REQ, RES> StreamObserver<REQ> manyToMany(
            StreamObserver<RES> observer,
            Function<Flux<REQ>, Flux<RES>> processor
    ) {
        Components.ManyStreamObserver<REQ> reqObserver = Components.many();
        try {
            processor.apply(reqObserver.asFlux())
                    .subscribe(
                            value -> whenNotCancel(observer, o -> o.onNext(value)),
                            observer::onError,
                            observer::onCompleted);
        } catch (Throwable throwable) {
            observer.onError(throwable);
        }
        return reqObserver;
    }


}
