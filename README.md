# Wutella
Gnutella like p2p application for file sharing

Compile: java *.java

Run node0: java Node port /path/to/sharedDIR
Run nodeX: java Node port /path/to/sharedDIR node0_ip node0_port
           java Node port /path/to/sharedDIR node0_ip node0_port [queryFileName]


Details:
    port                - UDP port number for the node. Pick a number between 1024 and 64535.
                        - TCP port number will be 1000 + UDP port number
    /path/to/sharedDIR  - This is the directory this node is sharing in the newtork.
                          Every file in this will be available for sharing. Furthermore,
                          when this node downloads a file, the file will end up in this
                          directory.
    node0_ip            - IP address of a node that is active node in the network. This doesn't 
                          have to be node0. Any node will do.
    node0_port          - UDP port number of the active node.
    [queryFileName]     - Optional field. A node that want's to query the network can run the command with this argument.
                          The query will be sent to the nodes neighbors. If a receiving node doesn't have
                          the requested file, it will relay the query to its neighbors. When the query is
                          resolved, the node maintaining the queried file will send a direct QUERYHIT message
                          to the requesting node. When this happens the two nodes will establish a TCP connection
                          to transfer the requested file.
                          

Explanation:

All nodes are equal. However, the very first node doesn't require the address of another node. 
Obviously it's the only one in the network. Other nodes will need to know the address of one
active node to join the network.

1. Nodes will maintain a list of neighbors. (set to Max=2 neighbors)
2. Nodes PING neighbors every 5 seconds.
3. PINGs are reciprocated with a PONG from the neighbors.
  - This way nodes are asserted of the liveliness of neighbors.
  - PONGs contain a list a neighbors from the PONGer.
  - This list can be used to further contact other nodes.
  - If the neighbors list is full however, nodes in this list will be added to the pool.
  - Whenever neighbors TIMEOUT, the node can contact another node from its pool to fill up its neighbors list.
4. A node can query the network by passing the queryFileName argument as shown above. The query will propagate
   through the network until a HIT is made. A node that has the requested file will establish a TCP connection
   with the requesting node to transfer the file.

Observations:
  The network nodes form a directed cyclic graph. Because of the MAX neighbors size, certain nodes are 
  unreachable from others.

Experiment:
  In this experiment, there are 4 nodes that run on the same machine.
  Node A shares db1/ which contains files
        ---> Ethiopia.txt
        ---> Kenya.txt
  Node B shares db2/ which contains files
        ---> Russia.txt
        ---> USA.txt
  Node C shares db3/ which contains files
        ---> Britain.txt
        ---> France.txt
  Node D shares db4/ which contains files
        ---> China.txt
        ---> Japan.txt
        
  A. java Node 4441 db1 localhost
  B. java Node 4442 db2 localhost 4441
  C. java Node 4443 db3 localhost 4441
  D. java Node 4444 db4 localhost 4441 Britain.txt
  
  Node D is requesting "Britain.txt"
  The nodes form this graph (Adj List, Node: neighbors):  A: B,C
                                                          B: A,C
                                                          C: A,B
                                                          D: A,B
  Notice "Britain.txt" belongs to C, however D and C are not neighbors.
  In fact, C cannot even contact D over the UDP overlay network. However D can contact C through A/B.
  1. D requests "Britain.txt", D --> A --> C (Via UDP overlay)
  2. C responds, C --> D (Via direct TCP)
