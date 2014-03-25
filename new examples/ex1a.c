/*-----------------------------------------------------------------------*/
/* This example demonstrates IPC in UNIX domain using datagram services. */
/* Two communicating processes will be created. It is assumed that the   */
/* code is contained in file ex1a.c.                                     */
/*-----------------------------------------------------------------------*/


#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <string.h>
#include <stdio.h>

#define NAME "/tmp/znati_socket_name" /*define the name of the socket*/

main()
{
struct	sockaddr_un local;
int	sk;
int     i;
char	buf[BUFSIZ];

/*-----------------------------------------------------------------------*/
/* Create a UNIX domain datagram socket from which to read               */
/*-----------------------------------------------------------------------*/

sk=socket(AF_UNIX,SOCK_DGRAM,0);

local.sun_family=AF_UNIX;        /* Define the socket domain    */
strcpy(local.sun_path,NAME);     /* Define the socket name      */
bind(sk,(struct sockaddr *)&local,strlen(NAME)+2); /* Bind the name to
the socket */

/*-----------------------------------------------------------------------*/
/* Read from the socket and print its content.                           */
/*-----------------------------------------------------------------------*/

/* initialize message buffer */
read(sk,buf,BUFSIZ);

printf("%s\n",buf);

/*-----------------------------------------------------------------------*/
/* Remove the link to the socket (delete i-node, in essence).            */
/*-----------------------------------------------------------------------*/
unlink(NAME);
close(sk);
}
