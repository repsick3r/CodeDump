import io.grpc.*;

public class MetadataInterceptor implements ClientInterceptor {
    private Metadata headers;
    private Metadata trailers;

    public Metadata getHeaders() {
        return headers;
    }

    public Metadata getTrailers() {
        return trailers;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                next.newCall(method, callOptions)) {
            
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                // Capture the headers
                MetadataInterceptor.this.headers = headers;

                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        // Capture the trailers
                        MetadataInterceptor.this.trailers = trailers;
                        super.onClose(status, trailers);
                    }
                }, headers);
            }
        };
    }
}
