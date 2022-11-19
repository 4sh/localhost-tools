package sh.quatre.localhost.dev.proxy.gen

import sh.quatre.localhost.dev.proxy.DevServerProtocol.HTTP_2
import sh.quatre.localhost.dev.proxy.RunningDevServer
import java.io.File

class EnvoyConfGenerator {
    fun generateToFile(from: List<RunningDevServer>, to: File) = to.writeText(generateToString(from))
    fun generateToString(servers: List<RunningDevServer>) = """
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
${servers.map { serverRouteConfig(it) }.joinToString("")}
            - name: default
              domains: ["*"]
              routes:
              - match: { prefix: "/" }
                response_headers_to_add:
                - header: {key: "content-type", value: "text/html"}
                direct_response:
                  status: 200
                  body:
                    filename: "/etc/localhost-server-manager/default.html"
          http_filters:
          - name: envoy.filters.http.router
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.filters.http.router.v3.Router
  clusters:
${servers.map { serverClusterConfig(it) }.joinToString("")}
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
""".trimStartLine().trimEnd()

    private fun serverRouteConfig(server: RunningDevServer) = """
            - name: ${server.server.name}
              domains: ["${server.server.name}.*"]
              routes:
              - match: { prefix: "/" }
                route: { cluster: ${server.server.name} }
""".trimStartLine()

    private fun serverClusterConfig(server: RunningDevServer) = """
  - name: ${server.server.name}
    type: STRICT_DNS
    per_connection_buffer_limit_bytes: 32768  # 32 KiB
    load_assignment:
      cluster_name: ${server.server.name}
      endpoints:
      - lb_endpoints:
        - endpoint:
            address:
              socket_address:
                address: host.docker.internal
                port_value: ${server.port}
""".trimStartLine() + if (server.server.protocol == HTTP_2) """
    typed_extension_protocol_options:
      envoy.extensions.upstreams.http.v3.HttpProtocolOptions:
        "@type": type.googleapis.com/envoy.extensions.upstreams.http.v3.HttpProtocolOptions
        explicit_http_config:
          http2_protocol_options:
            initial_stream_window_size: 65536  # 64 KiB
            initial_connection_window_size: 1048576  # 1 MiB
""".trimStartLine() else ""
}

private fun String.trimStartLine() = this.removePrefix("\n")