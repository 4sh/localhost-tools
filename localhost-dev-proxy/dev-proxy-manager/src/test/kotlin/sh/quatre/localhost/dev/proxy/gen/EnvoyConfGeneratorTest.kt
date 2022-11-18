package sh.quatre.localhost.dev.proxy.gen

import kotlinx.datetime.Clock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import sh.quatre.localhost.dev.proxy.DevServerProtocol.HTTP
import sh.quatre.localhost.dev.proxy.DevServerProtocol.HTTP_2
import sh.quatre.localhost.dev.proxy.LocalDevServer
import sh.quatre.localhost.dev.proxy.RunningDevServer

class EnvoyConfGeneratorTest {
    @Test
    fun `should generate envoy proxy conf`() {
        val generator = EnvoyConfGenerator()

        val startedAt = Clock.System.now()
        val conf = generator.generateToString(
            listOf(
                RunningDevServer(LocalDevServer("my-web-server", HTTP), 8080, startedAt),
                RunningDevServer(LocalDevServer("my-http2-server", HTTP_2), 8081, startedAt)
            )
        )

        Assertions.assertEquals(expectedEnvoyProxy, conf)
    }
}

private val expectedEnvoyProxy = """
    overload_manager:
      refresh_interval: 0.25s
      resource_monitors:
      - name: "envoy.resource_monitors.fixed_heap"
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.resource_monitors.fixed_heap.v3.FixedHeapConfig
          max_heap_size_bytes: 2147483648  # 2 GiB
      actions:
      - name: "envoy.overload_actions.shrink_heap"
        triggers:
        - name: "envoy.resource_monitors.fixed_heap"
          threshold:
            value: 0.95
      - name: "envoy.overload_actions.stop_accepting_requests"
        triggers:
        - name: "envoy.resource_monitors.fixed_heap"
          threshold:
            value: 0.98

    admin:
      address:
        socket_address: { address: 0.0.0.0, port_value: 9998 }

    static_resources:
      listeners:
      - name: listener_0
        address:
          socket_address: { address: 0.0.0.0, port_value: 9999 }
        listener_filters:
        - name: "envoy.filters.listener.tls_inspector"
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.listener.tls_inspector.v3.TlsInspector
        per_connection_buffer_limit_bytes: 32768  # 32 KiB    
        filter_chains:
        - filters:
          - name: envoy.filters.network.http_connection_manager
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
              stat_prefix: ingress_http
              use_remote_address: true
              normalize_path: true
              merge_slashes: true
              path_with_escaped_slashes_action: UNESCAPE_AND_REDIRECT
              common_http_protocol_options:
                idle_timeout: 3600s  # 1 hour
                headers_with_underscores_action: REJECT_REQUEST
              http2_protocol_options:
                max_concurrent_streams: 100
                initial_stream_window_size: 65536  # 64 KiB
                initial_connection_window_size: 1048576  # 1 MiB
              stream_idle_timeout: 300s  # 5 mins, must be disabled for long-lived and streaming requests
              request_timeout: 300s  # 5 mins, must be disabled for long-lived and streaming requests
              codec_type: AUTO
              route_config:
                name: local_route
                virtual_hosts:
                - name: my-web-server
                  domains: ["my-web-server.*"]
                  routes:
                  - match: { prefix: "/" }
                    route: { cluster: my-web-server }
                - name: my-http2-server
                  domains: ["my-http2-server.*"]
                  routes:
                  - match: { prefix: "/" }
                    route: { cluster: my-http2-server }

                - name: default
                  domains: ["*"]
                  routes:
                  - match: { prefix: "/" }
                    response_headers_to_add:
                    - header: {key: "content-type", value: "text/html"}
                    direct_response:
                      status: 200
                      body:
                        filename: "/etc/envoy/default.html"
              http_filters:
              - name: envoy.filters.http.router
                typed_config:
                  "@type": type.googleapis.com/envoy.extensions.filters.http.router.v3.Router
      clusters:
      - name: my-web-server
        type: STRICT_DNS
        per_connection_buffer_limit_bytes: 32768  # 32 KiB
        load_assignment:
          cluster_name: my-web-server
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: host.docker.internal
                    port_value: 8080
      - name: my-http2-server
        type: STRICT_DNS
        per_connection_buffer_limit_bytes: 32768  # 32 KiB
        load_assignment:
          cluster_name: my-http2-server
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: host.docker.internal
                    port_value: 8081
        typed_extension_protocol_options:
          envoy.extensions.upstreams.http.v3.HttpProtocolOptions:
            "@type": type.googleapis.com/envoy.extensions.upstreams.http.v3.HttpProtocolOptions
            explicit_http_config:
              http2_protocol_options:
                initial_stream_window_size: 65536  # 64 KiB
                initial_connection_window_size: 1048576  # 1 MiB

    layered_runtime:
      layers:
      - name: static_layer_0
        static_layer:
          envoy:
            resource_limits:
              listener:
                example_listener_name:
                  connection_limit: 10000
          overload:
            global_downstream_max_connections: 50000
""".trimIndent()
