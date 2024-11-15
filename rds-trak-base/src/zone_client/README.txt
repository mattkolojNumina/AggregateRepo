control parameters:

   zone       -- conveyor zone, used to assign message to box
   ip         -- network address; default localhost
   port       -- network port; default 10000
   startChar  -- start-of-message framing character; default STX
   endChar    -- end-of-message framing character; default ETX
   heartbeat  -- if received, msg discarded and keep-alive timer reset
   keepalive  -- msg sent to device to keep connection open
   timeout    -- communications timeout (seconds); default 60


notes:

 * the keepalive message, if defined, is framed with the defined start/end
   characters and sent every (timeout/2) seconds

 * any message successfully sent to or received from device will reset the
   keep-aliver timer

 * if keep-alive timer expires, a reconnect is triggered

 * an event <name>_comm is started if a connection cannot be established
   and is cleared on successful connection

 * a received message is assigned to a valid box using the standard
      util_box_set()
   function, which saves into the boxData table

 * the runtime value <name>/msg and webObjects value <name>_msg are populated
   upon receipt, regardless of success of assigning to box

 * the runtime value <name>/xmit will be sent to the device; there is no
   additional framing of the message, so control characters may need to
   be populated into the message (using, for example,
         CONCAT(CHAR(0x02),'msg',CHAR(0x03))


typical usage:

 * Keyence scanners:
      port 2112, start 0x02, end 0x03
      keepalive = "HEARTBEAT"
      heartbeat = "ER,HEARTBEAT,00"  (response is an error, but is predictable)
      timeout = 60

 * SICK scanners:
      port 2112, start 0x02, end 0x03
      heartbeat = "HEARTBEAT" (configured on device with 30-second timer)
      timeout = 60

 * Doran scale (new style):
      port 2101, start 0x02, end 0x0D
      no keepalive or heartbeat; reconnect triggered when timeout expires
      timeout = 300
      to trigger a transmission on stable weight:
            REPLACE runtime SET NAME = '<name>/xmit', VALUE = 'W\r';

