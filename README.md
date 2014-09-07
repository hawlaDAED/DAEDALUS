Daedalus
========

This project is a sister project of Icarus (https://github.com/TheDudes/Project_Icarus). Icarus interprets a structured text and then forwards binary commands to Daedalus via TCP/IP. Daedalus interprets these commands and translates them to the more less high-leveled Modbus protocol.
That means: Daedalus encapsulates polling work and simplifies the protocol for Icarus.

SPS2Modbus Protocol
-------------------

The following diagram displays the interaction between Daedalus and Icarus:

![SPS2Modbus](./doc_img/sps2modbus_overview.png?raw=true "SPS2Modbus protocol overview")

All of these channels use the following data structure:

```

              16 bit
|-----------------------------------|

  0                              15
+-----------------+-----------------+
|             DEVICE ID             |
+-----------------+-----------------+
|               PIN ID              |
+-----------------+-----------------+
|   NamespaceID   |     Count       |     Count: max=8;
+-----------------+-----------------+
| X X X X X S RWP |     VALUE       |     S: success-flag; RWP: Read/Write/Poll (2 bit); X: reserved;
+-----------------+-----------------+

```

The fields are used in the following ways (all numeric values are BigEndians):  

The following fields are returned without a change in the corresponding response to a request:  

 * **DEVICE ID** always specifies the target device for the Modbus protocol (needs to be configured)  
 * **PIN ID** always specifies the target address in the Modbus device
 * **NamespaceID** always specifies the target namespace (see the following list)
 * **Count** specifies how many pins should be read or should be written

These fields differ depending on the type of message:  
 * **S-Flag** is only set in responses if the attempt to execute the command was successful
 * **RWP-Flags** define the type of the message (see the following list)
 * **VALUE** has meaningful values in READ-responses, WRITE-requests and async polling messages. If so, these values represent the actual value of the referenced addess.


<TODO: the lists mentioned above>



