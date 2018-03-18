package com.seceon.ddos

import com.fasterxml.jackson.annotation._

import scala.beans.BeanProperty


case class Netflow  (
                      version: Int,
                      last_switched: String,
                      first_switched: String,
                      in_bytes: Long,
                      in_pkts: Long,
                      input_snmp: Int,
                      output_snmp: Int,
                      ipv4_src_addr: String,
                      ipv4_dst_addr: String,
                      protocol: Int,
                      src_tos: Int,
                      dst_tos: Int,
                      l4_src_port: Int,
                      l4_dst_port: Int,
                      tcp_flags: Int,
                      cycles_TTL: Int,
                      flow_seq_num: Long,
                      dst_mask: Int,
                      flow_sampler_id: Long,
                      flowset_id: Long,
                      ipv4_next_hop: String,
                      src_mask: Int,
                      direction: Int
                    )

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY )
case class NetflowMessage (
                            // These value don't require getter setter
                            `@timestamp`: String,
                            @BeanProperty var timestamp: String,
                            tenant_id: String,
                            tags: List[String],
                            `@version`: String,

                            // These value don't require getter setter
                            @JsonIgnore @BeanProperty var is_src_enriched : Boolean,
                            @JsonIgnore @BeanProperty var is_dst_enriched : Boolean,
                            @BeanProperty netflow: Netflow,
                            @BeanProperty var host: String,
                            @BeanProperty var cce_ip: String,
                            @BeanProperty var src_host_name: String,
                            @BeanProperty var dst_host_name: String,
                            @BeanProperty var app_port: Int,
                            @BeanProperty var app_name: String,
                            @BeanProperty var trojan_horse: String,
                            @BeanProperty var app_group: String,
                            @BeanProperty var src_network_tags: String,
                            @BeanProperty var dst_network_tags: String,
                            @BeanProperty var netflow_direction: Int,
                            @JsonInclude @BeanProperty var authorized: Int,
                            @BeanProperty var dscp: String,
                            @BeanProperty var src_confidence: Float,
                            @BeanProperty var blacklisted_country : Int,
                            @BeanProperty var dst_confidence: Float,
                            @JsonInclude @BeanProperty var src_highval: Int,
                            @JsonInclude @BeanProperty var dst_highval: Int,
                            @BeanProperty var invalid_src: Int,
                            @BeanProperty var invalid_dst: Int,
                            @BeanProperty var non_unicast_src: Int,
                            @BeanProperty var multicast: Int,
                            @BeanProperty var network: Int,
                            @BeanProperty var broadcast: Int,
                            @BeanProperty var src_network_name: String,
                            @BeanProperty var dst_network_name: String,
                            @JsonInclude @BeanProperty var src_blacklisted: Int,
                            @BeanProperty var dst_blacklisted_source: String,
                            @BeanProperty var src_blacklisted_source: String,
                            @JsonInclude @BeanProperty var dst_blacklisted: Int,
                            @BeanProperty var blacklisted_ips: Int,
                            @BeanProperty var src_addr_space: String,
                            @BeanProperty var dst_addr_space: String,
                            @BeanProperty var bytes_per_packet: Int,
                            @BeanProperty var malformed_tcp: Int,
                            @BeanProperty var tcp_syn: Int,
                            @BeanProperty var duration: Long,
                            @BeanProperty var tcp_nominal_payload: String,
                            @BeanProperty var tcp_flags_str: String,
                            @JsonIgnore @BeanProperty var is_enriched : Boolean,
                            @BeanProperty var dst_geolite: Map[String, Any],
                            @BeanProperty var src_geolite: Map[String, Any],
                            @JsonProperty("additional_info")
                            @BeanProperty var additional_info : Map[String, Any]

                          )