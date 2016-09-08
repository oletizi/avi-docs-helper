---
title: Manually Validate Server Health
layout: default
---
It is often helpful to validate the response of a server while troubleshooting reasons a server may be marked down. The challenge is to ensure the test is from a specific Service Engine, using the same tenant, network, and IP address. SEs have multiple network stacks, one for control plane which uses Linux, and a second for the data plane. Simply logging into an SE and pinging a server will go out the management port and IP address, which may route through different infrastructure than the SE data plane.

For more info on health monitoring servers see:

* <a href="/overview-of-health-monitors">Overview of Health Monitors</a>
* <a href="/why-is-a-server-marked-down/">Reasons a Server Can Be Marked Down</a>
* <a href="/servers-flapping-up-down/">Servers Flap Up / Down</a>
* <a href="/health-monitor-troubleshooting/">Health Monitor Troubleshooting</a>

 

### Prerequisites

1. Determine the IP address of the Service Engine hosting the virtual service.
1. SSH into the Controller
1. Log into the Avi shell
<pre><code class="language-lua" bgcolor="white" text="black">shell : &gt; attach serviceengine 10.10.25.28 admin@10-10-25-28:~$ ip netns : &gt; show pool p1 detail  | grep vrf_id
 | vrf_id   | 2 : &gt; show serviceengine 10.10.25.28 vnicdb show pool p1 detail | filter disable_aggregate se  se_ref 10.10.25.28
| vrf_id   | 2 root@test-se2:~# ip netns exec avi_ns1 ping 10.90.15.62
 PING 10.90.15.62 (10.90.15.62) 56(84) bytes of data.
 64 bytes from 10.90.15.62: icmp_seq=1 ttl=64 time=26.8 ms root@test-se2:~# ip netns exec avi_ns1 curl 10.90.15.62
 curl:  Failed to connect to 10.90.15.62 port 80: Connection refused

 root@test-se2:~# ip netns exec avi_ns1 curl 10.90.15.62:8000
 
 
 <title>Welcome - Served from port 80!</title>
 
 
 
     
  <center> 
   <h1>Welcome!</h1> 
  </center>
 
 
 root@test-se2:~#</code></pre>

### VMWare - No Tenants

1. Connect to a Service Engine's Linux shell:
<pre><code class="language-lua" bgcolor="white" text="black">shell : &gt; attach serviceengine 10.10.25.28 admin@10-10-25-28:~$ ip netns : &gt; show pool p1 detail  | grep vrf_id
 | vrf_id   | 2 : &gt; show serviceengine 10.10.25.28 vnicdb show pool p1 detail | filter disable_aggregate se  se_ref 10.10.25.28
| vrf_id   | 2 root@test-se2:~# ip netns exec avi_ns1 ping 10.90.15.62
 PING 10.90.15.62 (10.90.15.62) 56(84) bytes of data.
 64 bytes from 10.90.15.62: icmp_seq=1 ttl=64 time=26.8 ms root@test-se2:~# ip netns exec avi_ns1 curl 10.90.15.62
 curl:  Failed to connect to 10.90.15.62 port 80: Connection refused

 root@test-se2:~# ip netns exec avi_ns1 curl 10.90.15.62:8000
 
 
 <title>Welcome - Served from port 80!</title>
 
 
 
     
  <center> 
   <h1>Welcome!</h1> 
  </center>
 
 
 root@test-se2:~#</code></pre>
1. Validate the current namespace:
<pre><code class="language-lua" bgcolor="white" text="black">shell : &gt; attach serviceengine 10.10.25.28 admin@10-10-25-28:~$ ip netns : &gt; show pool p1 detail  | grep vrf_id
 | vrf_id   | 2 : &gt; show serviceengine 10.10.25.28 vnicdb show pool p1 detail | filter disable_aggregate se  se_ref 10.10.25.28
| vrf_id   | 2 root@test-se2:~# ip netns exec avi_ns1 ping 10.90.15.62
 PING 10.90.15.62 (10.90.15.62) 56(84) bytes of data.
 64 bytes from 10.90.15.62: icmp_seq=1 ttl=64 time=26.8 ms root@test-se2:~# ip netns exec avi_ns1 curl 10.90.15.62
 curl:  Failed to connect to 10.90.15.62 port 80: Connection refused

 root@test-se2:~# ip netns exec avi_ns1 curl 10.90.15.62:8000
 
 
 <title>Welcome - Served from port 80!</title>
 
 
 
     
  <center> 
   <h1>Welcome!</h1> 
  </center>
 
 
 root@test-se2:~#</code></pre>  Usually output is avi_ns1, which is the default namespace.
1. Execute a static health check from this namespace. See examples at the end of this article.

### VMWare - Multiple Tenants

For multiple tenants on VMware, Avi does not create VRFs/namespaces by default.

1. Attach to the Service Engine Linux shell:
<pre><code class="language-lua" bgcolor="white" text="black">shell : &gt; attach serviceengine 10.10.25.28 admin@10-10-25-28:~$ ip netns : &gt; show pool p1 detail  | grep vrf_id
 | vrf_id   | 2 : &gt; show serviceengine 10.10.25.28 vnicdb show pool p1 detail | filter disable_aggregate se  se_ref 10.10.25.28
| vrf_id   | 2 root@test-se2:~# ip netns exec avi_ns1 ping 10.90.15.62
 PING 10.90.15.62 (10.90.15.62) 56(84) bytes of data.
 64 bytes from 10.90.15.62: icmp_seq=1 ttl=64 time=26.8 ms root@test-se2:~# ip netns exec avi_ns1 curl 10.90.15.62
 curl:  Failed to connect to 10.90.15.62 port 80: Connection refused

 root@test-se2:~# ip netns exec avi_ns1 curl 10.90.15.62:8000
 
 
 <title>Welcome - Served from port 80!</title>
 
 
 
     
  <center> 
   <h1>Welcome!</h1> 
  </center>
 
 
 root@test-se2:~#</code></pre>
1. Execute a static health check. See examples at the end of this article.

### Multiple Tenants with VRF (Provider mode)

1. Find the namespace/VRF for the pool server:
<pre><code class="language-lua" bgcolor="white" text="black">shell : &gt; attach serviceengine 10.10.25.28 admin@10-10-25-28:~$ ip netns : &gt; show pool p1 detail  | grep vrf_id
 | vrf_id   | 2 : &gt; show serviceengine 10.10.25.28 vnicdb show pool p1 detail | filter disable_aggregate se  se_ref 10.10.25.28
| vrf_id   | 2 root@test-se2:~# ip netns exec avi_ns1 ping 10.90.15.62
 PING 10.90.15.62 (10.90.15.62) 56(84) bytes of data.
 64 bytes from 10.90.15.62: icmp_seq=1 ttl=64 time=26.8 ms root@test-se2:~# ip netns exec avi_ns1 curl 10.90.15.62
 curl:  Failed to connect to 10.90.15.62 port 80: Connection refused

 root@test-se2:~# ip netns exec avi_ns1 curl 10.90.15.62:8000
 
 
 <title>Welcome - Served from port 80!</title>
 
 
 
     
  <center> 
   <h1>Welcome!</h1> 
  </center>
 
 
 root@test-se2:~#</code></pre>  In this case, the vrf_id is 2, and the namespace is avi_ns2.
This information can also be obtained from:
<pre><code class="language-lua" bgcolor="white" text="black">shell : &gt; attach serviceengine 10.10.25.28 admin@10-10-25-28:~$ ip netns : &gt; show pool p1 detail  | grep vrf_id
 | vrf_id   | 2 : &gt; show serviceengine 10.10.25.28 vnicdb show pool p1 detail | filter disable_aggregate se  se_ref 10.10.25.28
| vrf_id   | 2 root@test-se2:~# ip netns exec avi_ns1 ping 10.90.15.62
 PING 10.90.15.62 (10.90.15.62) 56(84) bytes of data.
 64 bytes from 10.90.15.62: icmp_seq=1 ttl=64 time=26.8 ms root@test-se2:~# ip netns exec avi_ns1 curl 10.90.15.62
 curl:  Failed to connect to 10.90.15.62 port 80: Connection refused

 root@test-se2:~# ip netns exec avi_ns1 curl 10.90.15.62:8000
 
 
 <title>Welcome - Served from port 80!</title>
 
 
 
     
  <center> 
   <h1>Welcome!</h1> 
  </center>
 
 
 root@test-se2:~#</code></pre>
1. If there are multiple service-engines, find the vrf-id on the specific SE:
<pre><code class="language-lua" bgcolor="white" text="black">shell : &gt; attach serviceengine 10.10.25.28 admin@10-10-25-28:~$ ip netns : &gt; show pool p1 detail  | grep vrf_id
 | vrf_id   | 2 : &gt; show serviceengine 10.10.25.28 vnicdb show pool p1 detail | filter disable_aggregate se  se_ref 10.10.25.28
| vrf_id   | 2 root@test-se2:~# ip netns exec avi_ns1 ping 10.90.15.62
 PING 10.90.15.62 (10.90.15.62) 56(84) bytes of data.
 64 bytes from 10.90.15.62: icmp_seq=1 ttl=64 time=26.8 ms root@test-se2:~# ip netns exec avi_ns1 curl 10.90.15.62
 curl:  Failed to connect to 10.90.15.62 port 80: Connection refused

 root@test-se2:~# ip netns exec avi_ns1 curl 10.90.15.62:8000
 
 
 <title>Welcome - Served from port 80!</title>
 
 
 
     
  <center> 
   <h1>Welcome!</h1> 
  </center>
 
 
 root@test-se2:~#</code></pre>
1. Attach to the Service Engine Linux shell:
<pre><code class="language-lua" bgcolor="white" text="black">shell : &gt; attach serviceengine 10.10.25.28 admin@10-10-25-28:~$ ip netns : &gt; show pool p1 detail  | grep vrf_id
 | vrf_id   | 2 : &gt; show serviceengine 10.10.25.28 vnicdb show pool p1 detail | filter disable_aggregate se  se_ref 10.10.25.28
| vrf_id   | 2 root@test-se2:~# ip netns exec avi_ns1 ping 10.90.15.62
 PING 10.90.15.62 (10.90.15.62) 56(84) bytes of data.
 64 bytes from 10.90.15.62: icmp_seq=1 ttl=64 time=26.8 ms root@test-se2:~# ip netns exec avi_ns1 curl 10.90.15.62
 curl:  Failed to connect to 10.90.15.62 port 80: Connection refused

 root@test-se2:~# ip netns exec avi_ns1 curl 10.90.15.62:8000
 
 
 <title>Welcome - Served from port 80!</title>
 
 
 
     
  <center> 
   <h1>Welcome!</h1> 
  </center>
 
 
 root@test-se2:~#</code></pre>
1. Execute a static health check from this namespace. See examples at the end of this article.

### Bare-metal / Linux Cloud

For bare metal Linux clouds, there are no namespaces, reducing the necessary steps.

1. Attach to the Service Engine Linux shell:
<pre><code class="language-lua" bgcolor="white" text="black">shell : &gt; attach serviceengine 10.10.25.28 admin@10-10-25-28:~$ ip netns : &gt; show pool p1 detail  | grep vrf_id
 | vrf_id   | 2 : &gt; show serviceengine 10.10.25.28 vnicdb show pool p1 detail | filter disable_aggregate se  se_ref 10.10.25.28
| vrf_id   | 2 root@test-se2:~# ip netns exec avi_ns1 ping 10.90.15.62
 PING 10.90.15.62 (10.90.15.62) 56(84) bytes of data.
 64 bytes from 10.90.15.62: icmp_seq=1 ttl=64 time=26.8 ms root@test-se2:~# ip netns exec avi_ns1 curl 10.90.15.62
 curl:  Failed to connect to 10.90.15.62 port 80: Connection refused

 root@test-se2:~# ip netns exec avi_ns1 curl 10.90.15.62:8000
 
 
 <title>Welcome - Served from port 80!</title>
 
 
 
     
  <center> 
   <h1>Welcome!</h1> 
  </center>
 
 
 root@test-se2:~#</code></pre>
1. Execute a static health check. See examples at the end of this article.

### Common Manual Server Checks

Ping:
<pre><code class="language-lua" bgcolor="white" text="black">shell : &gt; attach serviceengine 10.10.25.28 admin@10-10-25-28:~$ ip netns : &gt; show pool p1 detail  | grep vrf_id
 | vrf_id   | 2 : &gt; show serviceengine 10.10.25.28 vnicdb show pool p1 detail | filter disable_aggregate se  se_ref 10.10.25.28
| vrf_id   | 2 root@test-se2:~# ip netns exec avi_ns1 ping 10.90.15.62
 PING 10.90.15.62 (10.90.15.62) 56(84) bytes of data.
 64 bytes from 10.90.15.62: icmp_seq=1 ttl=64 time=26.8 ms root@test-se2:~# ip netns exec avi_ns1 curl 10.90.15.62
 curl:  Failed to connect to 10.90.15.62 port 80: Connection refused

 root@test-se2:~# ip netns exec avi_ns1 curl 10.90.15.62:8000
 
 
 <title>Welcome - Served from port 80!</title>
 
 
 
   
  <center> 
   <h1>Welcome!</h1> 
  </center>
 
 
 root@test-se2:~#</code></pre>  Curl:
<pre><code class="language-lua" bgcolor="white" text="black">shell : &gt; attach serviceengine 10.10.25.28 admin@10-10-25-28:~$ ip netns : &gt; show pool p1 detail  | grep vrf_id
 | vrf_id   | 2 : &gt; show serviceengine 10.10.25.28 vnicdb show pool p1 detail | filter disable_aggregate se  se_ref 10.10.25.28
| vrf_id   | 2 root@test-se2:~# ip netns exec avi_ns1 ping 10.90.15.62
 PING 10.90.15.62 (10.90.15.62) 56(84) bytes of data.
 64 bytes from 10.90.15.62: icmp_seq=1 ttl=64 time=26.8 ms root@test-se2:~# ip netns exec avi_ns1 curl 10.90.15.62
 curl:  Failed to connect to 10.90.15.62 port 80: Connection refused

 root@test-se2:~# ip netns exec avi_ns1 curl 10.90.15.62:8000
 
 
 <title>Welcome - Served from port 80!</title>
 
 
 
   
  <center> 
   <h1>Welcome!</h1> 
  </center>
 
 
 root@test-se2:~#</code></pre>