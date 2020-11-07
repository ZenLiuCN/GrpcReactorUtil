# A Reactor Warping tool for GRPC StreamObserver
This library design to warp a Grpc service or client to Reactive Stream (use ProjectReactor).

+ this library just offers some utilities for easier transform original Grpc into Reactive style, Who use it must coding for a warped Stub or ImplBase.
+ if wanna a more complete and hidden way , can take at [reactive-grpc](https://github.com/salesforce/reactive-grpc)
## Artifacts
+ `grpc-reactor-component`: warp with Reactor >=3.4.0
+ `grpc-reactor-component-330x`: warp with Reactor <=3.3.X
+ `grpc-reacotr-util`: main tool library which must used with one component library

## Note
Not Production ready!