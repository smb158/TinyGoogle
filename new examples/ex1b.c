/*-----------------------------------------------------------------------*/
/* This example demonstrates IPC in UNIX domain using datagram services. */
/* Two communicating processes will be created. The code is assumed to   */
/* be contained in file ex1b.c.                                          */
/*-----------------------------------------------------------------------*/

/*-----------------------------------------------------------------------*/
/* This program sends a datagram message to the receiver.                */
/*-----------------------------------------------------------------------*/

#include<sys/types.h>
#include <sys/socket.h>
#include <stdio.h>
#include <sys/un.h>
#include <string.h>

#define MSG "This is a great stuff!!!"
#define NAME "/tmp/znati_socket_name"            /* Define the socket name */

main()
{
struct sockaddr_un remote;
int sk;

/*-----------------------------------------------------------------------*/
/* Create an UNIX domain datagram socket on which to send                */
/*-----------------------------------------------------------------------*/


sk = socket(AF_UNIX, SOCK_DGRAM, 0);

remote.sun_family = AF_UNIX;
strcpy(remote.sun_path, NAME);

/*-----------------------------------------------------------------------*/
/* Send the massage.                                                     */
/*-----------------------------------------------------------------------*/

sendto(sk,MSG,strlen(MSG)+1,0,(struct sockaddr *)&remote,strlen(NAME)+2);
close(sk);
}
